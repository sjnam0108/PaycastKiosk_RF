package kr.co.bbmc.paycast.presentation.paymentKicc

import android.annotation.SuppressLint
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import com.sewoo.jpos.command.ESCPOS
import com.sewoo.jpos.command.ESCPOSConst
import com.sewoo.request.android.RequestHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kr.co.bbmc.paycast.*
import kr.co.bbmc.paycast.App.Companion.APP
import kr.co.bbmc.paycast.KioskState.*
import kr.co.bbmc.paycast.R
import kr.co.bbmc.paycast.data.repository.local.KioskSettingPreference
import kr.co.bbmc.paycast.util.NetUtil.HttpKioskResponseString
import kr.co.bbmc.paycast.util.baseExceptionHandler
import kr.co.bbmc.selforderutil.*
import kr.co.bbmc.selforderutil.ProductInfo.deviceId
import kr.hstar.commonutil.delayRun
import org.json.JSONException
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.lang.Boolean
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import kotlin.Char
import kotlin.Int
import kotlin.String
import kotlin.Throws
import kotlin.getOrDefault
import kotlin.onFailure
import kotlin.requireNotNull
import kotlin.run
import kotlin.runCatching
import kotlin.to
import kotlin.toUShort

@FlowPreview
class PaymentViewModel: BaseViewModel() {

    var orderFlag: String? = null
    var totalorderNum: Int = 0
    var refillNumber: String? = null
    var refillOrderId: String? = null

    private val _viewStatus = MutableStateFlow<Int>(0)
    val viewStatus = _viewStatus
    fun changeViewStatus(status: Int) = _viewStatus.tryEmit(status)

    private val _startPay = MutableStateFlow(false)
    val startPay = _startPay.asStateFlow()

    private val _finish = MutableStateFlow(false)
    val finish = _finish.asStateFlow()

    private fun onSetOrderNumber() {
        totalorderNum = kotlin.runCatching {  KioskSettingPreference.getLastOrderNumber() + 1 }.getOrDefault(1)
        totalorderNum %= MAX_ORDER_NUM
        if (totalorderNum == 0) totalorderNum = 1
        mNumberOfOrder = totalorderNum
        KioskSettingPreference.setLastOrderNumber(totalorderNum)
    }

    fun getOrderNum(): Job = viewModelScope.launch(Dispatchers.IO + baseExceptionHandler() {
        Logger.e("Get orderNum error - $it")
        sendToast("Network Error: $it")
    }) {
        APP.repository.getOrderNumber()
            .retry(2)
            .collectLatest {
                Logger.w("OrderNumber is ${it.data}")
                orderStatus = MENU_PAY_START_STATE
                if(!it.success) onSetOrderNumber() else { mNumberOfOrder = it.data }
                _startPay.value = true
            }
    }

    fun updatePaymentInfo(payDataInfo: KioskPayDataInfo, payData: PaymentInfoData?, tran_type: String): Job = viewModelScope.launch(Dispatchers.IO + baseExceptionHandler() {
        sendToast("Network Error: $it")
        Logger.e("updatePaymentInfo Error msg : $it")
        waitOrderCount = 0
    })  {
        var res: String? = null
        var serverUrl: String

        Logger.w("update payment!! start!!!")
        launch {
            serverUrl = if (APP.stbOpt!!.serverPort == 80) {
                String.format("http://" + APP.stbOpt!!.serverHost)
            } else {
                String.format("http://" + APP.stbOpt!!.serverHost + ":" + APP.stbOpt!!.serverPort)
            }
            val ssl = PropUtil.configValue(APP.getString(R.string.serverSSLEnabled), APP.applicationContext)
            if (Boolean.valueOf(ssl)) {
                serverUrl = serverUrl.replace("http://", "https://")
            }

            try {
                res = HttpKioskResponseString(
                    "$serverUrl/info/paymentinfo",
                    payDataInfo,
                    mOrderList
                )
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            Logger.e("1 HttpKioskResponseString() RES=$res")
        }.join()

        Logger.w("get cookCount start!!")

        launch {
            if (ISVERSION == 2) {
                if (res != null && res.equals("Y", ignoreCase = true)) {
                    APP.repository.getOrderCookCount()
                        .retry(2)
                        .onCompletion {
                            releasePayment()
                            //TODO: 주방프린터 출력 후속조치 - 실패시 내부 디비 저장 + 주기적 리트라이
                        }
                        .catch {
                            Logger.e("Error : ${it.message}")
                            sendToast("결제를 진행할수 없습니다 : ${it.message}")
                        }
                        .collectLatest {
                            Logger.w("Get Cook count : $it")
                            waitOrderCount = if(it.data in 0..MAX_WAIT_COUNT) it.data else 0
                            changeViewStatus(2)
                            APP.mTaskPrint?.onStartPrint(payData, tran_type)
                            // 주방프린트
//                            try {
//                                val printRes = kitchenPrintTestV2(payData)
//                                Logger.w("Res : $printRes")
//                            } catch (e: Exception) {
//                                Logger.e("Err: ${e.message}")
//                            }
                        }
                } else if (res == null || res!!.isEmpty() || res.equals("N", ignoreCase = true)) {
                    Logger.w("res == null")
                    sendToast("인터넷 연결을 확인하시고 다시 시도해주세요.")
                }
            }
        }
    }

    private suspend fun wifiConn(ip: String) : Int {
        return runCatching {
            withContext(Dispatchers.IO) {
                APP.wifiPort?.connect(ip) ?: Logger.e("wifiPort is null")
                0
            }
        }.getOrDefault(-1)
    }

    // 주방 프린트
    private suspend fun kitchenPrintTestV2(payData: PaymentInfoData?) {
        try {
            val ip = APP.stbOpt?.mainPrtip ?: "192.168.0.218"
            viewModelScope.launch(Dispatchers.Main) {
                val res = wifiConn(ip)
                Logger.d("2. Wifi conn res is $res")
                if(res == 0) {
                    APP.wfThread = Thread(RequestHandler())
                    APP.wfThread?.start()
                    Logger.d("3. Start sample print!")
                    customSample1(payData)
                } else Logger.e("Error : Wifi connect failed!!")
            }.join()
            APP.releaseWifiThread()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    fun customSample1(
        payInfo: PaymentInfoData?,
    ): Int {

        val ESC = Char(ESCPOS.ESC.toUShort())
        val LF = Char(ESCPOS.LF.toUShort())

        val max_char = 39
        val myFormatter = DecimalFormat("###,###")
        var sts = -1

        val posPtr = APP.kitchenPrinter

        requireNotNull(posPtr) { Logger.e("Error : posPtr is null") }

        try {
            sts = posPtr.printerSts() ////posPtr.printerCheck() function 과 같이 쓰면 error 남.
            Logger.d("1customSample1() sts=$sts")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        if (sts != ESCPOSConst.STS_NORMAL) {
            return sts
        }

        //123456789012345678901234567890123456789012345678901234567890
        val spaceStr = "                                                            "
        requireNotNull(payInfo) {
            Logger.e("Error : PayData is null")
            errorMsgSubject.onNext("결제정보를 확인할수 없습니다.")
        }
        val totalPrice = myFormatter.format(java.lang.Float.valueOf(payInfo.payAmount))
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var tradingDate = kotlin.runCatching {
            sdf.parse(payInfo.tradingDate)
        }.onFailure { Logger.e("Err: ${it.message}") }.getOrDefault("")
        val storName = sellerData?.storeName ?: ""

        posPtr.printNormal("$ESC|cA$ESC|bC$ESC|2C[ 주문서 ]$LF$LF")
        posPtr.printNormal("$ESC|lA$storName$LF")
        //posPtr.printNormal(ESC.toString() + "|lA" + String.format("주문일시: %s", sdf.format(tradingDate)) + LF + LF)
        posPtr.printNormal(ESC.toString() + "|lA" + String.format("주문일시: %s", payInfo.tradingDate) + LF + LF)
        posPtr.printNormal(ESC.toString() + "|1F" + "주문번호           " + ESC + "|cA" + ESC + "|bC" + ESC + "|rA" + ESC + "|4C" + mNumberOfOrder + LF)
        mOrderList.forEach { item ->
            if(item.isPackage) {
                posPtr.printNormal("$ESC|lA$LF")
                posPtr.printNormal("$ESC|1F주문유형           $ESC|cA$ESC|bC$ESC|rA$ESC|4C포장$LF")
            }
            posPtr.printNormal("$ESC|lA$ESC|bC------------------------------------------$LF")
            posPtr.printNormal("$ESC|lA$ESC|bC  품목                              수량  $LF")
            posPtr.printNormal("$ESC|lA$ESC|bC------------------------------------------$LF")
            // 상품명
            if (item.text.length > max_char) posPtr.printNormal(ESC.toString() + "|lA" + item.text.substring(0, max_char) + LF)
            else posPtr.printNormal(ESC.toString() + "|lA" + item.text + LF)
            // 옵션
            if(item.optionList.isNotEmpty()) {
                val opt = item.optionList.map { it.requiredOptList to it.addOptList }.map {
                    it.first.filter { req -> req != null && req.optMenuName.isNotBlank() }.map { it?.optMenuName ?: "" } +
                    it.second.filter { add -> add != null && add.optMenuName.isNotBlank() }.map { it?.optMenuName ?: "" }
                }.flatten()
                opt.forEach { posPtr.printNormal(ESC.toString() + "|lA" + " -" + it + LF) }
            }
            // 수량
            posPtr.printNormal(ESC.toString() + "|lA" + spaceStr.substring(0, max_char - item.count.toString().length) + item.count + LF)
            posPtr.printNormal("$ESC|cA$ESC|bC $LF")
        }

        // 총액
        val pricelen = totalPrice.toByteArray(charset("euc-kr")).size
        val priceStr = " 총액:"
        var spacelen = 0
        try {
            spacelen = max_char - pricelen - priceStr.toByteArray(charset("euc-kr")).size - 2
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        if (spacelen < 0) spacelen = 0
        val printStr = spaceStr.substring(0, spacelen)
        posPtr.printNormal("$ESC|cA$ESC|bC------------------------------------------$LF")
        posPtr.printNormal(ESC.toString() + "|cA" + ESC + "|bC" + priceStr + printStr + totalPrice + "원" + LF)
        posPtr.printNormal("$ESC|cA$ESC|bC------------------------------------------$LF")

        // 매장요청 메시지
        try {
            posPtr.printNormal("$ESC|lA매장 요청 메시지$LF$LF")
            posPtr.printNormal("$ESC|1F$ESC|lA$ESC|bC$LF")
            posPtr.printNormal("$ESC|cA$ESC|bC------------------------------------------$LF")
            posPtr.printNormal("$ESC|lA이용해 주셔서 감사합니다.$LF")
            posPtr.printNormal(ESC.toString() + "|lA" + "기기번호 : " + deviceId + LF)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        // POSPrinter Only.
        posPtr.printNormal("$ESC|fP")
        posPtr.cutPaper()

        return ESCPOSConst.STS_NORMAL
    }

    private fun releasePayment() {
        viewModelScope.launch(Dispatchers.IO) {
            printEnd.collectLatest {
                withTimeoutOrNull(5000) {
                    if(it) {
                        delayRun({
                            _finish.value = true
                        }, 1000L)
                    }
                } ?: run {
                    sendToast("영수증 출력에 실패했습니다. 관리자에게 문의하세요.")
                    _finish.value = true
                }
            }
        }
    }

    fun cancelPayment(payDataInfo: KioskPayDataInfo, payData: PaymentInfoData?, tran_type: String): Job = viewModelScope.launch(Dispatchers.IO + baseExceptionHandler() {
        sendToast("Network Error: $it")
        waitOrderCount = 0
    })  {
        var res: String? = null
        var serverUrl = ""

        launch {
            serverUrl = if (APP.stbOpt!!.serverPort == 80) { String.format("http://" + APP.stbOpt!!.serverHost) }
            else { String.format("http://" + APP.stbOpt!!.serverHost + ":" + APP.stbOpt!!.serverPort) }
            // jason:serverssl: 서버 https 프로토콜 옵션(2017/10/12)
            val ssl = PropUtil.configValue(APP.getString(R.string.serverSSLEnabled), APP.applicationContext)
            if (Boolean.valueOf(ssl)) { serverUrl = serverUrl.replace("http://", "https://") }
            try {
                res = NetworkUtil.onHttpKioskCancelResponseString("$serverUrl/cancelSuccess", payDataInfo, deviceId)
            } catch (e: JSONException) { e.printStackTrace() }
        }.join()

        FileUtils.writeDebug("PaymentKissActivity CancelKioskPayDataInfoUploadThread res=$res payDataInfo.orderDate=${payDataInfo.orderDate}\r\n", "PayCast")

        launch {
            if (ISVERSION == 2) {
                if (res != null && res.equals("Y", ignoreCase = true)) {
                    val encodedQueryString = "storeId=$storeId&deviceId=$deviceId"
                    val response = NetworkUtil.getCookOrderCountServer("$serverUrl/cookordercount", encodedQueryString)
                    Logger.e("2 HttpKioskResponseString() RES=$res")
                    if (response != null && response!!.isNotEmpty() && response!!.length < 300) {
                        val waitCount = Integer.valueOf(response)
                        if (waitCount in 0..MAX_WAIT_COUNT) {
                            waitOrderCount = waitCount
                        }
                    } else if (response == null || response.isEmpty()) {
                        waitOrderCount = 0
                    }
                    APP.mTaskPrint?.onStartPrint(payData, tran_type)
                    changeViewStatus(2)
                    delayRun({_finish.value = true}, 300L)
                } else if (res == null || res!!.isEmpty() || res.equals("N", ignoreCase = true)) {
                    Logger.e("cancelPayment : failed!!")
                    FileUtils.writeDebug("PaymentKissActivity CancelKioskPayDataInfoUploadThread payDataInfo.orderDate=${payDataInfo.orderDate} \r\n", "PayCast")
                    sendToast("결제취소에 실패했습니다. 다시 시도해주세요.")
                }
            }
        }
    }

    companion object {
        const val DISPLAY_DIALOG_TIMER = 5 * 60 * 1000
        const val PAYMENT_FOR_RESULT = 1100
        const val MAX_WAIT_COUNT = 9999
    }
}