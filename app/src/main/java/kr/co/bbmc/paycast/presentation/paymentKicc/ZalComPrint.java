package kr.co.bbmc.paycast.presentation.paymentKicc;

import static kr.co.bbmc.paycast.ConstantKt.sellerData;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import com.lvrenyang.io.Pos;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kr.co.bbmc.paycast.R;
import kr.co.bbmc.paycast.data.model.CustomAddedOptionData;
import kr.co.bbmc.paycast.data.model.CustomOptionData;
import kr.co.bbmc.paycast.data.model.CustomRequiredOptionData;
import kr.co.bbmc.paycast.data.model.DataModel;
import kr.co.bbmc.paycast.data.model.MenuObject;
import kr.co.bbmc.selforderutil.FileUtils;
import kr.co.bbmc.selforderutil.PaymentInfoData;

public class ZalComPrint {
    private static int MAX_PRINT_TIMEOUT = 3000;

    public static int onGetPrintStatus(Pos pos)
    {
        int bPrintResult = -6;

        byte[] status = new byte[1];
        if (pos.POS_RTQueryStatus(status, 1, 3000, 2) && ((status[0] & 0x12) == 0x12)) {
            if ((status[0] & 0x08) == 0) {
                if(pos.POS_QueryStatus(status, 3000, 2)) {
                    bPrintResult = 0;
                }
                else
                    bPrintResult = -8;  //사용 중
            } else {
                bPrintResult = -4;  //영수증 용지 없음.
            }
        } else {
            bPrintResult = -7;
        }
        return bPrintResult;
    }
    public static int CustomCancelPrintTicket(Context ctx, Pos pos, int nPrintWidth, int nCount, int nPrintContent, int nCompressMethod, ArrayList list, String tprice, ArrayList sellerInfo, PaymentInfoData payInfo, String orderNum, int version) throws UnsupportedEncodingException {
        int bPrintResult = -6;

        Logger.w("Item list : " + list);

        byte[] status = new byte[1];
        if (pos.POS_RTQueryStatus(status, 1, MAX_PRINT_TIMEOUT, 2) && ((status[0] & 0x12) == 0x12)) {
            if ((status[0] & 0x08) == 0) {
                if(pos.POS_QueryStatus(status, MAX_PRINT_TIMEOUT, 2)) {
                    for(int i = 0; i < nCount; ++i)
                    {
                       if(!pos.GetIO().IsOpened())
                            break;

                        if(nPrintContent >= 1)
                        {
                            pos.POS_FeedLine();

                            DecimalFormat myFormatter = new DecimalFormat("###,###");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");

                            //123456789012345678901234567890123456789012345678901234567890
                            String spaceStr = "                                                            ";
                            String formattedStringvat = "-"+myFormatter.format(Float.valueOf(payInfo.payVat));
                            String formattedStringPrice = "-"+myFormatter.format(Float.valueOf(tprice) - Float.valueOf(payInfo.payVat));
                            String formattedStringtotalPrice = "-"+myFormatter.format(Float.valueOf(tprice));
                            Date tradingDate = null;

                            //거래 승인날짜 "YYMMDDHHMMSSW" W 는 요일 (일요일:0, 월요일:1, 화요일:2 …토요일:6) 예) 2013년 2월 1일 12시 15분 17초 금요일 인경우 "1302011215175"
                            String dayOfWk = (String) payInfo.tradingDate.subSequence(payInfo.tradingDate.length()-1, payInfo.tradingDate.length());
                            String date = (String) payInfo.tradingDate.subSequence(0, payInfo.tradingDate.length()-1);

                            try {
                                tradingDate = sdf.parse(date);
                            } catch (ParseException ex) {
                                Log.v("Exception", ex.getLocalizedMessage());
                            }
                            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


                            pos.POS_S_Align(1);

                            pos.POS_TextOut(String.format("[카드 취소 전표(고객용)]\r\n"), 5, 0, 0, 0, 0, 0x08);


                            pos.POS_S_Align(0);
                            for(int info = 0; info<sellerInfo.size(); info++)
                            {
                                String s = (String) sellerInfo.get(info);
                                pos.POS_TextOut(String.format("%s\r\n",s), 5, 0, 0, 0, 0, 0);

                            }
                            pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut(String.format("%s: %s %s\r\n", ctx.getString(R.string.str_receipt_print_card_name), payInfo.cardName,payInfo.acquirerName), 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut(String.format("%s: %s******** \r\n", ctx.getString(R.string.str_receipt_print_card_number), payInfo.cardNumber), 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut(String.format("%s: %s \r\n", ctx.getString(R.string.str_receipt_print_merchant_number),payInfo.shop_tid), 5, 0, 0, 0, 0, 0);
//                            pos.POS_TextOut(String.format("CATID: %s \r\n", payInfo.catId), 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut(String.format("%s: %s \r\n", ctx.getString(R.string.str_receipt_print_approval_number), payInfo.approvalNum), 5, 0, 0, 0, 0, 0);

                            if(payInfo.installmentMonth.equals("00")||payInfo.installmentMonth.equals("0"))
                                pos.POS_TextOut(String.format("%s: %s\r\n",ctx.getString(R.string.str_receipt_print_installment_months), ctx.getString(R.string.str_receipt_print_lump_sum) ), 5, 0, 0, 0, 0, 0);
                            else
                                pos.POS_TextOut(String.format("%s: %s%s \r\n", ctx.getString(R.string.str_receipt_print_installment_months), payInfo.installmentMonth, ctx.getString(R.string.str_receipt_print_month)), 5, 0, 0, 0, 0, 0);

                            pos.POS_TextOut(String.format("%s: %s \r\n", ctx.getString(R.string.str_receipt_print_date_transaction), sdf.format(tradingDate)) , 5, 0, 0, 0, 0, 0);

                            //주문 정보 ->
                            //123456789012345678901234567890123456789012345678901234567890
                            pos.POS_TextOut(String.format("--------------------------------\r\n"), 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut(String.format("%s                      %s \r\n", ctx.getString(R.string.str_receipt_print_item), ctx.getString(R.string.str_receipt_print_amount)), 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut(String.format("--------------------------------\r\n"), 5, 0, 0, 0, 0, 0);

                            for(int l = 0; l<list.size(); l++)
                            {
                                DataModel order = (DataModel) list.get(l);
                                String count= "-"+String.format("%d", order.getCount());
                                int menulen = order.getText().getBytes("euc-kr").length;
                                int countlen = count.getBytes("euc-kr").length;
                                String testStr = "";
                                boolean needline = false;
                                int len =30-menulen-countlen-2;
                                if(len <0) {
                                    testStr = order.getText().substring(0, 17 - count.length() - "...".length()) + "...";
                                    //testStr = String.format("%24s... ", order.text);
                                    if((testStr.getBytes("euc-kr").length%2)==0)
                                        pos.POS_TextOut( String.format("%s %s\r\n", testStr,count), 5, 0, 0, 0, 0, 0);
                                    else
                                        pos.POS_TextOut( String.format("%s %s\r\n", testStr,count), 5, 0, 0, 0, 0, 0);
                                }
                                else {
                                    testStr = order.getText() + spaceStr.substring(0, 30 - menulen - countlen);
                                    pos.POS_TextOut( String.format("%s\r\n", testStr+count), 5, 0, 0, 0, 0, 0);
                                }

                                if((order.getOptionList()!=null)&&(order.getOptionList().size()>0))
                                {
                                    needline = false;
                                    for(int listCount=0; listCount<order.getOptionList().size(); listCount++) {
                                        CustomOptionData listItem = order.getOptionList().get(listCount);
                                        if ((listItem.getRequiredOptList() != null) && (listItem.getRequiredOptList().size() > 0)) {
                                            if((listItem.getReqTitle()!=null)&&(!listItem.getReqTitle().isEmpty()))
                                                pos.POS_TextOut(String.format("(%s)\r\n", listItem.getReqTitle()), 5, 0, 0, 0, 0, 0);
                                            for (int r = 0; r < listItem.getRequiredOptList().size(); r++) {
                                                CustomRequiredOptionData itemReqData = listItem.getRequiredOptList().get(r);
                                                pos.POS_TextOut(String.format(" -%s \r\n", itemReqData.getOptMenuName()), 5, 0, 0, 0, 0, 0);
                                            }
                                            needline = true;
                                        }
                                        if ((listItem.getAddOptList() != null) && (listItem.getAddOptList().size() > 0)) {
                                            if(needline)
                                                pos.POS_TextOut(String.format("\r\n"), 5, 0, 0, 0, 0, 0);

                                            if((listItem.getAddTitle() !=null)&&(!listItem.getAddTitle().isEmpty()))
                                                pos.POS_TextOut(String.format("(%s)\r\n", listItem.getAddTitle()), 5, 0, 0, 0, 0, 0);
                                            for (int r = 0; r < listItem.getAddOptList().size(); r++) {
                                                CustomAddedOptionData addData = listItem.getAddOptList().get(r);
                                                pos.POS_TextOut(String.format(" -%s \r\n", addData.getOptMenuName()), 5, 0, 0, 0, 0, 0);
                                            }
                                            needline = false;
                                        }
                                    }
//                                    pos.POS_TextOut(String.format("\r\n"), 5, 0, 0, 0, 0, 0);
                                }
                                
                                // 행사 상품
                                MenuObject plusItem = order.getPlusItem();
                                if(plusItem != null) {
                                    pos.POS_TextOut(String.format("%s\r\n", "행사상품") , 5, 0, 0, 0, 0, 0);

                                    String cnt = count;
                                    int menuLength = plusItem.getName().getBytes("euc-kr").length;
                                    int countLength = cnt.getBytes("euc-kr").length;
                                    String menuName = "";
                                    int len2 =30-menuLength-countLength-2;
                                    if(len2 <0) {
                                        menuName = plusItem.getName().substring(0, 17 - cnt.length() - "...".length()) + "...";
                                        //testStr = String.format("%24s... ", order.text);
                                        if((menuName.getBytes("euc-kr").length%2)==0)
                                            pos.POS_TextOut( String.format("%s %s\r\n", menuName,cnt), 5, 0, 0, 0, 0, 0);
                                        else
                                            pos.POS_TextOut( String.format("%s %s\r\n", menuName,cnt), 5, 0, 0, 0, 0, 0);
                                    }
                                    else {
                                        menuName = order.getText() + spaceStr.substring(0, 30 - menuLength - countLength);
                                        pos.POS_TextOut( String.format("%s\r\n", menuName+cnt), 5, 0, 0, 0, 0, 0);
                                    }
                                }
                                pos.POS_TextOut(String.format("\r\n"), 5, 0, 0, 0, 0, 0);
                            }
                            String supplyPrice = String.format("%s:", ctx.getString(R.string.str_receipt_print_supply_price));
                            String vatPrice =    String.format("%s:", ctx.getString(R.string.str_receipt_print_vat));  //"부가세:";
                            String totalPrice =  String.format("%s:", ctx.getString(R.string.str_receipt_print_payment));   //"결제금액:";
                            int supplylen = supplyPrice.getBytes("euc-kr").length;
                            int vatlen = vatPrice.getBytes("euc-kr").length;
                            int totallen = totalPrice.getBytes("euc-kr").length;

                            String supplyStr = supplyPrice+spaceStr.substring(0, 32-supplylen-formattedStringPrice.getBytes().length-3)+formattedStringPrice;
                            String vatStr =    vatPrice+spaceStr.substring(0, 32-vatlen-formattedStringvat.getBytes().length-3)+formattedStringvat;
                            String totalStr =  totalPrice+spaceStr.substring(0, 32-totallen-formattedStringtotalPrice.getBytes().length-3)+formattedStringtotalPrice;

                            pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut(String.format("%s%s\r\n", supplyStr, ctx.getString(R.string.str_receipt_print_won)) , 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut(String.format("%s%s\r\n", vatStr, ctx.getString(R.string.str_receipt_print_won))  , 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut(String.format("%s%s\r\n", totalStr, ctx.getString(R.string.str_receipt_print_won))  , 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);
                            //<- 주문 정보
                            pos.POS_TextOut(String.format("%s\r\n", ctx.getString(R.string.str_receipt_print_thank_you)) , 5, 0, 0, 0, 0, 0);

                            pos.POS_FeedLine();
                            //pos.POS_Beep(1, 5);
                            pos.POS_CutPaper();
                            //주문서 출력
                            pos.POS_S_Align(1);
                            pos.POS_TextOut("[취소 주문서]\r\n", 5, 0, 0, 0, 0, 0x08);

                            pos.POS_S_Align(0);
                            String s = (String) sellerInfo.get(0);
                            pos.POS_TextOut(String.format(s+"\r\n"),5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut(String.format("주문일시:%s\r\r\n", sdf.format(tradingDate)), 5, 0, 0, 0, 0, 0);
                            pos.POS_S_Align(0);
                            pos.POS_TextOut( "주문번호:"+orderNum+"\r\n",5, 1, 1, 0, 0, 0x08);
                            pos.POS_S_Align(0);

                            if(version==2) {
                                if ((list != null) && (list.size() > 0)) {
                                    DataModel order = (DataModel) list.get(0);
                                    Log.e("ZalcomPrint", "order.isPackage=" + order.isPackage());
                                    if (order.isPackage())
                                        pos.POS_TextOut("                           포장 \r\n", 5, 0, 1, 0, 0, 0x08);

                                }
                            }
                            pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut("품목                      수량 \r\n", 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);

                            for(int l = 0; l<list.size(); l++)
                            {
                                DataModel order = (DataModel) list.get(l);
                                String count= "-"+String.valueOf(order.getCount());
                                int menulen = order.getText().getBytes("euc-kr").length;
                                int countlen = count.getBytes("euc-kr").length;
                                String testStr = order.getText() +spaceStr.substring(0, 30-menulen-countlen);

                                pos.POS_TextOut( testStr+count+"\r\n", 5, 0, 0, 0, 0, 0);

                            }
                            int pricelen = formattedStringtotalPrice.getBytes("euc-kr").length;
                            //123456789012345678901234567890123456789012345678901234567890
                            String priceStr = "총액:";
                            String printStr =spaceStr.substring(0, 30-pricelen-priceStr.getBytes("euc-kr").length-2);

                            pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut(priceStr+printStr+formattedStringtotalPrice+ "원\r\n",5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);
                            //<- 주문 정보
                            pos.POS_TextOut("이용해 주셔서 감사합니다.\r\r\n",5, 0, 0, 0, 0, 0);
                            pos.POS_FeedLine();
                            pos.POS_Beep(1, 5);
                            pos.POS_CutPaper();
                        }
                    }
/*
                    if(bDrawer)
                        pos.POS_KickDrawer(0, 100);
*/
                    //bPrintResult = pos.POS_TicketSucceed(0, 30000);
                    bPrintResult = pos.POS_TicketSucceed(0, 30000);
                    Log.d("ZalComPrint", " CustomPrintTicket() status=0x%x"+status[0]+" bPrintResult="+bPrintResult);
                    String errlog = String.format("2 print 후에  status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
                    // Place a breakpoint here to catch application crashes
                    FileUtils.writeDebug(errlog, "PayCast");
                    Log.d("ZalComPrint", errlog);
                    bPrintResult = 0;
                } else {
                    bPrintResult = -8;      //프린터가 사용중이거나 프린터 연결이 안되어 있는 상태.
                    String errlog = String.format("2 프린터가 사용중이거나 프린터 연결이 안되어 있는 상태 status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
                    // Place a breakpoint here to catch application crashes
                    FileUtils.writeDebug(errlog, "PayCast");
                    Log.d("ZalComPrint", errlog);
                    //pos.POS_Beep(1, 5);
                }
            } else {
                bPrintResult = -4;
                String errlog = String.format("2 용지 없는 상태.. status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
                // Place a breakpoint here to catch application crashes
                FileUtils.writeDebug(errlog, "PayCast");
                Log.d("ZalComPrint", errlog);
                //pos.POS_Beep(1, 5);
            }
        } else {
            bPrintResult = -7;
            String errlog = String.format("2 이건 어떤 상태?.. status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
            // Place a breakpoint here to catch application crashes
            FileUtils.writeDebug(errlog, "PayCast");
            Log.d("ZalComPrint", errlog);
            //pos.POS_Beep(1, 5);
        }

        return bPrintResult;
    }
    public static int CustomReceiptPrintTicket(Context ctx, Pos pos, int nPrintWidth, int nCount, int nPrintContent, int nCompressMethod, ArrayList list, String tprice, ArrayList sellerInfo, PaymentInfoData payInfo, String orderNum, int version) throws UnsupportedEncodingException {
        int bPrintResult = -6;

        byte[] status = new byte[1];
        //프린터 상태에 관계없이이 명령을 받으면 상태를 반환합니다.
        //        반환 상태는 상태를 저장합니다.
        if (pos.POS_RTQueryStatus(status, 1, MAX_PRINT_TIMEOUT, 3) && ((status[0] & 0x12) == 0x12)) {
            if ((status[0] & 0x08) == 0) {
                if(pos.POS_QueryStatus(status, MAX_PRINT_TIMEOUT, 2)) {
                    for(int i = 0; i < nCount; ++i)
                    {
                        if(!pos.GetIO().IsOpened())
                            break;

                        if(nPrintContent >= 1)
                        {
                            pos.POS_FeedLine();


                            DecimalFormat myFormatter = new DecimalFormat("###,###");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");

                            //123456789012345678901234567890123456789012345678901234567890
                            String spaceStr = "                                                            ";
                            String formattedStringvat = myFormatter.format(Float.valueOf(payInfo.payVat));
                            String formattedStringPrice = myFormatter.format(Float.valueOf(tprice) - Float.valueOf(payInfo.payVat));
                            String formattedStringtotalPrice = myFormatter.format(Float.valueOf(tprice));
                            Date tradingDate = null;

                            //거래 승인날짜 "YYMMDDHHMMSSW" W 는 요일 (일요일:0, 월요일:1, 화요일:2 …토요일:6) 예) 2013년 2월 1일 12시 15분 17초 금요일 인경우 "1302011215175"
                            String dayOfWk = (String) payInfo.tradingDate.subSequence(payInfo.tradingDate.length()-1, payInfo.tradingDate.length());
                            String date = (String) payInfo.tradingDate.subSequence(0, payInfo.tradingDate.length()-1);

                            try {
                                tradingDate = sdf.parse(date);
                            } catch (ParseException ex) {
                                Log.v("Exception", ex.getLocalizedMessage());
                            }
                            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                            try {
                                pos.POS_S_Align(1);
                                pos.POS_TextOut(String.format("[카드 매출 전표(고객용)]\r\n"), 5, 0, 0, 0, 0, 0x08);
                                pos.POS_S_Align(0);
                                Logger.w("sellerInfo : " + sellerData);
                                // 가맹점 정보 : sellerData
                                if(sellerData != null) {
                                    pos.POS_TextOut(String.format("%s: %s\r\n", "가맹점명: ", sellerData.getStoreName()), 5, 0, 0, 0, 0, 0);
                                    pos.POS_TextOut(String.format("%s: %s\r\n", "사업자번호: ", sellerData.getBusinessNumber()), 5, 0, 0, 0, 0, 0);
                                    pos.POS_TextOut(String.format("%s: %s\r\n", "대표자명: ", sellerData.getCeoName()), 5, 0, 0, 0, 0, 0);
                                    pos.POS_TextOut(String.format("%s: %s\r\n", "주소: ", sellerData.getAddressInfo()), 5, 0, 0, 0, 0, 0);
                                    pos.POS_TextOut(String.format("%s: %s\r\n", "TEL: ", sellerData.getStoreTelNumber()), 5, 0, 0, 0, 0, 0);
                                }

                                pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);
                                pos.POS_TextOut(String.format("%s: %s %s\r\n", ctx.getString(R.string.str_receipt_print_card_name), payInfo.cardName,payInfo.acquirerName), 5, 0, 0, 0, 0, 0);
                                pos.POS_TextOut(String.format("%s: %s******** \r\n", ctx.getString(R.string.str_receipt_print_card_number),payInfo.cardNumber), 5, 0, 0, 0, 0, 0);
                                pos.POS_TextOut(String.format("%s: %s \r\n", ctx.getString(R.string.str_receipt_print_merchant_number),payInfo.shop_tid), 5, 0, 0, 0, 0, 0);
//                            pos.POS_TextOut(String.format("CATID: %s \r\n", payInfo.catId), 5, 0, 0, 0, 0, 0);
                                pos.POS_TextOut(String.format("%s: %s \r\n", ctx.getString(R.string.str_receipt_print_approval_number), payInfo.approvalNum), 5, 0, 0, 0, 0, 0);

                                if(payInfo.installmentMonth.equals("00")||payInfo.installmentMonth.equals("0"))
                                    pos.POS_TextOut(String.format("%s: %s\r\n", ctx.getString(R.string.str_receipt_print_installment_months), ctx.getString(R.string.str_receipt_print_lump_sum)), 5, 0, 0, 0, 0, 0);
                                else
                                    pos.POS_TextOut(String.format("%s: %s%s \r\n", ctx.getString(R.string.str_receipt_print_installment_months), ctx.getString(R.string.str_receipt_print_month), payInfo.installmentMonth), 5, 0, 0, 0, 0, 0);

                                pos.POS_TextOut(String.format("%s: %s \r\n", ctx.getString(R.string.str_receipt_print_date_transaction),sdf.format(tradingDate)) , 5, 0, 0, 0, 0, 0);

                                //주문 정보 ->
                                //123456789012345678901234567890123456789012345678901234567890
                                pos.POS_TextOut(String.format("--------------------------------\r\n"), 5, 0, 0, 0, 0, 0);
                                pos.POS_TextOut(String.format("%s                     %s \r\n", ctx.getString(R.string.str_receipt_print_item), ctx.getString(R.string.str_receipt_print_amount)), 5, 0, 0, 0, 0, 0);
                                pos.POS_TextOut(String.format("--------------------------------\r\n"), 5, 0, 0, 0, 0, 0);

                                for(int l = 0; l<list.size(); l++)
                                {
                                    DataModel order = (DataModel) list.get(l);
                                    String count= String.valueOf(order.getCount());
                                    int menulen = order.getText().getBytes("euc-kr").length;
                                    int countlen = count.getBytes("euc-kr").length;
                                    String testStr = "";
                                    int len =30-menulen-countlen-2;
                                    if(len <0) {
                                        testStr = order.getText().substring(0, 17 - count.length() - "...".length()) + "...";
                                        //testStr = String.format("%24s... ", order.text);
                                        if((testStr.getBytes("euc-kr").length%2)==0)
                                            pos.POS_TextOut( String.format("%s %s\r\n", testStr,count), 5, 0, 0, 0, 0, 0);
                                        else
                                            pos.POS_TextOut( String.format("%s%s\r\n", testStr,count), 5, 0, 0, 0, 0, 0);
                                    }
                                    else {
                                        testStr = order.getText() + spaceStr.substring(0, 30 - menulen - countlen);
                                        pos.POS_TextOut( String.format("%s\r\n", testStr+count), 5, 0, 0, 0, 0, 0);
                                    }
                                    boolean needline = false;
                                    //pos.POS_TextOut( String.format("%s\r\n", testStr+count), 5, 0, 0, 0, 0, 0);
                                    if((order.getOptionList() !=null)&&(order.getOptionList().size()>0))
                                    {
                                        needline = false;
                                        for(int listCount = 0; listCount< order.getOptionList().size(); listCount++) {
                                            CustomOptionData listItem = order.getOptionList().get(listCount);
                                            if ((listItem.getRequiredOptList() != null) && (listItem.getRequiredOptList().size() > 0)) {
                                                pos.POS_TextOut(String.format("(%s)\r\n", "필수"), 5, 0, 0, 0, 0, 0);
                                                for (int r = 0; r < listItem.getRequiredOptList().size(); r++) {
                                                    CustomRequiredOptionData itemReqData = listItem.getRequiredOptList().get(r);
                                                    pos.POS_TextOut(String.format(" -%s \r\n", itemReqData.getOptMenuName()), 5, 0, 0, 0, 0, 0);
                                                }
                                                needline = false;
                                            }
                                            if ((listItem.getAddOptList() != null) && (listItem.getAddOptList().size() > 0)) {
                                                if(needline)
                                                    pos.POS_TextOut(String.format("\r\n"), 5, 0, 0, 0, 0, 0);
                                                pos.POS_TextOut(String.format("(%s)\r\n", "선택"), 5, 0, 0, 0, 0, 0);
                                                for (int r = 0; r < listItem.getAddOptList().size(); r++) {
                                                    CustomAddedOptionData addData = listItem.getAddOptList().get(r);
                                                    pos.POS_TextOut(String.format(" -%s \r\n", addData.getOptMenuName()), 5, 0, 0, 0, 0, 0);
                                                }
                                                needline = false;
                                            }
                                        }
//                                    pos.POS_TextOut(String.format("\r\n"), 5, 0, 0, 0, 0, 0);
                                    }

                                    MenuObject plusItem = order.getPlusItem();
                                    if(plusItem != null) {
                                        String plusEventTitle = plusItem.getGroupName();
                                        String title = (plusEventTitle == null) ? "행사상품" : plusEventTitle;

                                        pos.POS_TextOut(String.format("\r\n"), 5, 0, 0, 0, 0, 0);
                                        pos.POS_TextOut(String.format("%s\r\n", title) , 5, 0, 0, 0, 0, 0);

                                        String cnt = count;
                                        int menuLength = plusItem.getName().getBytes("euc-kr").length;
                                        int countLength = cnt.getBytes("euc-kr").length;
                                        String menuName = "";
                                        int len2 =30-menuLength-countLength-2;
                                        if(len2 <0) {
                                            menuName = plusItem.getName().substring(0, 17 - cnt.length() - "...".length()) + "...";
                                            //testStr = String.format("%24s... ", order.text);
                                            if((menuName.getBytes("euc-kr").length%2)==0)
                                                pos.POS_TextOut( String.format("%s %s\r\n", menuName,cnt), 5, 0, 0, 0, 0, 0);
                                            else
                                                pos.POS_TextOut( String.format("%s %s\r\n", menuName,cnt), 5, 0, 0, 0, 0, 0);
                                        }
                                        else {
                                            menuName = plusItem.getName() + spaceStr.substring(0, 30 - menuLength - countLength);
                                            pos.POS_TextOut( String.format("%s\r\n", menuName+cnt), 5, 0, 0, 0, 0, 0);
                                        }
                                    }
                                    if((!order.getPlusOptionList().isEmpty()) && (order.getPlusOptionList().size() > 0)) {
                                        needline = false;
                                        for(int listCount = 0; listCount< order.getPlusOptionList().size(); listCount++) {
                                            CustomOptionData listItem = order.getPlusOptionList().get(listCount);
                                            if ((listItem.getRequiredOptList() != null) && (listItem.getRequiredOptList().size() > 0)) {
                                                pos.POS_TextOut(String.format("(%s)\r\n", "필수"), 5, 0, 0, 0, 0, 0);
                                                for (int r = 0; r < listItem.getRequiredOptList().size(); r++) {
                                                    CustomRequiredOptionData itemReqData = listItem.getRequiredOptList().get(r);
                                                    pos.POS_TextOut(String.format(" -%s \r\n", itemReqData.getOptMenuName()), 5, 0, 0, 0, 0, 0);
                                                }
                                                needline = false;
                                            }
                                            if ((listItem.getAddOptList() != null) && (listItem.getAddOptList().size() > 0)) {
                                                if(needline)
                                                    pos.POS_TextOut(String.format("\r\n"), 5, 0, 0, 0, 0, 0);
                                                pos.POS_TextOut(String.format("(%s)\r\n", "선택"), 5, 0, 0, 0, 0, 0);
                                                for (int r = 0; r < listItem.getAddOptList().size(); r++) {
                                                    CustomAddedOptionData addData = listItem.getAddOptList().get(r);
                                                    pos.POS_TextOut(String.format(" -%s \r\n", addData.getOptMenuName()), 5, 0, 0, 0, 0, 0);
                                                }
                                                needline = false;
                                            }
                                        }
                                    }

                                    pos.POS_TextOut(String.format("\r\n"), 5, 0, 0, 0, 0, 0);
                                }

                                String supplyPrice = String.format("%s", ctx.getString(R.string.str_receipt_print_supply_price));    //"공급가액:";
                                String vatPrice =    String.format("%s", ctx.getString(R.string.str_receipt_print_vat));    //"부가세:";
                                String totalPrice =   String.format("%s", ctx.getString(R.string.str_receipt_print_payment));   //"결제금액:";
                                int supplylen = supplyPrice.getBytes("euc-kr").length;
                                int vatlen = vatPrice.getBytes("euc-kr").length;
                                int totallen = totalPrice.getBytes("euc-kr").length;

                                String supplyStr = supplyPrice+spaceStr.substring(0, 32-supplylen-formattedStringPrice.getBytes().length-3)+formattedStringPrice;
                                String vatStr =    vatPrice+spaceStr.substring(0, 32-vatlen-formattedStringvat.getBytes().length-3)+formattedStringvat;
                                String totalStr =  totalPrice+spaceStr.substring(0, 32-totallen-formattedStringtotalPrice.getBytes().length-3)+formattedStringtotalPrice;

                                pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);
                                pos.POS_TextOut(String.format("%s%s\r\n", supplyStr, ctx.getString(R.string.str_receipt_print_won)) , 5, 0, 0, 0, 0, 0);
                                pos.POS_TextOut(String.format("%s%s\r\n", vatStr, ctx.getString(R.string.str_receipt_print_won))  , 5, 0, 0, 0, 0, 0);
                                pos.POS_TextOut(String.format("%s%s\r\n", totalStr, ctx.getString(R.string.str_receipt_print_won))  , 5, 0, 0, 0, 0, 0);
                                pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);
                                //<- 주문 정보
                                pos.POS_TextOut(String.format("%s\r\n", ctx.getString(R.string.str_receipt_print_thank_you))  , 5, 0, 0, 0, 0, 0);

                                pos.POS_FeedLine();
                                //pos.POS_Beep(1, 5);
                                pos.POS_CutPaper();
                            } catch (Exception e) {
                                printError(pos, e.getMessage());
                            }
                        }
                    }

/*
                    if(bDrawer)
                        pos.POS_KickDrawer(0, 100);
*/
                    /* 0	success, -1	closed,  -2	write Failed, -3 read Failed, -4 offline, -5 nopaper, -6 unknow error  */
                    bPrintResult = pos.POS_TicketSucceed(0, 30000);
                    Log.d("ZalComPrint", " CustomPrintTicket() status=0x%x"+status[0]+" bPrintResult="+bPrintResult);
                    String errlog = String.format("1 print 후에  status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
                    // Place a breakpoint here to catch application crashes
                    FileUtils.writeDebug(errlog, "PayCast");
                    Log.d("ZalComPrint", errlog);
                    bPrintResult = 0;

                } else {
                    bPrintResult = -8;      //프린터가 사용중이거나 프린터 연결이 안되어 있는 상태.
                    String errlog = String.format("1 프린터가 사용중이거나 프린터 연결이 안되어 있는 상태 status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
                    // Place a breakpoint here to catch application crashes
                    FileUtils.writeDebug(errlog, "PayCast");
                    Log.d("ZalComPrint", errlog);
                }
            } else {
                bPrintResult = -4;
                String errlog = String.format("1 용지 없는 상태.. status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
                // Place a breakpoint here to catch application crashes
                FileUtils.writeDebug(errlog, "PayCast");
                Log.d("ZalComPrint", errlog);
            }
        } else {
            bPrintResult = -7;
            String errlog = String.format("1 이건 어떤 상태?.. status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
            // Place a breakpoint here to catch application crashes
            FileUtils.writeDebug(errlog, "PayCast");
            Log.d("ZalComPrint", errlog);

        }

        return bPrintResult;
    }
    
    // 주문번호 주문서 출력
    public static int CustomOrderPrintTicket(Context ctx, Pos pos, int nPrintWidth, int nCount, int nPrintContent, int nCompressMethod, ArrayList list, String tprice, ArrayList sellerInfo, PaymentInfoData payInfo, String orderNum, int version) throws UnsupportedEncodingException {
        int bPrintResult = -6;
        byte[] status = new byte[1];

        Logger.w("CustomOrderPrintTicket : status -> " + status + " // Order number : " + orderNum);
        //프린터 상태에 관계없이이 명령을 받으면 상태를 반환합니다.
        //        반환 상태는 상태를 저장합니다.
        if (pos.POS_RTQueryStatus(status, 1, MAX_PRINT_TIMEOUT, 3) && ((status[0] & 0x12) == 0x12)) {
            if ((status[0] & 0x08) == 0) {
                if(pos.POS_QueryStatus(status, MAX_PRINT_TIMEOUT, 2)) {
                    for(int i = 0; i < nCount; ++i)
                    {
                        if(!pos.GetIO().IsOpened())
                            break;

                        if(nPrintContent >= 1)
                        {
                            pos.POS_FeedLine();

                            DecimalFormat myFormatter = new DecimalFormat("###,###");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");

                            //123456789012345678901234567890123456789012345678901234567890
                            String spaceStr = "                                                            ";
                            String formattedStringvat = myFormatter.format(Float.valueOf(payInfo.payVat));
                            String formattedStringPrice = myFormatter.format(Float.valueOf(tprice) - Float.valueOf(payInfo.payVat));
                            String formattedStringtotalPrice = myFormatter.format(Float.valueOf(tprice));
                            Date tradingDate = null;

                            //거래 승인날짜 "YYMMDDHHMMSSW" W 는 요일 (일요일:0, 월요일:1, 화요일:2 …토요일:6) 예) 2013년 2월 1일 12시 15분 17초 금요일 인경우 "1302011215175"
                            String dayOfWk = (String) payInfo.tradingDate.subSequence(payInfo.tradingDate.length()-1, payInfo.tradingDate.length());
                            String date = (String) payInfo.tradingDate.subSequence(0, payInfo.tradingDate.length()-1);

                            try {
                                tradingDate = sdf.parse(date);
                            } catch (ParseException ex) {
                                Logger.e("Exception " + ex.getLocalizedMessage());
                            }
                            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                            //주문서 출력
                            pos.POS_S_Align(1);
                            pos.POS_TextOut(String.format("[주문서]\r\n"), 5, 0, 0, 0, 0, 0x08);

                            pos.POS_S_Align(0);
                            String s = "";
                            try {
                                s = (String) sellerData.getStoreName();
                            } catch (Exception e) {
                                Logger.e("sellerInfo parse failed : " + e.getMessage());
                            }
                            try {
                                pos.POS_TextOut(String.format(s+"\r\n"),5, 0, 0, 0, 0, 0);
                                pos.POS_TextOut(String.format("%s:%s\r\r\n", ctx.getString(R.string.str_receipt_print_date_order), sdf.format(tradingDate)), 5, 0, 0, 0, 0, 0);
                                pos.POS_S_Align(0);
                                pos.POS_TextOut( String.format("%s:", ctx.getString(R.string.str_receipt_print_order_number))+orderNum+"\r\n",5, 1, 1, 0, 0, 0x08);
                                pos.POS_S_Align(0);

                                if(version==2) {
                                    if ((list != null) && (list.size() > 0)) {
                                        DataModel order = (DataModel) list.get(0);
                                        Logger.e("ZalcomPrint ㅣ order.isPackage=" + order.isPackage());
                                        if (order.isPackage())
                                            pos.POS_TextOut(String.format("                           %s \r\n", ctx.getString(R.string.str_receipt_print_packing)), 5, 0, 1, 0, 0, 0x08);

                                    }
                                }
                                pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);
                                pos.POS_TextOut(String.format("%s                     %s \r\n", ctx.getString(R.string.str_receipt_print_item), ctx.getString(R.string.str_receipt_print_amount)), 5, 0, 0, 0, 0, 0);
                                pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);

                                for(int l = 0; l<list.size(); l++)
                                {
                                    DataModel order = (DataModel) list.get(l);
                                    String count= String.valueOf(order.getCount());
                                    int menulen = order.getText().getBytes("euc-kr").length;
                                    int countlen = count.getBytes("euc-kr").length;
                                    String testStr ="";
                                    int len = 30-menulen-countlen-2;
                                    if(len < 0) {
                                        testStr = order.getText().substring(0, 17- count.length()-"...".length())+"...";
                                        if((testStr.getBytes("euc-kr").length%2)==0)
                                            pos.POS_TextOut(String.format("%s %s\r\n", testStr, count ), 5, 0, 0, 0, 0, 0);
                                        else
                                            pos.POS_TextOut(String.format("%s%s\r\n", testStr, count ), 5, 0, 0, 0, 0, 0);
                                    }
                                    else {
                                        testStr = order.getText() + spaceStr.substring(0, 30 - menulen - countlen);
                                        pos.POS_TextOut(String.format("%s\r\n", testStr + count), 5, 0, 0, 0, 0, 0);
                                    }
                                    boolean needline = false;

                                    if((order.getOptionList() !=null)&&(order.getOptionList().size()>0))
                                    {
                                        needline = false;
                                        for(int listCount = 0; listCount< order.getOptionList().size(); listCount++) {
                                            CustomOptionData listItem = order.getOptionList().get(listCount);
                                            if ((listItem.getRequiredOptList() != null) && (listItem.getRequiredOptList().size() > 0)) {
                                                pos.POS_TextOut(String.format("(%s)\r\n", "필수"), 5, 0, 0, 0, 0, 0);
                                                for (int r = 0; r < listItem.getRequiredOptList().size(); r++) {
                                                    CustomRequiredOptionData itemReqData = listItem.getRequiredOptList().get(r);
                                                    pos.POS_TextOut(String.format(" -%s \r\n", itemReqData.getOptMenuName()), 5, 0, 0, 0, 0, 0);
                                                }
                                                needline = true;
                                            }
                                            if ((listItem.getAddOptList() != null) && (listItem.getAddOptList().size() > 0)) {
                                                if(needline)
                                                    pos.POS_TextOut(String.format("\r\n"), 5, 0, 0, 0, 0, 0);
                                                pos.POS_TextOut(String.format("(%s)\r\n", "선택"), 5, 0, 0, 0, 0, 0);
                                                for (int r = 0; r < listItem.getAddOptList().size(); r++) {
                                                    CustomAddedOptionData addData = listItem.getAddOptList().get(r);
                                                    pos.POS_TextOut(String.format(" -%s \r\n", addData.getOptMenuName()), 5, 0, 0, 0, 0, 0);
                                                }
                                            }
                                        }
//                                    pos.POS_TextOut(String.format("\r\n"), 5, 0, 0, 0, 0, 0);
                                    }

                                    MenuObject plusItem = order.getPlusItem();
                                    if(plusItem != null) {
                                        String plusEventTitle = plusItem.getGroupName();
                                        String title = (plusEventTitle == null) ? "행사상품" : plusEventTitle;

                                        pos.POS_TextOut(String.format("\r\n"), 5, 0, 0, 0, 0, 0);
                                        pos.POS_TextOut(String.format("%s\r\n", title) , 5, 0, 0, 0, 0, 0);

                                        String cnt = count;
                                        int menuLength = plusItem.getName().getBytes("euc-kr").length;
                                        int countLength = cnt.getBytes("euc-kr").length;
                                        String menuName = "";
                                        int len2 =30-menuLength-countLength-2;
                                        if(len2 <0) {
                                            menuName = plusItem.getName().substring(0, 17 - cnt.length() - "...".length()) + "...";
                                            //testStr = String.format("%24s... ", order.text);
                                            if((menuName.getBytes("euc-kr").length%2)==0)
                                                pos.POS_TextOut( String.format("%s %s\r\n", menuName,cnt), 5, 0, 0, 0, 0, 0);
                                            else
                                                pos.POS_TextOut( String.format("%s %s\r\n", menuName,cnt), 5, 0, 0, 0, 0, 0);
                                        }
                                        else {
                                            menuName = plusItem.getName() + spaceStr.substring(0, 30 - menuLength - countLength);
                                            pos.POS_TextOut( String.format("%s\r\n", menuName+cnt), 5, 0, 0, 0, 0, 0);
                                        }
                                    }
                                    if((!order.getPlusOptionList().isEmpty()) && (order.getPlusOptionList().size() > 0)) {
                                        needline = false;
                                        for(int listCount = 0; listCount< order.getPlusOptionList().size(); listCount++) {
                                            CustomOptionData listItem = order.getPlusOptionList().get(listCount);
                                            if ((listItem.getRequiredOptList() != null) && (listItem.getRequiredOptList().size() > 0)) {
                                                pos.POS_TextOut(String.format("(%s)\r\n", "필수"), 5, 0, 0, 0, 0, 0);
                                                for (int r = 0; r < listItem.getRequiredOptList().size(); r++) {
                                                    CustomRequiredOptionData itemReqData = listItem.getRequiredOptList().get(r);
                                                    pos.POS_TextOut(String.format(" -%s \r\n", itemReqData.getOptMenuName()), 5, 0, 0, 0, 0, 0);
                                                }
                                                needline = true;
                                            }
                                            if ((listItem.getAddOptList() != null) && (listItem.getAddOptList().size() > 0)) {
                                                if(needline)
                                                    pos.POS_TextOut(String.format("\r\n"), 5, 0, 0, 0, 0, 0);
                                                pos.POS_TextOut(String.format("(%s)\r\n", "선택"), 5, 0, 0, 0, 0, 0);
                                                for (int r = 0; r < listItem.getAddOptList().size(); r++) {
                                                    CustomAddedOptionData addData = listItem.getAddOptList().get(r);
                                                    pos.POS_TextOut(String.format(" -%s \r\n", addData.getOptMenuName()), 5, 0, 0, 0, 0, 0);
                                                }
                                                needline = false;
                                            }
                                        }
                                    }
                                    pos.POS_TextOut(String.format("\r\n"), 5, 0, 0, 0, 0, 0);
                                }
                                int pricelen = formattedStringtotalPrice.getBytes("euc-kr").length;
                                //123456789012345678901234567890123456789012345678901234567890
                                String priceStr = String.format("%s", ctx.getString(R.string.str_receipt_print_total_amount));  //"총액:"
                                String printStr =spaceStr.substring(0, 30-pricelen-priceStr.getBytes("euc-kr").length-3);

                                pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);
                                pos.POS_TextOut(String.format("%s", priceStr+printStr+formattedStringtotalPrice+ String.format(" %s\r\n", ctx.getString(R.string.str_receipt_print_won))),5, 0, 0, 0, 0, 0);
                                pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);
                                //<- 주문 정보
                                pos.POS_TextOut(String.format("%s\r\r\n", ctx.getString(R.string.str_receipt_print_thank_you)),5, 0, 0, 0, 0, 0);
                                pos.POS_FeedLine();
                                //pos.POS_Beep(1, 5);
                                pos.POS_CutPaper();
                            } catch (Exception e) {
                                e.printStackTrace();
                                printError(pos, e.getMessage());
                            }
                        }
                    }

                    /* 0	success, -1	closed,  -2	write Failed, -3 read Failed, -4 offline, -5 nopaper, -6 unknow error  */
                    bPrintResult = pos.POS_TicketSucceed(0, 30000);
                    Log.d("ZalComPrint", " CustomPrintTicket() status=0x%x"+status[0]+" bPrintResult="+bPrintResult);
                    String errlog = String.format("1 print 후에  status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
                    Logger.w("1 print 후에  status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
                    // Place a breakpoint here to catch application crashes
                    FileUtils.writeDebug(errlog, "PayCast");
                    Log.d("ZalComPrint", errlog);
                    bPrintResult = 0;

                } else {
                    bPrintResult = -8;      //프린터가 사용중이거나 프린터 연결이 안되어 있는 상태.
                    String errlog = String.format("1 프린터가 사용중이거나 프린터 연결이 안되어 있는 상태 status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
                    Logger.w("1 프린터가 사용중이거나 프린터 연결이 안되어 있는 상태 status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
                    // Place a breakpoint here to catch application crashes
                    FileUtils.writeDebug(errlog, "PayCast");
                    Log.d("ZalComPrint", errlog);
                }
            } else {
                bPrintResult = -4;
                String errlog = String.format("1 용지 없는 상태.. status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
                Logger.w("1 용지 없는 상태.. status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
                // Place a breakpoint here to catch application crashes
                FileUtils.writeDebug(errlog, "PayCast");
                Log.d("ZalComPrint", errlog);
            }
        } else {
            bPrintResult = -7;
            String errlog = String.format("1 이건 어떤 상태?.. status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
            Logger.w("1 이건 어떤 상태?.. status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
            // Place a breakpoint here to catch application crashes
            FileUtils.writeDebug(errlog, "PayCast");
            Log.d("ZalComPrint", errlog);

        }
        {
            String errlog = String.format("CustomOrderPrintTicket end() bPrintResult=%d\n", bPrintResult);
            Logger.e("CustomOrderPrintTicket end() bPrintResult=%d\n", bPrintResult);
            // Place a breakpoint here to catch application crashes
            FileUtils.writeDebug(errlog, "PayCast");
        }

        Logger.w("bPrint result is - "+bPrintResult);
        return bPrintResult;
    }

    private static void printError(Pos pos, String errorMsg) {
        pos.POS_FeedLine();
        pos.POS_TextOut(String.format("%s\r\r\n", "ERROR!!!!"),5, 0, 0, 0, 0, 0);
        pos.POS_TextOut(String.format("%s\r\r\n", "용지 출력중 오류가 발생했습니다."),5, 0, 0, 0, 0, 0);
        pos.POS_TextOut(String.format("%s\r\r\n", errorMsg),5, 0, 0, 0, 0, 0);
        pos.POS_Beep(1, 5);
        pos.POS_CutPaper();
    }

    public static int PrintTicket(Context ctx, Pos pos, int nPrintWidth, boolean bCutter, boolean bDrawer, boolean bBeeper, int nCount, int nPrintContent, int nCompressMethod)
    {
        int bPrintResult = -6;

        byte[] status = new byte[1];
        if (pos.POS_RTQueryStatus(status, 1, 3000, 2) && ((status[0] & 0x12) == 0x12)) {
            if ((status[0] & 0x08) == 0) {
                if(pos.POS_QueryStatus(status, 3000, 2)) {

                    Bitmap bm1 = getTestImage1(nPrintWidth, nPrintWidth);
                    Bitmap bm2 = getTestImage2(nPrintWidth, nPrintWidth);
                    Bitmap bmBlackWhite = getImageFromAssetsFile(ctx, "blackwhite.png");
                    Bitmap bmIu = getImageFromAssetsFile(ctx, "iu.jpeg");
                    Bitmap bmYellowmen = getImageFromAssetsFile(ctx, "yellowmen.png");
                    for(int i = 0; i < nCount; ++i)
                    {
                        if(!pos.GetIO().IsOpened())
                            break;

                        if(nPrintContent >= 1)
                        {
                            pos.POS_FeedLine();
                            pos.POS_S_Align(1);
                            pos.POS_S_TextOut("REC" + String.format("%03d", i) + "\r\nPrinter\r\n测试页\r\n\r\n", 0, 1, 1, 0, 0x100);
                            pos.POS_S_TextOut("扫二维码下载苹果APP\r\n", 0, 0, 0, 0, 0x100);
                            pos.POS_TextOut("鳥インフル\r\n", 4, 0, 0, 0, 0, 0);
                            pos.POS_S_SetQRcode("https://appsto.re/cn/2KF_bb.i", 8, 0, 3);
                            //pos.POS_DoubleQRCode("abc", 120,3,0, "def", 340, 3, 0, 3);
                            //pos.POS_DoubleQRCode("AB112233441020523999900000144000001540000000001234567ydXZt4LAN1UHN/j1juVcRA==:**********:3:3:1:乾電池:1:105:",120,3,0,"**口罩:1:210:牛奶:1:25",340,3,0,3);
                            //pos.POS_DoubleQRCode("乾電池:1:105:",120,3,0,"**口罩:1:210:牛奶:1:25",340,3,0,1);
                            //pos.POS_DoubleQRCode("乾電池:1:105", 0,3,0, "口罩:1:210:牛奶:1:25", 288, 3, 0, 1);
                            pos.POS_FeedLine();
                            pos.POS_S_SetBarcode("20160618", 0, 72, 3, 60, 0, 2);
                            pos.POS_FeedLine();
                        }

                        if(nPrintContent >= 2)
                        {
                            if(bm1 != null)
                            {
                                pos.POS_PrintPicture(bm1, nPrintWidth, 1, nCompressMethod);
                            }
                            if(bm2 != null)
                            {
                                pos.POS_PrintPicture(bm2, nPrintWidth, 1, nCompressMethod);
                            }
                        }

                        if(nPrintContent >= 3)
                        {
                            if(bmBlackWhite != null)
                            {
                                pos.POS_PrintPicture(bmBlackWhite, nPrintWidth, 1, nCompressMethod);
                            }
                            if(bmIu != null)
                            {
                                pos.POS_PrintPicture(bmIu, nPrintWidth, 0, nCompressMethod);
                            }
                            if(bmYellowmen != null)
                            {
                                pos.POS_PrintPicture(bmYellowmen, nPrintWidth, 0, nCompressMethod);
                            }
                        }
                    }

                    if(bBeeper)
                        pos.POS_Beep(1, 5);
                    if(bCutter)
                        pos.POS_CutPaper();
                    if(bDrawer)
                        pos.POS_KickDrawer(0, 100);

                    bPrintResult = pos.POS_TicketSucceed(0, 30000);
                } else {
                    bPrintResult = -8;  //프린터가 작업 중이거나 프린터 연결이 안되어 있습니다.
                }
            } else {
                bPrintResult = -4;  //용지 없음.
            }
        } else {
            bPrintResult = -7;
        }

        return bPrintResult;
    }

    public static String ResultCodeToString(int code) {
        switch (code) {
            case 0:
                return "打印成功";  //success
            case -1:
                return "连接断开";  //Disconnect
            case -2:
                return "写入失败";  //Imprecision failure 부정확한 실패
            case -3:
                return "读取失败";  //Defeat 무효, 패배
            case -4:
                return "打印机脱机"; //오프라인 프린터
            case -5:
                return "打印机缺纸"; //프린터에 용지가 없습니다.
            case -7:
                return "实时状态查询失败";  //실시간 상태 쿼리에 실패했습니다.
            case -8:
                return "查询状态失败";    //검색어 상태가 실패했습니다.
            case -6:
            default:
                return "未知错误";  //알 수없는 오류
        }
    }

    /**
     * 从Assets中读取图片
     */
    public static Bitmap getImageFromAssetsFile(Context ctx, String fileName) {
        Bitmap image = null;
        AssetManager am = ctx.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        // load the origial Bitmap
        Bitmap BitmapOrg = bitmap;

        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        // calculate the scale
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the Bitmap
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);

        // make a Drawable from Bitmap to allow to set the Bitmap
        // to the ImageView, ImageButton or what ever
        return resizedBitmap;
    }

    public static Bitmap getTestImage1(int width, int height)
    {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, width, height, paint);

        paint.setColor(Color.BLACK);
        for(int i = 0; i < 8; ++i)
        {
            for(int x = i; x < width; x += 8)
            {
                for(int y = i; y < height; y += 8)
                {
                    canvas.drawPoint(x, y, paint);
                }
            }
        }
        return bitmap;
    }

    public static Bitmap getTestImage2(int width, int height)
    {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, width, height, paint);

        paint.setColor(Color.BLACK);
        for(int y = 0; y < height; y += 4)
        {
            for(int x = y%32; x < width; x += 32)
            {
                canvas.drawRect(x, y, x+4, y+4, paint);
            }
        }
        return bitmap;
    }
    public static int CustomRefillOrderPrintTicket(Context ctx, Pos pos, int nPrintWidth, int nCount, int nPrintContent, int nCompressMethod, ArrayList list, String tprice, ArrayList sellerInfo, PaymentInfoData payInfo, String orderNum, int version) throws UnsupportedEncodingException {
        int bPrintResult = -6;

        byte[] status = new byte[1];

        {
            String errlog = String.format("CustomOrderPrintTicket start()\n");
            // Place a breakpoint here to catch application crashes
            FileUtils.writeDebug(errlog, "PayCast");
        }

        //프린터 상태에 관계없이이 명령을 받으면 상태를 반환합니다.
        //        반환 상태는 상태를 저장합니다.
        if (pos.POS_RTQueryStatus(status, 1, MAX_PRINT_TIMEOUT, 3) && ((status[0] & 0x12) == 0x12)) {
            if ((status[0] & 0x08) == 0) {
                if(pos.POS_QueryStatus(status, MAX_PRINT_TIMEOUT, 2)) {
                    for(int i = 0; i < nCount; ++i)
                    {
                        if(!pos.GetIO().IsOpened())
                            break;

                        if(nPrintContent >= 1)
                        {
                            pos.POS_FeedLine();

                            DecimalFormat myFormatter = new DecimalFormat("###,###");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

                            //123456789012345678901234567890123456789012345678901234567890
                            String spaceStr = "                                                            ";
                            String formattedStringvat = myFormatter.format(Float.valueOf(payInfo.payVat));
                            String formattedStringPrice = myFormatter.format(Float.valueOf(tprice) - Float.valueOf(payInfo.payVat));
                            String formattedStringtotalPrice = myFormatter.format(Float.valueOf(tprice));
                            Date tradingDate = null;

                            //거래 승인날짜 "YYMMDDHHMMSSW" W 는 요일 (일요일:0, 월요일:1, 화요일:2 …토요일:6) 예) 2013년 2월 1일 12시 15분 17초 금요일 인경우 "1302011215175"
                            String dayOfWk = (String) payInfo.tradingDate.subSequence(payInfo.tradingDate.length()-1, payInfo.tradingDate.length());
                            String date = (String) payInfo.tradingDate.subSequence(0, payInfo.tradingDate.length()-1);

                            try {
                                tradingDate = sdf.parse(date);
                            } catch (ParseException ex) {
                                Log.v("Exception", ex.getLocalizedMessage());
                            }
                            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                            //주문서 출력
                            pos.POS_S_Align(1);
                            pos.POS_TextOut(String.format("[주문서]\r\n"), 5, 0, 0, 0, 0, 0x08);

                            pos.POS_S_Align(0);
                            String s = (String) sellerInfo.get(0);
                            pos.POS_TextOut(String.format(s+"\r\n"),5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut(String.format("%s:%s\r\r\n", ctx.getString(R.string.str_receipt_print_date_order), sdf.format(tradingDate)), 5, 0, 0, 0, 0, 0);
                            pos.POS_S_Align(0);
                            pos.POS_TextOut( String.format("%s:", ctx.getString(R.string.str_receipt_print_order_number))+orderNum+"\r\n",5, 1, 1, 0, 0, 0x08);
                            pos.POS_S_Align(0);

                            if(version==2) {
                                if ((list != null) && (list.size() > 0)) {
                                    DataModel order = (DataModel) list.get(0);
                                    Log.e("ZalcomPrint", "order.isPackage=" + order.isPackage());
                                    pos.POS_TextOut("\r\n", 5, 0, 0, 0, 0, 0);
                                    pos.POS_TextOut(String.format("유형:   %s \r\n", "리필"), 5, 0, 1, 0, 0, 0x08);
                                }
                            }
                            pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut(String.format("%s                     %s \r\n", ctx.getString(R.string.str_receipt_print_item), ctx.getString(R.string.str_receipt_print_amount)), 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);

                            for(int l = 0; l<list.size(); l++)
                            {
                                DataModel order = (DataModel) list.get(l);
                                String count= String.valueOf(order.getCount());
                                int menulen = order.getText().getBytes("euc-kr").length;
                                int countlen = count.getBytes("euc-kr").length;
                                String testStr = "";
                                int len = 30-menulen-countlen-2;
                                if(len<0) {
                                    testStr = order.getText().substring(0, 17 - count.length() - "...".length()) + "...";
                                    //testStr = String.format("%24s... ", order.text);
                                    if((testStr.getBytes("euc-kr").length%2)==0)
                                        pos.POS_TextOut( String.format("%s %s\r\n", testStr,count), 5, 0, 0, 0, 0, 0);
                                    else
                                        pos.POS_TextOut( String.format("%s%s\r\n", testStr,count), 5, 0, 0, 0, 0, 0);

                                    pos.POS_TextOut(String.format("%s%s\r\n", testStr,count), 5, 0, 0, 0, 0, 0);
                                }
                                else {
                                    testStr = order.getText() + spaceStr.substring(0, 30 - menulen - countlen);
                                    pos.POS_TextOut(String.format("%s\r\n", testStr + count), 5, 0, 0, 0, 0, 0);
                                }
                                boolean needline = false;

                                if((order.getOptionList() !=null)&&(order.getOptionList().size()>0))
                                {
                                    needline = false;
                                    for(int listCount = 0; listCount< order.getOptionList().size(); listCount++) {
                                        CustomOptionData listItem = order.getOptionList().get(listCount);
                                        if ((listItem.getRequiredOptList() != null) && (listItem.getRequiredOptList().size() > 0)) {
                                            pos.POS_TextOut(String.format("(%s)\r\n", listItem.getReqTitle()), 5, 0, 0, 0, 0, 0);
                                            for (int r = 0; r < listItem.getRequiredOptList().size(); r++) {
                                                CustomRequiredOptionData itemReqData = listItem.getRequiredOptList().get(r);
                                                pos.POS_TextOut(String.format(" -%s \r\n", itemReqData.getOptMenuName()), 5, 0, 0, 0, 0, 0);
                                            }
                                            needline = true;
                                        }
                                    }
//                                    pos.POS_TextOut(String.format("\r\n"), 5, 0, 0, 0, 0, 0);
                                }
                                pos.POS_TextOut(String.format("\r\n"), 5, 0, 0, 0, 0, 0);
                            }
                            int pricelen = formattedStringtotalPrice.getBytes("euc-kr").length;
                            //123456789012345678901234567890123456789012345678901234567890
                            String priceStr = String.format("%s", ctx.getString(R.string.str_receipt_print_total_amount));  //"총액:"
                            String printStr =spaceStr.substring(0, 30-pricelen-priceStr.getBytes("euc-kr").length-3);

                            pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut(String.format("%s", priceStr+printStr+formattedStringtotalPrice+ String.format(" %s\r\n", ctx.getString(R.string.str_receipt_print_won))),5, 0, 0, 0, 0, 0);
                            pos.POS_TextOut("--------------------------------\r\n", 5, 0, 0, 0, 0, 0);
                            //<- 주문 정보
                            pos.POS_TextOut(String.format("%s\r\r\n", ctx.getString(R.string.str_receipt_print_thank_you)),5, 0, 0, 0, 0, 0);

                            pos.POS_FeedLine();
                            //pos.POS_Beep(1, 5);
                            pos.POS_CutPaper();

                        }

                    }

/*
                    if(bDrawer)
                        pos.POS_KickDrawer(0, 100);
*/
                    /* 0	success, -1	closed,  -2	write Failed, -3 read Failed, -4 offline, -5 nopaper, -6 unknow error  */
                    bPrintResult = pos.POS_TicketSucceed(0, 30000);
                    Log.d("ZalComPrint", " CustomPrintTicket() status=0x%x"+status[0]+" bPrintResult="+bPrintResult);
                    String errlog = String.format("1 print 후에  status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
                    // Place a breakpoint here to catch application crashes
                    FileUtils.writeDebug(errlog, "PayCast");
                    Log.d("ZalComPrint", errlog);
                    bPrintResult = 0;

                } else {
                    bPrintResult = -8;      //프린터가 사용중이거나 프린터 연결이 안되어 있는 상태.
                    String errlog = String.format("1 프린터가 사용중이거나 프린터 연결이 안되어 있는 상태 status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
                    // Place a breakpoint here to catch application crashes
                    FileUtils.writeDebug(errlog, "PayCast");
                    Log.d("ZalComPrint", errlog);
                }
            } else {
                bPrintResult = -4;
                String errlog = String.format("1 용지 없는 상태.. status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
                // Place a breakpoint here to catch application crashes
                FileUtils.writeDebug(errlog, "PayCast");
                Log.d("ZalComPrint", errlog);
            }
        } else {
            bPrintResult = -7;
            String errlog = String.format("1 이건 어떤 상태?.. status=0x%x bPrintResult =%d\n",status[0], bPrintResult);
            // Place a breakpoint here to catch application crashes
            FileUtils.writeDebug(errlog, "PayCast");
            Log.d("ZalComPrint", errlog);

        }
/*
        {
            String errlog = String.format("CustomOrderPrintTicket end() bPrintResult=%d\n", bPrintResult);
            // Place a breakpoint here to catch application crashes
            FileUtils.writeDebug(errlog, "PayCast");
        }
*/
        return bPrintResult;
    }
}
