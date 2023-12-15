package kr.co.bbmc.paycast

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import kr.co.bbmc.paycast.App.Companion.APP
import kr.co.bbmc.paycast.presentation.dialog.model.DlgInfo
import kr.co.bbmc.paycast.presentation.paymentKicc.CustomPaymentKiccActivity
import kr.co.bbmc.paycast.presentation.paymentKicc.TaskPrintV2
import kr.co.bbmc.paycast.ui.component.theme.ButtonType
import kr.co.bbmc.paycast.util.withIoThread
import kr.co.bbmc.selforderutil.PlayerCommand
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@FlowPreview
open class BaseViewModel : ViewModel() {

    val compositeDisposable = CompositeDisposable()

    private val _toast = MutableLiveData<String>()
    val toast: LiveData<String> = _toast
    fun sendToast(msg: String) = _toast.postValue(msg)

    private val _showDlg = MutableLiveData(false)
    val showDlg = _showDlg
    fun showDialog(show: Boolean) = _showDlg.postValue(show)

    private val _dlgInfo = MutableLiveData<DlgInfo>()
    val dlgInfo = _dlgInfo
    fun setDlgInfo(dlg: DlgInfo) = _dlgInfo.postValue(dlg)
    fun showPopupDlg(
        title: String,
        contents: String,
        icon: Int? = null,
        type: ButtonType = ButtonType.Single,
    ) {
        showDialog(true)
        setDlgInfo(
            DlgInfo(
                type = type,
                contentTitle = title,
                contents = contents,
                positiveCallback = { showDialog(false) },
                iconResource = icon
            )
        )
    }

    fun errorPopupDlg(msg: String = "") {
        showPopupDlg(
            "오류",
            "${msg}인터넷 연결을 확인하시고\n앱을 재시작 해주세요.",
            R.drawable.logo_error_w,
            type = ButtonType.Notice
        )
    }

    private val kioskStateInfo = MutableLiveData<Triple<String, String, String>>()

    init {
        observerData()
    }

    private fun observerData() {
        kioskEnableSubject
            .withIoThread()
            .doOnError { Logger.e("Error : ${it.message}") }
            .onErrorComplete()
            .subscribe({ data ->
                Logger.w("kioskEnableSubject changed : $data")
                // koEnabled, atEnabled, openType: "O: open "C": closed
                kioskStateInfo.value = data
            }, {
                Logger.e("ERR : ${it.message}")
            })
            .addTo(compositeDisposable = compositeDisposable)

        errorMsgSubject
            .withIoThread()
            .doOnError { Logger.e("Error : ${it.message}") }
            .onErrorComplete()
            .subscribe({ msg ->
                if (msg.isNotBlank()) sendToast(msg)
            }, {
                Logger.e("ERR : ${it.message}")
            })
            .addTo(compositeDisposable = compositeDisposable)
    }

    fun showKioskStateInfoPopup() {
        Logger.w("Current state : ${kioskStateInfo.value}")
        when {
            kioskStateInfo.value?.third == "C" -> {
                showKioskStatePopup(type = KioskState.CLOSED)
            }
            kioskStateInfo.value?.first == "false" -> {
                showKioskStatePopup(type = KioskState.ENABLED)
            }
            kioskStateInfo.value?.first == "true" -> {
                showDialog(false); sendToast("영업을 시작합니다.")
            }
            kioskStateInfo.value?.third == "O" -> {
                showDialog(false); sendToast("키오스크 주문이 가능합니다.")
            }
            else -> {
                Logger.e("kioskEnableSubject : Else")
            }
        }
    }

    fun showUpdateMenuDialog() {
        showPopupDlg(
            APP.getString(R.string.str_noti_menu_info_title),
            APP.getString(R.string.str_noti_menu_update_msg),
            R.drawable.icon_no_order,
            type = ButtonType.Notice
        )
    }

    private fun showKioskStatePopup(type: KioskState = KioskState.ENABLED) {
        when (type) {
            KioskState.ENABLED -> {
                showPopupDlg(
                    "키오스크 주문 불가능",
                    "지금은 주문하실 수 없습니다.\n이용에 불편을 드려 죄송합니다.",
                    R.drawable.icon_no_order,
                    type = ButtonType.Notice
                )
            }
            KioskState.CLOSED -> {
                showPopupDlg(
                    "영업종료",
                    "지금은 영업이 종료되었습니다.\n다음에 이용해 주세요.",
                    R.drawable.icon_no_order,
                    type = ButtonType.Notice
                )
            }
        }
    }

    enum class KioskState { ENABLED, CLOSED }

    private var countTimerObserver: Disposable? = null
    fun countTimer(baseTime: Long, callback: () -> Unit) {
        releaseTimer()
        countTimerObserver = Observable.interval(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .map { baseTime - it }
            .takeWhile { it > 0L }
            .onErrorComplete()
            .subscribe({
                if (it <= 1L) {
                    callback.invoke()
                }
            }, {}).addTo(compositeDisposable)
    }

    fun releaseTimer() {
        countTimerObserver?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
        countTimerObserver = null
    }

    //서비스 태스크
    fun commandTask(cmd: Bundle) {
        Logger.w("commandTask : ${cmd.getString("command")}")

        val c = PlayerCommand()
        c.command = cmd.getString("command")
        c.requestDateTime = cmd.getString("requestDateTime")
        c.executeDateTime = cmd.getString("executeDateTime")
        c.addInfo = cmd.getString("addInfo")

        when (c.command) {
            APP.getString(R.string.str_command_connect_server) -> {
                Logger.w("server connected!!! ")
                //getBannerInfo()
            }
            APP.getString(R.string.str_command_player_restart) -> {}
            APP.getString(R.string.kiosk_menu_update_command),
            APP.getString(R.string.str_command_menu_update),
            -> {
                sendToast("메뉴가 업데이트 되었습니다.")
            }
            APP.getString(R.string.kiosk_banner_update_command) -> {
                // 광고 재생중 노출 여부 확인할 것
                sendToast("광고 정보가 업데이트 되었습니다.")
            }
            APP.getString(R.string.paycast_did_network_stability) -> {
                // network enable
                Logger.e("Net work On")
            }
            APP.getString(R.string.paycast_did_network_instability) -> {
                // network disable
                Logger.e("Net work OFF")
            }
            APP.getString(R.string.str_command_player_auth_fail) -> {
                //에이전트 앱을 사용하지 않아서 인증기능을 사용하지 않는다.
            }
            APP.getString(R.string.str_command_player_deviceid) -> {
                c.addInfo?.let {
                    with(APP.stbOpt) {
                        this?.deviceId = it
                        this?.koEnable = cmd.getString("koEnabled") ?: ""
                        this?.atEnable = cmd.getString("atEnabled") ?: ""
                        this?.openType = cmd.getString("openType") ?: ""
                    }
                }
            }
            APP.getString(R.string.str_command_player_not_exist_deviceid_file) -> { }
            APP.getString(R.string.kiosk_menu_update_end_command) -> {
                showDialog(false)
                sendToast("메뉴가 업데이트 되었습니다.")
            }
            "xml load failed" -> {
                sendToast("xml 파일정보를 불러올수 없습니다.")
            }
            "New menu updated" -> {
                showDialog(false)
                sendToast("메뉴가 업데이트가 완료되었습니다.")
            }
            APP.getString(R.string.kiosk_opentype_update_command) -> {
                Logger.w("App open Type : ${APP.stbOpt?.openType}")
                when (APP.stbOpt?.openType ?: "O") {
                    "O" -> {
                        showDialog(false)
                        //startMainMenuActivity()
                    }
                    else -> showPopupDlg(
                        "영업종료",
                        "지금은 영업이 종료되었습니다.\n다음에 이용해 주세요.",
                        R.drawable.icon_no_order,
                        type = ButtonType.Notice
                    )
                }
            }
            APP.getString(R.string.kiosk_disenable_command) -> {
                if ((APP.stbOpt?.koEnable ?: "true").equals("false", ignoreCase = true)) {
                    when {
                        (APP.stbOpt?.openType ?: "O").equals("O", ignoreCase = true) -> {
                            showPopupDlg(
                                "키오스크 주문 불가능",
                                "지금은 주문하실 수 없습니다.\n이용에 불편을 드려 죄송합니다.",
                                R.drawable.icon_no_order,
                                type = ButtonType.Notice
                            )
                        }
                        else -> {
                            showPopupDlg(
                                "영업종료",
                                "지금은 영업이 종료되었습니다.\n다음에 이용해 주세요.",
                                R.drawable.icon_no_order,
                                type = ButtonType.Notice
                            )
                        }
                    }
                } else {
                    showDialog(false)
                }
            }
        }
    }

    private val es: ExecutorService = Executors.newScheduledThreadPool(CustomPaymentKiccActivity.POOL_SIZE)

    fun print(activity: Activity) {
        APP.usbTarget?.let {
            CoroutineScope(Dispatchers.IO).launch {
                launch {
                    val bOpen = APP.mUsb!!.Open(APP.mUsbManager, it, activity)
                    Logger.d("Print open result : $bOpen")
                    if (bOpen) {
                        APP.mTaskPrint = TaskPrintV2(APP.mPos)
                        es.submit(APP.mTaskPrint)
                    } else {
                        sendToast("장치를 열수 없습니다. 프린터 설정을 확인해 주세요.")
                    }
                }.join()
                Logger.w("Finish check usb device")
            }
        } ?: run { sendToast("장치가 연결되지 않았습니다."); return }
    }

    override fun onCleared() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        super.onCleared()
    }
}