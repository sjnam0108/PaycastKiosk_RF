package kr.co.bbmc.paycast.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.orhanobut.logger.Logger
import kr.co.bbmc.paycast.bundleSharedFlow
import kr.hstar.commonutil.goAsync
import kr.co.bbmc.selforderutil.SingCastPlayIntent

class OnPlayerCmdReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = goAsync {
        intent.let { intent ->
            if (intent.action == SingCastPlayIntent.ACTION_PLAYER_COMMAND) {
                val bundle: Bundle? = intent.extras
                bundle?.let {
                    bundleSharedFlow.emit(it)
                } ?: Logger.w("Bundle is Null")

            }
        }
    }

}