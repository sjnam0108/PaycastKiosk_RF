@file:OptIn(FlowPreview::class)

package kr.co.bbmc.paycast.presentation.paymentKicc

import com.lvrenyang.io.Pos
import com.orhanobut.logger.Logger
import kotlinx.coroutines.FlowPreview
import kr.co.bbmc.paycast.*
import kr.co.bbmc.paycast.App.Companion.APP
import kr.co.bbmc.selforderutil.PaymentInfoData
import java.io.UnsupportedEncodingException

class TaskPrintV2(pos: Pos?) : Runnable {
    var pos: Pos? = null
    var paymentInfoData: PaymentInfoData? = null
    var tranType = ""
    var bPrintResult = -6
    var bPrintRun = false
    var bPrintEnd = false

    init {
        this.pos = pos
    }

    fun onStartPrint(pInfo: PaymentInfoData?, tran: String) {
        paymentInfoData = pInfo
        tranType = tran
        if (pInfo != null) {
            bPrintRun = true
        }
    }

    override fun run() {
        while (!bPrintRun) {
            if (bPrintEnd) return
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        val orderlist: ArrayList<*> = mOrderList
        Logger.w("-1. order Info - $mOrderList")
        Logger.w("0. print payment order - " + orderlist.size)
        if (tranType.equals("D4", ignoreCase = true)) //당일취소/전일취소(반품환불)
        {
            Logger.w("2. print cancel payment - " + orderlist.size)
            try {
                bPrintResult = ZalComPrint.CustomCancelPrintTicket(
                    APP.applicationContext,
                    pos,
                    nPrintWidth,
                    nPrintCount,
                    nPrintContent,
                    nCompressMethod,
                    orderlist,
                    paymentInfoData?.payAmount ?: "0",
                    APP.sellerInfo,
                    paymentInfoData,
                    mNumberOfOrder.toString(),
                    ISVERSION
                )
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            if (bPrintResult != 0) {
                try {
                    bPrintResult = ZalComPrint.CustomCancelPrintTicket(
                        APP.applicationContext,
                        pos,
                        nPrintWidth,
                        nPrintCount,
                        nPrintContent,
                        nCompressMethod,
                        orderlist,
                        paymentInfoData?.payAmount ?: "0",
                        APP.sellerInfo,
                        paymentInfoData,
                        waitOrderCount.toString(),
                        ISVERSION
                    )
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }
        } else if (tranType.equals("RF", ignoreCase = true)) //refill
        {
            Logger.w("1. print payment")
            try {
                bPrintResult = ZalComPrint.CustomRefillOrderPrintTicket(
                    APP.applicationContext,
                    pos,
                    nPrintWidth,
                    nPrintCount,
                    nPrintContent,
                    nCompressMethod,
                    orderlist,
                    paymentInfoData?.payAmount ?: "0",
                    APP.sellerInfo,
                    paymentInfoData,
                    waitOrderCount.toString(),
                    ISVERSION
                )
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            if (bPrintResult != 0) {
                try {
                    bPrintResult = ZalComPrint.CustomRefillOrderPrintTicket(
                        APP.applicationContext,
                        pos,
                        nPrintWidth,
                        nPrintCount,
                        nPrintContent,
                        nCompressMethod,
                        orderlist,
                        paymentInfoData?.payAmount ?: "0",
                        APP.sellerInfo,
                        paymentInfoData,
                        waitOrderCount.toString(),
                        ISVERSION
                    )
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }
        } else {
            Logger.w("Print start!! - $approveMoney // paymentInfoData : ${paymentInfoData?.payAmount}")
            try {
                bPrintResult = ZalComPrint.CustomReceiptPrintTicket(
                    APP.applicationContext,
                    pos,
                    nPrintWidth,
                    nPrintCount,
                    nPrintContent,
                    nCompressMethod,
                    orderlist,
                    approveMoney.toString(),
                    APP.sellerInfo,
                    paymentInfoData,
                    mNumberOfOrder.toString(),
                    ISVERSION
                )
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            if (bPrintResult != 0) {
                try {
                    bPrintResult = ZalComPrint.CustomReceiptPrintTicket(
                        APP.applicationContext,
                        pos,
                        nPrintWidth,
                        nPrintCount,
                        nPrintContent,
                        nCompressMethod,
                        orderlist,
                        paymentInfoData?.payAmount.toString(),
                        APP.sellerInfo,
                        paymentInfoData,
                        mNumberOfOrder.toString(),
                        ISVERSION
                    )
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }
            try {
                Logger.w("START custom orderPrint ticket!! - $approveMoney // paymentInfoData?.payAmount : ${paymentInfoData?.payAmount}")
                bPrintResult = ZalComPrint.CustomOrderPrintTicket(
                    APP.applicationContext,
                    pos,
                    nPrintWidth,
                    nPrintCount,
                    nPrintContent,
                    nCompressMethod,
                    orderlist,
                    paymentInfoData?.payAmount.toString(),
                    APP.sellerInfo,
                    paymentInfoData,
                    mNumberOfOrder.toString(),
                    ISVERSION
                )

            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            if (bPrintResult != 0) {
                try {
                    bPrintResult = ZalComPrint.CustomOrderPrintTicket(
                        APP.applicationContext,
                        pos,
                        nPrintWidth,
                        nPrintCount,
                        nPrintContent,
                        nCompressMethod,
                        orderlist,
                        paymentInfoData?.payAmount.toString(),
                        APP.sellerInfo,
                        paymentInfoData,
                        waitOrderCount.toString(),
                        ISVERSION
                    )
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }
        }
        Logger.e("Finish print")
        printEnd.tryEmit(true)
        bPrintRun = false
    }

    companion object {
        var nPrintWidth = 384
        var bCutter = true
        var bDrawer = false
        var bBeeper = true
        var nPrintCount = 1
        var nCompressMethod = 0
        var bAutoPrint = false
        var nPrintContent = 1
    }
}