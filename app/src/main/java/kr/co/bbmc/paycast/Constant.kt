@file:OptIn(FlowPreview::class)

package kr.co.bbmc.paycast

import android.os.Bundle
import android.os.Message
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kr.co.bbmc.paycast.data.model.DataModel
import kr.co.bbmc.paycast.data.model.MenuCatagoryObject
import kr.co.bbmc.paycast.network.model.BannerInfo
import kr.co.bbmc.paycast.network.model.PaymentInfo
import kr.co.bbmc.paycast.network.model.ResMenuCategory
import kr.co.bbmc.paycast.network.model.SellerInfo
import kr.co.bbmc.selforderutil.FileUtils
import java.io.File.separator

@JvmField
var ISVERSION: Int = 2
const val PKG_NAME = "kr.co.bbmc.paycast"
const val KICC_PKG_NAME = "kr.co.kicc.easycarda"
//intent action
const val ACTION_PAY_ORDER = "$PKG_NAME.payorder"
const val ACTION_CANCEL_PAY = "$PKG_NAME.cancelpay"

// file path
val BANNER_FILE_DIRECTORY get() = FileUtils.BBMC_PAYCAST_DIRECTORY+"banner"+separator

// 키오스크 상태 정보 : 0 초기화, 2 메뉴화면, 4, 결제중 -> 나머지 상태정보는 사용하지 않는다.
enum class KioskState(val state: Int = 0) { MENU_PLAY_INIT_STATE(0), MENU_SELECTING_STATE(2), MENU_PAY_START_STATE(4) }
//const val MENU_PLAY_INIT_STATE = 0
//const val MENU_DISPLAY_STATE = 1
//const val MENU_SELECTING_STATE = 2
//const val MENU_SELECTED_STATE = 3
//const val MENU_PAY_START_STATE = 4
//const val MENU_PAYING_STATE = 5
//const val MENU_PAYTED_STATE = 6
//const val MENU_ORDER_PRINTING_STATE = 7
//const val MENU_ORDER_PRINTED_STATE = 8
//const val MENU_ORDER_CARD_FAIL_STATE = 9

@JvmField
var orderStatus = KioskState.MENU_PLAY_INIT_STATE
@JvmField
var approvalNum = ""
@JvmField
var cancleType = '1'.code.toByte()

@JvmField
var installment = 0
@JvmField
var approveMoney: Float = 0f
// TCP 서버에서만 사용되는 변수들 제거
@JvmField
var taxMoney: Float = 0f
@JvmField
var serviceFee = 0f
@JvmField
var payCmdDate = ""
@JvmField
var autoOnOff = true

@JvmField
var networkStatus = true
@JvmField
var waitOrderCount = -1
@JvmField
var storeOrderId = ""
//paymentUpload 용 data
@JvmField
var storeId = -1
// 주문번호
@JvmField
var mNumberOfOrder = 1
@JvmField
var paymentStatus = 0x00
@JvmField
var mTranTypte = ""
@JvmField
var cmPlayerStatus = 1
@JvmField
var mOrderList: ArrayList<DataModel> = arrayListOf()
@JvmField
var cmStoreMenuInfo: MenuCatagoryObject = MenuCatagoryObject()
@JvmField
var cmTcpServer: TCPServer = TCPServer()
@JvmField
var mTelephone = ""
@JvmField
var paymentCancelData : PaymentInfo = PaymentInfo()
@JvmField
var mOpenType = App.APP.stbOpt?.openType
@JvmField
var INIT_ORDER_NUM = 0
@JvmField
var MAX_ORDER_NUM = 100 + INIT_ORDER_NUM
@JvmField
var sellerData: SellerInfo? = null  // 사업자 정보
@JvmField
var globalMenuInfo = MutableStateFlow(ResMenuCategory())    // 전자 메뉴 생성용 메뉴 정보
@JvmField
var globalBannerData = MutableStateFlow(listOf<BannerInfo>())    // 광고 정보
val BANNER_COUNT get() = globalBannerData.value.size
@JvmField
var extraBannerList: List<String>? = null    // 광고 정보
@JvmField
var menuDownloadFinished: MutableSharedFlow<Boolean> = MutableSharedFlow(
    replay = 0,
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)
@JvmField
var printEnd: MutableSharedFlow<Boolean> = MutableSharedFlow(
    replay = 0,
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)

// 배너 화면 타입
enum class BannerType { E_MENU, VIDEO, EMPTY }


// 인텐트 번들
val bundleSharedFlow: MutableSharedFlow<Bundle> =
    MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

// 공지
val noticeSharedFlow = MutableStateFlow("키오스크를 초기화 중입니다...")

// 핸들러 메시지
val activityHandlerMsgFlow: MutableSharedFlow<Message> =
    MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

val handlerMsgSubject = PublishSubject.create<Triple<Int, Int, Int>>()

// 인터넷 연결
val netWorkConnectionFlow: MutableSharedFlow<Boolean> =
    MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

// 메뉴 리스트
@JvmField
val menuListSubject = BehaviorSubject.create<MenuCatagoryObject>()
val initStoreId = PublishSubject.create<Boolean>()
val showPopup = PublishSubject.create<Boolean>()

@JvmField
val kioskEnableSubject = BehaviorSubject.create<Triple<String, String, String>>()    // koEnabled, atEnabled, openType: "O: open "C": closed

@JvmField
val errorMsgSubject = BehaviorSubject.create<String>()

//mainMenu Screen List
enum class LaunchType(val type: Int) { Payment(0), Provision(1), Refill(2), Cancel(3) }

// intent key
const val INTENT_REFILL_NUM = "refill tel"
const val INTENT_ORDER_ID = "paOrderId"


