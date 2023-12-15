package kr.co.bbmc.paycast.presentation.main

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kr.co.bbmc.paycast.*
import kr.co.bbmc.paycast.App.Companion.APP
import kr.co.bbmc.paycast.service.CONNECT
import kr.co.bbmc.paycast.service.SEND_VALUE
import kr.co.bbmc.paycast.ui.component.theme.ButtonType
import kr.co.bbmc.paycast.util.baseExceptionHandler
import kr.co.bbmc.paycast.util.getOrDefault
import kr.co.bbmc.paycast.util.sendBroadCastService
import kr.co.bbmc.selforderutil.SingCastPlayIntent

@FlowPreview
class MainViewModel(savedStateHandle: SavedStateHandle) : BaseViewModel() {

    var playerStatus: Int = savedStateHandle.getOrDefault(KEY_PLAYER_STATUS, SingCastPlayIntent.PLAYER_STOP)
    var isBound : Boolean = savedStateHandle.getOrDefault(KEY_IS_BOUND, false)
    var agentStartFlag: Boolean = savedStateHandle.getOrDefault(KEY_IS_BOUND, false)

    val networkStatus = netWorkConnectionFlow.asLiveData()

    private val _startAction = MutableStateFlow(false)
    val startAction = _startAction
    private fun launchToMainMenu() { _startAction.value = true }

    var activityMessenger: Messenger? = null

    // 스토어 상태 정보
    val kioskStateInfo = kioskEnableSubject.share()

    // 서비스 커넥션
    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            APP.serviceMessenger = Messenger(service)
            var msg = Message.obtain(null, CONNECT, this@MainViewModel.playerStatus)
            msg.replyTo = activityMessenger
            try {
                APP.serviceMessenger?.send(msg) ?: Logger.e("serviceMessenger is null")
                Logger.i("send service msg : $msg")
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            val arg2 = if (agentStartFlag) 1 else 0
            msg = Message.obtain(null, SEND_VALUE, SingCastPlayIntent.PLAYER_READY, arg2)
            msg.replyTo = activityMessenger
            try {
                APP.serviceMessenger?.send(msg) ?: Logger.e("serviceMessenger is null")
                Logger.i("send service msg2 SingCastPlayIntent.PLAYER_READY : $msg")
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            APP.sendBroadCastService(APP.getString(R.string.str_command_connect_server), true)
            Logger.d("Service Connected!!")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            APP.serviceMessenger = null
            sendToast("service disconnected!!")
        }
    }

    // 초기화 코드(스토어아이디를 생성한 이후 초기화되어야하는 작업목록)
    fun initializeKiosk() = CoroutineScope(Dispatchers.IO + baseExceptionHandler {
            Logger.w("initializeKiosk failed : $it"); sendToast("초기화에 실패했습니다.")
            errorPopupDlg()
        }).launch {
            val state = async { runCatching { APP.repository.getStoreStateRes() }.onFailure { Logger.e("state load failed: ${it.message}") }.getOrNull() }
            Logger.i("1. kioskState = ${state.await()?.data}")
            require(state.await()?.success ?: false) { sendToast("키오스크 정보를 가져오지 못했습니다.") }
            state.await()?.let {
                sellerData = it.data.sellerInfo
                startLaunch(it.data)
            }
        }

    private fun startLaunch(kioskState: kr.co.bbmc.paycast.network.model.KioskState) {
        CoroutineScope(Dispatchers.IO).launch {
            requireNotNull(kioskState.openType) { sendToast("키오스크 정보를 가져올 수 없습니다."); return@launch }
            when {
                kioskState.openType == "C" -> {
                    showPopupDlg("영업종료", "지금은 영업이 종료되었습니다.\n다음에 이용해 주세요.", R.drawable.icon_no_order, type = ButtonType.Notice)
                }
                kioskState.kioskEnable == "false" -> {
                    showPopupDlg("키오스크 주문 불가능", "지금은 주문하실 수 없습니다.\n이용에 불편을 드려 죄송합니다.", R.drawable.icon_no_order, type = ButtonType.Notice)
                }
                else -> {
                    cmPlayerStatus = SingCastPlayIntent.PLAYER_READY
                    withTimeoutOrNull(90000) {
                        menuDownloadFinished
                            .collectLatest {
                                if (it) { launchToMainMenu() }
                                else { errorPopupDlg("메뉴파일을 가져올수 없습니다.\n") }
                            }
                    } ?: kotlin.run {
                        errorPopupDlg("메뉴파일을 가져올수 없습니다.\n")
                    }
                }
            }
        }
    }

    companion object {
        const val KEY_PLAYER_STATUS = "key_player_status"
        const val KEY_IS_BOUND = "key_is_bound"
    }
}