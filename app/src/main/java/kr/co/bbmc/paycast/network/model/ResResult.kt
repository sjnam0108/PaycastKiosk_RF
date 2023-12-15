package kr.co.bbmc.paycast.network.model

import androidx.annotation.Keep
import kr.co.bbmc.paycast.BANNER_FILE_DIRECTORY
import java.io.File

// api response
@Keep
data class ResOrderNumber(
    val data: Int,
    val message: String? = "",
    val success: Boolean
)

@Keep
data class ResCookCount(
    val data: Int,
    val message: String? = "",
    val success: Boolean
)

@Keep
data class ResServerSync(
    val data: KioskState,
    val message: String? = "",
    val success: Boolean
)

@Keep
data class KioskState(
    val kioskEnable: String? = "true",
    val openType: String? = "O",
    val sellerInfo: SellerInfo? = null,
)

@Keep
data class SellerInfo(
    val addressInfo: String? = null,    // 주소
    val storeTelNumber: String? = null, // 전화
    val storeName: String? = null,      // 상호
    val businessNumber: String? = null, // 사업자번호
    val ceoName: String? = null,        // 대표자
)

@Keep
data class ResPayCancel(
    val data: PaymentInfo,
    val message: String? = "",
    val success: Boolean
)

@Keep
data class PaymentInfo(
    val AuthCode: String? = null,
    val fnCd: String? = "",
    val fnCd1: String? = "",
    val fnName: String? = "",
    val fnName1: String? = "",
    val goodsAmt: String? = "",
    val mid: String? = "",
    val orderDate: String? = null,
    val orderMenu: List<OrderMenu>? = null,
    val orderNumber: String? = "",
    val storeOrderId: String? = "",
    val tid: String? = "",
    val totalIndex: String? = ""
)

@Keep
data class OrderMenu(
    val add: String? = null,
    val ess: String? = null,
    val orderCount: String? = null,
    val orderMenuPacking: String? = null,
    val orderPrice: String? = null,
    val productID: String? = null,
    val productName: String? = null
)
@Keep
data class ResMenuInfo(
    val resMenuCategory: ResMenuCategory,
    val message: String? = "",
    val success: Boolean
)

//전자 메뉴 옵션 화면 생성용
@Keep
data class ResMenuCategory (
    var storeId: String? = null,
    var storeName: String? = null,
    var storeImage: String? = null,
    var menuType: String? = null,
    var storeBackground: String? = null,
    var categoryNum: String? = null,
    var unit: String = "KRW",
    var categoryObjectList: List<CategoryObject>? = null
)

@Keep
data class CategoryObject (
    var seq: String? = null,
    var name: String? = null,
    var image: String? = null,
    var menuNum: String? = null,
    var menuObjectList: List<MenuObject>? = null
)

@Keep
data class MenuObject (
    var productId: String? = null,
    var seq: String? = null,
    var name: String? = null,
    var price: String? = null,
    var imageFile: String? = null,
    var description: String? = null,
    var popular:String? = "false",
    var newMenu:String? = "false",
    var soldOut:String? = "false",
    var optMenusList: List<MenuOptionData>? = ArrayList()
)

@Keep
data class MenuOptionData (
    var menuOptName: String? = "",
    var menuGubun: String? = "",
    var menuOptSeq: String? = "",
    var optionList: List<SubOption>? = ArrayList()
)

@Keep
data class SubOption(
    var optionName: String? = "",
    var price: String? = ""
)


// 광고 정보
@Keep
data class ResBannerInfo(
    val data: List<BannerInfo>? = null,
    val message: String? = "",
    val success: Boolean
)

@Keep
data class BannerInfo(
    var bannerList: List<DownloadFiles>? = null,
    var e_menu_type: Boolean = false,
) {
    fun toLocalize() = this.bannerList?.map { it.toLocalizeUrl() }

}

@Keep
data class DownloadFiles(
    val local_filename: String = "",
    val uid: String = "",
    var url: String = "",
    val file_size: Long = 0L,
) {
    fun toLocalizeUrl() {
        val filePath = File(BANNER_FILE_DIRECTORY).absolutePath + File.separator
        this.url = filePath + local_filename
    }
}

@Keep
data class ResPrintMenu(
    val data: KioskState,
    val message: String? = "",
    val success: Boolean
)