@file:OptIn(FlowPreview::class)

package kr.co.bbmc.paycast

import android.app.Application
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.IBinder
import android.os.Messenger
import androidx.appcompat.app.AppCompatDelegate
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.rexod.escpos.usblib.Rexodusb
import com.sam4s.printer.Sam4sFinder
import com.sewoo.jpos.printer.ESCPOSPrinter
import com.sewoo.port.android.DeviceConnection
import com.sewoo.port.android.WiFiPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import kr.co.bbmc.paycast.data.repository.local.KioskSettingPersister
import kr.co.bbmc.paycast.data.repository.local.KioskSettingPreference
import kr.co.bbmc.paycast.data.repository.local.PayCastPreference
import kr.co.bbmc.paycast.data.repository.remote.KioskRepository
import kr.co.bbmc.paycast.presentation.paymentKicc.TaskPrintV2
import kr.co.bbmc.paycast.printer.ConnectionInfo
import kr.co.bbmc.paycast.printer.PrinterConnection
import kr.co.bbmc.paycast.receiver.OnPlayerCmdReceiver
import kr.co.bbmc.selforderutil.PlayerOptionEnv
import kr.co.bbmc.selforderutil.SingCastPlayIntent
import kr.co.bbmc.selforderutil.StbOptionEnv
import service.vcat.smartro.com.vcat.SmartroVCatInterface

@FlowPreview
class App : Application() {

    val repository by lazy { KioskRepository() }
    // Global receiver
    private lateinit var brPlayerCmdReceiver: BroadcastReceiver
    var serviceMessenger: Messenger? = null
    @JvmField
    var mUsbConn: Rexodusb? = null
    @JvmField
    var samPrint: Sam4sFinder? = null
    @JvmField
    var mPrinterConnection: PrinterConnection? = null
    // printer field
    @JvmField var mPos: Pos? = null
    @JvmField var mUsbManager: UsbManager? = null
    @JvmField var mUsb: USBPrinting? = null
    //private var mPayDataInfo: PaymentInfoData? = null
    @JvmField var mTaskPrint: TaskPrintV2? = null
    @JvmField var usbTarget: UsbDevice? = null  // 실제 USB에 연결된 프린터(내장프린터)

    // smarto service
    @JvmField var mSmartroVCatInterface: SmartroVCatInterface? = null
    @JvmField val mServiceConnectionExample: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            APP.mSmartroVCatInterface = SmartroVCatInterface.Stub.asInterface(service)
            try {
                Logger.w("Smartro service binded")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Logger.d("Smartro service discnnected")
        }
    }

    val dataStore by lazy { DataStoreManager(this) }

    // Global value
    @JvmField
    var playerOpt: PlayerOptionEnv? = null
    @JvmField
    var stbOpt: StbOptionEnv? = null
    @JvmField
    var sellerInfo: ArrayList<String> = arrayListOf()
    @JvmField
    var kitchenPrinter: ESCPOSPrinter? = null
    @JvmField
    var wifiPort: WiFiPort? = null
    @JvmField
    var wfThread: Thread? = null

    @JvmField
    var printConnect: DeviceConnection? = null

    private lateinit var cm : ConnectivityManager
    private val networkCallBack = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            networkStatus = true
            CoroutineScope(Dispatchers.IO).launch { netWorkConnectionFlow.emit(true) }
        }
        override fun onLost(network: Network) {
            networkStatus = false
            CoroutineScope(Dispatchers.IO).launch { netWorkConnectionFlow.emit(false) }
        }
    }

    override fun onCreate() {
        super.onCreate()
        //App 디폴드 화면을 Dark 모드로 설정
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        APP = this
        //ireStoreDataSource.init(SCREEN_ID) //릴리즈 버전에서는 사용하지 않는다.
        Logger.addLogAdapter(AndroidLogAdapter())
        registerPlayerCmdReceiver()
        registerNetworkCallback()

        samPrint = Sam4sFinder()

        mUsbConn = Rexodusb(this)
        kitchenPrinter = ESCPOSPrinter("EUC-KR")
        wifiPort = WiFiPort.getInstance()
        Logger.w("USB conn is $mUsbConn")
        printConnect = ConnectionInfo.getInstance().connection
//        Logger.w("Print connect : $printConnect")
        initPreference()
    }

    private fun initPreference() {
        KioskSettingPersister.initPrefs(this)
        PayCastPreference.initPrefs(this)
        KioskSettingPreference.initPrefs(this)
    }

    private fun registerPlayerCmdReceiver() {
        brPlayerCmdReceiver = OnPlayerCmdReceiver()
        IntentFilter().apply {
            addAction(SingCastPlayIntent.ACTION_PLAYER_COMMAND)
            registerReceiver(brPlayerCmdReceiver, this)
        }
    }

    private fun registerNetworkCallback() {
        cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerNetworkCallback(NetworkRequest.Builder().build(),networkCallBack)
    }

    private fun releaseNetworkCallback() {
        if(::cm.isInitialized) cm.unregisterNetworkCallback(networkCallBack)
    }

    private fun releasePlayerCmdReceiver() {
        if(::brPlayerCmdReceiver.isInitialized) unregisterReceiver(brPlayerCmdReceiver)
    }

    fun releaseWifiThread() {
        APP.wfThread?.let {
            Logger.d("releaseWifiThread - start")
            if (it.isAlive) {
                it.interrupt()
                APP.wfThread = null
            }
        } ?: { Logger.d("APP.wfThread is null") }
    }

    fun release() {
        releaseNetworkCallback()
        releasePlayerCmdReceiver()
        try { unbindService(mServiceConnectionExample) } catch (e: Exception) { Logger.e("Unbind Error : ${e.message}")}
    }

    fun releaseUsb() {
        Logger.e("Release usb !! ")
        APP.mTaskPrint?.let {
            if(!it.bPrintEnd) APP.mUsb?.Close()
        }
        APP.mTaskPrint = null
    }

    companion object {
        lateinit var APP: App
    }
}