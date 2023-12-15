package kr.co.bbmc.paycast.presentation.mainMenu.model

data class ResPrintInfo(
    val addrDetail: String,
    val amt: String,
    val bookingTime: String,
    val cancel: String,
    val date: String,
    val deliMsg: String,
    val deliPrice: Any,
    val name: String,
    val number: String,
    val orderMenu: List<OrderMenu>,
    val orderTable: Any,
    val orderType: String,
    val payment: String,
    val recommand: String,
    val roadAddr: String,
    val storeMsg: String,
    val tel: String,
    var printOk: Boolean = false,
)

data class OrderMenu(
    val add: String,
    val cnt: String,
    val code: Any,
    val ess: String,
    val id: String,
    val name: String,
    val packing: String,
    val price: String,
)