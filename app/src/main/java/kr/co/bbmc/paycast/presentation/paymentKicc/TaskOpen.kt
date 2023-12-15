package kr.co.bbmc.paycast.presentation.paymentKicc

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.lvrenyang.io.USBPrinting
import com.orhanobut.logger.Logger

class TaskOpen(
    usb: USBPrinting?,
    usbManager: UsbManager?,
    usbDeviceList: Iterator<UsbDevice>?,
    context: Context?,
    callBack: ((Boolean) -> Unit)?

) : Runnable {
    var usb: USBPrinting? = null
    var usbManager: UsbManager? = null
    var usbDevice: UsbDevice? = null
    var context: Context? = null
    var usbDeviceList: Iterator<UsbDevice>? = null
    var callBack: ((Boolean) -> Unit)? = null

    init {
        this.usb = usb
        this.usbManager = usbManager
        this.usbDeviceList = usbDeviceList
        this.context = context
        this.callBack = callBack
    }

    override fun run() {
        Logger.e("START Run check - usb device!! ($)")
        var usbTarget: UsbDevice? = null
        while (usbDeviceList!!.hasNext()) {
            usbDevice = usbDeviceList!!.next()
            Logger.e("usbDevice.getProductName()= ${usbDevice!!.productName} // name : ${usbDevice!!.deviceName} // manufacturerName : ${usbDevice!!.manufacturerName} // ${usbDevice!!.productId} ")
            if (usbDevice != null && usbDevice!!.productName != null && usbDevice!!.productName == "USB-Serial Controller") {
                usbTarget = usbDevice
                break
            }
        }
        val bOpen = usb!!.Open(usbManager, usbTarget, context)
        Logger.e("USB TaskOpen() bOpen=$bOpen && usbDevice is ${usbTarget?.productName}")
        callBack?.invoke(bOpen)
    }
}