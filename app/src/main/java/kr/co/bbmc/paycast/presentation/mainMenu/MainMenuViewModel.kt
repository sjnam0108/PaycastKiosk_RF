package kr.co.bbmc.paycast.presentation.mainMenu

import android.content.pm.PackageManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kr.co.bbmc.paycast.*
import kr.co.bbmc.paycast.App.Companion.APP
import kr.co.bbmc.paycast.data.model.*
import kr.co.bbmc.paycast.network.model.PaymentInfo
import kr.co.bbmc.paycast.presentation.dialog.model.DlgInfo
import kr.co.bbmc.paycast.ui.component.theme.ButtonType
import kr.co.bbmc.paycast.util.baseExceptionHandler
import kr.co.bbmc.paycast.util.getPackageInfoCompat
import kr.co.bbmc.paycast.util.mutation
import kr.co.bbmc.paycast.util.totalPrice

@FlowPreview
class MainMenuViewModel : BaseViewModel() {

    // 내부 저장소 ( 관리자 페이지 진입용 비밀 번호 저장소 )
    private val prefManager = APP.dataStore

    // Custom New Lock Screen visible 처리
    private val _showNewPasswordScreen = MutableLiveData(false)
    val showNewPasswordScreen = _showNewPasswordScreen
    fun showNewPasswordScreen(show: Boolean) = _showNewPasswordScreen.postValue(show)

    // Custom New Lock Screen 용 신규 비밀 번호, 신규 비밀 번호 확인용
    private val _newPassword = MutableStateFlow(TextFieldValue())
    val newPassword = _newPassword.asStateFlow()
    fun saveNewPassword(pw: TextFieldValue) { _newPassword.value = pw }

    private val _chkNewPassword = MutableStateFlow(TextFieldValue())
    val chkNewPassword = _chkNewPassword.asStateFlow()
    fun saveChkNewPassword(pw: TextFieldValue) { _chkNewPassword.value = pw }

    fun isSamePassword(newPw: String) = viewModelScope.launch(Dispatchers.IO) {
        // 새 비밀 번호와 확인 비밀 번호가 같은지 검증 됐으니 파라 미터로 하나의 비밀 번호만 받아 왔음
        // DataStore 에 넣어 주면 됨
        prefManager.setPassword(newPw)
    }

    // Custom Lock Screen 에서 입력 하는 Password 변수
    private val _inputPassword = MutableStateFlow(TextFieldValue())
    val inputPassword = _inputPassword.asStateFlow()
    fun saveInputPassword(pw: TextFieldValue) {
        _inputPassword.value = pw
    }

    // TextField 에 입력한 비밀 번호가 맞는지 검증 - CustomLockScreen 을 불러올 때 값을 초기화 해줌
    private val _rightPassword = MutableLiveData(false)
    val rightPassword = _rightPassword

    fun rightPassword(right: Boolean) = _rightPassword.postValue(right)

    // TextField 에 입력한 password 와 데이터 스토어 에 저장된 password 가 같은지 체크
    fun passwordMatch(pw: String) = viewModelScope.launch(Dispatchers.IO) {
        rightPassword(pw == prefManager.readPassword())
    }

    // 관리자 페이지 진입 시 pw 입력 하는 화면 visible
    private val _enterLockScreen = MutableLiveData(false)
    val enterLockScreen = _enterLockScreen
    fun enterLockScreen(enter: Boolean) = _enterLockScreen.postValue(enter)

    //storeName 우측 롱클릭 하면 setting dlg 표시
    private val _showSettingDlg = MutableLiveData(false)
    val showSettingDlg = _showSettingDlg
    fun showSettingDlg(show: Boolean) = _showSettingDlg.postValue(show)

    private val _showOptionDlg = MutableStateFlow(false)
    val showOption = _showOptionDlg
    fun showOption(show: Boolean) = _showOptionDlg.tryEmit(show)

    // 행사상품 관련 처리 1+1
    private val _showPlusDlg = MutableStateFlow(false)
    val showPlusDlg = _showPlusDlg
    fun showPlusDlg(show: Boolean) = _showPlusDlg.tryEmit(show)

    // plus option dialog
    private val _showPlusOptDlg = MutableStateFlow(false)
    val showPlusOptDlg = _showPlusOptDlg
    fun showPlusOptDlg(show: Boolean) = _showPlusOptDlg.tryEmit(show)

    // 선택된 행사 상품 Item
    private val _plusSelectedItem = MutableStateFlow(MenuObject())
    val plusSelectedItem = _plusSelectedItem
    fun setPlusItem(item: MenuObject) = _plusSelectedItem.tryEmit(item)

    // 현재 메뉴정보
    val currentMenus = menuListSubject.share()

    // 초기 화면 메뉴 데이터
    private val _menuData = MutableLiveData<List<CatagoryObject>>()
    val menuData: LiveData<List<CatagoryObject>> = _menuData
    fun setMenus(data: List<CatagoryObject>) = _menuData.postValue(data)

    // 옵션화면 정보 표시용 데이터
    private val _optionData = MutableStateFlow(MenuObject())
    val optionData = _optionData
    fun setOption(item: MenuObject) = _optionData.tryEmit(item)

    // Plus 옵션화면 정보 표시용 데이터
    private val _optionPlusData = MutableStateFlow(MenuObject())
    val optionPlusData = _optionPlusData
    fun setPlusOption(item: MenuObject) = _optionPlusData.tryEmit(item)

    // 행사상품 정보 표시용 데이터
    private val _plusItem = MutableStateFlow(MenuObject() to listOf<MenuObject>())
    val plusItem = _plusItem
    private fun setPlusItem(item: Pair<MenuObject, List<MenuObject>>) = _plusItem.tryEmit(item)

    private val _isPackage = MutableLiveData(false)
    val isPackage = _isPackage

    private val _init = MutableStateFlow(false)
    val init = _init
    fun setInit(init: Boolean) = _init.tryEmit(init)

    // 선택된 메뉴 리스트 표시를 위한 데이터
    private val _selectedOrderGroup = MutableLiveData<List<List<DataModel>>>()
    val selectedOrderGroup: LiveData<List<List<DataModel>>> = _selectedOrderGroup
    fun setOrderGroup() {
        _selectedOrderGroup.value = _selectedOrders.value?.groupBy { md ->
            val optionData = md.optionList.map {
                val sortedAddOpts = it.addOptList.sortedBy { opt -> opt?.optMenuName }
                sortedAddOpts to it.requiredOptList
            }
            val plusData = md.plusItem?.let { menu ->
                val plusData = md.plusOptionList.map {
                    val sortedPlusOpt = it.addOptList.sortedBy { opt -> opt?.optMenuName }
                    sortedPlusOpt to it.requiredOptList
                }
                menu.name + plusData
            } ?: ""
            md.text + optionData + plusData
        }?.map { it.value }
    }

    fun mergeOrder() {
        mOrderList.clear()
        _selectedOrderGroup.value?.forEach {
            val newItem = DataModel(
                pId = it[0].productId,
                t = it[0].text,
                d = it[0].drawable,
                c = "#000000",
                p = it[0].price ?: "0",
                cnt = it.size,
                oList = it[0].optionList,
                plus = it[0].plusItem,
                plusOptions = it[0].plusOptionList
            )
            mOrderList.add(newItem)
        }
    }

    private val _totalPrice = MutableLiveData<Long>(0)
    val totalPrice: LiveData<Long> = _totalPrice
    fun setTotalPrice() { _totalPrice.value = _selectedOrders.value.totalPrice() }

    private val _selectedOrders = MutableLiveData<List<DataModel>>()
    val selectedOrders = _selectedOrders
    fun addOrders(menuItem: DataModel) {
        _selectedOrders.value = _selectedOrders.value?.plus(menuItem) ?: listOf(menuItem)
        countPopup()
    }

    // 결제 취소 번호
    private val _cancelPaymentCode = MutableStateFlow(TextFieldValue())
    val cancelPaymentCode = _cancelPaymentCode.asStateFlow()
    fun saveCancelCode(code: TextFieldValue) {
        _cancelPaymentCode.value = code
    }

    fun removeOrders(menuItem: DataModel) {
        _selectedOrders.value = _selectedOrders.value?.minus(menuItem) ?: listOf(menuItem)
        countPopup()
    }

    fun clearOrders(menuItem: List<DataModel>) {
        _selectedOrders.value = (_selectedOrders.value?.minus(menuItem.toSet()) ?: listOf(menuItem)) as List<DataModel>?
        countPopup()
    }

    fun clearAllOrders() {
        _selectedOrders.value = emptyList()
        mOrderList = arrayListOf()
        mTelephone = ""
        setItemPackage(false)
    }

    fun countPopup() {
        if (_selectedOrders.value.isNullOrEmpty()) {
            Logger.w("selected order is empty")
            return
        }
        countTimer(BASE_WAITING_TIME) {
            sendToast("장시간 입력이 없어서 주문이 취소 됩니다.")
            clearAllOrders()
        }
    }

    private fun MenuObject.addOrdersToDataModel() {
        val b: Boolean = this.popular.equals("true", ignoreCase = true)
        val n: Boolean = this.newmenu.equals("true", ignoreCase = true)
        val s: Boolean = this.soldout.equals("true", ignoreCase = true)
        val r: Boolean = if (this.refill.equals("infinity", ignoreCase = true)) //무제한 리필
            true else this.refill.equals("limit", ignoreCase = true)
        // 실제 선택된 옵션 정보
        val optList: ArrayList<CustomOptionData> = ArrayList(customOptions.value ?: emptyList())
        // 행사상품의 추가 옵션 정보
        val plusOptList: ArrayList<CustomOptionData> = ArrayList(customOptionsV2.value ?: emptyList())
        // 행사 상품의 정보
        val plusItem = plusSelectedItem.value
        val plus = if (plusItem.productId.isEmpty()) { null } else plusItem
        val targetItem = if (optList.isEmpty()) {
            DataModel(
                this.code,
                this.name,
                this.imagefile,
                "#000000",
                this.price,
                b,
                n,
                s,
                r,
                1,
                plus,
                plusOptList
            )
        } else {
            DataModel(
                this.code,
                this.name,
                this.imagefile,
                "#000000",
                this.price,
                1,
                optList,
                plus,
                plusOptList
            )
        }
        addOrders(targetItem)
        clearMenuData()
    }

    fun clearMenuData() {
        clearOptions()
        clearPlusOptions()
        setPlusItem(MenuObject())
    }

    fun setItemPackage(checked: Boolean) {
        Logger.w("setItemPackage = $checked")
        _isPackage.value = checked
        _selectedOrders.mutation {
            it.value?.forEach { item -> item.isPackage = checked }
        }
        Logger.e("SetPackage result(${_isPackage.value}) : ${_selectedOrders.value?.map { it.text to it.isPackage }}")
    }

    //추가 옵션 아이템
    private val customOptions = MutableLiveData<List<CustomOptionData>>()
    private val tempOrderOptions = MutableLiveData<List<CustomAddedOptionData>>()
    private val tempRequiredOption = MutableLiveData<MutableList<CustomRequiredOptionData?>>()
    private val tempRequiredSingleOpt = MutableLiveData<CustomRequiredOptionData>()
    fun tempAddOptions(menuItem: CustomAddedOptionData) { tempOrderOptions.value = tempOrderOptions.value?.plus(menuItem) ?: listOf(menuItem) }
    fun tempRemoveAddOptions(menuItem: CustomAddedOptionData) { tempOrderOptions.value = tempOrderOptions.value?.minus(menuItem) ?: listOf(menuItem) }

    // 필수옵션 설정 데이터
    private lateinit var setRequiredOptionList: MutableList<CustomRequiredOptionData?>
    fun setRequiredOptions(menuItem: CustomRequiredOptionData, index: Int) {
        tempRequiredSingleOpt.value = menuItem
        if (::setRequiredOptionList.isInitialized) {
            setRequiredOptionList[index] = menuItem
            tempRequiredOption.value = setRequiredOptionList
        } else {
            sendToast("오류가 발생했습니다.")
        }
    }

    fun setInitOptSize(size: Int) { setRequiredOptionList = MutableList(size) { null } }

    // 필수옵션이 모두 선택되었는지 확인하는 함수 - true : 미선택, false : 모두선택됨
    fun checkValidRequiredOption(): Boolean {
        if (_optionData.value.optMenusList.count { it.menuGubun == "0" } < 1) return false
        return tempRequiredOption.value.isNullOrEmpty()
    }

    // 현재 선택된 옵션 설정
    fun setCustomOptions() {
        val optionData = CustomOptionData()
        optionData.requiredOptList = ArrayList(tempRequiredOption.value ?: emptyList())
        optionData.addOptList = ArrayList(tempOrderOptions.value ?: emptyList())
        customOptions.value = customOptions.value?.plus(optionData) ?: listOf(optionData)
    }

    // 옵션정보 초기화
    fun clearOptions() {
        tempRequiredOption.value = mutableListOf()
        tempOrderOptions.value = emptyList()
        customOptions.value = emptyList()
    }

    // 추가 상품 정보 옵션
    private val customOptionsV2 = MutableLiveData<List<CustomOptionData>>()
    private val tempOrderOptionsV2 = MutableLiveData<List<CustomAddedOptionData>>()
    private val tempRequiredOptionV2 = MutableLiveData<MutableList<CustomRequiredOptionData?>>()
    private val tempRequiredSingleOptV2 = MutableLiveData<CustomRequiredOptionData>()
    fun tempPlusAddOptions(menuItem: CustomAddedOptionData) {
        tempOrderOptionsV2.value = tempOrderOptionsV2.value?.plus(menuItem) ?: listOf(menuItem)
    }

    fun tempRemovePlusAddOptions(menuItem: CustomAddedOptionData) {
        tempOrderOptionsV2.value = tempOrderOptionsV2.value?.minus(menuItem) ?: listOf(menuItem)
    }

    // 필수옵션 설정 데이터
    private lateinit var setRequiredOptionListV2: MutableList<CustomRequiredOptionData?>
    fun setPlusRequiredOptions(menuItem: CustomRequiredOptionData, index: Int) {
        tempRequiredSingleOptV2.value = menuItem
        if (::setRequiredOptionListV2.isInitialized) {
            setRequiredOptionListV2[index] = menuItem
            tempRequiredOptionV2.value = setRequiredOptionListV2
        } else {
            sendToast("오류가 발생했습니다.")
        }
    }

    fun setInitPlusOptSize(size: Int) {
        setRequiredOptionListV2 = MutableList(size) { null }
    }

    // 필수옵션이 모두 선택되었는지 확인하는 함수 - true : 미선택, false : 모두선택됨
    fun checkValidPlusRequiredOption(): Boolean {
        if ((_optionPlusData.value.optMenusList.count { it.menuGubun == "0" }) < 1) return false
        return tempRequiredOptionV2.value.isNullOrEmpty()
    }

    // 행사상품이 선택되어있는지 확인
    fun checkValidPlusSelectedItem(): Boolean = plusSelectedItem.value.productId.isEmpty()

    // 현재 선택된 옵션 설정
    fun setPlusCustomOptions() {
        val optionData = CustomOptionData()
        optionData.requiredOptList = ArrayList(tempRequiredOptionV2.value ?: emptyList())
        optionData.addOptList = ArrayList(tempOrderOptionsV2.value ?: emptyList())
        customOptionsV2.value = customOptionsV2.value?.plus(optionData) ?: listOf(optionData)
    }

    // 옵션정보 초기화
    fun clearPlusOptions() {
        tempRequiredOptionV2.value = mutableListOf()
        tempOrderOptionsV2.value = emptyList()
        customOptionsV2.value = emptyList()
    }

    // 주문 추가
    fun addMenus(menuItem: MenuObject) = menuItem.addOrdersToDataModel()

    // 상점이름
    private val _storeName = MutableLiveData<String>()
    val storeName = _storeName
    fun setStoreName(name: String) = _storeName.postValue(name)

    // 결제 화면 정보 : LaunchType(val type: Int) { Payment(0), Provision(1), Refill(2) }
    private val _startActivity = MutableLiveData<Int>()
    val startActivity = _startActivity
    private fun startLaunchActivity(type: Int) = _startActivity.postValue(type)

    // 결제 함수
    fun payment() {
        Logger.w("START Payment - $approveMoney")
        // 1. 결제금액 0원이하, 네트워크 끊김, 주문리스트가 없을때
        if (_selectedOrders.value?.isNotEmpty() != true || !networkStatus || approveMoney < 1) {
            Logger.w("payment Failed - empty check... : approveMoney - $approveMoney")
            sendToast("결제를 진행할수 없습니다. 주문을 다시한번 확인해주세요.")
            return
        }
        when (isKiccAppInstalled()) {
            true -> {
                startPay()
            }
            else -> {
                sendToast("KICC 결제앱이 설치되지 않았습니다. 설치후 이용해주세요.")
            }
        }
    }

    fun cancelPayment(): Job = viewModelScope.launch(Dispatchers.IO + baseExceptionHandler {
        Logger.e("Error : $it")
        sendToast("주문취소에 실패했습니다 : $it")
    }) {
        val code = cancelPaymentCode.value.text
        require(code.length > 3) { sendToast("취소번호가 유효하지 않습니다.") }
        Logger.e("Cancel code : $code")
        APP.repository.getCancelInfo(code)
            .retry(2)
            .collectLatest {
                require(it.success) { sendToast("주문내역을 가져오지 못했습니다 : ${it.message}") }
                requireNotNull(it.data) {
                    paymentCancelData = PaymentInfo()
                    sendToast("주문취소 내역을 가져오지 못했습니다.")
                }
                paymentCancelData = it.data
                startLaunchActivity(LaunchType.Cancel.type)
            }
    }

    private fun startPay() {
        when {
            (!networkStatus) -> {
                showDialog(true)
                setDlgInfo(
                    DlgInfo(
                        type = ButtonType.Single,
                        contentTitle = APP.getString(R.string.str_paycastdid_network_title),
                        contents = APP.getString(R.string.str_paycastdid_strength_network),
                        positiveCallback = { showDialog(false) })
                )
            }
            else -> {
                releaseTimer()
                mTranTypte = "D1"
                storeOrderId = ""
                if (APP.stbOpt!!.atEnable != null && APP.stbOpt!!.atEnable.isNotEmpty()) {
                    val type = if (APP.stbOpt!!.atEnable.equals("true", ignoreCase = true) && (mTelephone.isEmpty())
                    ) LaunchType.Provision.type else LaunchType.Payment.type
                    startLaunchActivity(type)
                } else {
                    // 결제화면 이동..
                    startLaunchActivity(LaunchType.Payment.type)
                }
            }
        }
    }

    private fun isKiccAppInstalled(): Boolean {
        val packageManager: PackageManager = APP.packageManager
        val pi = packageManager.getPackageInfoCompat(KICC_PKG_NAME)
        return pi?.applicationInfo?.processName.equals(KICC_PKG_NAME)
    }

    private fun getPlusMenuItem(item: MenuObject): List<MenuObject>? {
        return runCatching {
            menuData.value?.map {
                it.menuObjectList.filter { gr -> !gr.groupName.isNullOrEmpty() }
                    .filter { gr -> gr.groupName == item.groupName }
                //.filter { it.productId != item.productId }
            }?.flatten()
        }
            .onSuccess { Logger.w("Group Item List - ${it?.map { item -> item.name to item.price }}") }
            .onFailure { Logger.e("Get Group Item Failed - ${it.message}") }
            .getOrNull()
    }

    fun checkPlusMenuItem(item: MenuObject) {
        if (!item.groupName.isNullOrEmpty()) {
            val listOfGroupItem = getPlusMenuItem(item)
            listOfGroupItem?.let {
                if (it.isNotEmpty()) setPlusItem(item to it)
                else sendToast("행사상품을 가져올수 없습니다.")
            } ?: sendToast("행사상품을 가져올수 없습니다.")
            showPlusDlg(true)
            Logger.w("showPlusDlg!!")
        } else {
            Logger.d("Normal Item")
            addMenus(item)
            countPopup()
        }
    }

    override fun onCleared() {
        super.onCleared()
        releaseTimer()
    }

    // 웹 결제를 통해 결제된 주문목록들을 주방 프린터로 연결
    fun getPrintMenu(): Job = viewModelScope.launch(Dispatchers.IO + baseExceptionHandler {
        Logger.e("Error : $it")
        sendToast("프린터 정보 획득에 실패했습니다 : $it")
    }) {
        APP.repository.getPrintMenuInfo()
            .retry(2)
            .onCompletion {

            }
            .collectLatest {
                Logger.w("Get Print info : $it")
                //printKitchen(it)
            }
    }

    companion object {
        const val BASE_WAITING_TIME = 30L // 기본 대기 시간(30초)
    }
}
