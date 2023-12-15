@file:OptIn(FlowPreview::class, ExperimentalFoundationApi::class)

package kr.co.bbmc.paycast.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.lifecycle.*
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.*
import kr.co.bbmc.paycast.App
import kr.co.bbmc.paycast.data.model.DataModel
import kr.co.bbmc.paycast.receiver.ScAlarmReceiver
import kr.co.bbmc.paycast.service.MainServiceV2
import kr.co.bbmc.selforderutil.PlayerCommand
import java.text.SimpleDateFormat
import java.util.*

fun <T> SavedStateHandle.getOrDefault(
    key: String, defaultValue: T
) = this[key] ?: defaultValue

fun ActivityResultCaller.registerResultCode(action: (ActivityResult?) -> Unit) = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
    action(it)
}

fun <T> MutableLiveData<T>.mutation(actions: (MutableLiveData<T>) -> Unit) {
    actions(this)
    this.value = this.value
}

fun <T : Any> BehaviorSubject<T>.withIoThread(): Observable<T> = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

@SuppressLint("UnspecifiedImmutableFlag")
fun Activity.scheduleAlarm(interval: Long = (2 * 60 * 1000).toLong()) {
    val intent = Intent(this, ScAlarmReceiver::class.java)
    val pIntent = PendingIntent.getBroadcast(
        App.APP.applicationContext, ScAlarmReceiver.REQUEST_CODE,
        intent, PendingIntent.FLAG_UPDATE_CURRENT
    )
    val firstMillis = System.currentTimeMillis() // alarm is set right away
    val alarm = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, interval, pIntent)
}

@SuppressLint("UnspecifiedImmutableFlag")
fun Activity.cancelScheduleAlarm() {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val myIntent = Intent(applicationContext, ScAlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        applicationContext, ScAlarmReceiver.REQUEST_CODE, myIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
}

fun Activity.serviceBind(connection: ServiceConnection): Boolean {
    return bindService(
        Intent(this, MainServiceV2::class.java),
        connection,
        Context.BIND_AUTO_CREATE
    )
}

fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo? {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
        }
    }.getOrDefault(null)
}

@SuppressLint("SimpleDateFormat")
fun Context.sendBroadCastService(commandString: String, toService: Boolean = false) {
    Logger.e("sendBroadCastService($toService) : $commandString")
    val command = PlayerCommand()
    val currentTime = Calendar.getInstance().time
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    command.executeDateTime = simpleDateFormat.format(currentTime)
    command.requestDateTime = simpleDateFormat.format(currentTime)
    command.command = commandString
    val bIntent = sendPlayerCommand(command, toService)
    sendBroadcast(bIntent)
}

fun List<DataModel>?.sumOfOptionPrice(): Long {
    if (this == null) return 0L
    val totalOptionPrice = this.sumOf { it.sumOfOptions() }
    Logger.w("Total option price - $totalOptionPrice")
    return totalOptionPrice
}

fun DataModel?.sumOfOptions(): Long {
    if (this == null) return 0L
    val addOptPrice =
        this.optionList.sumOf { it?.addOptList?.sumOf { opt -> opt?.optMenuPrice?.toLong() ?: 0L } ?: 0L }
    val reqOptPrice = this.optionList.sumOf {
        it?.requiredOptList?.sumOf { opt -> opt?.optMenuPrice?.toLong() ?: 0L} ?: 0L
    }
    return addOptPrice + reqOptPrice
}

fun List<DataModel>?.sumOfTotalPrice(): Long {
    if (this == null) return 0L
    return this.sumOf { it.sumOfPrice() }
}

fun DataModel?.sumOfPrice(): Long {
    if (this == null) return 0L
    return runCatching { this.itemprice.toLong() }.getOrDefault(0L)
}

fun List<DataModel>?.totalPrice(): Long = this.sumOfTotalPrice() + this.sumOfOptionPrice()

fun List<String?>?.withSeparatorString(separator: String = ", "): String {
    var dest = ""
    this?.forEach { dest += it + separator }
    return dest.dropLast(separator.length).ifEmpty { "" }
}