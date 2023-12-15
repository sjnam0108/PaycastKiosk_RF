package kr.co.bbmc.paycast.service

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.*
import android.os.*
import android.util.Log
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.retry
import kr.co.bbmc.paycast.*
import kr.co.bbmc.paycast.App.Companion.APP
import kr.co.bbmc.paycast.R
import kr.co.bbmc.paycast.data.model.*
import kr.co.bbmc.paycast.data.model.CatagoryObject
import kr.co.bbmc.paycast.data.model.CustomAddedOptionData
import kr.co.bbmc.paycast.data.model.CustomOptionData
import kr.co.bbmc.paycast.data.model.CustomRequiredOptionData
import kr.co.bbmc.paycast.data.model.MenuCatagoryObject
import kr.co.bbmc.paycast.data.model.MenuObject
import kr.co.bbmc.paycast.data.model.MenuOptionData
import kr.co.bbmc.paycast.util.*
import kr.co.bbmc.paycast.util.XmlOptionParser
import kr.co.bbmc.selforderutil.*
import kr.co.bbmc.selforderutil.FileUtils
import kr.co.bbmc.selforderutil.ProductInfo.deviceId
import kr.hstar.commonutil.delayRun
import org.apache.commons.net.ftp.FTPClient
import org.json.JSONException
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.*
import java.net.*
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.concurrent.scheduleAtFixedRate

@FlowPreview
@Suppress("DEPRECATION")
open class MainServiceV2 : Service() {
    var customUtils = CustomUtils()

    //private val mServiceMessenger: Messenger = Messenger(ServiceHandler())
    private var mActivityMessenger: Messenger? = null
    private lateinit var mMessenger: Messenger
    private var lastTimeSyncDt: Date? = null
    private val downloadList: MutableList<DownFileInfo> = ArrayList()
    private val commandList: MutableList<CommandObject> = ArrayList()
    private var newcommandList: MutableList<CommandObject>? = ArrayList()

    private var sendConfigNow = false
    private var sendConfigSeq = -1

    // jason:multiplesyncmode: 다중 STB 동기화 모드(2016/08/21)
    private var syncStartDt: Date? = null
    private var syncOffsetMillisecs: Long = 0
    private var receiveBgImgFile = ""
    private var receiveConfigSeq = -1
    private var resultOpCode = ""
    private var receiveConfigNow = false
    private var captureNow = false
    private var captureParam = ""

    //public List<NameValuePair> mTransTempFiles = new ArrayList<NameValuePair>();
    private var udpServerThread: UdpServerThread? = null
    private var mMenuObject: MenuCatagoryObject? = null
    private var mCatagoryObject: CatagoryObject? = null
    private var mMenuItemObject: MenuObject? = null
    private var mMenuOptData: MenuOptionData? = null
    private var mMenuReqOptData: CustomRequiredOptionData? = null
    private var mMenuAddOptData: CustomAddedOptionData? = null
    private var mMenuOptLists: CustomOptionData? = null
    private var mAgentCmdReceiver: BroadcastReceiver? = null
    private var taskTimer: Timer? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate() {
        super.onCreate()
        Logger.w("MainService Start!!!!")
        SettingPersister.initPrefs(applicationContext)
        mEnv = SCEnvironment()
        mfileUtils = FileUtils()
        mStbOpt = StbOptionEnv()
        mStbOpt!!.init()
        APP.playerOpt = PlayerOptionEnv()
        APP.playerOpt!!.init()
        Logger.w("APP.playerOpt init - " + APP.playerOpt!!.optionDefaultMenuFile)
        // 시스템으로부터 현재시간(ms) 가져오기
        val now = System.currentTimeMillis()
        val sdfNow = SimpleDateFormat("yyyyMMddHHmmss")
        mPlayerStartNewDt = sdfNow.format(Date(System.currentTimeMillis()))
        lastTimeSyncDt = Date(now)
        mXmlOptUtil = XmlOptionParser()
        //Read the default STB player info start=>
        val dir = FileUtils.makeDirectory(FileUtils.BBMC_DIRECTORY)
        var bbmcDefault = File(FileUtils.BBMC_DEFAULT_DIR)
        if (!bbmcDefault.exists()) { bbmcDefault.mkdir() }
        bbmcDefault = File(FileUtils.BBMC_PAYCAST_DIRECTORY)
        if (!bbmcDefault.exists()) bbmcDefault.mkdir()
        bbmcDefault = File(FileUtils.BBMC_PAYCAST_DATA_DIRECTORY)
        if (!bbmcDefault.exists()) bbmcDefault.mkdir()
        bbmcDefault = File(FileUtils.BBMC_PAYCAST_MENU_DIRECTORY)
        if (!bbmcDefault.exists()) bbmcDefault.mkdir()
        bbmcDefault = File(FileUtils.BBMC_PAYCAST_CONTENT_DIRECTORY)
        if (!bbmcDefault.exists()) bbmcDefault.mkdir()
        bbmcDefault = File(FileUtils.BBMC_PAYCAST_DIRECTORY+"banner"+File.separator)
        if (!bbmcDefault.exists()) bbmcDefault.mkdir()
        Utils.DebugAuto("")
        Utils.DebugAuto("==================================================")
        Utils.DebugAuto("Initializing SignCast Player")

        /*  Agent option    */
        FileUtils.makeAgentOptionFile(
            dir, FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + FileUtils.getFilename(
                FileUtils.PayCastAgent
            ), application, mStbOpt
        )
        OptUtil.ReadOptions(
            FileUtils.PayCastAgent,
            true,
            FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + FileUtils.getFilename(
                FileUtils.PayCastAgent
            ),
            applicationContext
        )
        //mStbOpt = parseAgentOptionXML(FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + mfileUtils.getFilename(mfileUtils.PayCastAgent), mStbOpt);
        mStbOpt = parseAgentOptionXMLV2(FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + FileUtils.getFilename(FileUtils.PayCastAgent), mStbOpt!!)
        try {
            if (mStbOpt!!.stbId < 0) {
                sendBroadCastService("xml load failed")
            }
            SettingPersister.setStbOptionEnv(mStbOpt)
            storeId = kotlin.runCatching { Integer.parseInt(mStbOpt?.storeId ?: "-1") }.getOrDefault(-1)
            deviceId = mStbOpt?.deviceId ?: ""
            Logger.e("storeId = $storeId // deviceId = $deviceId")
            initStoreId.onNext(true)
            Logger.w("0. START setStbOptionEnv - StbId : " + mStbOpt!!.stbId + "// Server Info : " + mStbOpt!!.serverHost + " // Server.FtpHost : " + mStbOpt!!.ftpHost)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        onReadPlayerOption()
        mServrReq = ServerReqUrl()
        //mPropUtil = PropUtil()
        mMenuObject = MenuCatagoryObject()

        /*  Set Current schedule file */
        mStbOpt!!.menuName =
            if (APP.playerOpt != null) APP.playerOpt!!.optionDefaultMenuFile else null
        customUtils.setInitOrderNum(APP.playerOpt!!.optionDefaultOrderNum)
        APP.stbOpt = mStbOpt
        if (mStbOpt!!.menuName == null) {
            Logger.d("menuName is null")
        } else if (mStbOpt!!.menuName.isNotEmpty()) {
            try {
                Logger.d("menuName is not empty!! : " + mStbOpt!!.menuName)
                mMenuObject = parseMenuObject(FileUtils.BBMC_PAYCAST_MENU_DIRECTORY + mStbOpt!!.menuName)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        mMenuObject?.let {
            cmStoreMenuInfo = it
            menuListSubject.onNext(it)
            Logger.d("Menu exist!!")
        } ?: run {
            Logger.w("Update optionDefaultMenuFile " + APP.playerOpt!!.optionDefaultMenuFile)
            APP.playerOpt!!.optionDefaultMenuFile = ""
            mStbOpt!!.menuName = APP.playerOpt!!.optionDefaultMenuFile
            APP.stbOpt = mStbOpt
            val fdir = FileUtils.makeDirectory(FileUtils.BBMC_PAYCAST_DATA_DIRECTORY)
            val menuObj = FileUtils.updatePlayerOptionFile(
                fdir, FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + FileUtils.getFilename(
                    FileUtils.PayCastPlayer
                ), application, APP.playerOpt
            )
            Logger.e("menuObj updatePlayerOptionFile : " + menuObj.name)
        }

        //Store infomation start->
        // seller info는 서버에서 가져오는것으로 변경! 2023.06.13
        //val sellerInfoList = CustomUtils.onUpdateStoreInfo(mStbOpt) as ArrayList<String>
        //APP.setSellerInfo(sellerInfoList)
        //<- Store infomation end
        PropUtil.init(applicationContext)
        if (!FileUtils.checkMandatoryDirectory()) {
            Logger.e("Require check the Directory ")
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(SingCastPlayIntent.ACTION_SERVICE_COMMAND)
        mAgentCmdReceiver = object : BroadcastReceiver() {
            @SuppressLint("SimpleDateFormat")
            @Suppress("NAME_SHADOWING")
            override fun onReceive(context: Context, intent: Intent) {
                Logger.e("mAgentCmdReceiver : - ${intent.extras?.getString("command") ?: "null intent"}")
                if (intent.action == SingCastPlayIntent.ACTION_SERVICE_COMMAND) {
                    intent.extras?.let {
                        val c = PlayerCommand().apply {
                            command = it.getString("command")
                            requestDateTime = it.getString("requestDateTime")
                            executeDateTime = it.getString("executeDateTime")
                        }
                        Logger.d("Command is $c")
//                        c.command = it.getString("command")
//                        c.requestDateTime = it.getString("requestDateTime")
//                        c.executeDateTime = it.getString("executeDateTime")
                        when(c.command) {
                            getString(R.string.str_command_store_info_change) -> storeInfoChanged()
                            getString(R.string.str_command_connect_server) -> {
                                Logger.d("Connect server command!!")
                                taskTimer = Timer()
                                repeatTask()
                            }
                            getString(R.string.paycast_agent_file_update) -> agentFileUpdate()
                            else -> { Logger.d("c.command : ${c.command}")}
                        }
                    }
                }
            }
        }
        registerReceiver(mAgentCmdReceiver, intentFilter)
    }

    private fun storeInfoChanged() {
        val dir = FileUtils.makeDirectory(FileUtils.BBMC_PAYCAST_DIRECTORY)
        FileUtils.makeAgentOptionFile(
            dir, FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + FileUtils.getFilename(FileUtils.PayCastAgent), application, mStbOpt
        )
        OptUtil.ReadOptions(
            FileUtils.PayCastAgent, true, FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + FileUtils.getFilename(FileUtils.PayCastAgent),
            applicationContext
        )
        mStbOpt = parseAgentOptionXML(FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + FileUtils.getFilename(FileUtils.PayCastAgent), mStbOpt)
        try {
            SettingPersister.setStbOptionEnv(mStbOpt)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mStbOpt!!.menuName = APP.playerOpt!!.optionDefaultMenuFile
        APP.stbOpt = mStbOpt
    }

    private fun agentFileUpdate() {
        mStbOpt = parseAgentOptionXML(FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + FileUtils.getFilename(FileUtils.PayCastAgent), mStbOpt)
        try { SettingPersister.setStbOptionEnv(mStbOpt) } catch (e: JSONException) { e.printStackTrace() }
        mStbOpt!!.menuName = APP.playerOpt!!.optionDefaultMenuFile
        APP.stbOpt = mStbOpt
        sendBroadCastService(getString(R.string.kiosk_disenable_command))
    }

    private fun repeatTask() {
        Logger.e("START Repeat Task")
        taskTimer?.let {
            // 패키지 체크 : 0.5분 주기
            it.scheduleAtFixedRate(APP_CHECK_CYCLE, CHECK_PACKAGE_TIMEOUT.toLong()) {
                packageCheckTimerTask()
            }
            // MonitorTask : 0.5분 주기
            it.scheduleAtFixedRate(APP_CHECK_CYCLE, APP_CHECK_CYCLE) {
                monitoringTimerTask()
            }

            // CommandTask : 서버로부터 받은 명령어를 수행하는 태스크 - 추후 사용자가 사용중일경우 막는 코드 필요..(결제중, 등등..)
            it.scheduleAtFixedRate(APP_CHECK_CYCLE + 500, APP_CHECK_CYCLE) {
                CoroutineScope(Dispatchers.IO).launch {
                    commandTimerTask()
                }
            }
        }
    }

    private suspend fun commandTimerTask() {
        val delCommandList: MutableList<CommandObject> = ArrayList()
        if (newcommandList!!.size > 0) {
            var size = newcommandList!!.size
            val i = 0
            while (i < size) {
                commandList.add(newcommandList!![i])
                newcommandList!!.removeAt(i)
                size--
            }
        }
        if (commandList.size > 0) {
            for (command in commandList) {
                Logger.d("In Queue Current COMMAND : ${command.rcCommandid}")
                val result = executeCommand(command)
                Logger.d("Result : $result")
                if (result.isEmpty()) {
                    delCommandList.add(command)
                    continue
                }
                val reqUrl = ServerReqUrl.getServerRcCommandUrl(mStbOpt, applicationContext)
                command.result = result
                val url: URL? = try {
                    URL(reqUrl + "?" + "rcCmdId=" + command.rcCommandid + "&result=" + command.result)
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                    continue
                }
                Logger.e("URL : $url")
                var conn: HttpURLConnection? = null // URL을 연결한 객체 생성.
                conn = try {
                    withContext(Dispatchers.IO) {
                        url?.openConnection() as HttpURLConnection
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    conn!!.disconnect()
                    continue
                }
                try {
                    conn!!.requestMethod = "GET" // get방식 통신
                } catch (e: ProtocolException) {
                    e.printStackTrace()
                    conn!!.disconnect()
                    continue
                }
                conn.doInput = true // 읽기모드 지정
                conn.connectTimeout = MAX_CONNECTION_TIMEOUT // 통신 타임아웃
                conn.readTimeout = MAX_SOCKET_TIMEOUT
                Logger.e("CommandAsynTask() 3")
                Logger.d("명령실행보고  url=" + String.format("%s", url.toString()))
                Logger.d("CommandAsynTask rcCmdId=" + command.rcCommandid + " result=" + command.result)
                val resCode: Int = try {
                    conn.responseCode
                } catch (e: IOException) {
                    e.printStackTrace()
                    conn.disconnect()
                    continue
                }
                if (resCode == HttpURLConnection.HTTP_OK || resCode == HttpURLConnection.HTTP_CREATED) {
                    val `is`: InputStream? = try {
                        conn.inputStream
                    } catch (e: IOException) {
                        e.printStackTrace()
                        conn.disconnect()
                        continue
                    } //input스트림 개방
                    val builder = StringBuilder() //문자열을 담기 위한 객체
                    val reader: BufferedReader? = try {
                        withContext(Dispatchers.IO) {
                            BufferedReader(InputStreamReader(`is`, "UTF-8"))
                        }
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                        conn.disconnect()
                        continue
                    } //문자열 셋 세팅
                    var line: String?
                    while (true) {
                        try {
                            if (reader?.readLine().also { line = it } == null) break
                        } catch (e: IOException) {
                            e.printStackTrace()
                            conn.disconnect()
                            continue
                        }
                        builder.append("$line\n")
                    }
                    Logger.e("CommandAsynTask() 8")
                    Logger.d("실행보고 response=$builder")
                    delCommandList.add(command)
                }
            }
        }
        for (command in delCommandList) {
            commandList.remove(command)
        }
    }

    private fun packageCheckTimerTask() {
        if (!runningActivityList) {
            val errLog = "MainService packageCheckTimerTask() startPlayer()\n"
            FileUtils.writeDebug(errLog, "PayCast")
            startPlayer()
        }
    }

    private fun monitoringTimerTask(): Boolean {
        //Logger.w("monitoringTimerTask start! - network on : $networkStatus")
        if (!networkStatus) return false
        val reqUrl = ServerReqUrl.getServerChgInfoUrl(APP.stbOpt, applicationContext)
        val infoStr = String.format("?storeId=%s&deviceId=%s", APP.stbOpt!!.storeId, APP.stbOpt!!.deviceId)
        val tempString = reqUrl + infoStr
        val res = NetworkUtil.onGetStoreChgSync(tempString)
        //Logger.e("@@Req Url : $tempString // Res - $res")
        if(res.length > 63) Logger.d("onGetOrderPrintStringFrServer() 1 RES=$res")
        return if (res.isNotEmpty()) {
            ParseXML(res); true
        } else {
            false
        }
    }

    override fun onDestroy() {
        if (mAgentCmdReceiver != null) unregisterReceiver(mAgentCmdReceiver)
        taskTimer?.cancel()
        super.onDestroy()
    }

    private fun startTimerService() {
        /*  AGENT LOG Delete */
        FileUtils.removeFile(FileUtils.BBMC_LOG_DIR + "*.log")

        /*  DEBUG Delete */
        FileUtils.removeFile(FileUtils.BBMC_DEBUG_DIR + "*.log")

        /*  Report jpg Delete */FileUtils.removeFile(FileUtils.BBMC_REPORT_DIR + "*.jpg")
        mStartTimer = Timer("mStartTimer")
        mStartTimer!!.scheduleAtFixedRate(StartTimerTask(), 0, 1000)

//        onSaveScreenShot("0502_test");
        udpServerThread =
            UdpServerThread(applicationContext, APP.playerOpt!!.optionStbUdpPort, application)
        udpServerThread!!.start()
        //        rm = new RecvMessage();
//        rm.start();
        onCheckMemory()
        FileUtils.deletePaymentResult()
    }

    private inner class StartTimerTask : TimerTask() {
        var cancelFlag = false
        override fun cancel(): Boolean {
            cancelFlag = true
            return super.cancel()
        }

        override fun run() {
            Logger.w("start timer task!!")
            if (cancelFlag) return
            if (!networkStatus) return
            FileUtils.writeDebug("MainService startTimerTask() startPlayer() state=${cmPlayerStatus}\n", "PayCast")
            if (cmPlayerStatus != SingCastPlayIntent.PLAYER_SHOWING) {
                startPlayer()
                Logger.w("START player != SingCastPlayIntent.PLAYER_SHOWING ")
            }
            mStartTimer!!.cancel()
            Logger.d("START() mStbOpt.monitorMins=" + mStbOpt!!.monitorMins)
            executeSyncContent()
        }
    }

    private fun onReadPlayerOption() {
        val dir = FileUtils.makeDirectory(FileUtils.BBMC_PAYCAST_DATA_DIRECTORY)

        //Player option data
        FileUtils.makePlayerOptionFile(
            dir, FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + FileUtils.getFilename(
                FileUtils.PayCastPlayer
            ), application, APP.playerOpt
        )
        //(int optType, boolean doInitOption, String pathFilename, Context c)
        OptUtil.ReadOptions(
            FileUtils.PayCastPlayer,
            true,
            FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + FileUtils.getFilename(
                FileUtils.PayCastPlayer
            ),
            applicationContext
        )
        //APP.playerOpt = mXmlOptUtil.parsePlayerOptionXML(FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + FileUtils.getFilename(FileUtils.PayCastPlayer), APP.playerOpt, getApplicationContext());
        try {
            parsePlayerOptionXMLV2(
                FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + FileUtils.getFilename(
                    FileUtils.PayCastPlayer
                )
            )
            //SettingPersister.setPlayerOptionEnv(APP.playerOpt);
            Logger.w("App.playOpt = " + APP.playerOpt)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun parseAgentOptionXML(fileName: String, stbOption: StbOptionEnv?): StbOptionEnv? {
        try {
            val fXmlFile = File(fileName)
            Logger.d("parseAgentOptionXML fileName=$fileName")
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true
            val dBuilder = factory.newDocumentBuilder()
            val doc = dBuilder.parse(fXmlFile)
            doc.documentElement.normalize()
            val nList = doc.getElementsByTagName("Option")
            for (temp in 0 until nList.length) {
                val nNode = nList.item(temp)
                Logger.d("Current Element :" + nNode.nodeName)
                if (nNode.nodeType == Node.ELEMENT_NODE) {
                    val eElement = nNode as Element
                    stbOption!!.ftpActiveMode = java.lang.Boolean.parseBoolean(
                        eElement.getElementsByTagName(
                            resources.getString(R.string.ftpActiveMode)
                        ).item(0).textContent
                    )
                    Logger.w(
                        "ftpActiveMode : " + eElement.getElementsByTagName(
                            resources.getString(R.string.ftpActiveMode)
                        ).item(0).textContent
                    )
                    stbOption.monitorMins =
                        eElement.getElementsByTagName(resources.getString(R.string.monitorMins))
                            .item(0).textContent.toInt()
                    stbOption.playerStart = java.lang.Boolean.parseBoolean(
                        eElement.getElementsByTagName(
                            resources.getString(R.string.playerStart)
                        ).item(0).textContent
                    )
                    stbOption.ftpHost =
                        eElement.getElementsByTagName(resources.getString(R.string.serverftpHost))
                            .item(0).textContent
                    stbOption.ftpPassword =
                        eElement.getElementsByTagName(resources.getString(R.string.serverftpPassword))
                            .item(0).textContent
                    stbOption.ftpPort =
                        eElement.getElementsByTagName(resources.getString(R.string.serverftpPort))
                            .item(0).textContent.toInt()
                    stbOption.ftpUser =
                        eElement.getElementsByTagName(resources.getString(R.string.serverftpUser))
                            .item(0).textContent
                    stbOption.serverHost =
                        eElement.getElementsByTagName(resources.getString(R.string.serverserverHost))
                            .item(0).textContent
                    if (stbOption.serverHost == null || stbOption.serverHost.isEmpty()) stbOption.serverHost =
                        "m.paycast.co.kr"
                    stbOption.serverPort =
                        eElement.getElementsByTagName(resources.getString(R.string.serverserverPort))
                            .item(0).textContent.toInt()
                    if (stbOption.serverPort == 0) stbOption.serverPort = 80
                    stbOption.serverUkid =
                        eElement.getElementsByTagName(resources.getString(R.string.serverserverUkid))
                            .item(0).textContent
                    Logger.e("StbId : " + eElement.getElementsByTagName(resources.getString(R.string.serverstbId)).item(0).textContent)
                    stbOption.stbId = eElement.getElementsByTagName(resources.getString(R.string.serverstbId)).item(0).textContent.toInt()
                    stbOption.stbName = eElement.getElementsByTagName(resources.getString(R.string.serverstbName)).item(0).textContent
                    stbOption.stbServiceType = eElement.getElementsByTagName(resources.getString(R.string.serverstbServiceType)).item(0).textContent
                    stbOption.stbUdpPort = eElement.getElementsByTagName(resources.getString(R.string.serverstbUdpPort)).item(0).textContent.toInt()
                    stbOption.storeName = eElement.getElementsByTagName(resources.getString(R.string.server_store_name)).item(0).textContent
                    stbOption.storeAddr = eElement.getElementsByTagName(resources.getString(R.string.server_store_addr)).item(0).textContent
                    stbOption.storeBusinessNum = eElement.getElementsByTagName(resources.getString(R.string.server_business_num)).item(0).textContent
                    stbOption.storeTel = eElement.getElementsByTagName(resources.getString(R.string.server_store_tel)).item(0).textContent
                    stbOption.storeMerchantNum = eElement.getElementsByTagName(resources.getString(R.string.server_merchant_num)).item(0).textContent
                    stbOption.storeCatId = eElement.getElementsByTagName(resources.getString(R.string.server_store_catid)).item(0).textContent
                    stbOption.storeRepresent = eElement.getElementsByTagName(resources.getString(R.string.server_represent)).item(0).textContent
                    stbOption.storeId = eElement.getElementsByTagName(resources.getString(R.string.server_store_id)).item(0).textContent
                    stbOption.deviceId = eElement.getElementsByTagName(resources.getString(R.string.server_device_id)).item(0).textContent
                    stbOption.mainPrtEnable = eElement.getElementsByTagName(resources.getString(R.string.main_print_enable)).item(0).textContent
                    stbOption.mainPrtip = eElement.getElementsByTagName(resources.getString(R.string.main_print_ip)).item(0).textContent
                    stbOption.koEnable = eElement.getElementsByTagName(resources.getString(R.string.ko_enabled)).item(0).textContent
                    stbOption.atEnable = eElement.getElementsByTagName(resources.getString(R.string.at_enabled)).item(0).textContent
                    stbOption.openType = eElement.getElementsByTagName(resources.getString(R.string.openType)).item(0).textContent
                }
            }
        } catch (e: Exception) {
            Logger.d("3. Error in ParseXML() : " + e.message)
            return stbOption
        }
        Logger.w("Current stbOption : stbOption.ftpActiveMode = ${stbOption!!.ftpActiveMode}")
        Logger.w("monitorMins = ${stbOption.monitorMins}")
        Logger.w("ftpUser = ${stbOption.ftpUser}")
        Logger.w("stbId = ${stbOption.stbId}")
        Logger.w(" storeId = ${stbOption.storeId}")
        Logger.w("deviceId = ${stbOption.deviceId}")
        return stbOption
    }

    override fun onBind(intent: Intent): IBinder? {
        mMessenger = Messenger(ServiceHandler())
        return mMessenger.binder
    }

    @SuppressLint("HandlerLeak", "SimpleDateFormat")
    internal inner class ServiceHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            Logger.w("ServiceHandler msg : $msg")
            when (msg.what) {
                CONNECT -> {
                    val `val` = msg.arg1
                    mActivityMessenger = msg.replyTo
                    if (`val` != SingCastPlayIntent.PLAYER_SHOWING) {
                        Logger.w("startTimerService!!!")
                        startTimerService()
                    }
                }
                DISCONNECT -> mActivityMessenger = null
                QUIT_MESSAGE -> {
                    stopSelf()
                    Process.killProcess(Process.myPid())
                }
                SEND_VALUE -> {
                    val `val` = msg.arg1
                    val val2 = msg.arg2
                    if (`val` == SingCastPlayIntent.PLAYER_READY && val2 == 0) {
                        if (msg.replyTo != null) mActivityMessenger = msg.replyTo
                    } else if (`val` == SingCastPlayIntent.PLAYER_READY && val2 == 1) {
                        val replayMsg =
                            Message.obtain(null, SEND_VALUE, SingCastPlayIntent.AGENT_RESTART_OK, 0)
                        replayMsg.replyTo = mMessenger
                        try {
                            mActivityMessenger!!.send(replayMsg)
                        } catch (e: RemoteException) {
                            e.printStackTrace()
                        }
                    } else if (`val` == SingCastPlayIntent.SERVICE_DESTROY) {
                        stopSelf()
                        if (LOG) Log.e(ContentValues.TAG, "SingCastPlayIntent.SERVICE_DESTROY..")
                    } else if (`val` == SingCastPlayIntent.PLAYER_SHOWING) {
                        if (msg.replyTo != null) mActivityMessenger = msg.replyTo
                        if (LOG) Log.e(ContentValues.TAG, "SingCastPlayIntent.PLAYER_SHOWING..")
                    }
                }
                SELF_SEND_VALUE -> {
                    val arg1 = msg.arg1
                    if (arg1 == SingCastPlayIntent.AGENT_RESTART) {
                        val options = HashMap<String, String>()
                        val date = Utils.getCurrentDate()
                        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
                        val s = sdf.format(date)
                        options["executeTime"] = s
                        val replayMsg = Message.obtain(
                            null,
                            SEND_VALUE,
                            SingCastPlayIntent.AGENT_RESTART,
                            0,
                            options
                        )
                        replayMsg.replyTo = mMessenger
                        try {
                            mActivityMessenger!!.send(replayMsg)
                        } catch (e: RemoteException) {
                            e.printStackTrace()
                        }
                        val serviceRestartTimer = Timer("serviceRestartTimer")
                        serviceRestartTimer.schedule(serviceRestartTask(), 500)
                    }
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private inner class serviceRestartTask : TimerTask() {
        override fun run() {
            cancel()
            stopSelf()
        }
    }

    // jason:serverssl: 서버 https 프로토콜 옵션(2017/10/12)
    private val serverSyncContentUrl: String
        //-
        //        String encodedQueryString = "storeId=" + mStbOpt.storeId + "&catId=" + mStbOpt.storeCatId;
        get() {
            var serverUrl: String
            serverUrl = if (mStbOpt!!.serverPort == 80) {
                String.format("http://" + mStbOpt!!.serverHost + "/info/menuInfo")
            } else {
                String.format("http://" + mStbOpt!!.serverHost + ":" + mStbOpt!!.serverPort + "/info/menuInfo")
            }
            // jason:serverssl: 서버 https 프로토콜 옵션(2017/10/12)
            val ssl = PropUtil.configValue(getString(R.string.serverSSLEnabled), application)
            if (LOG) Log.d(ContentValues.TAG, "startTimerTask() ssl=$ssl")
            if (java.lang.Boolean.parseBoolean(ssl)) {
                serverUrl = serverUrl.replace("http://", "https://")
            }
            //-
            Logger.w("Current StoreId=%s & deviceId=%s", mStbOpt!!.stbId, mStbOpt!!.deviceId)
            //        String encodedQueryString = "storeId=" + mStbOpt.storeId + "&catId=" + mStbOpt.storeCatId;
            val encodedQueryString =
                String.format("storeId=%s&deviceId=%s", mStbOpt!!.storeId, mStbOpt!!.deviceId)
            Logger.w("mStbOpt.storeId : " + mStbOpt!!.storeId)
            return String.format("%s?%s", serverUrl, encodedQueryString)
        }

    @SuppressLint("SuspiciousIndentation")
    fun ParseXML(fls: String?) {
//        Logger.w("Parse Xml : " + fls);
        var cmd: CommandObject? = null
        var addCmd = false
        if (fls == null || fls.isEmpty()) return
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            val `is`: InputStream = ByteArrayInputStream(fls.toByteArray())
            parser.setInput(`is`, null)
            var eventType = parser.eventType
            var downloadInfo: DownFileInfo?
            val tempStbOpt = StbOptionEnv()
            copyStbOption(APP.stbOpt, tempStbOpt)
            var parErr = false
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val name = parser.name
                        if (name == resources.getString(R.string.server)) {
                            var i = 0
                            while (i < parser.attributeCount) {
                                val attrName = parser.getAttributeName(i)
                                //Log.d(TAG, "parser.getAttributeName[" + i + "]" + parser.getAttributeName(i));
                                if (attrName == resources.getString(R.string.ftpActiveMode)) {
                                    tempStbOpt.ftpActiveMode =
                                        java.lang.Boolean.parseBoolean(parser.getAttributeValue(i))
                                    //Log.d(TAG, "FtpActiveMode:" + mStbOpt.ftpActiveMode);
                                } else if (attrName == resources.getString(R.string.monitorMins)) {
                                    val monitorMin = Integer.valueOf(parser.getAttributeValue(i))
                                    if (monitorMin > 0 && monitorMin <= 3) tempStbOpt.monitorMins =
                                        monitorMin else parErr = true
                                    //Log.d(TAG, "MonitorMins:" + mStbOpt.monitorMins);
                                } else if (attrName == resources.getString(R.string.playerStart)) {
                                    tempStbOpt.playerStart =
                                        java.lang.Boolean.parseBoolean(parser.getAttributeValue(i))
                                    //Log.d(TAG, "PlayerStart:" + mStbOpt.playerStart);
                                } else if (attrName == resources.getString(R.string.ftpHost)) {
                                    val ftphost = parser.getAttributeValue(i)
                                    if (ftphost != null && ftphost.isNotEmpty()) tempStbOpt.ftpHost =
                                        ftphost else parErr = true
                                    //Log.d(TAG, "Server.FtpHost:" + mStbOpt.ftpHost);
                                } else if (attrName == resources.getString(R.string.ftpPassword)) {
                                    val ftppw = parser.getAttributeValue(i)
                                    if (ftppw != null && ftppw.isNotEmpty()) tempStbOpt.ftpPassword =
                                        ftppw else parErr = true
                                    //Log.d(TAG, "Server.FtpPassword:" + mStbOpt.ftpPassword);
                                } else if (attrName == resources.getString(R.string.ftpPort)) {
                                    val ftpport = Integer.valueOf(parser.getAttributeValue(i))
                                    if (ftpport > 0) tempStbOpt.ftpPort = ftpport else parErr = true
                                    //Log.d(TAG, "Server.FtpPort:" + mStbOpt.ftpPort);
                                } else if (attrName == resources.getString(R.string.ftpUser)) {
                                    val ftpUser = parser.getAttributeValue(i)
                                    if (ftpUser != null && ftpUser.isNotEmpty()) tempStbOpt.ftpUser =
                                        ftpUser else parErr = true
                                    //Log.d(TAG, "Server.FtpUser:" + mStbOpt.ftpUser);
                                } else if (attrName == resources.getString(R.string.serverHost)) {
                                    val serverHost = parser.getAttributeValue(i)
                                    if (serverHost != null && serverHost.isNotEmpty()) tempStbOpt.serverHost =
                                        serverHost else parErr = true
                                    //Log.d(TAG, "Server.ServerHost:" + mStbOpt.serverHost);
                                } else if (attrName == resources.getString(R.string.serverPort)) {
                                    val serverPort = Integer.valueOf(parser.getAttributeValue(i))
                                    if (serverPort > 0) tempStbOpt.serverPort =
                                        serverPort else parErr = true
                                    //Log.d(TAG, "Server.ServerPort:" + mStbOpt.serverPort);
                                } else if (attrName == resources.getString(R.string.serverUkid)) {
                                    val serverUkid = parser.getAttributeValue(i)
                                    if (serverUkid != null && serverUkid.isNotEmpty()) tempStbOpt.serverUkid =
                                        serverUkid else parErr = true
                                    //Log.d(TAG, "Server.ServerUkid:" + mStbOpt.serverUkid);
                                } else if (attrName == resources.getString(R.string.stbId)) {
                                    val stbId = Integer.valueOf(parser.getAttributeValue(i))
                                    if (stbId > 0) tempStbOpt.stbId = stbId
                                    if (tempStbOpt.stbId > 0) {
                                        if (tempStbOpt.stbStatus == 0) tempStbOpt.stbStatus = 5
                                    } else parErr = true

                                    //Log.d(TAG, "Server.StbId:" + mStbOpt.stbId);
                                } else if (attrName == resources.getString(R.string.stbName)) {
                                    val stbName = parser.getAttributeValue(i)
                                    if (stbName != null && stbName.isNotEmpty()) tempStbOpt.stbName =
                                        stbName else parErr = true
                                    //Log.d(TAG, "Server.StbName:" + mStbOpt.stbName);
                                } else if (attrName == resources.getString(R.string.stbServiceType)) {
                                    val stbServiceType = parser.getAttributeValue(i)
                                    if (stbServiceType != null && !stbServiceType.isEmpty()) tempStbOpt.stbServiceType =
                                        stbServiceType else parErr = true
                                    //Log.d(TAG, "Server.StbServiceType:" + mStbOpt.stbServiceType);
                                } else if (attrName == resources.getString(R.string.stbUdpPort)) {
                                    val stbUdpPort = Integer.valueOf(parser.getAttributeValue(i))
                                    if (stbUdpPort > 0) tempStbOpt.stbUdpPort =
                                        stbUdpPort else parErr = true
                                    //Log.d(TAG, "Server.StbUdpPort:" + mStbOpt.stbUdpPort);
                                } else if (attrName == resources.getString(R.string.store_name)) {
                                    val storeName = parser.getAttributeValue(i)
                                    if (storeName != null && storeName.isNotEmpty()) tempStbOpt.storeName =
                                        storeName
                                } else if (attrName == resources.getString(R.string.store_addr)) {
                                    val storeAddr = parser.getAttributeValue(i)
                                    if (storeAddr != null && storeAddr.isNotEmpty()) {
                                        tempStbOpt.storeAddr =
                                            storeAddr
                                    }
                                } else if (attrName == resources.getString(R.string.business_num)) {
                                    val storeBusinessNum = parser.getAttributeValue(i)
                                    if (storeBusinessNum != null && storeBusinessNum.isNotEmpty()) tempStbOpt.storeBusinessNum =
                                        storeBusinessNum
                                } else if (attrName == resources.getString(R.string.store_tel)) {
                                    val storeTel = parser.getAttributeValue(i)
                                    if (storeTel != null && storeTel.isNotEmpty()) tempStbOpt.storeTel =
                                        storeTel
                                } else if (attrName == resources.getString(R.string.merchant_num)) {
                                    val storeMerchantNum = parser.getAttributeValue(i)
                                    if (storeMerchantNum != null && storeMerchantNum.isNotEmpty()) tempStbOpt.storeMerchantNum =
                                        storeMerchantNum
                                } else if (attrName == resources.getString(R.string.store_catid)) {
                                    val storeCatId = parser.getAttributeValue(i)
                                    if (storeCatId != null && storeCatId.isNotEmpty()) tempStbOpt.storeCatId =
                                        storeCatId
                                } else if (attrName == resources.getString(R.string.represent)) {
                                    val storeRepresent = parser.getAttributeValue(i)
                                    if (storeRepresent != null && storeRepresent.isNotEmpty()) tempStbOpt.storeRepresent =
                                        storeRepresent
                                    Log.d(
                                        ContentValues.TAG,
                                        "Server.storeRepresent:" + tempStbOpt.storeRepresent
                                    )
                                } else if (attrName == resources.getString(R.string.store_id)) {
                                    val storeId = parser.getAttributeValue(i)
                                    if (storeId != null && storeId.isNotEmpty()) tempStbOpt.storeId =
                                        storeId else parErr = true
                                    Log.d(ContentValues.TAG, "Server.storeId:" + tempStbOpt.storeId)
                                } else if (attrName == resources.getString(R.string.store_operating_time)) {
                                    val operatingTime = parser.getAttributeValue(i)
                                    if (operatingTime != null && operatingTime.isNotEmpty()) tempStbOpt.operatingTime =
                                        operatingTime
                                } else if (attrName == resources.getString(R.string.store_introduction_msg)) {
                                    val introMsg = parser.getAttributeValue(i)
                                    if (introMsg != null && !introMsg.isEmpty()) tempStbOpt.introMsg =
                                        introMsg
                                } else if (attrName == resources.getString(R.string.server_device_id)) {
                                    val deviceId = parser.getAttributeValue(i)
                                    if (deviceId != null && !deviceId.isEmpty()) tempStbOpt.deviceId =
                                        deviceId else parErr = true
                                } else if (attrName == resources.getString(R.string.main_print_enable)) {
                                    val mainPrtEnable = parser.getAttributeValue(i)
                                    if (mainPrtEnable != null && !mainPrtEnable.isEmpty()) tempStbOpt.mainPrtEnable =
                                        mainPrtEnable else parErr = true
                                } else if (attrName == resources.getString(R.string.main_print_ip)) {
                                    val mainPrtip = parser.getAttributeValue(i)
                                    if (mainPrtip != null && !mainPrtip.isEmpty()) tempStbOpt.mainPrtip = mainPrtip else parErr = true
                                } else if (attrName == resources.getString(R.string.ko_enabled)) {
                                    val enable = parser.getAttributeValue(i)
                                    if (enable != null && !enable.isEmpty()) tempStbOpt.koEnable = enable
                                } else if (attrName.equals("atEnabled", ignoreCase = true)) {
                                    val enable = parser.getAttributeValue(i)
                                    if (enable != null && !enable.isEmpty()) tempStbOpt.atEnable = enable
                                } else if (attrName.equals("openType", ignoreCase = true)) {
                                    val enable = parser.getAttributeValue(i)
                                    if (enable != null && !enable.isEmpty()) tempStbOpt.openType = enable
                                }
                                if (parErr) {
                                    Log.e(ContentValues.TAG, "attrName=" + attrName + " value=" + parser.getAttributeValue(i))
                                }
                                i++
                            }
                            if (!parErr) {
                                mStbOpt = tempStbOpt
                                FileUtils.updateFile(FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + FileUtils.AGENT_OPT_FILE, application, mStbOpt)
                            }
                        }
                        if (name == "command") {
//                            String ref = parser.getAttributeValue(null, "ref");
                            if (LOG) {
                                Log.d(ContentValues.TAG, "count:" + parser.attributeCount)
                                Log.d(ContentValues.TAG, "Text:" + parser.text)
                            }
                            if (parser.attributeCount == 0) {
                                if (mStbOpt!!.stbStatus == 2) isAdditionalReportRequired = false
                                cmd = null
                            } else cmd = CommandObject()
                            var i = 0
                            while (i < parser.attributeCount) {
                                val attrName = parser.getAttributeName(i)
                                //Log.d(TAG, "parser.getAttributeName[" + i + "]" + parser.getAttributeName(i));
                                if (attrName == "rcCommandId") {
                                    cmd!!.rcCommandid = parser.getAttributeValue(i)
                                } else if (attrName == "command") {
                                    cmd!!.command = parser.getAttributeValue(i)
                                } else if (attrName == "execTime") {
                                    cmd!!.execTime = parser.getAttributeValue(i)
                                } else if (attrName == "koEnabled") {
                                    cmd!!.koEnabled = parser.getAttributeValue(i)
                                } else if (attrName == "atEnabled") {
                                    cmd!!.atEnabled = parser.getAttributeValue(i)
                                } else if (attrName == "CDATA") {
                                    cmd!!.execTime = parser.getAttributeValue(i)
                                } else if (attrName == "openType") {
                                    cmd!!.openType = parser.getAttributeValue(i)
                                }
                                i++
                            }
                            if (commandList.size == 0 && cmd!!.rcCommandid != null && cmd.command.isNotEmpty()) addCmd =
                                true
                            for (c in commandList) {
                                if (cmd!!.rcCommandid != null && cmd.command.isNotEmpty()) {
                                    if (c.rcCommandid == cmd.rcCommandid) {
                                        addCmd = false
                                        break
                                    } else addCmd = true
                                } else {
                                    addCmd = false
                                    break
                                }
                            }
                            if (cmd == null) {
                                addCmd = false
                            }
                        }
                        if (name == "![CDATA") {
                            if (LOG) Log.d(ContentValues.TAG, "![CDATA!!  = $name")
                        }
                        if (name == resources.getString(R.string.item)) {
                            downloadInfo = DownFileInfo()
                            var i = 0
                            while (i < parser.attributeCount) {
                                val attrName = parser.getAttributeName(i)
                                //Log.d(TAG, "parser.getAttributeName[" + i + "]" + parser.getAttributeName(i));
                                if (attrName == resources.getString(R.string.foldername)) {
                                    downloadInfo.folderName = parser.getAttributeValue(i)
                                    //                                    downloadInfo.folderName = downloadInfo.folderName.replace("Schedule", "Menu");
//                                    if (LOG)
//                                        Log.d(TAG, "folderName:" + downloadInfo.folderName);
                                } else if (attrName == resources.getString(R.string.filename)) {
                                    downloadInfo.fileName = parser.getAttributeValue(i)
                                    if (LOG) Log.d(
                                        ContentValues.TAG,
                                        "fileName:" + downloadInfo.fileName
                                    )
                                } else if (attrName == resources.getString(R.string.filelength)) {
                                    downloadInfo.fileLength = parser.getAttributeValue(i).toLong()
                                    //                                    if (LOG)
//                                        Log.d(TAG, "filelength:" + downloadInfo.fileLength);
                                } else if (attrName == resources.getString(R.string.stbfileid)) {
                                    downloadInfo.stbfileid =
                                        Integer.valueOf(parser.getAttributeValue(i))
                                    if (downloadInfo.stbfileid == -1) {
                                        downloadInfo.scheduleContent = false
                                    } else {
                                        downloadInfo.scheduleContent = true
                                        downloadInfo.downFileId = downloadInfo.stbfileid
                                    }
                                } else if (attrName == resources.getString(R.string.kfileid)) {
                                    downloadInfo.kfileid = Integer.valueOf(parser.getAttributeValue(i))
                                    if (downloadInfo.stbfileid == -1) downloadInfo.downFileId = downloadInfo.kfileid
                                } else if (attrName == resources.getString(R.string.kroot)) {
                                    downloadInfo.kroot = parser.getAttributeValue(i)
                                } else if (attrName == resources.getString(R.string.playatonce)) {
                                    downloadInfo.playatonce = parser.getAttributeValue(i)
                                }
                                i++
                            }
                            var addDown = true
                            for (d in downloadList) {
                                if (d.fileName == downloadInfo.fileName && d.folderName == downloadInfo.folderName) {
                                    addDown = false
                                    break
                                }
                            }
                            if (addDown) {
                                if (downloadInfo.fileName != null && downloadInfo.fileLength > 0) {
                                    //Logger.e("Add Download File : " + downloadInfo.fileName);
                                    downloadList.add(downloadInfo)
                                }
                            }
                        }
                        checkContentFileExistence()
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text
                        if (LOG) Log.d(ContentValues.TAG, "TEXT = $text")
                        if (text != null && text.isNotEmpty()) {
                            if (cmd != null) cmd.prameter = text
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val name = parser.name
                        //                        if (LOG)
//                            Log.d(TAG, "END_TAG.name=" + name + "");
                        if (name == "command") {
                            if (cmd != null) {
                                if (addCmd) {
                                    if (newcommandList == null) newcommandList = ArrayList()
                                    var newAddcmd = true
                                    if (newcommandList!!.size > 0) {
                                        var c = 0
                                        while (c < newcommandList!!.size) {
                                            val cmdObj = newcommandList!![c]
                                            if (cmdObj.command.equals(
                                                    cmd.command,
                                                    ignoreCase = true
                                                )
                                            ) {
                                                if (cmdObj.rcCommandid.equals(
                                                        cmd.rcCommandid,
                                                        ignoreCase = true
                                                    )
                                                ) {
                                                    newAddcmd = false
                                                    break
                                                }
                                            }
                                            c++
                                        }
                                    }
                                    if (newAddcmd) {
                                        newcommandList!!.add(cmd)
                                        Utils.LOG("--------------------")
                                        Utils.LOG(getString(R.string.Log_CmdAdditionalCommand) + " #: " + cmd.rcCommandid)
                                        Utils.LOG(getString(R.string.Log_CmdCommandName) + ": " + cmd.command)
                                        Utils.LOG(getString(R.string.Log_CmdExecTime) + ": " + cmd.execTime)
                                        Utils.LOG(getString(R.string.Log_CmdParameter) + ": " + cmd.prameter)
                                        Utils.LOG(getString(R.string.Log_CmdTotalCommandCount) + ": " + newcommandList!!.size)
                                        Utils.LOG("--------------------")
                                    }
                                }
                            }
                        }
                    }
                    else -> if (LOG) Log.d(ContentValues.TAG, "EVENT TYPE = $eventType")
                }
                //                Log.d(TAG, "parser.NEXT="+parser.nextToken());
                eventType = parser.next()
            }
        } catch (e: Exception) {
            if (LOG) Log.d(ContentValues.TAG, "2 Error in ParseXML()", e)
        }
    }

    // jason:reporttowrongserver: 잘못된 정보의 서버 보고 시의 오류 개선(2015/03/03)
    private fun checkContentFileExistence(queueAddIncluded: Boolean = false): Boolean {
        var fInfo: File?
        val appRootFolder = FileUtils.BBMC_PAYCAST_DIRECTORY
        var completeY = "|"
        var completeN = "|"
        var completeKY = "|"
        var completeKN = "|"

        //Logger.w("File content Existence : " + queueAddIncluded);
        val tempList: MutableList<DownFileInfo> = ArrayList()
        for (dfi in downloadList) {
            if (dfi.scheduleContent) {
                fInfo = File(appRootFolder + dfi.LocalFolderName() + dfi.fileName)
                if (!fInfo.exists() || fInfo.length() != dfi.fileLength) {
                    dfi.completed = false
                    completeN += dfi.downFileId.toString() + "|"
                    if (queueAddIncluded) {
                        tempList.add(
                            DownFileInfo(
                                dfi.folderName,
                                dfi.fileName,
                                dfi.fileLength,
                                dfi.downFileId
                            )
                        )
                    }
                    continue
                }
                completeY += dfi.downFileId.toString() + "|"
            } else {
                fInfo = File(dfi.LocalFolderName().replace("/", File.separator) + dfi.fileName)
                if (!fInfo.exists() || fInfo.length() != dfi.fileLength) {
                    dfi.completed = false
                    completeKN += dfi.downFileId.toString() + "|"
                    if (queueAddIncluded) {
                        tempList.add(
                            DownFileInfo(
                                dfi.rootKContent,
                                dfi.folderName,
                                dfi.fileName,
                                dfi.fileLength,
                                dfi.downFileId
                            )
                        )
                    }
                    continue
                }
                completeKY += dfi.downFileId.toString() + "|"
            }
        }
        if (tempList.size > 0) downloadList.addAll(tempList)
        val url = ServerReqUrl.getServerSyncContentReportUrl(mStbOpt, applicationContext)
        // val res = NetworkUtil.HttpResponseString(url, temp, mPropUtil)
        //        if (LOG)
//            Log.d(TAG, "HttpResponseString() RES=" + res);
        var result = true
//        if (!res.isEmpty()) {
//            if (res == "Y") result = true
//        }
        return result
    }

    // jason:restartifnewer: 새로운 상시 스케줄 유무 체크(2015/08/31)
    private fun executeRestartPlayerIfNewer(): String {
        val playerStatusCode = getPlayerAgentCheck(false) //String.valueOf(mStbOpt.stbStatus);
        var playerControlMode = true
        if (playerStatusCode.isEmpty() || playerStatusCode != "3" || playerStatusCode == "5" || playerStatusCode == "6") {
            playerControlMode = false
        }
        var ret = "N"
        Utils.LOG(getString(R.string.Log_CheckNewerSchedule))

        if (playerControlMode) {
            val tmp = communicateWithPlayer("RestartPlayerIfNewer", null)
            ret = if (tmp == "Y") "Y" else "N"
        }
        return ret
    }

    //-
    private fun getPlayerAgentCheck(isScheduleIncluded: Boolean): String {
        return if (isScheduleIncluded) {
            communicateWithPlayer("AgentChk", "4|?").replace(".scd", "")
        } else {
            communicateWithPlayer("Chk", null)
        }
    }

    private fun communicateWithPlayer(command: String, initValue: String?): String {
        return executeReqCommand(command)
    }

    @SuppressLint("SimpleDateFormat")
    private fun executeReqCommand(reqCommand: String?): String {
        var retStr = ""
        if (reqCommand != null && reqCommand.isNotEmpty()) {
            // jason:logdebuginfo3: 플레이어 디버깅 로그 옵션(2014/03/17)
            Utils.DebugAuto("")
            Utils.DebugAuto("--------------------------------------------------")
            Utils.DebugAuto("Listen from Mgr: $reqCommand")
            //-
            if (reqCommand == "MN" || reqCommand == "MF" || reqCommand == "PF" || reqCommand == "RB" || reqCommand == "HT" || reqCommand == "SB" || reqCommand == "CONNECT") {
                retStr = if (Utils.MakeCommandFile(reqCommand, 0)) "Y" else "N"
            } else if (reqCommand == "Chk") {
                retStr = SettingPersister.getPlayerStatus()
            } else if (reqCommand == "TrackUploadTime") {
                retStr = if (isTrackUploadTime) "Y" else "N"
            } else if (reqCommand == "AgentChk") {
                // jason:playeragentstartdt: 플레이어 및 에이전트 시작일시 보고(2015/09/30)
                // jason:agentreportintsched: 통합스케줄 서버 보고(2013/05/28)
                retStr =
                    SettingPersister.getPlayerStatus() + "|" + mPlayerStartDt + "|" + reportScheduleFile
//                retStr = mStbOpt.stbStatus + "|" +mPlayerStartDt + "|" + mStbOpt.scheduleName;
            } else if (reqCommand == "StartKctnt") {
                // jason:logdebuginfo2: 플레이어 디버깅 로그 옵션(2014/03/11)
                Utils.DebugAuto("")
                Utils.DebugAuto("Listener From Manager: StartKctnt")
                //-
                Utils.killCustomExec()
                kioskContentDownloading = true
                retStr = "Y"
            } else if (reqCommand == "EndKctnt") {
                kioskContentDownloading = false
                retStr = "Y"
            } else if (reqCommand.startsWith("GetConfig:")) {
                run {
                    retStr = "Y"
                    sendConfigSeq = Integer.valueOf(reqCommand.substring(10))
                    sendConfigNow = true
                }

            } else if (reqCommand.startsWith("Ping:")) {
                val tmp = reqCommand.substring(5)
                var ret = false
                val runtime = Runtime.getRuntime()
                try {
                    val ipProcess = runtime.exec("/system/bin/ping -c 1 $tmp")
                    val exitValue = ipProcess.waitFor()
                    if (exitValue == 0) {
                        ret = true
                    }
                    if (LOG) Log.d(ContentValues.TAG, "EXIT VALUE=$exitValue")
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                retStr = if (ret) "Y" else "N"
            } else if (reqCommand.startsWith("ReceiveFile:")) {
                val tmp = reqCommand.substring(12)
                val seps = charArrayOf('|')
                val values = tmp.split(String(seps).toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                var srcFile = ""
                var dstFile = ""
                var isOverwrittenMode = true
                if (values.size == 3) {
                    srcFile = values[0]
                    dstFile = values[1]
                    isOverwrittenMode = java.lang.Boolean.valueOf(values[2])
                } else if (values.size == 2) {
                    srcFile = values[0]
                    dstFile = values[1]
                }
                if (values.size == 2 || values.size == 3) {
                    dstFile = dstFile.replace("\${Root}", FileUtils.BBMC_PAYCAST_DIRECTORY)
                    // jason:debugshowtext: 패키지 파일 전송 개선 중 긴급 공지 비활성화 디버깅(2012/12/27)
                    // jason:pkgfiletran:패키지 파일 전송 개선(2012/11/23)
                    if (dstFile.indexOf("\${CurrentSTCommandFileName}") > -1) {
                        dstFile = dstFile.replace(
                            "\${CurrentSTCommandFileName}",
                            FileUtils.BBMC_PAYCAST_DIRECTORY + "/Command/" + FileUtils.getCurrentDateTimeCommandFileName(
                                "ST"
                            )
                        )
                        // jason:logdebuginfo3: 플레이어 디버깅 로그 옵션(2014/03/17)
                        Utils.DebugAuto("")
                        Utils.DebugAuto("Call SignCastModel.ReceiveFile: " + mEnv!!.managerIp + ", " + mEnv!!.managerTcpPort + ", " + srcFile + ", " + dstFile + ", " + isOverwrittenMode)
                    } else {
                        // jason:logdebuginfo3: 플레이어 디버깅 로그 옵션(2014/03/17)
                        Utils.DebugAuto("")
                        Utils.DebugAuto("Call SignCastModel.ReceiveTransferredFile: " + mEnv!!.managerIp + ", " + mEnv!!.managerTcpPort + ", " + srcFile + ", " + dstFile + ", " + isOverwrittenMode)
                        //MaxFileLength 가 800m 이상의 파일 압출 오류??                        isSuccess = SignCastModel.ReceiveTransferredFile(mEnv.managerIp, mEnv.managerTcpPort,
//                                srcFile, dstFile, isOverwrittenMode);
                    }
                    val isSuccess: Boolean = receiveFile(
                        mEnv!!.managerIp,
                        mEnv!!.managerTcpPort,
                        srcFile,
                        dstFile,
                        isOverwrittenMode
                    )
                    retStr = if (isSuccess) "Y" else "N"
                } else {
                    retStr = "N"
                }
            } else if (reqCommand.startsWith("SetConfig:")) {
                val tmp = reqCommand.substring(10)
                if (tmp.endsWith("|")) {
                    receiveConfigSeq = Integer.valueOf(tmp.replace("|", ""))
                    receiveBgImgFile = ""
                } else {
                    receiveConfigSeq = Integer.valueOf(tmp.substring(0, tmp.indexOf("|")))
                    receiveBgImgFile = tmp.substring(tmp.indexOf("|") + 1)
                }
                retStr = "Y"
                resultOpCode = "."
                receiveConfigNow = true
            } else if (reqCommand.startsWith("ShowText:")) {
                val tmp = reqCommand.substring(9)
                Log.d(ContentValues.TAG, "구현 해야 함. ShowText : $tmp")
                retStr = "Y"
            } else if (reqCommand.startsWith("SetNewMenu:")) {
                if (reqCommand == "SetNewMenu:") {
                    APP.playerOpt!!.optionDefaultMenuFile = ""
                    /*  Player option    */
                    val dir = FileUtils.makeDirectory(FileUtils.BBMC_DIRECTORY)
                    val file = FileUtils.makePlayerOptionFile(
                        dir,
                        FileUtils.BBMC_DIRECTORY + FileUtils.getFilename(FileUtils.Player),
                        application,
                        APP.playerOpt
                    )
                    Logger.e("SetMenu : Crate new player option file : ${file.name}")
                } else {
                    val fileName = reqCommand.substring(11)
                    val pos = fileName.lastIndexOf(".")
                    val tmp = fileName.substring(0, pos)
                    Logger.e("Make new order file : $fileName // tmp : $tmp")
                    if (tmp.isNotEmpty()) {
                        val fInfo = File(FileUtils.BBMC_PAYCAST_MENU_DIRECTORY + tmp + ".self")
                        if (fInfo.exists()) {
                            if (APP.playerOpt!!.optionDefaultMenuFile == null || APP.playerOpt!!.optionDefaultMenuFile != "$tmp.self") {
                                APP.playerOpt!!.optionDefaultMenuFile = "$tmp.self"
                                mStbOpt!!.menuName = "$tmp.self"
                                /*  Player option    */
                                val dir = FileUtils.makeDirectory(FileUtils.BBMC_PAYCAST_DIRECTORY)
                                FileUtils.updatePlayerOptionFile(
                                    dir,
                                    FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + FileUtils.getFilename(
                                        FileUtils.PayCastPlayer
                                    ),
                                    application,
                                    APP.playerOpt
                                )
                                Logger.w("Update OptionFile : ${APP.playerOpt?.optionDefaultMenuFile} // menuName: $APP.stbOpt?.menuName")
                                val updatedMenu = parseMenuObject(FileUtils.BBMC_PAYCAST_MENU_DIRECTORY + APP.stbOpt?.menuName)
                                updatedMenu?.let {
                                    menuListSubject.onNext(it)                    //mMenuObject = parseMenuObject(FileUtils.BBMC_PAYCAST_MENU_DIRECTORY + MainServiceV2.mStbOpt.menuName)
                                    delayRun({ sendBroadCastService("New menu updated") })
                                } ?: run {
                                    Logger.e("Error: updatedMenu is null")
                                }
                                //delayRun({ sendBroadCastService("Player restart") }, 3000)
                            }
                        }
                    }
                }
                retStr = "Y"
            } else if (reqCommand == "OperationResult") {
                retStr = resultOpCode
            } else if (reqCommand == "CleanOperationResult") {
                resultOpCode = ""
                retStr = "Y"
            } else if (reqCommand == "RCMode") {
                retStr = "Y"
                Log.d(ContentValues.TAG, "RCMode ???  미구현")
                //waitDuringRemoteControl(true);
            } else if (reqCommand.startsWith("SetManagerIp:")) {
                val tmp = reqCommand.substring(13)
                if (tmp.isNotEmpty() && tmp.length >= 3 && !tmp.startsWith("|") && !tmp.endsWith("|")) {
                    mEnv!!.managerIp = tmp.substring(0, tmp.indexOf("|"))
                    mEnv!!.managerTcpPort = Integer.valueOf(tmp.substring(tmp.indexOf("|") + 1))
                    Log.d(
                        ContentValues.TAG,
                        "미구현 SetManagerIp : " + mEnv!!.managerIp + " managerTcpPort=" + mEnv!!.managerTcpPort
                    )
                }
                retStr = "Y"
            } else if (reqCommand == "SetConfigByServer") {
                retStr = "Y"
                mfileUtils!!.readRequestedStbConfigFileByServer(
                    applicationContext
                )
            } else if (reqCommand == "SetConfig") {
                retStr = "Y"
                //(XmlOptionParser optUtil, String tempSavedConfigFile, PlayerOptionEnv option, Context c)
                val tempSavedConfigFile = Utils.tempSavedConfigFileName()
                ConfigUtil.readRequestedStbConfigFile(
                    tempSavedConfigFile,
                    APP.playerOpt!!,
                    applicationContext
                )
            } else if (reqCommand == "Restart") {
                // jason:pkgfiletran:패키지 파일 전송 개선(2012/11/23)
                Log.d(ContentValues.TAG, "reCommand Restart.. 미구현")
                retStr = "N"
            } else if (reqCommand == "RestartPlayer") {
                retStr = if (Utils.MakeCommandFile("RP")) "Y" else "N"
            } else if (reqCommand == "Touch") {
                retStr = "Y"
                val dateFromSec = Utils.getCurrentDate()
                val dateFromDay = Utils.getCurrentDate()
                dateFromSec.seconds = dateFromSec.seconds + APP.playerOpt!!.optionSleepSecs
                dateFromDay.date = dateFromDay.day + 1
            } else if (reqCommand == "Pause" || reqCommand.startsWith("Pause:")) {
                retStr = if (Utils.MakeCommandFile("PP")) "Y" else "N"
                if (reqCommand != "Pause" && !reqCommand.endsWith(":")) {
                    val tmp = reqCommand.substring(reqCommand.indexOf(":") + 1)
                    val sec = Integer.valueOf(tmp)
                    val dateFromSec = Utils.getCurrentDate()
                    val dateFromDay = Utils.getCurrentDate()
                    dateFromSec.seconds = dateFromSec.seconds + sec
                    dateFromDay.date = dateFromDay.day + 1
                }
            } else if (reqCommand.startsWith("Capture:")) {
                retStr = "Y"
                captureNow = true
                captureParam = reqCommand.substring(8)
            } else if (reqCommand == "DeleteAllSchedule") {
                retStr = if (Utils.MakeCommandFile("PP")) "Y" else "N"
                try {
                    Thread.sleep(3000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                assert(APP.playerOpt != null)
                APP.playerOpt!!.optionDefaultScheduleFile = ""
                FileUtils.deleteAllFiles(FileUtils.BBMC_SCH_DIRECTORY)
                FileUtils.deleteAllFiles(FileUtils.BBMC_PAYCAST_DIRECTORY + "Content\\Audio")
                FileUtils.deleteAllFiles(FileUtils.BBMC_PAYCAST_DIRECTORY + "Content\\Component")
                FileUtils.deleteAllFiles(FileUtils.BBMC_PAYCAST_DIRECTORY + "Content\\Flash")
                FileUtils.deleteAllFiles(FileUtils.BBMC_PAYCAST_DIRECTORY + "Content\\Image")
                FileUtils.deleteAllFiles(FileUtils.BBMC_PAYCAST_DIRECTORY + "Content\\PowerPoint")
                FileUtils.deleteAllFiles(FileUtils.BBMC_PAYCAST_DIRECTORY + "Content\\Text")
                FileUtils.deleteAllFiles(FileUtils.BBMC_PAYCAST_DIRECTORY + "Content\\Video")
            } else if (reqCommand.startsWith("AuthMgrApp:")) {
                retStr = if (reqCommand.length == 23) {
                    val tmp = reqCommand.substring(11)
                    Log.d(ContentValues.TAG, "구현 필요AuthMgrApp : $tmp")
                    "Y"
                } else {
                    "N"
                }
            } else if (reqCommand.startsWith("Update:") || reqCommand == "Update") {
                Utils.DebugAuto("")
                Utils.DebugAuto("Listener From Manager: $reqCommand")
                retStr = updateByRemote(if (reqCommand == "Update") "" else reqCommand.substring(7))
            } else if (reqCommand == "Debug10Mins") {
                Utils.DebugAuto("")
                Utils.DebugAuto("Listener From Manager: $reqCommand")
                val d = Utils.getCurrentDate()
                d.minutes = d.minutes + 10
                //SignCastManager.ViewModels.DebugModel.TempLogDt = d;
                Utils.TempLogDt = d
                Log.d(ContentValues.TAG, "model.savePlayerConfigData() 미구현")
                //model.savePlayerConfigData();
                retStr = "Y"
            } else if (reqCommand == "RestartPlayerIfNewer") {
                retStr = if (Utils.MakeCommandFile("CR")) "Y" else "N"
            } else if (reqCommand == "Shutdown") {
                retStr = if (Utils.MakeCommandFile("SD")) "Y" else "N"
            } else if (reqCommand.startsWith("SyncShow:")) {
                syncStartDt = Utils.getCurrentDate()
                val tmp = reqCommand.substring(9)
                val pos: Long = java.lang.Long.valueOf(tmp)
                syncOffsetMillisecs = pos
                Log.d(ContentValues.TAG, "미구현..startShow()")
                retStr = "Ok"
            } else if (reqCommand.startsWith("SyncTick:")) {
                if (mStbOpt!!.stbStatus == 5) {
                    syncStartDt = Utils.getCurrentDate()
                    val tmp = reqCommand.substring(9)
                    val pos: Long = java.lang.Long.valueOf(tmp)
                    syncOffsetMillisecs = pos
                    Log.d(ContentValues.TAG, "미구현 SyncTick: startShow()....")
                }
                retStr = "Ok"
            } else if (reqCommand == "SyncStandBy") {
                Log.d(ContentValues.TAG, "SyncStandBy 구현 확인 필요")
                standByForMultiSyncMode()
                retStr = "Ok"
            } else {
                retStr = "?"
            }

            // jason:logdebuginfo3: 플레이어 디버깅 로그 옵션(2014/03/17)
            Utils.DebugAuto("")
            Utils.DebugAuto("Listen from Mgr: Result: $retStr")
            //-

            //data = Xml.Encoding.UTF_8..GetBytes(retStr);
            //newsock.SendTo(data, data.Length, SocketFlags.None, Remote);
            return retStr
        }
        return retStr
    }

    private fun updateByRemote(param: String?): String {
        Utils.DebugWriteLine("-------------------------------------")
        Utils.DebugWriteLine("Starting updateByRemote...")

        //
        // param의 형식: EE2.1.32.1 or ""
        //
        if (myFtp != null) {
            Utils.DebugWriteLine("updateByRemote: F - case 1(myFtp is not null)")
            Utils.DebugWriteLine("-------------------------------------")
            return "N"
        }
        if (param == null || param.isEmpty()) {
            val dstVer = latestStableVersion
            if (dstVer.isEmpty() || dstVer == "NO") {
                Utils.DebugWriteLine("updateByRemote: F - case 2(dstVer data is invalid)")
                Utils.DebugWriteLine("-------------------------------------")
                return "N"
            }
        } else {
            if (param.length < 3) {
                Utils.DebugWriteLine("updateByRemote: F - case 3(version info is invalid; $param)")
                Utils.DebugWriteLine("-------------------------------------")
                return "N"
            }
        }

        // 대상 에디션 및 버전이 현재와 동일할 경우 자동 취소
        myFtp = FTPClient()
        Log.d(ContentValues.TAG, "updateByRemote() 구현 필요")
        Utils.DebugWriteLine("updateByRemote: F - case 4(end of process)")
        Utils.DebugWriteLine("-------------------------------------")
        return "N"
    }

    private val latestStableVersion: String
        // jason:serverssl: 서버 https 프로토콜 옵션(2017/10/12)
        get() {
            val postString = StringBuilder()
            postString.append("type=P&")
            postString.append("edition=" + getString(R.string.editionCode))

            val dstTmpUrl = PropUtil.customConfigValue("UpdateInfoURL")
            var dstUrl = "NO"

            // jason:serverssl: 서버 https 프로토콜 옵션(2017/10/12)
            if (dstTmpUrl.startsWith("http://") || dstTmpUrl.startsWith("https://")) {
                val tmp = dstTmpUrl.replace("http://", "").replace("https://", "")
                if (tmp.indexOf("/") > -1) {
                    dstUrl =
                        (if (dstTmpUrl.startsWith("http://")) "http://" else "https://") + tmp.substring(
                            0,
                            tmp.indexOf("/")
                        ) + "/dsg/agent/updatelatest"
                }
            }
            if (dstUrl == "NO") {
                return dstUrl
            }
            Log.d(ContentValues.TAG, "getLatestStableVersion() 미구현 ...구현필요")
            return "NO"
        }

    private fun executeChangeMenu(newMenu: String) {
        communicateWithPlayer("SetNewMenu:$newMenu", null)
    }

    private fun standByForMultiSyncMode() {
        if (mStbOpt!!.stbStatus != 3) {
            mStbOpt!!.stbStatus = 5
        }
        Utils.DebugAuto("")
        Utils.DebugAuto("--------------------------------------------------")
        Utils.DebugAuto("Stand-by for Multiple STB Sync Mode(status code = " + mStbOpt!!.stbStatus + ")")

        // 미구현 2차 구현 예정 stopAreaBgAudioPlay();
    }


    private fun receiveFile(
        ip: String,
        port: Int,
        dstFileName: String,
        fileName: String,
        isOverWrittenMode: Boolean
    ): Boolean {
        Log.d(ContentValues.TAG, "receiveFile()")
        connectTask(ip, port, dstFileName, fileName, isOverWrittenMode)
        // Step 1
        if (mTcpClient != null) {
            mTcpClient!!.sendMessage("GetFile")
        }
        // Step 3
        if (mTcpClient != null) {
            mTcpClient!!.sendMessage(dstFileName)
        }
        return true
    }

    private fun connectTask(
        ip: String,
        port: Int = 11002,
        destFile: String,
        sourceFile: String,
        isOverWrittenMode: Boolean
    ) {
        var step = 0
        var response1: String? = ""
        var response2: String? = ""
        var lengthStr: String
        var maxFileLength = 0
        var fs1: FileOutputStream? = null
        var savedFileSize = 0L

        CoroutineScope(Dispatchers.IO).launch {
            mTcpClient = NetworkUtil.TcpClientRec(
                { message ->
                    //here the messageReceived method is implemented
                    //this method calls the onProgressUpdate
                    when (step) {
                        1 -> {
                            step = 2
                            response1 = message
                            mTcpClient!!.sendMessage(destFile)
                        }
                        2 -> {
                            step = 3
                            lengthStr = message
                            maxFileLength = Integer.valueOf(lengthStr)
                            sourceFile.replace(
                                "\${Root}",
                                FileUtils.BBMC_DEFAULT_DIR
                            )
                            mTcpClient!!.sendMessage(sourceFile)
                        }
                        3 -> {
                            step = 4
                            response2 = message
                            mTcpClient!!.sendMessage("Y")
                            savedFileSize = 0L
                        }
                        4 -> {
                            val f = File(sourceFile)
                            Logger.d(
                                "1 NetworkUtil.TcpClientRec doInBackground() fileName=%s",
                                sourceFile
                            )
                            if (response1 != null && !response1!!.isEmpty() && response2 != null && !response2!!.isEmpty() && (!f.exists() || f.exists()) && isOverWrittenMode) {
                                try {
                                    fs1 = FileOutputStream(sourceFile)
                                    if (sourceFile !== "") {
                                        step = 5
                                        val b = message.toByteArray()
                                        fs1!!.write(b)
                                        fs1!!.flush()
                                        savedFileSize += message.length.toLong()
                                    }
                                    if (savedFileSize < maxFileLength) {
                                        fs1!!.flush()
                                        fs1!!.close()
                                    }
                                } catch (e: FileNotFoundException) {
                                    e.printStackTrace()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        5 -> {
                            val b = message.toByteArray()
                            Logger.d("2 NetworkUtil.TcpClientRec doInBackground() fileName=" + sourceFile)
                            try {
                                fs1!!.write(b)
                                fs1!!.flush()
                                savedFileSize += message.length.toLong()
                                if (savedFileSize < maxFileLength) {
                                    fs1!!.flush()
                                    fs1!!.close()
                                    mTcpClient!!.stopClient()
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                        else -> {}
                    }
                }, ip, port
            )
            mTcpClient!!.run()
        }
    }

    @Throws(FileNotFoundException::class)
    fun parseMenuObject(fileName: String): MenuCatagoryObject? {
        Logger.w("parseMenuObject : $fileName")
        val f = File(fileName)
        if (!f.exists()) return null
        val fis = FileInputStream(fileName)
        var menuCatagoryObject: MenuCatagoryObject?
        Log.e(ContentValues.TAG, "parseMenuObject()START=========================")
        try {
            val parserFactory = XmlPullParserFactory.newInstance()
            val parser = parserFactory.newPullParser()
            val no = -1
            val name: String? = null
            parser.setInput(fis, null)
            //          parser.setInput(str, null) ;
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    // XML 데이터 시작
                } else if (eventType == XmlPullParser.START_TAG) {
                    val startTag = parser.name
                    if (startTag == "Store") {
                        menuCatagoryObject = MenuCatagoryObject()
                        mMenuObject = mXmlOptUtil!!.getStoreCatagory(parser, menuCatagoryObject)
                        mMenuObject?.catagoryObjectList = ArrayList()
                    } else if (startTag == "Catagory") {
                        val catagoryObject = CatagoryObject()
                        mCatagoryObject = mXmlOptUtil!!.getMenuCatagory(parser, catagoryObject)
                        mCatagoryObject?.menuObjectList = ArrayList()
                        //                        mMenuObject.catagoryObjectList.add(mCatagoryObject);
                    } else if (startTag == "Menu") {
                        val menuObject = MenuObject()
                        mMenuItemObject = mXmlOptUtil!!.getMenuItem(parser, menuObject)
                        //                        mCatagoryObject.menuObjectList.add(menuObject);
                    } else if (startTag == "optionMenu") {
                        val menuOOpt = MenuOptionData()
                        mMenuOptData = mXmlOptUtil!!.getMenuItemOption(parser, menuOOpt)
                    } else if (startTag == "option") {
                        mMenuOptLists = CustomOptionData()
                        if (mMenuOptData!!.menuGubun.equals("1", ignoreCase = true)) //추가옵션
                        {
                            val menuObject = CustomAddedOptionData()
                            mMenuAddOptData = mXmlOptUtil!!.getMenuItemOption(parser, menuObject)
                            if (mMenuAddOptData != null) {
                                if (mMenuOptLists!!.addOptList == null) mMenuOptLists!!.addOptList =
                                    ArrayList()
                                mMenuOptLists!!.addOptList.add(mMenuAddOptData)
                            }
                        } else if (mMenuOptData!!.menuGubun.equals("0", ignoreCase = true)) //필수옵션
                        {
                            val menuObject = CustomRequiredOptionData()
                            mMenuReqOptData = mXmlOptUtil!!.getMenuItemOption(parser, menuObject)
                            if (mMenuReqOptData != null) {
                                if (mMenuOptLists!!.requiredOptList == null) mMenuOptLists!!.requiredOptList =
                                    ArrayList()
                                mMenuOptLists!!.requiredOptList.add(mMenuReqOptData)
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    when (parser.name) {
                        "Store" -> {}
                        "Catagory" -> {
                            mCatagoryObject?.let { mMenuObject!!.catagoryObjectList.add(it) }
                        }
                        "Menu" -> {
                            mMenuItemObject?.let { mCatagoryObject!!.menuObjectList.add(it) }
                        }
                        "optionMenu" -> {
                            if (mMenuItemObject!!.optMenusList == null) mMenuItemObject!!.optMenusList = ArrayList()
                            mMenuOptData?.let { mMenuItemObject!!.optMenusList.add(it) }
                        }
                        "option" -> {
                            if (mMenuOptData!!.optList == null) mMenuOptData!!.optList = ArrayList()
                            mMenuOptLists?.let { mMenuOptData!!.optList.add(it) }
                        }
                    }
                }
                eventType = parser.next()
            }
            if (no == -1 || name == null) {
                //errorMsgSubject.onNext("XML is invalid.")
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.e(ContentValues.TAG, "parseMenuObject()END=========================")
        return mMenuObject
    }

    private fun startPlayer() {
        Logger.w("1. startPlayer() ... ")
        val message = Message.obtain(null, SEND_VALUE, SingCastPlayIntent.PLAYER_READY, 0)
        try {
            if (mActivityMessenger != null) {
                mActivityMessenger!!.send(message)
                Logger.w("Send msg : $message")
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private suspend fun executeCommand(ci: CommandObject?): String {
        var result = "N"
        ci?.let {
            getPlayerAgentCheck(false)
//            Logger.e("ci.command=" + ci.command)
//            Logger.e("ci.koEnabled=${ci.koEnabled} // atEnabled=${ci.atEnabled} // openType=${ci.openType}")
            when (ci.command) {
                "MenuInfoChg.bbmc" -> {
                    Logger.d("MenuInfoChg.bbmc!!!!")
                    run {
                        val log = String.format("executeCommand() ci.command=%s ci.rcCommandid=%s", ci.command, ci.rcCommandid)
                        FileUtils.writeLog(log, "PayCastLog")
                    }
                    Logger.e("MenuInfoChg.bbmc menuOrderStatus()=$orderStatus")
                    if (checkCommandState()) { // 키오스크가 활성화중인경우
                        sendBroadCastService(getString(R.string.kiosk_menu_update_command))
                        showPopup.onNext(true)
                        result = getMenuInfoChgFromServer()
                        Logger.e("MenuInfoChg.bbmc result=$result")
                    } else {
                        Logger.w("Update failed - orderStatus is $orderStatus // APP.stbOpt?.openType is ${APP.stbOpt?.openType} // APP.stbOpt?.koEnable is ${APP.stbOpt?.koEnable}")
                        result = ""
                    }
                }
                "BannerInfoChg.bbmc" -> {
                    Logger.e("BannerInfoChg.bbmc menuOrderStatus()=$orderStatus")
                    result = if (checkCommandState()) {
                        sendBroadCastService(getString(R.string.kiosk_banner_update_command))
                        "P"
                    } else ""
                }
                "StoreInfoChg.bbmc" -> {
                    result = storeInfoUpdate()
                    Logger.w("5. result : $result")
                }
                "KioskEnabled.bbmc" -> {
                    kioskEnableSubject.onNext(Triple(ci.koEnabled, ci.atEnabled, ci.openType))
                    Logger.d("1 KioskEnabled.bbmc ci.koEnabled=" + ci.koEnabled + " ci.atEnabled=" + ci.atEnabled + "ci.openTyp=" + ci.openType)
                    result = "P"
                }
                "UpdatePlayer.bbmc" -> {}
                "SetConfig.bbmc" ->                     //result = executeSetConfig(playerControlMode, ci.prameter);
                    Log.e(ContentValues.TAG, "CommandAsynTask SetConfig.bbmc() RESULT=$result")
                "MonitorOn.bbmc" -> {}
                "MonitorOff.bbmc" -> {}
                "PowerOff.bbmc" -> {}
                "Reboot.bbmc" -> {}
                "HideEmergenText.bbmc" -> {}
                "SyncContent.bbmc" -> Log.e(
                    ContentValues.TAG,
                    "executeCommand() SyncContent.bbmc executeSyncContent()"
                )
                "UploadCapture.bbmc" -> {}
                "UploadCaptures.bbmc" -> {}
                "UploadTrackFile.bbmc" -> {}
                "UploadLogFile.bbmc" -> {}
                "UploadDebugFile.bbmc" -> {}
                "UploadTodayFile.bbmc" -> {}
                "PowerOnWol.bbmc" -> result = "Y"
                "DeleteTrackFile.bbmc" -> {}
                "RestartPlayer.bbmc" -> {}
                "ShowEmergenText.bbmc" -> {}
                "DeleteAllSchedule.bbmc" -> {}
                "Debug10Mins.bbmc" -> {}
                "RestartAgent.bbmc" -> {
                    Utils.LOG(getString(R.string.Log_RCRestartAgent))
                    result = "P"
                }
                "CheckResources.bbmc" -> {}
                "SetSchedule.bbmc" -> {}
                else -> {}
            }
            when (ci.command) {
                "MonitorOn.bbmc", "MonitorOff.bbmc", "RestartPlayer.bbmc", "DeleteAllSchedule.bbmc", "PowerOnWol.bbmc" -> {}
                else -> {}
            }
        }

        return result
    }

    private fun checkCommandState(): Boolean {
        return orderStatus == KioskState.MENU_PLAY_INIT_STATE     // 주문중이 아닐경우
                && ((APP.stbOpt?.openType ?: "O") == "O")   // 키오스크가 영업중인경우
                && ((APP.stbOpt?.koEnable ?: "true") == "true")
    }

    private fun executeSyncContent(rcCommandId: String = ""): String {
        var ret = "N"
        Log.e(ContentValues.TAG, "1 executeSyncContent() isDownloading=$isDownloading")
        if (!isDownloading) {
            // return values: S / P / F
            ret = getSyncContentInfoFromServer(rcCommandId)
        }
        Log.e(ContentValues.TAG, "2 executeSyncContent() isDownloading=$isDownloading")
        Logger.e("getSyncContentInfoFromServer : result = $ret")
        return ret
    }

    private suspend fun storeInfoUpdate(): String {
        var result = "N"
        val resultWait = CoroutineScope(Dispatchers.IO).async {
            APP.repository.getStoreState()
                .retry(2)
                .catch {
                    Logger.w("Error : ${it.message}")
                }
                .collectLatest {
                    sellerData = it.data.sellerInfo
                    Logger.e("getStoreState@@2 is $it")
                    result = "P"
                }
        }
        resultWait.await()
        return result
    }

    @SuppressLint("SimpleDateFormat")
    private fun getSyncContentInfoFromServer(rcCommandId: String): String {
        val reqUrl = serverSyncContentUrl
        Logger.d("getServerSyncContentUrl(%s) : %s", rcCommandId, reqUrl)
        Utils.LOG(getString(R.string.Log_GetSyncContentInfoFromServer) + ": " + reqUrl)
        Logger.w("1. 메뉴 정보 로드 : $reqUrl")
        val url: URL? = try {
            URL(reqUrl)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return ""
        }
        // URL을 연결한 객체 생성.
        val conn: HttpURLConnection? = try {
            url?.openConnection() as HttpURLConnection
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
        try {
            conn!!.requestMethod = "GET" // get방식 통신
        } catch (e: ProtocolException) {
            e.printStackTrace()
            conn!!.disconnect()
            return ""
        }
        conn.doInput = true // 읽기모드 지정
        conn.connectTimeout = MAX_CONNECTION_TIMEOUT // 통신 타임아웃
        conn.readTimeout = MAX_SOCKET_TIMEOUT
        val resCode: Int = try {
            conn.responseCode
        } catch (e: IOException) {
            e.printStackTrace()
            conn.disconnect()
            return ""
        }
        val responseString: String
        if (resCode == HttpURLConnection.HTTP_OK || resCode == HttpURLConnection.HTTP_CREATED) {
            val `is`: InputStream? = try {
                conn.inputStream
            } catch (e: IOException) {
                e.printStackTrace()
                conn.disconnect()
                return ""
            } //input스트림 개방
            val builder = StringBuilder() //문자열을 담기 위한 객체
            val reader: BufferedReader? = try {
                BufferedReader(InputStreamReader(`is`, "UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                conn.disconnect()
                return ""
            } //문자열 셋 세팅
            var line: String?
            while (true) {
                try {
                    if (reader?.readLine().also { line = it } == null) break
                } catch (e: IOException) {
                    e.printStackTrace()
                    conn.disconnect()
                    return ""
                }
                builder.append(line)
            }
            responseString = builder.toString()
            conn.disconnect()
            //Logger.d("MonitorAsynTask() resp=$responseString")
            if (responseString.isEmpty()) {
                Utils.LOG(R.string.Log_ResultGetSyncContentInfoFromServer.toString() + ": " + R.string.Log_Failure)
                return "N"
            }
            ParseXML(responseString)
            if (LOG) Log.e(ContentValues.TAG, "2 downloadList.size=" + downloadList.size)
            if (downloadList.size > 0) {
                Logger.d("getSyncContentInfoFromServer() isDownloading=$isDownloading")
                if (!isDownloading) {
                    Utils.LOG(getString(R.string.Log_ResultGetSyncContentInfoFromServer) + ": " + getString(
                            R.string.Log_Success
                        ))
                    CoroutineScope(Dispatchers.IO).launch {
                        noticeSharedFlow.tryEmit("서버에 연결중 입니다...")
                        ftpFileDownloads(downloadList) {
                            executeRestartPlayerIfNewer()
                            executeChangeMenu(it)
                        }
                    }
                } else return "N"
            } else {
                return "Y"
            }
            Utils.LOG(R.string.Log_ResultReportContentExistence.toString() + ": " + R.string.Log_Success)
            return "P"
        } else {
            conn.disconnect()
        }
        return "N"
    }

    @SuppressLint("SimpleDateFormat")
    private fun getMenuInfoChgFromServer(): String {
        val reqUrl = serverMenuInfoChgUrl
        Log.d(ContentValues.TAG, "getMenuInfoChgFromServer() reqUrl:$reqUrl")
        Utils.LOG(getString(R.string.Log_get_menu_info_server) + ": " + reqUrl)
        val url: URL? = try {
            URL(reqUrl)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return "N"
        } // URL화 한다.
        // URL을 연결한 객체 생성.
        val conn: HttpURLConnection? = try {
            url?.openConnection() as HttpURLConnection
        } catch (e: IOException) {
            e.printStackTrace()
            return "N"
        }
        try {
            conn!!.requestMethod = "GET" // get방식 통신
        } catch (e: ProtocolException) {
            e.printStackTrace()
            conn!!.disconnect()
            return "N"
        }
        conn.doInput = true // 읽기모드 지정
        conn.connectTimeout = MAX_CONNECTION_TIMEOUT // 통신 타임아웃
        conn.readTimeout = MAX_SOCKET_TIMEOUT
        val resCode: Int = try {
            conn.responseCode
        } catch (e: IOException) {
            e.printStackTrace()
            conn.disconnect()
            return "N"
        }
        if (resCode == HttpURLConnection.HTTP_OK || resCode == HttpURLConnection.HTTP_CREATED) {
            val `is`: InputStream? = try {
                conn.inputStream
            } catch (e: IOException) {
                e.printStackTrace()
                conn.disconnect()
                return "N"
            } //input스트림 개방
            val builder = StringBuilder() //문자열을 담기 위한 객체
            val reader: BufferedReader? = try {
                BufferedReader(InputStreamReader(`is`, "UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                conn.disconnect()
                return "N"
            } //문자열 셋 세팅
            var line: String?
            while (true) {
                try {
                    if (reader?.readLine().also { line = it } == null) break
                } catch (e: IOException) {
                    e.printStackTrace()
                    conn.disconnect()
                    continue
                }
                builder.append("$line\n")
            }
            val responseString = builder.toString()
            if (responseString.isEmpty()) {
                Utils.LOG(R.string.Log_result_get_menu_info_server.toString() + ": " + R.string.Log_Failure)
                return "N"
            }
            ParseXML(responseString)
            if (downloadList.size > 0) {
                Log.d(ContentValues.TAG, "getMenuInfoChgFromServer() isDownloading=$isDownloading")
                if (!isDownloading) {
                    Utils.LOG(
                        getString(R.string.Log_result_get_menu_info_server) + ": " + getString(
                            R.string.Log_Success
                        )
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        ftpFileDownloads(downloadList) {
                            executeRestartPlayerIfNewer()
                            executeChangeMenu(it)
                        }
                    }
                } else return "N"
            } else {
                return "Y"
            }
            Utils.LOG(R.string.Log_ResultReportContentExistence.toString() + ": " + R.string.Log_Success)
            return "P"
        }
        return "N"
    }

    // jason:serverssl: 서버 https 프로토콜 옵션(2017/10/12)
    private val serverMenuInfoChgUrl: String
        get() {
            var serverUrl: String = if (mStbOpt!!.serverPort == 80) {
                String.format("http://" + mStbOpt!!.serverHost + "/info/menuInfo")
            } else {
                String.format("http://" + mStbOpt!!.serverHost + ":" + mStbOpt!!.serverPort + "/info/menuInfo")
            }
            // jason:serverssl: 서버 https 프로토콜 옵션(2017/10/12)
            val ssl = PropUtil.configValue(getString(R.string.serverSSLEnabled), application)
            if (java.lang.Boolean.valueOf(ssl)) {
                serverUrl = serverUrl.replace("http://", "https://")
            }
            Logger.w("Current StoreId=%s & deviceId=%s", mStbOpt!!.stbId, mStbOpt!!.deviceId)
            val encodedQueryString =
                String.format("storeId=%s&deviceId=%s", mStbOpt!!.storeId, mStbOpt!!.deviceId)
            return String.format("%s%s%s", serverUrl, "?", encodedQueryString)
        }// jason:serverssl: 서버 https 프로토콜 옵션(2017/10/12)

    private val runningActivityList: Boolean
        get() {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val proInfos = activityManager.getRunningTasks(30)
            var runningPlayer = false
            if (proInfos != null) {
                for (i in proInfos.indices) {
                    if (proInfos[0].topActivity!!.packageName == application.packageName || proInfos[0].topActivity!!.packageName == "kr.co.kicc.easycarda") {
                        runningPlayer = true
                    } else {
                        Log.e(
                            ContentValues.TAG,
                            "topActivity name=" + proInfos[i].topActivity!!.className
                        )
                        if (proInfos[0].topActivity!!.packageName == "kr.co.kicc.easycarda") {
                            Log.e(ContentValues.TAG, "kr.co.kicc.easycarda is equal")
                        } else Log.e(ContentValues.TAG, "kr.co.kicc.easycarda is Not equal")
                    }
                    break
                }
            }
            return runningPlayer
        }

    private fun copyStbOption(source: StbOptionEnv?, dest: StbOptionEnv) {
        dest.playerStart = source!!.playerStart
        dest.monitorMins = source.monitorMins
        dest.stbId = source.stbId
        dest.stbName = source.stbName
        dest.stbUdpPort = source.stbUdpPort
        dest.serverPort = source.serverPort
        dest.stbServiceType = source.stbServiceType
        dest.serverHost = source.serverHost
        dest.serverUkid = source.serverUkid
        dest.ftpActiveMode = source.ftpActiveMode
        dest.ftpHost = source.ftpHost
        dest.ftpPort = source.ftpPort
        dest.ftpUser = source.ftpUser
        dest.ftpPassword = source.ftpPassword
        dest.scheduleName = source.scheduleName
        dest.stbStatus =
            source.stbStatus //0: 미확인, 2 : 장비꺼짐, 3:모니터 꺼짐, 4:플레이어 꺼짐, 5:스케줄 미지정, 6: 정상방송
        dest.menuName = source.menuName
        dest.storeName = source.storeName //매장명
        dest.storeAddr = source.storeAddr //매장 주소
        dest.storeBusinessNum = source.storeBusinessNum //매장사업자 번호
        dest.storeTel = source.storeTel //매장 전화번호
        dest.storeMerchantNum = source.storeMerchantNum //매장 가맹점 번호
        dest.storeCatId = source.storeCatId //kiosk 카드 단말기 cat id
        dest.storeRepresent = source.storeRepresent //매장 대표자 명
        dest.storeId = source.storeId //매장 번호
        dest.deviceId = source.deviceId //deviceId;
        dest.operatingTime = source.operatingTime //매장 영업시간
        dest.introMsg = source.introMsg //매장소개글
        dest.mainPrtEnable = source.mainPrtEnable //main print enable?
        dest.mainPrtip = source.mainPrtip //main print ip
    }

    @SuppressLint("DefaultLocale")
    private fun onCheckMemory() {
        Logger.w("Check memory")
        val mi = ActivityManager.MemoryInfo()
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(mi)
        val availableMegs = (mi.availMem / 1048576L).toDouble()
        val totalMegs = (mi.totalMem / 1048576L).toDouble()
        val percentAvaillong = ((availableMegs / totalMegs).toFloat() * 100)
        if (LOG) {
            Log.d(
                ContentValues.TAG,
                "onCheckMemory() percentAvaillong=$percentAvaillong availableMegs=$availableMegs totalMegs=$totalMegs"
            )
            Utils.DebugAuto("")
            Utils.DebugAuto("onCheckMemory() : Percent=$percentAvaillong availMem=$availableMegs totalMem=$totalMegs")
        }
        if (100 - percentAvaillong > 80) {
            Utils.DebugAuto("")
            Utils.DebugAuto(
                String.format(
                    "사용가능 메모리 부족\r\n   사용 가능 메모리 : %.0f MB \r\n",
                    availableMegs
                )
            )
            Utils.DebugAuto(String.format("   전체 메모리 : %.0f MB \r\n", totalMegs))
            FileUtils.deleteDebugFile()
            //return false;
        }
    }


    companion object {
        var mPlayerStartNewDt: String? = null
        private var mXmlOptUtil: XmlOptionParser? = null
        private var mEnv: SCEnvironment? = null
        private var mfileUtils: FileUtils? = null
        var mStbOpt: StbOptionEnv? = null
        private var mServrReq: ServerReqUrl? = null
        private var mStartTimer: Timer? = null
        private var isAdditionalReportRequired = true
        private var myFtp: FTPClient? = null
        var mPlayerStartDt: String? = null
        private var mTcpClient: NetworkUtil.TcpClientRec? = null
        const val APP_CHECK_CYCLE = (1 * 30 * 1000).toLong()
        val reportScheduleFile: String
            get() = ""
    }
}