package kr.co.bbmc.paycast.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.orhanobut.logger.Logger
import kr.hstar.commonutil.goAsync

@Suppress("DEPRECATION")
class UsbPrintReceiver : BroadcastReceiver() {
    // printer intent action
    private val actionUsbPermission = "com.android.example.USB_PERMISSION"

    override fun onReceive(context: Context, intent: Intent) = goAsync {
        if (actionUsbPermission == intent.action) {
            synchronized(this) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    device?.apply {
                        //call method to set up device communication
                        Logger.e("Usb printer connected!! = ${this.manufacturerName}")
                    }
                } else {
                    Logger.d("permission denied for device $device")
                }
            }
        }
    }

}