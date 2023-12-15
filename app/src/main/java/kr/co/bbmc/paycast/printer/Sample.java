package kr.co.bbmc.paycast.printer;

import static com.sewoo.jpos.command.ESCPOSConst.LK_ALIGNMENT_LEFT;

import android.graphics.Typeface;
import android.util.Log;

import com.sewoo.jpos.command.ESCPOS;
import com.sewoo.jpos.command.ESCPOSConst;
import com.sewoo.jpos.printer.ESCPOSPrinter;
import com.sewoo.port.android.DeviceConnection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kr.co.bbmc.paycast.data.model.DataModel;
import kr.co.bbmc.selforderutil.PaymentInfoData;

public class Sample {
    public ESCPOSPrinter posPtr;
    final char ESC = ESCPOS.ESC;
    final char LF = ESCPOS.LF;

    public Sample(DeviceConnection connection)
    {
        if(connection==null)
            return;
        posPtr = new ESCPOSPrinter(connection);
    }

    public void Sample1() throws UnsupportedEncodingException
    {

        posPtr.printNormal(ESC + "|1F" + ESC + "|cA" + ESC + "|bC" + ESC + "|2C" + "Receipt" + LF + LF);

        posPtr.printNormal(ESC + "|rA" + ESC + "|bC" + "TEL (123)-456-7890" + LF);
        posPtr.printNormal(ESC + "|cA" + ESC + "|bC" + "Thank you for coming to our shop!" + LF + LF);


        posPtr.printNormal("Chicken                   $10.00" + LF);
        posPtr.printNormal("Hamburger                 $20.00" + LF);
        posPtr.printNormal("Pizza                     $30.00" + LF);
        posPtr.printNormal("Lemons                    $40.00" + LF);
        posPtr.printNormal("Drink                     $50.00" + LF + LF);
        posPtr.printNormal("Excluded tax             $150.00" + LF);

        posPtr.printNormal( ESC + "|2F" + ESC + "|uC" + "Tax(5%)                    $7.50" + LF);
        posPtr.printNormal( ESC + "|bC" + ESC + "|2C" + "Total   $157.50" + LF + LF);
        posPtr.printNormal( ESC + "|bC" + "Payment                  $200.00" + LF);

        posPtr.printNormal( ESC + "|bC" + "Change                    $42.50" + LF);
//        posPtr.printBarCode("1234567890", ESCPOSConst.LK_BCS_Code39, 40, 2, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
        posPtr.printNormal( ESC + "|cA" + ESC + "|1fB" + ESC + "|2wB" + ESC + "|80hB" + ESC + "|2rB" + ESC + "|4tB" + ESC + "|10dB" + "1234567890" + LF);
        posPtr.printNormal(ESC + "|cA" + ESC + "|4C" + ESC + "|bC" + "Thank you" + LF);

//        posPtr.lineFeed(3);
        // POSPrinter Only.
        posPtr.printNormal(ESC + "|fP");
//        posPtr.cutPaper();
    }

    public void Sample2() throws UnsupportedEncodingException
    {
        posPtr.printBarCode("1234567890", ESCPOSConst.LK_BCS_Code39, 40, 2, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
        posPtr.printBarCode("0123498765", ESCPOSConst.LK_BCS_Code93, 40, 2, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
        posPtr.printBarCode("0987654321", ESCPOSConst.LK_BCS_ITF, 40, 2, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
        posPtr.printBarCode("{ACODE 128", ESCPOSConst.LK_BCS_Code128, 40, 2, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
        posPtr.printBarCode("{BCode 128", ESCPOSConst.LK_BCS_Code128, 40, 2, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
        posPtr.printBarCode("{C12345", ESCPOSConst.LK_BCS_Code128, 40, 2, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
        posPtr.printBarCode("A1029384756A", ESCPOSConst.LK_BCS_Codabar, 40, 2, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);

        posPtr.lineFeed(3);
        // POSPrinter Only.
        posPtr.cutPaper();

    }

    public void Sample3() throws IOException
    {
        posPtr.printBitmap("//sdcard//temp//test//logo_s.jpg", ESCPOSConst.LK_ALIGNMENT_CENTER);
        posPtr.printBitmap("//sdcard//temp//test//danmark_windmill.jpg", LK_ALIGNMENT_LEFT);
        posPtr.printBitmap("//sdcard//temp//test//sample_2.jpg", ESCPOSConst.LK_ALIGNMENT_RIGHT);
        posPtr.printBitmap("//sdcard//temp//test//sample_3.jpg", ESCPOSConst.LK_ALIGNMENT_CENTER);
        posPtr.printBitmap("//sdcard//temp//test//sample_4.jpg", LK_ALIGNMENT_LEFT);

        posPtr.lineFeed(3);
        // POSPrinter Only.
        posPtr.cutPaper();
    }

    public int Sample4() throws IOException
    {
        int check = posPtr.printerCheck();
        if(check == ESCPOSConst.LK_SUCCESS)
        {
            Log.i("Sample","sts= "+posPtr.status());
            return posPtr.status();
        }
        else
        {
            Log.i("Sample","Retrieve Status Failed");
            return -1;
        }
    }

    public void printAndroidFont() throws IOException
    {
        int nLineWidth = 384;
        String data = "Receipt";
//    	String data = "영수증";
        Typeface typeface = null;

        try
        {
            posPtr.printAndroidFont(data, nLineWidth, 100, ESCPOSConst.LK_ALIGNMENT_CENTER);
            posPtr.lineFeed(2);
            posPtr.printAndroidFont("Left Alignment", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont("Center Alignment", nLineWidth, 24, ESCPOSConst.LK_ALIGNMENT_CENTER);
            posPtr.printAndroidFont("Right Alignment", nLineWidth, 24, ESCPOSConst.LK_ALIGNMENT_RIGHT);

            posPtr.lineFeed(2);
            posPtr.printAndroidFont(Typeface.SANS_SERIF, "SANS_SERIF : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.SERIF, "SERIF : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(typeface.MONOSPACE, "MONOSPACE : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);

            posPtr.lineFeed(2);
            posPtr.printAndroidFont(Typeface.SANS_SERIF, "SANS : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.SANS_SERIF, true, "SANS BOLD : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.SANS_SERIF, true, false, "SANS BOLD : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.SANS_SERIF, false, true, "SANS ITALIC : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.SANS_SERIF, true, true, "SANS BOLD ITALIC : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.SANS_SERIF, true, true, true, "SANS B/I/U : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);

            posPtr.lineFeed(2);
            posPtr.printAndroidFont(Typeface.SERIF, "SERIF : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.SERIF, true, "SERIF BOLD : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.SERIF, true, false, "SERIF BOLD : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.SERIF, false, true, "SERIF ITALIC : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.SERIF, true, true, "SERIF BOLD ITALIC : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.SERIF, true, true, true, "SERIF B/I/U : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);

            posPtr.lineFeed(2);
            posPtr.printAndroidFont(Typeface.MONOSPACE, "MONOSPACE : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.MONOSPACE, true, "MONO BOLD : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.MONOSPACE, true, false, "MONO BOLD : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.MONOSPACE, false, true, "MONO ITALIC : 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.MONOSPACE, true, true, "MONO BOLD ITALIC: 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            posPtr.printAndroidFont(Typeface.MONOSPACE, true, true, true, "MONO B/I/U: 1234iwIW", nLineWidth, 24, LK_ALIGNMENT_LEFT);

            posPtr.lineFeed(4);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void printMultilingualFont() throws IOException
    {
        int nLineWidth = 384;
        String Koreandata = "영수증";
        String Turkishdata = "Turkish(İ,Ş,Ğ)";
        String Russiandata = "Получение";
        String Arabicdata = "الإيصال";
        String Greekdata = "Παραλαβή";
        String Japanesedata = "領収書";
        String GB2312data = "收据";
        String BIG5data = "收據";

        try
        {
            posPtr.printAndroidFont("Korean Font", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            // Korean 100-dot size font in android device.
            posPtr.printAndroidFont(Koreandata, nLineWidth, 100, ESCPOSConst.LK_ALIGNMENT_CENTER);

            posPtr.printAndroidFont("Turkish Font", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            // Turkish 50-dot size font in android device.
            posPtr.printAndroidFont(Turkishdata, nLineWidth, 50, ESCPOSConst.LK_ALIGNMENT_CENTER);

            posPtr.printAndroidFont("Russian Font", 384, 24, LK_ALIGNMENT_LEFT);
            // Russian 60-dot size font in android device.
            posPtr.printAndroidFont(Russiandata, nLineWidth, 60, ESCPOSConst.LK_ALIGNMENT_CENTER);

            posPtr.printAndroidFont("Arabic Font", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            // Arabic 100-dot size font in android device.
            posPtr.printAndroidFont(Arabicdata, nLineWidth, 100, ESCPOSConst.LK_ALIGNMENT_CENTER);

            posPtr.printAndroidFont("Greek Font", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            // Greek 60-dot size font in android device.
            posPtr.printAndroidFont(Greekdata, nLineWidth, 60, ESCPOSConst.LK_ALIGNMENT_CENTER);

            posPtr.printAndroidFont("Japanese Font", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            // Japanese 100-dot size font in android device.
            posPtr.printAndroidFont(Japanesedata, nLineWidth, 100, ESCPOSConst.LK_ALIGNMENT_CENTER);

            posPtr.printAndroidFont("GB2312 Font", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            // GB2312 100-dot size font in android device.
            posPtr.printAndroidFont(GB2312data, nLineWidth, 100, ESCPOSConst.LK_ALIGNMENT_CENTER);

            posPtr.printAndroidFont("BIG5 Font", nLineWidth, 24, LK_ALIGNMENT_LEFT);
            // BIG5 100-dot size font in android device.
            posPtr.printAndroidFont(BIG5data, nLineWidth, 100, ESCPOSConst.LK_ALIGNMENT_CENTER);

            posPtr.lineFeed(4);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public int Sample5() throws IOException, InterruptedException
    {
        return posPtr.printerSts();
    }
    public void customSample1(ArrayList list, String tprice, ArrayList sellerInfo, PaymentInfoData payInfo, String orderNum) throws IOException
    {
        int nLineWidth = 384;
        DecimalFormat myFormatter = new DecimalFormat("###,###");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

                         //123456789012345678901234567890123456789012345678901234567890
        String spaceStr = "                                                            ";
        String formattedStringvat = myFormatter.format(Integer.valueOf(payInfo.payVat));
        String formattedStringPrice = myFormatter.format(Integer.valueOf(tprice) - Integer.valueOf(payInfo.payVat));
        String formattedStringtotalPrice = myFormatter.format(Integer.valueOf(tprice));

        Date tradingDate = null;
        try {
            tradingDate = sdf.parse(payInfo.tradingDate);
        } catch (ParseException ex) {
            Log.v("Exception", ex.getLocalizedMessage());
        }
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        posPtr.setCharSet("EUC-KR");
        posPtr.printNormal(ESC + "|1F" + ESC + "|cA" + ESC + "|bC" + ESC + "|2C" + "[카드 매출 전표(고객용)]" + LF + LF);
        for(int i = 0; i<sellerInfo.size(); i++)
        {
            String s = (String) sellerInfo.get(i);
            posPtr.printNormal(s+LF);

//            posPtr.printNormal(ESC + "|rA" + ESC + "|bC" + s + LF);
        }
        posPtr.printNormal(" "  + LF);

        //카드정보 ->
        posPtr.printNormal("--------------------------------------------"  + LF);
        posPtr.printNormal(String.format("카드명: %s %s", payInfo.issueerName,payInfo.acquirerName)  + LF);
        posPtr.printNormal(String.format("카드번호: %s", payInfo.cardNumber)  + LF);
        posPtr.printNormal(String.format("CATID: %s", payInfo.catId)  + LF);
        posPtr.printNormal(String.format("승인번호: %s", payInfo.approvalNum)  + LF);
        if(payInfo.installmentMonth.equals("00"))
            posPtr.printNormal(String.format("할부개월: 일시불")  + LF);
        else
            posPtr.printNormal(String.format("할부개월: %s개월", payInfo.installmentMonth)  + LF);
        posPtr.printNormal(String.format("거래일시: %s", sdf.format(tradingDate))  + LF);
//        posPtr.printNormal(LF+LF+String.format("가맹번호: %s", payInfo.approvalNum)  + LF);
        //<-카드정보
        {
            //주문 정보 ->
                             //123456789012345678901234567890123456789012345678901234567890
            String supplyPrice = "공급가액:";
            String vatPrice =    "부가세:";
            String totalPrice =  "결제금액:";
            int supplylen = supplyPrice.getBytes("euc-kr").length;
            int vatlen = vatPrice.getBytes("euc-kr").length;
            int totallen = totalPrice.getBytes("euc-kr").length;

            String supplyStr = supplyPrice+spaceStr.substring(0, 42-supplylen-formattedStringPrice.getBytes().length-2)+formattedStringPrice;
            String vatStr =    vatPrice+spaceStr.substring(0, 42-vatlen-formattedStringvat.getBytes().length-2)+formattedStringvat;
            String totalStr =  totalPrice+spaceStr.substring(0, 42-totallen-formattedStringtotalPrice.getBytes().length-2)+formattedStringtotalPrice;

            posPtr.printNormal("--------------------------------------------" + LF);
            posPtr.printNormal(String.format("%s원", supplyStr) + LF);
            posPtr.printNormal(String.format("%s원", vatStr) + LF);
            posPtr.printNormal(String.format("%s원", totalStr) + LF);
            posPtr.printNormal("--------------------------------------------" + LF + LF);
            //<- 주문 정보
        }
        posPtr.printNormal("이용해 주셔서 감사합니다."  + LF+LF);

//        posPtr.lineFeed(3);
        // POSPrinter Only.
        posPtr.printNormal(ESC + "|fP");
        posPtr.cutPaper();

        posPtr.printNormal(ESC + "|1F" + ESC + "|cA" + ESC + "|bC" + ESC + "|2C" + "[ 주문서 ]" + LF + LF);

        String s = (String) sellerInfo.get(0);
        posPtr.printNormal(s+LF);
        posPtr.printNormal(String.format("주문일시:   %s", sdf.format(tradingDate))  + LF+LF+LF);
        posPtr.printNormal( ESC + "|1F"+"주문번호:            "+ESC + "|cA" + ESC + "|bC" + ESC + "|rA"+ ESC + "|4C"+orderNum+  LF);
        if((list!=null)&&(list.size()>0))
        {
            DataModel order = (DataModel) list.get(0);
            if(order.isPackage())
                posPtr.printNormal("                             포장 "+ LF);
        }

        posPtr.printNormal("--------------------------------------------"  + LF);
        posPtr.printNormal(" 품목                                  수량  "  + LF);
        posPtr.printNormal("--------------------------------------------"  + LF);

        for(int i = 0; i<list.size(); i++)
        {
            DataModel order = (DataModel) list.get(i);
            String count= String.valueOf(order.getCount());
            int menulen = order.getText().getBytes("euc-kr").length;
            int countlen = count.getBytes("euc-kr").length;

            String testStr = "";
            int len = 42-menulen-countlen;
            if(len <0)
                testStr = order.getText().substring(0, 42-countlen);
            else
                testStr = order.getText() +spaceStr.substring(0, 42-menulen-countlen);
            posPtr.printNormal( testStr+count+LF);

/*
            posPtr.printText(order.text, LK_ALIGNMENT_LEFT, LK_FNT_DEFAULT, LK_TXT_1WIDTH);
            posPtr.printText(String.format("%d  ",order.count), LK_ALIGNMENT_RIGHT, LK_FNT_DEFAULT, LK_TXT_1WIDTH);
            posPtr.printNormal(" "  + LF);
*/
//            posPtr.printNormal(ESC + "|rA" + ESC + "|bC" + s + LF);
        }
//        posPtr.printNormal(" "  + LF);

        {
            int pricelen = formattedStringtotalPrice.getBytes("euc-kr").length;
                            //123456789012345678901234567890123456789012345678901234567890
            String priceStr = " 총액:";
            String printStr =spaceStr.substring(0, 42-pricelen-priceStr.getBytes("euc-kr").length-2);

            posPtr.printNormal("--------------------------------------------" + LF);
            posPtr.printNormal(priceStr+printStr+formattedStringtotalPrice+ "원" + LF);
            posPtr.printNormal("--------------------------------------------" + LF + LF);
        }
        //<- 주문 정보
        posPtr.printNormal("이용해 주셔서 감사합니다."  + LF+ LF);


//        posPtr.lineFeed(3);
        // POSPrinter Only.
        posPtr.printNormal(ESC + "|fP");
        posPtr.cutPaper();

    }
}
