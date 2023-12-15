package kr.co.bbmc.paycast.util

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import com.orhanobut.logger.Logger
import kr.co.bbmc.paycast.*
import kr.co.bbmc.paycast.App.Companion.APP
import kr.co.bbmc.paycast.R
import kr.co.bbmc.selforderutil.*
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

fun sendPlayerCommand(c: PlayerCommand, toService: Boolean = false): Intent {
    val intentAction = if(toService) SingCastPlayIntent.ACTION_SERVICE_COMMAND else SingCastPlayIntent.ACTION_PLAYER_COMMAND
    val sendIntent = Intent(intentAction)
    val b = Bundle().apply {
        putString("executeDateTime", c.executeDateTime)
        putString("requestDateTime", c.requestDateTime)
        putString("command", c.command)
    }
    Logger.d("sendBroadcast command($intentAction) = " + c.command)
    Logger.i("sendBroadcast Bundle = $b")
    sendIntent.putExtras(b)
    run {
        FileUtils.writeLog("sendPlayerCommand() command=${c.command}", "PayCastLog")
    }
    return sendIntent
}

//전화번호 정규식
val regexPhonNumber = "^01([0|1|6|7|8|9])?([0-9]{7,8})$"

fun checkValidPhoneNumber(phoneNumber: String): Boolean {
    val pattern = Pattern.compile(regexPhonNumber)
    return pattern.matcher(phoneNumber).matches()
}

fun onGetStringPrinterErr(err: Int): String {
    var retString: String = APP.getString(R.string.str_check_printer_status)
    var reason = ""
    when (err) {
        -8 -> {
            reason = "프린터가 사용 중 입니다."
            retString += String.format("\r\n %s(Error Code : %d)", reason, err)
        }
        -4, -5 -> {
            reason = "영수증 용지가 없습니다."
            retString += String.format("\r\n %s(Error Code : %d)", reason, err)
        }
        else -> retString += String.format("\r\n (Error Code : %d)", err)
    }
    return retString
}

fun requestPayIntent(): Intent {
    val nComponentName = ComponentName("kr.co.kicc.easycarda", "kr.co.kicc.easycarda.CallPopup")
    val intent = Intent(Intent.ACTION_MAIN)
    val tax = (approveMoney * 0.1).toInt()
    Logger.w("requestPayIntent : TranType - $mTranTypte // numOrder : $mNumberOfOrder // TAX : $tax")

    with(intent) {
        if (mTranTypte.equals("D1", ignoreCase = true)) {
            putExtra("TRAN_TYPE", mTranTypte) //거래구분자 ‘D1’ : 승인 ‘D4’ : 당일취소/전일취소(반품환불)
            putExtra("TRAN_NO", mNumberOfOrder.toString()) //요청 순번 예) "1", "2", "3" …..
            putExtra("TIP", serviceFee.toInt().toString()) // 봉사료 금액
            putExtra("INSTALLMENT", "0") // 할부유무 "0" - 일시불, "N" - 할부 N개월, N은 할부 개월수
        } else if (mTranTypte.equals("D4", ignoreCase = true)) {
            putExtra("TRAN_TYPE", "D4") //당일취소/전일취소(반품환불)
            putExtra("INSTALLMENT", "0") // 할부유무 "0" - 일시불, "N" - 할부 N개월, N은 할부 개월수
            putExtra("APPROVAL_NUM", approvalNum) //ESSENTIAL(취소거래시)
            putExtra("APPROVAL_DATE", payCmdDate) //ESSENTIAL(취소거래시)  "YYMMDD"
            putExtra("TIP", "0") // 봉사료 금액
        }
        putExtra("TERMINAL_TYPE", "40") //단말기 구분 40: 일반거래
        putExtra("TOTAL_AMOUNT", approveMoney.toInt().toString()) // 총 결제 금액
        putExtra("TAX", tax.toString()) // 수동 부가세
        Logger.e(" OrderPay.... TOTAL_AMOUNT=${approveMoney.toInt().toString()} // TAX=$taxMoney // APPROVAL_NUM=$approvalNum // APPROVAL_DATE=$payCmdDate")
        putExtra("FALLBACK_FLAG", "N") //MS 대기상태에 머무는 현상에 대한 답변으로 KICC에서 "N"로 설정하도록 가이드 전달됨.(2019.07.19)
        putExtra("TEXT_MAIN_SIZE", "25") //결제진행 텍스트 사이즈 예) 18
        putExtra("TEXT_MAIN_COLOR", "#303030") //결제진행 텍스트 컬러 예) "#303030"
        putExtra("TEXT_SUB1_SIZE", "12") // 무결성검사 텍스트 사이즈 예) 12
        putExtra("TEXT_SUB1_COLOR", "#ff752a") //무결성검사 텍스트 컬러 예) "#ff752a"
        putExtra("TEXT_SUB2_SIZE", "10") // 식별번호 텍스트 사이즈 예) 10
        putExtra("TEXT_SUB2_COLOR", "#ff752a") // 식별번호 텍스트 컬러 예) "#ff752a"
        putExtra("TEXT_SUB3_SIZE", "16") // 타임아웃 텍스트 사이즈 예) 16
        putExtra("TEXT_SUB3_COLOR", "#ff752a") //타임아웃 텍스트 컬러 예) 예) "#909090"
        putExtra("IMG_BG_PATH", "/sdcard/kicc/background.png") //배경이미지 경로 예) "/sdcard/kicc/background.png" 사이즈 370 * 150 권장
        putExtra("IMG_CARD_PATH", "/sdcard/kicc/card.png") //카드이미지 경로 예) 예) "/sdcard/kicc/card.png" 사이즈 370 * 150 권장
        putExtra("IMG_CLOSE_PATH", "/sdcard/kicc/close.png") //닫기버튼 이미지 경로 예) "/sdcard/kicc/close.png" 사이즈 45 * 45 권장
        component = nComponentName
    }
    return intent
}

fun getCancelOrderData(): Intent {
    Logger.e("Start cancel pay order : $paymentCancelData")
    val nComponentName = ComponentName(KICC_PKG_NAME, "kr.co.kicc.easycarda.CallPopup")
    val mIntent = Intent(Intent.ACTION_MAIN)
    val tax = runCatching {  (java.lang.Float.valueOf(paymentCancelData.goodsAmt!!) * 0.1).toInt() }.getOrDefault(0)

    with(mIntent) {
        putExtra("TRAN_TYPE", "D4") //당일취소/전일취소(반품환불)
        putExtra("INSTALLMENT", "0") // 할부유무 "0" - 일시불, "N" - 할부 N개월, N은 할부 개월수
        putExtra("APPROVAL_NUM", paymentCancelData.AuthCode) //ESSENTIAL(취소거래시)
        putExtra("APPROVAL_DATE", paymentCancelData.orderDate?.substring(0, 6)) //ESSENTIAL(취소거래시)  "YYMMDD"
        putExtra("TERMINAL_TYPE", "40") //단말기 구분 40: 일반거래
        putExtra("TOTAL_AMOUNT", paymentCancelData.goodsAmt) // 총 결제 금액
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
    return mIntent
}

fun onSetKioskPayDataInfoV2(kiccData: PaymentInfoData): KioskPayDataInfo {
    val payDataInfo = KioskPayDataInfo()
    payDataInfo.tid = mNumberOfOrder.toString() //거래고유번호
    payDataInfo.mid = kiccData.shop_tid //가맹점 번호
    payDataInfo.fnCd = kiccData.issueer //발급사 코드
    payDataInfo.fnCd1 = kiccData.acquirer //매입사 코드
    payDataInfo.fnName1 = kiccData.acquirerName //매입사명
    payDataInfo.cardName = kiccData.cardName //카드사
    payDataInfo.cardNum = kiccData.cardNumber //카드사

//        payDataInfo.storeIdpay = mAgentExterVarApp.mStbOpt.storeId;    //매장 ID
    payDataInfo.storeIdpay = storeId.toString()
    val orderList = mOrderList
    payDataInfo.totalindex = orderList.size.toString() //주문상품 개수
    payDataInfo.goodsAmt = kiccData.payAmount //approveMoney.toString() //메뉴 주문 총 금액
    payDataInfo.orderNumber = mNumberOfOrder.toString() //주문번호
    payDataInfo.sotreOrderId = storeOrderId
    var sdf = SimpleDateFormat("yyMMddHHmmssw")
    var tdate: Date? = null
    try {
        tdate = sdf.parse(kiccData.tradingDate) // ex) 1901311626224 (2019년 1월 31일
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    sdf = SimpleDateFormat("yyyyMMddHHmmss")
    payDataInfo.orderDate = runCatching { sdf.format(tdate) }.getOrDefault(Date().toString()) //주문날짜
    payDataInfo.authCode = kiccData.approvalNum //승인번호
    payDataInfo.fnName = kiccData.cardName //결제카드사명",  (예 : 삼성 / 우리)
    payDataInfo.catID = "0000000000" //cat id 10자리
    payDataInfo.goodsTotal = orderList.size.toString() //메뉴 주문 총 수량
    payDataInfo.orderMenuList = ArrayList()
    for (i in orderList.indices) {
        val menuItem = KioskOrderMenuItem()
        val item = orderList[i]
        //goodstotal += item.count;
        menuItem.productID = item.productId
        menuItem.productName = item.text
        menuItem.orderCount = item.count.toString()
        menuItem.orderPrice = item.price
        menuItem.orderPackage = item.isPackage
        payDataInfo.orderMenuList.add(menuItem)
    }
    //        payDataInfo.deviceId = mAgentExterVarApp.mStbOpt.deviceId;
    payDataInfo.deviceId = APP.stbOpt!!.deviceId
    payDataInfo.payMethod = "1" //1: 신용카드, 2: 현금 영수증, c: 취소, other:선불카드
    payDataInfo.transType = kiccData.transType //거래구분자 ‘D1’ : 승인 ‘D4’ : 당일취소/전일취소(반품환불) 'RF':refill
    payDataInfo.paOrderId = kiccData.paOrdId //부모 거래 id : -1(정상거래) other(refill)

    return payDataInfo
}

// 숫자 천 단위에 쉼표 넣기
fun getDecimalFormat(number: Int): String {
    val dec = DecimalFormat("#,###")
    return dec.format(number)
}
