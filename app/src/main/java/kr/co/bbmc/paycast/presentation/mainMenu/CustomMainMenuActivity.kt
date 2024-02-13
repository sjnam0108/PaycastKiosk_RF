package kr.co.bbmc.paycast.presentation.mainMenu

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kr.co.bbmc.paycast.*
import kr.co.bbmc.paycast.App.Companion.APP
import kr.co.bbmc.paycast.TCPServer.CMD_TX_WAITNG
import kr.co.bbmc.paycast.network.model.PaymentInfo
import kr.co.bbmc.paycast.presentation.paymentKicc.CustomPaymentKiccActivity
import kr.co.bbmc.paycast.presentation.paymentKicc.ZalComPrint
import kr.co.bbmc.paycast.presentation.provisioninfo.CustomProvisionInfoActivity
import kr.co.bbmc.paycast.ui.component.theme.AdNetTheme
import kr.co.bbmc.paycast.util.CustomUtils
import kr.co.bbmc.paycast.util.onGetStringPrinterErr
import kr.co.bbmc.paycast.util.registerResultCode
import kr.co.bbmc.paycast.util.scheduleAlarm
import kr.co.bbmc.selforderutil.SingCastPlayIntent
import kr.hstar.commonutil.delayRun
import kr.hstar.commonutil.repeatOnState

@FlowPreview
class CustomMainMenuActivity : AppCompatActivity() {

    private val mainMenuViewModel: MainMenuViewModel by viewModels()
    private val customUtils = CustomUtils()

    private val provisionInfoLauncher = registerResultCode {
        with(mainMenuViewModel) {
            it?.let { result ->
                when (result.resultCode) {
                    RESULT_CANCELED -> {
                        sendToast("결제가 취소되었습니다.")
                        countPopup()
                    }
                    RESULT_OK -> {
                        sendToast("결제가 완료되었습니다.")
                        clearAllOrders()
                        setItemPackage(false)
                        showPopupDlg(
                            "결제완료",
                            "결제가 완료되었습니다.",
                            R.drawable.icon_no_order
                        )
                        delayRun({ showDialog(false) }, 4000L)
                    }
                    else -> {
                        Logger.i("ELSE!!!!!!!!")
                    }
                }
            } ?: sendToast("Result code is empty..")
        }
    }

    private val paymentActivityLauncher = registerResultCode {

        it?.let { result ->
            Logger.w("paymentActivityLauncher registerResultCode - ${result.resultCode}")
            when (result.resultCode) {
                RESULT_OK -> {
                    mainMenuViewModel.clearAllOrders()
                    mOrderList = arrayListOf()
                    APP.releaseUsb()
                    orderStatus = KioskState.MENU_PLAY_INIT_STATE
                    //mainMenuViewModel.getPrintInfo()
                }
                RESULT_CANCELED -> {
                    // back press 에 의해 눌러진 경우 다시 메인메뉴로 넘긴다.
                    cmTcpServer.sendMessage(CMD_TX_WAITNG)
                    mainMenuViewModel.countPopup()
                    orderStatus = KioskState.MENU_SELECTING_STATE
                }
                else -> {
                    Logger.i("ELSE!!!!!!!!")
                }
            }
        } ?: mainMenuViewModel.sendToast("Result code is empty..")
    }

    private val cancelPaymentLauncher = registerResultCode {
        it?.let { result ->
            when (result.resultCode) {
                RESULT_CANCELED -> {
                    // back press 에 의해 눌러진 경우 다시 메인메뉴로 넘긴다.
                }
                else -> {
                    Logger.i("ELSE!!!!!!!!")
                }
            }
        } ?: mainMenuViewModel.sendToast("Result code is empty..")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderStatus = KioskState.MENU_PLAY_INIT_STATE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            run {
                AdNetTheme {
                    Surface {
                        Modifier.padding(vertical = 20.dp)
                         MainMenuScreen(mainMenuViewModel)
                    }
                }
            }
        }

        initData()
        observerData()
    }

    private fun initData() {
        // 알람 스케쥴 설정
        scheduleAlarm()
        mainMenuViewModel.countPopup()
        cmPlayerStatus = SingCastPlayIntent.PLAYER_START
        cmTcpServer.sendMessage(CMD_TX_WAITNG)
        // 프린터 체크 : 스케쥴러 추가 필요..
        checkPrintEnv()
        // 주문 이용시간 체크
        checkKioskEnableTime()

    }

    private fun observerData() {

        with(mainMenuViewModel) {
            toast.observe(this@CustomMainMenuActivity) {
                Toast.makeText(this@CustomMainMenuActivity, it, Toast.LENGTH_SHORT).show()
            }

            currentMenus
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnError { Logger.e("Error : ${it.message}") }
                .onErrorComplete()
                .subscribe({ menuData ->
                    mainMenuViewModel.setStoreName(menuData.storename)
                    mainMenuViewModel.setStoreImage(menuData.storeImage)
                    mainMenuViewModel.setMenus(menuData.catagoryObjectList)
                    delayRun({ mainMenuViewModel.setInit(true) }, 1000L)
                }, { it.printStackTrace() })
                .addTo(compositeDisposable = compositeDisposable)

            showPopup
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnError { Logger.e("Error : ${it.message}") }
                .onErrorComplete()
                .subscribe({ show ->
                    if(show) showUpdateMenuDialog()
                }, { it.printStackTrace() })
                .addTo(compositeDisposable = compositeDisposable)

            selectedOrders.observe(this@CustomMainMenuActivity) {
                setOrderGroup()
                mergeOrder()
                setTotalPrice()
            }

            totalPrice.observe(this@CustomMainMenuActivity) {
                approveMoney = it.toFloat()
            }

            startActivity.observe(this@CustomMainMenuActivity) {
                Logger.w("START PAYMENT : $it")
                startActivity(it)
            }
        }

        repeatOnState(Lifecycle.State.STARTED) {
            bundleSharedFlow.asSharedFlow().debounce(10000).collectLatest {
                mainMenuViewModel.commandTask(it)
            }
        }
    }

    private fun startActivity(payType: Int) {
        when (payType) {
            LaunchType.Payment.type -> {
                orderStatus = KioskState.MENU_PAY_START_STATE
                val intent = Intent(this, CustomPaymentKiccActivity::class.java)
                intent.action = ACTION_PAY_ORDER
                intent.putExtra("paOrderId", "-1")
                paymentActivityLauncher.launch(intent)
            }
            LaunchType.Provision.type -> {
                orderStatus = KioskState.MENU_PAY_START_STATE
                val intent = Intent(APP.applicationContext, CustomProvisionInfoActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP
                provisionInfoLauncher.launch(intent)
            }
            LaunchType.Cancel.type -> {
                requireNotNull(paymentCancelData.AuthCode) { mainMenuViewModel.sendToast("주문취소 데이터를 불러올수 없습니다.") }
                setCancelOrderPay(paymentCancelData)
            }
        }
    }

    private fun setCancelOrderPay(pInfo: PaymentInfo) {
        Logger.e("Start cancel pay order : $pInfo")
        val nComponentName = ComponentName(KICC_PKG_NAME, "kr.co.kicc.easycarda.CallPopup")
        val mIntent = Intent(Intent.ACTION_MAIN)
        val tax = (java.lang.Float.valueOf(pInfo.goodsAmt ?: "0") * 0.1).toInt()

       with(mIntent) {
            putExtra("TRAN_TYPE", "D4") //당일취소/전일취소(반품환불)
            putExtra("INSTALLMENT", "0") // 할부유무 "0" - 일시불, "N" - 할부 N개월, N은 할부 개월수
            putExtra("APPROVAL_NUM", pInfo.AuthCode) //ESSENTIAL(취소거래시)
            putExtra("APPROVAL_DATE", pInfo.orderDate?.substring(0, 6)) //ESSENTIAL(취소거래시)  "YYMMDD"
            putExtra("TERMINAL_TYPE", "40") //단말기 구분 40: 일반거래
            putExtra("TOTAL_AMOUNT", pInfo.goodsAmt) // 총 결제 금액
            putExtra("TAX", tax.toString()) // 수동 부가세
            putExtra("TIP", "0") // 봉사료 금액
            putExtra("TEXT_MAIN_SIZE", "20") //결제진행 텍스트 사이즈 예) 18
            putExtra("TEXT_MAIN_COLOR", "#303030") //결제진행 텍스트 컬러 예) "#303030"
            putExtra("TEXT_SUB1_SIZE", "12") // 무결성검사 텍스트 사이즈 예) 12
            putExtra("TEXT_SUB1_COLOR", "#ff752a") //무결성검사 텍스트 컬러 예) "#ff752a"
            putExtra("TEXT_SUB2_SIZE", "10") // 식별번호 텍스트 사이즈 예) 10
            putExtra("TEXT_SUB2_COLOR", "#ff752a") // 식별번호 텍스트 컬러 예) "#ff752a"
            putExtra("TEXT_SUB3_SIZE", "16") // 타임아웃 텍스트 사이즈 예) 16.putExtra("TEXT_SUB3_COLOR", "#ff752a") //타임아웃 텍스트 컬러 예) 예) "#909090"
            putExtra("IMG_BG_PATH", "/sdcard/kicc/background.png") //배경이미지 경로 예) "/sdcard/kicc/background.png" 사이즈 370 * 150 권장
            putExtra("IMG_CARD_PATH", "/sdcard/kicc/card.png") //카드이미지 경로 예) 예) "/sdcard/kicc/card.png" 사이즈 370 * 150 권장
            putExtra("IMG_CLOSE_PATH", "/sdcard/kicc/close.png") //닫기버튼 이미지 경로 예) "/sdcard/kicc/close.png" 사이즈 45 * 45 권장
            component = nComponentName
       }

        val intent = Intent(this, CustomPaymentKiccActivity::class.java)
        intent.putExtra("paOrderId", "-1")
        intent.action = ACTION_CANCEL_PAY
        intent.putExtras(mIntent)
        cancelPaymentLauncher.launch(intent)
    }

    private fun checkPrintEnv() {
        APP.usbTarget?.let {
            val printResult = ZalComPrint.onGetPrintStatus(APP.mPos)
            Logger.d("printResult : printResult is $printResult")
            when(printResult) {
                0, -7 -> mainMenuViewModel.sendToast("프린터가 정상적으로 연결되었습니다.")
                -4 -> mainMenuViewModel.showPopupDlg("프린터 상태", "프린터 용지가 부족합니다.")
                else -> mainMenuViewModel.sendToast("프린터 오류 : ${onGetStringPrinterErr(printResult)}")
            }
        } ?: mainMenuViewModel.showPopupDlg("프린터 상태", "프린터 연결 상태를 확인해 주세요.")
    }

    private fun checkKioskEnableTime() {
        Logger.e("customUtils.kioskEnable : ${customUtils.kioskEnable}")
        if (!customUtils.kioskEnable) {
            val contents = when (mOpenType.equals("O", ignoreCase = true)) {
                true -> "지금은 주문하실 수 없습니다.\n이용에 불편을 드려 죄송합니다."
                else -> "지금은 영업이 종료되었습니다.\n다음에 이용해 주세요."
            }
            Logger.w("contents : $contents")
            mainMenuViewModel.showPopupDlg(getString(R.string.str_order_button), contents)
        }
    }
}

