package kr.co.bbmc.paycast.printer

import android.content.Context
import android.hardware.usb.UsbManager
import com.orhanobut.logger.Logger
import com.sam4s.printer.Sam4sBuilder
import com.sam4s.usb.driver.UsbPrinterProber
import kotlinx.coroutines.FlowPreview
import kr.co.bbmc.paycast.App.Companion.APP
import java.util.*

fun Context.refreshDeviceList(): MutableList<USBPrinterInfo> {
    val drivers = UsbPrinterProber.getDefaultProber()
        .findAllDrivers(Objects.requireNonNull<Any>(getSystemService(Context.USB_SERVICE)) as UsbManager)

    val result: MutableList<USBPrinterInfo> = ArrayList()
    for (driver in drivers) {
        val ports = driver.ports
        Logger.e("Print - ${driver.device.deviceName}")
        for(p in ports)
           result.add(USBPrinterInfo(p))
    }

    return result
}

@FlowPreview
fun Context.connectUsbPrinter(): String {
    val conn = APP.mPrinterConnection
    conn?.ClosePrinter()
    val  info = this.refreshDeviceList().firstOrNull()  // Usb printer info

    info?.let {
        val  connection = USBConnection(this)
        connection.setUsbPort(info.getPort())
        val connected = connection.OpenPrinter()
        Logger.w("trying print connect : $connected")
        APP.mPrinterConnection = connection
    }

    Logger.w("APP.mPrinterConnection  = ${APP.mPrinterConnection?.mSam4sPrinter?.printerName}")
    return info?.printername ?: ""
}

@FlowPreview
fun printTest() {
    var builder: Sam4sBuilder? = null
    //create builder
    //Builder
    //create builder
    //Builder
    builder = Sam4sBuilder("USB_PRINTER", Sam4sBuilder.LANG_KO)
    with(builder) {
        addTextLang(Sam4sBuilder.LANG_EN)
        addTextFont(Sam4sBuilder.FONT_A)
        addTextSize(2,2)
        addText("Hi - ")
        addText("Bye!!")
        addTextLineSpace(10)
        addText("Hey")
        addText("WTF!!")
        addCut(Sam4sBuilder.CUT_FEED)
    }

    //send builder data
    try {
        //cl_Menu.mPrinter.sendData(builder);
        Logger.w("start print!! $builder")
        APP.mPrinterConnection?.sendData(builder)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    //remove builder
    builder = try {
        builder.clearCommandBuffer()
        null
    } catch (e: Exception) {
        null
    }
}