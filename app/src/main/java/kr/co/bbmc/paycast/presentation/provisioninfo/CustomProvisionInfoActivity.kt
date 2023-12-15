package kr.co.bbmc.paycast.presentation.provisioninfo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.collectLatest
import kr.co.bbmc.paycast.ACTION_PAY_ORDER
import kr.co.bbmc.paycast.INTENT_REFILL_NUM
import kr.co.bbmc.paycast.mTelephone
import kr.co.bbmc.paycast.presentation.paymentKicc.CustomPaymentKiccActivity
import kr.co.bbmc.paycast.ui.component.theme.AdNetTheme
import kr.hstar.commonutil.delayRun
import kr.hstar.commonutil.repeatOnState

class CustomProvisionInfoActivity: AppCompatActivity() {

    private val vm: ProvisionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            run {
                AdNetTheme {
                    Surface {
                        ProvisionInfoScreen(vm)
                    }
                }
            }
        }
        initData()
        observerData()
    }

    private fun observerData() {
        repeatOnState(Lifecycle.State.STARTED) {
            vm.phoneNumber.collectLatest {
                Logger.w("Phone number changed - ${it.text}")
            }
        }

        repeatOnState(Lifecycle.State.STARTED) {
            vm.agreePrivacyCollection.collectLatest {
                Logger.w("Phone agreePrivacyCollection changed - $it")
            }
        }

        with(vm) {
            startActivity.observe(this@CustomProvisionInfoActivity) {
                Logger.w("START PAYMENT : $it")
                startLauncher(it)
            }

            toast.observe(this@CustomProvisionInfoActivity) {
                Logger.i("Toast : $it")
                Toast.makeText(this@CustomProvisionInfoActivity, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startLauncher(it: ProvisionViewModel.ProvisionLaunchType?) {
        if(it != ProvisionViewModel.ProvisionLaunchType.FINISH) {
            val intent = Intent(this, CustomPaymentKiccActivity::class.java)
            intent.putExtra("paOrderId", "-1")
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
            intent.action = ACTION_PAY_ORDER
            mTelephone = when(it) {
                ProvisionViewModel.ProvisionLaunchType.SKIP -> {
                    setResult(RESULT_CANCELED)
                    ""
                }
                else -> {
                    setResult(RESULT_OK)
                    vm.phoneNumber.value.text.replace("-", "")
                }
            }
            Logger.w("mTelephone : $mTelephone")
            startActivity(intent)
        }
        delayRun({finish()}, 500)
    }

    private fun initData() {
        val refillNum = intent?.getStringExtra(INTENT_REFILL_NUM)
        refillNum?.let { vm.savePhoneNumber(TextFieldValue(it)) } ?:
        run { if(mTelephone.isNotEmpty()) vm.savePhoneNumber(TextFieldValue(mTelephone)) }
        vm.countPopup()
    }

}