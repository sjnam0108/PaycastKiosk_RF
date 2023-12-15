package kr.co.bbmc.paycast.presentation.provisioninfo

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import com.orhanobut.logger.Logger
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.co.bbmc.paycast.BaseViewModel
import kr.co.bbmc.paycast.util.checkValidPhoneNumber

@OptIn(FlowPreview::class)
class ProvisionViewModel: BaseViewModel() {

    private val _phoneNumber = MutableStateFlow(TextFieldValue())
    val phoneNumber = _phoneNumber.asStateFlow()
    fun savePhoneNumber(tel: TextFieldValue) { _phoneNumber.value = tel }

    private val _agreePrivacyCollection = MutableStateFlow(false)
    val agreePrivacyCollection = _agreePrivacyCollection.asStateFlow()
    fun setAgreePrivacyCollection(agree: Boolean) = run { _agreePrivacyCollection.value = agree }

    // 결제 화면 정보 : LaunchType(val type: Int) { Payment(0), Provision(1), Refill(2) }
    private val _startActivity = MutableLiveData<ProvisionLaunchType>()
    val startActivity = _startActivity
    private fun startLaunchActivity(type: ProvisionLaunchType) = _startActivity.postValue(type)

    private fun checkValidation(): String? {
        return when {
            !checkValidPhoneNumber(_phoneNumber.value.text) -> "전화번호를 다시 입력해주세요."
            !_agreePrivacyCollection.value -> "개인정보 제3자 제공에 동의하셔야 합니다."
            else -> null
        }
    }

    fun skipAction() {
        startLaunchActivity(ProvisionLaunchType.SKIP)
    }

    fun startPayment() {
        val msg = checkValidation()
        msg?.let { sendToast(it) } ?: run { startLaunchActivity(ProvisionLaunchType.AGREE) }
    }

    fun countPopup() {
        countTimer(DISPLAY_DIALOG_TIMER) {
            Logger.w("Time over - finish!!")
            sendToast("장시간 입력이 없어 주문화면으로 돌아갑니다.")
            startLaunchActivity(ProvisionLaunchType.FINISH)
        }
    }

    enum class ProvisionLaunchType { SKIP, AGREE, FINISH }

    companion object {
        const val DISPLAY_DIALOG_TIMER = 60L
    }
}