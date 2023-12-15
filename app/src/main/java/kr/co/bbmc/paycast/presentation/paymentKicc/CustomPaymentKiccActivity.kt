@file:OptIn(FlowPreview::class)

package kr.co.bbmc.paycast.presentation.paymentKicc

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import com.lvrenyang.io.base.IOCallBack
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kr.co.bbmc.paycast.*
import kr.co.bbmc.paycast.App.Companion.APP
import kr.co.bbmc.paycast.data.repository.local.KioskSettingPreference
import kr.co.bbmc.paycast.ui.component.theme.AdNetTheme
import kr.co.bbmc.paycast.util.*
import kr.co.bbmc.selforderutil.*
import kr.hstar.commonutil.delayRun
import kr.hstar.commonutil.repeatOnState
import java.text.SimpleDateFormat
import java.util.*

class CustomPaymentKiccActivity : AppCompatActivity(), IOCallBack {

    @OptIn(FlowPreview::class)
    private val vm: PaymentViewModel by viewModels()

    private val paymentAppLauncher = registerResultCode {
        it?.let { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    requireNotNull(result.data) { Logger.e("Intent is null") }
                    onPayResult(data = result.data!!, resResultCode = result.data!!.getStringExtra("RESULT_CODE") ?: "")
                }
                else -> {
                    APP.releaseUsb()
                    setResult(RESULT_CANCELED)
                    Logger.w("onClick() finish() 06066")
                    FileUtils.writeDebug("OnDelayTimerTask() finish() 06066\r\n", "PayCast")
                    finish()
                }
            }
        } ?: vm.sendToast("Result code is empty..")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            run {
                AdNetTheme {
                    Surface {
                        PaymentKiccScreen(vm)
                    }
                }
            }
        }

        initData()
        observerData()
    }

    private fun initData() {
        paymentStatus = paymentStatus or BEFOREPAY_STATUS
        orderStatus = KioskState.MENU_PAY_START_STATE
        Logger.w("Payment Activity Create - OrderList : %s - Total price : ${mOrderList.sumOfTotalPrice()}", mOrderList.map { it.text to it.itemprice })
        vm.orderFlag = intent?.action
        Logger.e("orderFlag : ${vm.orderFlag}")
        when (vm.orderFlag) {
            ACTION_PAY_ORDER -> {
                mTranTypte = "D1"
                vm.refillNumber = intent.getStringExtra(INTENT_REFILL_NUM) //refill
                vm.refillOrderId = intent.getStringExtra(INTENT_ORDER_ID) //refill
            }
            ACTION_CANCEL_PAY -> {
                vm.refillNumber = null
                vm.refillOrderId = null
                mTranTypte = "D4"
            }
            else -> {
                Logger.e("No data")
            }
        }

        initPrintData()

    }

    private fun initPrintData() {
        try {
            APP.mUsb!!.SetCallBack(this)
            vm.print(this@CustomPaymentKiccActivity)
        } catch (e: Exception) {
            vm.sendToast("USB 프린터 연결이 되지 않았습니다.")
        }
    }

    private fun observerData() {

        vm.toast.observe(this@CustomPaymentKiccActivity) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        repeatOnState(Lifecycle.State.STARTED) {
            vm.startPay.collectLatest {
                if (it) paymentStart()
            }
        }

        repeatOnState(Lifecycle.State.STARTED) {
            vm.finish.collectLatest {
                if (it) {
                    setResult(RESULT_OK)
                    delayRun({ finish() }, 300L)
                }
            }
        }
    }

    private fun paymentStart() {
        onRequestPay()
    }

    override fun OnOpen() {
        Logger.w("On open!!")
        CoroutineScope(Dispatchers.Main).launch {
            if (!networkStatus) {
                vm.sendToast("네트워크 연결을 확인해 주세요.")
                return@launch
            }

            var printResult = ZalComPrint.onGetPrintStatus(APP.mPos)
            Logger.w("1. print result = $printResult")
            if (printResult != 0) printResult = ZalComPrint.onGetPrintStatus(APP.mPos)
            Logger.w("2. print result = $printResult")
            when(printResult) {
                0 -> requestPayTask()
                -4 -> {
                    vm.sendToast("프린터 용지가 부족합니다.")
                    vm.showPopupDlg("프린터 상태", "프린터 용지가 부족합니다.")
                    delayRun({finish()}, 3000L)
                }
                else -> vm.sendToast("프린터 오류 : ${onGetStringPrinterErr(printResult)}")
            }
        }
    }

    private fun requestPayTask() {
        var count = 0
        Logger.w("requestPayTask start!!")
        if (vm.orderFlag == ACTION_PAY_ORDER) {
            vm.getOrderNum()
        } else {    // refill
            if (approveMoney > 0) {
                onRequestPay()
            } else {
                while (mNumberOfOrder <= 0) {
                    count++
                    if (count > 10) break
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        break
                    }
                }
                if (mNumberOfOrder > 0) onSendRefillInfo(vm.refillNumber)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun onSendRefillInfo(tel: String?) {
        if (tel == null) {
            vm.sendToast("리필용 전화번호를 다시 확인해주세요.")
            return
        }
        val payData = PaymentInfoData()
        val tran_type = "RF" //거래구분자 ‘D1’ : 승인 ‘D4’ : 당일취소/전일취소(반품환불)  refilll 을 위해 RF 추가
        val date = Utils.getCurrentDate()
        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        var refilldate = sdf.format(date)
        val cal = Calendar.getInstance()
        val dayNum = cal[Calendar.DAY_OF_WEEK]
        refilldate += String.format("%d", dayNum - 1)
        Logger.e("getmNumberOfOrder()=" + KioskSettingPreference.getLastOrderNumber())
        payData.paOrdId = vm.refillOrderId
        payData.transType = tran_type
        payData.storeOrderId = ""
        payData.cardNumber = "" //결제한 카드번호/식별번호 예) 카드결제인경우 - "499912" (Card BIN 만 전송) 식별번호인 경우 - "010****5678" , "820101*******"
        payData.cardName = "" //결제한 카드사 예) "신한카드"
        payData.payAmount = "0" //총 결제 금액 예) 5만원인경우 - "50000"
        payData.payVat = "0" //부가세 금액 예)  1천원인경우 - "1000"
        payData.installmentMonth = "0" //할부유무/현금영수증 취소사유  (신용승인/취소) "0" - 일시불 "N" - 할부 N개월, N은 할부 개월수
        payData.approvalNum = "" //거래 승인번호 예) "12345678"
        payData.tradingDate = refilldate //거래 승인날짜 "YYMMDDHHMMSSW" W 는 요일 (일요일:0, 월요일:1, 화요일:2 …토요일:6) 예) 2013년 2월 1일 12시 15분 17초 금요일 인경우 "1302011215175"
        payData.acquirer = "" //매입사 코드 예) "031"
        payData.acquirerName = "" //매입사명 예) "신한카드"
        payData.storeMerchantNum = "" //가맹점번호
        payData.shop_tid = "" //결제된 가맹점 TID
        payData.shop_bizNum = "" //결제된 가맹점 사업자번호

        paymentStatus = paymentStatus or AFTERPAY_STATUS
        val kioskPayDataInfo = onSetKioskPayDataInfoV2(payData)
        kioskPayDataInfo.telephone = mTelephone
        vm.updatePaymentInfo(kioskPayDataInfo, payData, tran_type)
    }

    private fun onRequestPay() {
        val intent = when (mTranTypte) {
            "D1" -> requestPayIntent()
            "D4" -> getCancelOrderData()
            else -> null
        }
        requireNotNull(intent) { vm.sendToast("데이터를 가져올수 없습니다.") }
        delayRun({
            paymentAppLauncher.launch(intent)
        }, 1000L)
        FileUtils.writeDebug("Card app start()======= \n", "PayCast")
    }

    override fun OnOpenFailed() {
        Logger.e("USB Port open Failed.")
        vm.sendToast("USB Port open Failed.")
        vm.getOrderNum()
    }

    override fun OnClose() {
        Logger.e("USB Port onClose!!")
        APP.mTaskPrint!!.bPrintEnd = true
    }

    private fun onPayResult(data: Intent, resResultCode: String) {
        Logger.d("Pay start!!")
        val payData = PaymentInfoData()
        if (resResultCode == "0000") {
            val tran_type: String = data.getStringExtra("TRAN_TYPE") ?: "" //거래구분자 ‘D1’ : 승인 ‘D4’ : 당일취소/전일취소(반품환불)
            payData.transType = tran_type
            if (tran_type.equals("D4", ignoreCase = true)) {
                payData.storeOrderId = storeOrderId
            } else payData.storeOrderId = ""
            payData.cardNumber = data.getStringExtra("CARD_NUM") ?: ""//결제한 카드번호/식별번호 예) 카드결제인경우 - "499912" (Card BIN 만 전송) 식별번호인 경우 - "010****5678" , "820101*******"
            payData.cardName = data.getStringExtra("CARD_NAME") ?: "" //결제한 카드사 예) "신한카드"
            payData.payAmount = data.getStringExtra("TOTAL_AMOUNT") ?: ""//총 결제 금액 예) 5만원인경우 - "50000"
            payData.payVat = data.getStringExtra("TAX") ?: ""//부가세 금액 예)  1천원인경우 - "1000"
            payData.installmentMonth = data.getStringExtra("INSTALLMENT") ?: ""//할부유무/현금영수증 취소사유  (신용승인/취소) "0" - 일시불 "N" - 할부 N개월, N은 할부 개월수
            payData.approvalNum = data.getStringExtra("APPROVAL_NUM") ?: ""//거래 승인번호 예) "12345678"
            payData.tradingDate = data.getStringExtra("APPROVAL_DATE") ?: "" //거래 승인날짜 "YYMMDDHHMMSSW" W 는 요일 (일요일:0, 월요일:1, 화요일:2 …토요일:6) 예) 2013년 2월 1일 12시 15분 17초 금요일 인경우 "1302011215175"
            payData.acquirer = data.getStringExtra("ACQUIRER_CODE") ?: ""//매입사 코드 예) "031"
            payData.acquirerName = data.getStringExtra("ACQUIRER_NAME") ?: ""//매입사명 예) "신한카드"
            payData.storeMerchantNum = data.getStringExtra("MERCHANT_NUM") ?: ""//가맹점번호
            payData.shop_tid = data.getStringExtra("SHOP_TID") ?: ""//결제된 가맹점 TID
            payData.shop_bizNum = data.getStringExtra("SHOP_BIZ_NUM") ?: ""//결제된 가맹점 사업자번호
            val cashAmount: String = data.getStringExtra("CASHAMOUNT") ?: ""//현금지급금액
            Logger.d("====결제응답===")
            Logger.d("cardNumber=" + payData.cardNumber + " cardName=" + payData.cardName + " payAmount=" + payData.payAmount)
            Logger.d("shop_tid=" + payData.shop_tid + " shop_biz_num=" + payData.shop_bizNum + " cashamount=" + cashAmount)
            Logger.d("approvalNum=" + payData.approvalNum + " tradingDate=" + payData.tradingDate + " acquirer=" + payData.acquirer)
            Logger.d("acquirerName=" + payData.acquirerName + " storeMerchantNum=" + payData.storeMerchantNum + " shop_tid=" + payData.shop_tid)
            Logger.d("shop_bizNum=" + payData.shop_bizNum)
            Logger.d("Tax=" + payData.payVat)
            Logger.d("=====================")
            paymentStatus = paymentStatus or AFTERPAY_STATUS
            val kioskPayDataInfo = onSetKioskPayDataInfoV2(payData)
            kioskPayDataInfo.telephone = mTelephone
            when(tran_type) {
                "D4" -> { vm.cancelPayment(kioskPayDataInfo, payData, tran_type) }
                else -> { vm.updatePaymentInfo(kioskPayDataInfo, payData, tran_type) }
            }
        } else {
            val errReason = KiccErrReason.getReasonErr(resResultCode)
            Logger.e("Error : payment failed -> $errReason")
            vm.showPopupDlg("카드결제", "$errReason($resResultCode)")
            vm.sendToast("오류 : $errReason")
            setResult(RESULT_CANCELED)
            delayRun({finish()}, 300L)
            FileUtils.writeDebug(
                "Card app Error errReason=$errReason($resResultCode)======\r\n",
                "PayCast"
            )
        }
    }


    companion object {
        const val POOL_SIZE = 30
        // 결제 상태 정보
        const val AFTERPAY_STATUS = 0x0090
        const val BEFOREPAY_STATUS = 0x0080
        const val INITPAY_STATUS = 0x0000
        const val MS_CARD = 0x1000
        const val RF_CARD = 0x0100
    }
}