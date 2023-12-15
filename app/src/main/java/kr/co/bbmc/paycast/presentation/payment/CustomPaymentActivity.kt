package kr.co.bbmc.paycast.presentation.payment

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.core.view.WindowCompat
import com.orhanobut.logger.Logger
import kr.co.bbmc.paycast.ui.component.theme.AdNetTheme

// 사용안함 - paymentKicc 화면을 사용
class CustomPaymentActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.w("PaymentActivity : onCreate!!")
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            run {
                AdNetTheme {
                    Surface {
                        //PaymentScreen()
                    }
                }
            }
        }
    }
}