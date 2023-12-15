package kr.co.bbmc.paycast.printer

import android.content.Context
import com.orhanobut.logger.Logger
import com.sam4s.printer.Sam4sBuilder
import com.sam4s.usb.driver.UsbPrinterPort
import com.sam4s.usb.util.CommunicationManager

class USBConnection(mContext: Context):PrinterConnection(mContext) {
    private var mUsbPort: UsbPrinterPort? = null
    private var mManager: CommunicationManager? = null

    init {
        device_type = DEVTYPE_USB
    }

     fun setUsbPort(port: UsbPrinterPort?): USBConnection {
        mUsbPort = port
        return this
    }

    override fun OpenPrinter(): Boolean {
        if (mUsbPort == null) return false
        mManager = CommunicationManager(mContext, mUsbPort)
        return mManager!!.open()
    }

    override fun ClosePrinter() {
        if (mManager != null) {
            mManager!!.close()
            mManager = null
        }
    }

    override fun IsConnected(): Boolean {
        return if (mManager != null) mManager!!.isConnected else false
    }

    override fun sendData(builder: Sam4sBuilder?): Int {
        var iret = -1
        try {
            if (mManager != null) {
                iret = mManager!!.write(builder!!.dataOutputStreamEx.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return iret
    }

    override fun ReceiveData(): String? {
        var result: String? = null
        if (mManager != null) {
            val data = mManager!!.read()
            if (data != null) {
                result = String(data)
            }
        }
        return result
    }

    override fun getPrinterName(): String? {
        var result: String? = null
        try {
            if (mManager != null) {
                result = mManager!!.printerName
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Logger.d("USBConnection : getPrinterName : $result")
        return result
    }

    override fun getPrinterStatus(): String? {
        var result: String? = null
        var iret: Int
        try {
            if (mManager != null) {
                val res = mManager!!.printerStatus
                if (res != null) when (res[0] +0) {
                    0x12 -> result = "Printer Ready"
                    0x56, 0x76 -> result = "Cover Open"
                    0x72 -> result = "No Paper"
                }

                //result = new String(res);
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Logger.w("USBConnection: getPrinterStatus is $result")
        return result
    }

}