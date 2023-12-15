package kr.co.bbmc.paycast.receiver

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import kotlinx.coroutines.FlowPreview
import kr.co.bbmc.paycast.presentation.main.MainKioskActivity

@ExperimentalFoundationApi
class OnBootReceiver : BroadcastReceiver() {
    @OptIn(FlowPreview::class)
    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            // 부팅을 통한 앱 구동 플래그 설정
            val i = Intent(context, MainKioskActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 1, i, PendingIntent.FLAG_UPDATE_CURRENT)
            try {
                pendingIntent.send()
            } catch (e: CanceledException) {
                e.printStackTrace()
            }
        }
    }
}