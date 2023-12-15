package kr.co.bbmc.paycast.presentation.main

import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.*
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
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
import kr.co.bbmc.paycast.presentation.mainMenu.CustomMainMenuActivity
import kr.co.bbmc.paycast.service.SEND_VALUE
import kr.co.bbmc.paycast.service.SHOW_MESSAGE
import kr.co.bbmc.paycast.ui.component.theme.AdNetTheme
import kr.co.bbmc.paycast.util.*
import kr.co.bbmc.selforderutil.FileUtils
import kr.co.bbmc.selforderutil.ProductInfo.deviceId
import kr.co.bbmc.selforderutil.SingCastPlayIntent
import kr.co.bbmc.selforderutil.SingCastPlayIntent.PLAYER_READY
import kr.hstar.commonutil.delayRun
import kr.hstar.commonutil.repeatOnState
import java.util.*

@ExperimentalFoundationApi
@FlowPreview
class MainKioskActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    // Activity Result Code
    private val mainMenuLauncher = registerResultCode() {
        mainViewModel.showKioskStateInfoPopup()
        orderStatus = KioskState.MENU_PLAY_INIT_STATE
        it?.let { result ->
            Logger.w("mainMenuLauncher registerResultCode - ${result.resultCode}")
            when (result.resultCode) {
                RESULT_RESTART_PAYER,
                RESULT_MENUUPDATE_PAYER -> { } // 에이전트 제거로 사용 안함
                RESULT_CANCELED -> {
                    // back press 에 의해 눌러진 경우 다시 메인메뉴로 넘긴다.
                    delayRun({ startMainMenuActivity() }, 500L)
                }
                else -> { }
            }
        } ?: mainViewModel.sendToast("Result code is empty..")
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @FlowPreview
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            run {
                AdNetTheme {
                    Surface {
                        Modifier.padding(vertical = 20.dp)
                        MainScreen(mainViewModel)
                    }
                }
            }
        }
        initData()
    }

    private fun initData() {
        // 서비스 바인드 (메인서비스, 스마트로 결제서비스)
        initServiceBind()
        //bindSmartro()
        // 알람 매니저 등록
        scheduleAlarm()
        //옵저버 데이터
        observerData()
        //프린터 체크
        initPrinter()
    }

    private fun initPrinter() {
        APP.mUsbManager = getSystemService(USB_SERVICE) as UsbManager
        val deviceList: HashMap<String, UsbDevice>? = APP.mUsbManager?.deviceList

        deviceList?.let {
            Logger.i("Number of device : " + deviceList.size)
        } ?: kotlin.run {
            Logger.i("No device found")
            mainViewModel.sendToast("연결된 장치가 없습니다.")
            return
        }

        APP.usbTarget = deviceList.values.find { it.productName == "USB-Serial Controller" }
        try {
            with(APP) {
                mPos = Pos()
                mUsb = USBPrinting()
                mPos!!.Set(mUsb)
            }
        } catch (e: Exception) {
            mainViewModel.sendToast("USB 프린터 연결이 되지 않았습니다.")
        }
    }

    private fun observerData() {
        with(mainViewModel) {
            toast.observe(this@MainKioskActivity) {
                Logger.i("Toast : $it")
                Toast.makeText(this@MainKioskActivity, it, Toast.LENGTH_SHORT).show()
            }

            networkStatus.observe(this@MainKioskActivity) {
                if (!it) mainViewModel.sendToast("Network disconnect!!")
            }

            initStoreId
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnError { Logger.e("Error : ${it.message}") }
                .onErrorComplete()
                .subscribe({ init ->
                    Logger.w("initStoreId changed : $init // storeId: $storeId & deviceId: $deviceId")
                    if(init) initializeKiosk()
                }, {
                    Logger.e("ERR : ${it.message}")
                })
                .addTo(compositeDisposable = compositeDisposable)

            handlerMsgSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnError { Logger.d("Error : ${it.message}") }
                .onErrorComplete()
                .subscribe({
                    Logger.d("Current message is - ${it.first} // arg1 : ${it.second} // arg2 : ${it.third} ")
                    handleEvent(it)
                }, {})
                .addTo(compositeDisposable = compositeDisposable)

            kioskStateInfo
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnError { Logger.e("Error : ${it.message}") }
                .onErrorComplete()
                .subscribe({
                    Logger.w("kioskStateInfo changed - $it")
                    mainViewModel.showKioskStateInfoPopup()
                    if(it.first == "true" && it.third == "O") startMainMenuActivity()
                }, {
                    Logger.e("ERR : ${it.message}")
                })
                .addTo(compositeDisposable = compositeDisposable)

        }

        repeatOnState(Lifecycle.State.RESUMED) {
            mainViewModel.startAction.collectLatest {
                if(it) startMainMenuActivity()
            }
        }

        repeatOnState(Lifecycle.State.STARTED) {
            bundleSharedFlow.asSharedFlow().debounce(500).collectLatest {
                mainViewModel.commandTask(it)
            }
        }
    }

    private fun initServiceBind() {
        mainViewModel.activityMessenger = Messenger(ActivityHandler())
        mainViewModel.isBound = serviceBind(mainViewModel.serviceConnection)
        Logger.w("Bind Service result : ${mainViewModel.isBound}")
    }

    private fun bindSmartro() {
        Intent("smartro.vcat.action")
            .apply {
                setPackage("service.vcat.smartro.com.vcat")
                putExtra("package", super.getPackageName())
            }.let {
                bindService(it, APP.mServiceConnectionExample, Context.BIND_AUTO_CREATE)
            }
        //To bind Interface-Constructor at Service
    }

    private fun handleEvent(msg: Triple<Int, Int, Int>) {
        Logger.w("Handle Event : ${msg.first}")
        when (msg.first) {
            SHOW_MESSAGE -> {
                val resId: Int = msg.second
                if (resId > 0) mainViewModel.sendToast(getString(resId))
            }
            SEND_VALUE -> {
                val intentAction: Int = msg.second
                if (networkStatus && intentAction == PLAYER_READY) {
                    val status: Int = mainViewModel.playerStatus
                    val validPlayerCondition = APP.playerOpt?.optionDefaultMenuFile?.isNotBlank() ?: false
                    FileUtils.writeDebug("MainActivity ActivityHandler() status=$status\n", "PayCast")
                    when (status) {
                        SingCastPlayIntent.PLAYER_STOP -> {
                            if (validPlayerCondition) {
                                FileUtils.writeDebug(String.format("MainActivity ActivityHandler() playActivityKiccStartTimerTask() start()\n"), "PayCast")
                            } else {
                                Logger.w("validPlayerCondition error!!")
                                mainViewModel.sendToast("optionDefaultMenuFile을 확인할 수 없습니다.")
                            }
                        }
                        SingCastPlayIntent.PLAYER_SHOWING -> {
                            FileUtils.writeDebug(String.format("MainActivity ActivityHandler() PLAYER_SHOWING.. \n"), "PayCast")
                        }
                    }
                } else if (intentAction == SingCastPlayIntent.AGENT_RESTART_OK) {
                    // 현재 버전은 에이전트를 사용하지 않는다.
                } else {
                    Logger.d("SingCastPlayIntent val=$intentAction")
                }
            }
            else -> {
                Logger.e("None Msg")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseTask()
        cancelScheduleAlarm()
    }

    private fun releaseTask() {
        APP.release()
    }

    private fun startMainMenuActivity() {
        val sendIntent = Intent(this, CustomMainMenuActivity::class.java)
        sendIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP
        mainMenuLauncher.launch(sendIntent)
    }

    companion object {
        const val RESULT_RESTART_PAYER = 202
        const val RESULT_END_PAYMENT = 203
        const val RESULT_MENUUPDATE_PAYER = 302
    }
}