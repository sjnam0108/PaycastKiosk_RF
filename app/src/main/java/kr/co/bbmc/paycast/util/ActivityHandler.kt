package kr.co.bbmc.paycast.util

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.orhanobut.logger.Logger
import kr.co.bbmc.paycast.handlerMsgSubject

class ActivityHandler : Handler(Looper.getMainLooper()) {
    override fun handleMessage(data: Message) {
        Logger.i("Activity Handler Status : ${data.what} // arg1: ${data.arg1} // arg2: ${data.arg2}")
        handlerMsgSubject.onNext(Triple(data.what, data.arg1, data.arg2))
    }
}