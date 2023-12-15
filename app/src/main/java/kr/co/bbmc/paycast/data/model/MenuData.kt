package kr.co.bbmc.paycast.data.model

data class MenuCatagoryObject(
    var storeId: String = "",
    var storename: String = "",
    var storeImage: String = "",
    var menutype: String = "",
    var storebackground: String = "",
    var catagorynum: String? = "",
    var unit: String = "KRW",
    var catagoryObjectList: ArrayList<CatagoryObject> = arrayListOf()
) {

}

data class CatagoryObject(
    var seq: String? = null,
    var name: String? = null,
    var image: String? = null,
    var menuNum: String? = null,
    var menuObjectList: ArrayList<MenuObject> = arrayListOf()
) {

}

data class MenuObject(
    var productId: String = "",
    var seq: String = "",
    var name: String = "",
    var price: String = "",
    var imagefile: String = "",
    var description: String = "",
    var popular: String = "false",
    var newmenu: String = "false",
    var soldout: String = "false",
    var refill: String = "none",
    var discount: Float = 0F,
    var code: String = "",          // 상품코드
    var plusMenu: Boolean = true,   // 1:1 행사상품 아래로 변경
    var groupName: String? = null,  // 플러스 행사상품 그룹명 -> 조회함수 추가 -> MenuCatagoryObject에서부터 조회해야하기 때문에 클라입장에선 불편함.. 애초에 받아올때 그룹으로 미리 만들어서 사용해야함.
    //var plusItem: PlusGroupItem? = null,
    var optMenusList: ArrayList<MenuOptionData> = arrayListOf()
) {

}

data class PlusGroupItem(
    var groupId: Int,
    var groupName: String = "특별기확전",
    var salesList: ArrayList<MenuObject> = arrayListOf() // 또는 ArrayList<String> : 행사 상품의 productId들의 배열
) {

}

data class MenuOptionData(
    var menuOptId: String = "",
    var menuOptName: String = "",
    var menuGubun: String = "", // 0: 필수, 1: 옵션
    var menuOptSeq: String = "",
    var optList: ArrayList<CustomOptionData> = arrayListOf()
) {

}

data class CustomOptionData(
    var reqTitle: String = "",
    var requiredOptList: ArrayList<CustomRequiredOptionData?> = arrayListOf(),
    var addTitle: String = "",
    var addOptList: ArrayList<CustomAddedOptionData?> = arrayListOf(),
    var refillCount: Int = 0
) { }

data class CustomRequiredOptionData(
    var optId: String = "",
    var optMenuName: String = "",
    var optMenuPrice: String = "",
) { }

data class CustomAddedOptionData(
    var optId: String = "",
    var optMenuName: String = "",
    var optMenuPrice: String = "",
) { }

class DataModel {
    var productId: String
    var text: String
    var drawable: String
    var color: String
    var itemprice: String
    var price: String? = null
    var count = 1
    var refill = false
    var popular = false
    var newmenu = false
    var soldoutmenu = false
    var isPackage = false
    var rfcount = ""
    var paOrdId = ""
    var rfTel = ""
    var description = ""
    var optionList: ArrayList<CustomOptionData> = arrayListOf()
    var plusItem: MenuObject? = null
    var plusOptionList: ArrayList<CustomOptionData> = arrayListOf()
    var code = ""

    constructor(
        pId: String,
        t: String,
        d: String,
        c: String,
        p: String,
        pop: Boolean,
        nm: Boolean,
        sm: Boolean,
        rm: Boolean,
        cnt: Int,
        plus: MenuObject?,
        plusOptions: ArrayList<CustomOptionData>,
    ) {
        productId = pId
        text = t
        drawable = d
        color = c
        itemprice = p
        count = cnt
        popular = pop
        newmenu = nm
        soldoutmenu = sm
        refill = rm
        plusItem = plus
        plusOptionList = plusOptions
    }

    constructor(
        pId: String,
        t: String,
        d: String,
        c: String,
        p: String,
        cnt: Int,
        oList: ArrayList<CustomOptionData>,
        plus: MenuObject?,
        plusOptions: ArrayList<CustomOptionData>,
    ) {
        productId = pId
        text = t
        drawable = d
        color = c
        itemprice = p
        count = cnt
        popular = false
        newmenu = false
        soldoutmenu = false
        optionList = oList
        plusItem = plus
        plusOptionList = plusOptions
    }

    constructor(
        pId: String,
        t: String,
        d: String,
        c: String,
        p: String,
        itemcount: Int,
        des: String
    ) {
        productId = pId
        text = t
        drawable = d
        color = c
        itemprice = p
        count = itemcount
        popular = false
        newmenu = false
        soldoutmenu = false
        description = des
    }

    constructor(
        pId: String,
        t: String,
        d: String,
        c: String,
        p: String,
        itemcount: Int,
        pop: Boolean,
        nm: Boolean,
        des: String,
    ) {
        productId = pId
        text = t
        drawable = d
        color = c
        itemprice = p
        count = itemcount
        popular = pop
        newmenu = nm
        soldoutmenu = false
        description = des
    }

    constructor() {
        productId = ""
        text = ""
        drawable = ""
        color = ""
        itemprice = ""
        price = ""
        count = 1
        popular = false
        newmenu = false
        soldoutmenu = false
        isPackage = false
        description = ""
        optionList = arrayListOf()
        plusItem = null
        plusOptionList = arrayListOf()
    }

    override fun toString(): String {
        return "productId: $productId, text: $text, count: $count, price: $price, optionList: $optionList, plusItem: $plusItem, plusOptionList: $plusOptionList"
    }
}