package kr.co.bbmc.paycast.printer

import com.sam4s.usb.driver.UsbPrinterPort
import com.sam4s.usb.driver.UsbPrinterProber

class USBPrinterInfo(val mUsbPrinterPort: UsbPrinterPort): PrinterInfo() {
    init {
        printername = "USB_PRINTER"
        type = TYPE_USB
    }

    fun getPort():UsbPrinterPort {
        return mUsbPrinterPort
    }

    override fun getTitle():String {
        val device = mUsbPrinterPort.driver.device
        return String.format("Vendor 0x%04X Product 0x%04X", device.vendorId, device.productId)
    }

    override fun getSubTitle():String {
        return mUsbPrinterPort.driver.javaClass.simpleName
    }

    override fun is203dpi():Boolean {
        return UsbPrinterProber.is203dpi_supprot(mUsbPrinterPort.driver.device)
    }
}