package kr.co.bbmc.paycast.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kr.co.bbmc.selforderutil.PropUtil
import kr.co.bbmc.selforderutil.StbOptionEnv

fun baseExceptionHandler(callback: ((String) -> Unit)? = null): CoroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
    val error = runCatching { e.message ?: "Uncaught Error" }.getOrDefault("Error")
    callback?.invoke(error)
}

fun reportServerSyncV2(
    completeY: String,
    completeKY: String,
    model: StbOptionEnv,
    prop: PropUtil?,
    c: Context?
): String? {
    return reportServerSyncV2(completeY, "|", completeKY, "|", model, prop, c)
}

fun reportServerSyncV2(
    completeY: String,
    completeN: String,
    completeKY: String,
    completeKN: String,
    model: StbOptionEnv,
    prop: PropUtil?,
    c: Context?
): String? {
    return if (completeY == "|" && completeN == "|" && completeKY == "|" && completeKN == "|") {
        null
    } else {
        val postString = StringBuilder()
        postString.append("storeId=" + model.storeId + "&")
        Log.e("ServerReqUrl", "completeY=$completeY")
        if (completeY != "|") {
            postString.append("completeY=$completeY&")
        }
        if (completeN != "|") {
            postString.append("completeN=$completeN&")
        }
        if (completeKY != "|") {
            postString.append("completeKY=$completeKY&")
        }
        if (completeKN != "|") {
            postString.append("completeKN=$completeKN")
        }
        var tmp = String.format("%s", postString.toString())
        tmp = if (tmp.endsWith("&")) tmp.substring(0, tmp.length - 1) else tmp
        tmp
    }
}