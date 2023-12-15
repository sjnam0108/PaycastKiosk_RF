package kr.co.bbmc.paycast.printer;

import android.graphics.Typeface;

import com.orhanobut.logger.Logger;
import com.sewoo.jpos.command.ESCPOS;
import com.sewoo.jpos.command.ESCPOSConst;
import com.sewoo.jpos.printer.ESCPOSPrinter;
import com.sewoo.jpos.printer.LKPrint;

import java.io.IOException;


public class Sample_Print {

    private final char ESC = ESCPOS.ESC;

    private ESCPOSPrinter escposPrinter;
    private int rtn;

    public Sample_Print()
    {
        // escposPrinter = new ESCPOSPrinter("EUC-KR"); // Korean.
        // escposPrinter = new ESCPOSPrinter("BIG5"); // BIG5.
        // escposPrinter = new ESCPOSPrinter("GB2312"); // GB2312.
        // escposPrinter = new ESCPOSPrinter("Shift_JIS"); // Japanese.
        escposPrinter = new ESCPOSPrinter();
    }

    public int Print_Sample_1() throws InterruptedException
    {

        try
        {
            rtn = escposPrinter.printerSts();
            Logger.e("printerSts = " + rtn);
            // Do not check the paper near empty.
            // if( (rtn != 0) && (rtn != ESCPOSConst.STS_PAPERNEAREMPTY)) return rtn;
            // check the paper near empty.
            if( rtn != 0 )  return rtn;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return rtn;
        }

        try
        {
/*
			escposPrinter.printNormal(ESC+"|bC"+ESC+"|1CNormal\n");
			escposPrinter.printNormal(ESC+"|bC"+ESC+"|2CDouble width\n");
			escposPrinter.printNormal(ESC+"|bC"+ESC+"|3CDouble height\n");
			escposPrinter.printNormal(ESC+"|bC"+ESC+"|4CDouble width/height\n");
*/
            escposPrinter.printNormal(ESC+"|cA"+ESC+"|2CReceipt\r\n\r\n\r\n");

            escposPrinter.printNormal(ESC+"|rATEL (123)-456-7890\n\n\n");
            escposPrinter.printNormal(ESC+"|cAThank you for coming to our shop!\n");
            escposPrinter.printNormal(ESC+"|cADate\n\n");
            escposPrinter.printNormal("Chicken                             $10.00\n");
            escposPrinter.printNormal("Hamburger                           $20.00\n");
            escposPrinter.printNormal("Pizza                               $30.00\n");
            escposPrinter.printNormal("Lemons                              $40.00\n");
            escposPrinter.printNormal("Drink                               $50.00\n");
            escposPrinter.printNormal("Excluded tax                       $150.00\n");
            escposPrinter.printNormal(ESC+"|uCTax(5%)                              $7.50\n");
            escposPrinter.printNormal(ESC+"|bC"+ESC+"|2CTotal         $157.50\n\n");
            escposPrinter.printNormal("Payment                            $200.00\n");
            escposPrinter.printNormal("Change                              $42.50\n\n");
            escposPrinter.printBarCode("{Babc456789012", LKPrint.LK_BCS_Code128, 40, 512, LKPrint.LK_ALIGNMENT_CENTER, LKPrint.LK_HRI_TEXT_BELOW); // Print Barcode
            escposPrinter.lineFeed(4);
            escposPrinter.cutPaper();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return 0;
    }

    public int Print_Sample_2() throws InterruptedException
    {
        try
        {
            rtn = escposPrinter.printerSts();
            // Do not check the paper near empty.
            // if( (rtn != 0) && (rtn != ESCPOSConst.STS_PAPERNEAREMPTY)) return rtn;
            // check the paper near empty.
            if( rtn != 0 )  return rtn;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return rtn;
        }

        try
        {
            escposPrinter.printText("Receipt\r\n\r\n\r\n", LKPrint.LK_ALIGNMENT_CENTER, LKPrint.LK_FNT_DEFAULT, LKPrint.LK_TXT_2WIDTH|LKPrint.LK_TXT_2HEIGHT);
            escposPrinter.printText("TEL (123)-456-7890\r\n", LKPrint.LK_ALIGNMENT_RIGHT, LKPrint.LK_FNT_DEFAULT, LKPrint.LK_TXT_1WIDTH);
            escposPrinter.printText("Thank you for coming to our shop!\r\n", LKPrint.LK_ALIGNMENT_CENTER, LKPrint.LK_FNT_DEFAULT, LKPrint.LK_TXT_1WIDTH);
            escposPrinter.printText("Chicken                             $10.00\r\n", LKPrint.LK_ALIGNMENT_LEFT, LKPrint.LK_FNT_DEFAULT, LKPrint.LK_TXT_1WIDTH);
            escposPrinter.printText("Hamburger                           $20.00\r\n", LKPrint.LK_ALIGNMENT_LEFT, LKPrint.LK_FNT_DEFAULT, LKPrint.LK_TXT_1WIDTH);
            escposPrinter.printText("Pizza                               $30.00\r\n", LKPrint.LK_ALIGNMENT_LEFT, LKPrint.LK_FNT_DEFAULT, LKPrint.LK_TXT_1WIDTH);
            escposPrinter.printText("Lemons                              $40.00\r\n", LKPrint.LK_ALIGNMENT_LEFT, LKPrint.LK_FNT_DEFAULT, LKPrint.LK_TXT_1WIDTH);
            escposPrinter.printText("Drink                               $50.00\r\n\r\n", LKPrint.LK_ALIGNMENT_LEFT, LKPrint.LK_FNT_DEFAULT, LKPrint.LK_TXT_1WIDTH);
            escposPrinter.printText("Excluded tax                       $150.00\r\n", LKPrint.LK_ALIGNMENT_LEFT, LKPrint.LK_FNT_DEFAULT, LKPrint.LK_TXT_1WIDTH);
            escposPrinter.printText("Tax(5%)                              $7.50\r\n", LKPrint.LK_ALIGNMENT_LEFT, LKPrint.LK_FNT_UNDERLINE, LKPrint.LK_TXT_1WIDTH);
            escposPrinter.printText("Total         $157.50\r\n\r\n", LKPrint.LK_ALIGNMENT_LEFT, LKPrint.LK_FNT_DEFAULT, LKPrint.LK_TXT_2WIDTH);
            escposPrinter.printText("Payment                            $200.00\r\n", LKPrint.LK_ALIGNMENT_LEFT, LKPrint.LK_FNT_DEFAULT, LKPrint.LK_TXT_1WIDTH);
            escposPrinter.printText("Change                              $42.50\r\n\r\n", LKPrint.LK_ALIGNMENT_LEFT, LKPrint.LK_FNT_DEFAULT, LKPrint.LK_TXT_1WIDTH);
            // Reverse print
            //posPtr.printText("Change                              $42.50\r\n\r\n", LKPrint.LK_ALIGNMENT_LEFT, LKPrint.LK_FNT_DEFAULT | LKPrint.LK_FNT_REVERSE, LKPrint.LK_TXT_1WIDTH);
            escposPrinter.printBarCode("{Babc456789012", LKPrint.LK_BCS_Code128, 40, 512, LKPrint.LK_ALIGNMENT_CENTER, LKPrint.LK_HRI_TEXT_BELOW); // Print Barcode
            escposPrinter.lineFeed(4);
            escposPrinter.cutPaper();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return 0;
    }

    public int Print_Image() throws InterruptedException
    {
        try
        {
            rtn = escposPrinter.printerSts();
            // Do not check the paper near empty.
            // if( (rtn != 0) && (rtn != ESCPOSConst.STS_PAPERNEAREMPTY)) return rtn;
            // check the paper near empty.
            if( rtn != 0 )  return rtn;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return rtn;
        }

        try
        {
            escposPrinter.printBitmap("//sdcard//temp//test//car_s.jpg", LKPrint.LK_ALIGNMENT_CENTER);
            escposPrinter.printBitmap("//sdcard//temp//test//danmark_windmill.jpg", LKPrint.LK_ALIGNMENT_LEFT);
            escposPrinter.printBitmap("//sdcard//temp//test//denmark_flag.jpg", LKPrint.LK_ALIGNMENT_RIGHT);
            escposPrinter.lineFeed(4);
            escposPrinter.cutPaper();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return 0;
    }
    public int Print_westernLatinCharTest() throws InterruptedException
    {
        final char [] diff = {0x23,0x24,0x40,0x5B,0x5C,0x5D,0x5E,0x6C,0x7B,0x7C,0x7D,0x7E,
                0xA4,0xA6,0xA8,0xB4,0xB8,0xBC,0xBD,0xBE};
        try
        {
            rtn = escposPrinter.printerSts();
            // Do not check the paper near empty.
            // if( (rtn != 0) && (rtn != ESCPOSConst.STS_PAPERNEAREMPTY)) return rtn;
            // check the paper near empty.
            if( rtn != 0 )  return rtn;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return rtn;
        }

        try
        {
            String ad = new String(diff);
            escposPrinter.printText(ad+"\r\n\r\n", LKPrint.LK_ALIGNMENT_LEFT, LKPrint.LK_FNT_DEFAULT, LKPrint.LK_TXT_1WIDTH);
            escposPrinter.lineFeed(4);
            escposPrinter.cutPaper();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return 0;
    }

    public int Print_1D_Barcode() throws InterruptedException
    {
        String barCodeData = "123456789012";

        try
        {
            rtn = escposPrinter.printerSts();
            // Do not check the paper near empty.
            // if( (rtn != 0) && (rtn != ESCPOSConst.STS_PAPERNEAREMPTY)) return rtn;
            // check the paper near empty.
            if( rtn != 0 )  return rtn;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return rtn;
        }

        try
        {
            escposPrinter.printString("UPCA\r\n");
            escposPrinter.printBarCode(barCodeData, ESCPOSConst.LK_BCS_UPCA, 70, 3, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
            escposPrinter.printString("UPCE\r\n");
            escposPrinter.printBarCode(barCodeData, ESCPOSConst.LK_BCS_UPCE, 70, 3, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
            escposPrinter.printString("EAN8\r\n");
            escposPrinter.printBarCode("1234567", ESCPOSConst.LK_BCS_EAN8, 70, 3, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
            escposPrinter.printString("EAN13\r\n");
            escposPrinter.printBarCode(barCodeData, ESCPOSConst.LK_BCS_EAN13, 70, 3, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
            escposPrinter.printString("CODE39\r\n");
            escposPrinter.printBarCode("ABCDEFGHI", ESCPOSConst.LK_BCS_Code39, 70, 3, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
            escposPrinter.printString("ITF\r\n");
            escposPrinter.printBarCode(barCodeData, ESCPOSConst.LK_BCS_ITF, 70, 3, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
            escposPrinter.printString("CODABAR\r\n");
            escposPrinter.printBarCode(barCodeData, ESCPOSConst.LK_BCS_Codabar, 70, 3, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
            escposPrinter.printString("CODE93\r\n");
            escposPrinter.printBarCode(barCodeData, ESCPOSConst.LK_BCS_Code93, 70, 3, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
            escposPrinter.printString("CODE128\r\n");
            escposPrinter.printBarCode("{BNo.{C4567890120", ESCPOSConst.LK_BCS_Code128, 70, 3, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_HRI_TEXT_BELOW);
            escposPrinter.lineFeed(4);
            escposPrinter.cutPaper();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return 0;
    }

    public int Print_2D_Barcode() throws InterruptedException
    {
        String data = "ABCDEFGHIJKLMN";
        try
        {
            rtn = escposPrinter.printerSts();
            // Do not check the paper near empty.
            // if( (rtn != 0) && (rtn != ESCPOSConst.STS_PAPERNEAREMPTY)) return rtn;
            // check the paper near empty.
            if( rtn != 0 )  return rtn;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return rtn;
        }

        try
        {
            escposPrinter.printString("PDF417\r\n");
            escposPrinter.printPDF417(data, data.length(), 0, 10, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printString("QRCode\r\n");
            escposPrinter.printQRCode(data, data.length(), 3, ESCPOSConst.LK_QRCODE_EC_LEVEL_L, ESCPOSConst.LK_ALIGNMENT_CENTER);
            escposPrinter.lineFeed(4);
            escposPrinter.cutPaper();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return 0;
    }

    public int Print_Android_Font() throws InterruptedException
    {
        String data = "Receipt";
//    	String data = "영수증";
        Typeface typeface = null;

        try
        {
            rtn = escposPrinter.printerSts();
            // Do not check the paper near empty.
            // if( (rtn != 0) && (rtn != ESCPOSConst.STS_PAPERNEAREMPTY)) return rtn;
            // check the paper near empty.
            if( rtn != 0 )  return rtn;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return rtn;
        }

        try
        {
            escposPrinter.printAndroidFont(data, 512, 100, ESCPOSConst.LK_ALIGNMENT_CENTER);
            escposPrinter.lineFeed(2);
            escposPrinter.printAndroidFont("Left Alignment", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont("Center Alignment", 512, 24, ESCPOSConst.LK_ALIGNMENT_CENTER);
            escposPrinter.printAndroidFont("Right Alignment", 512, 24, ESCPOSConst.LK_ALIGNMENT_RIGHT);

            escposPrinter.lineFeed(2);
            escposPrinter.printAndroidFont(Typeface.SANS_SERIF, "SANS_SERIF : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.SERIF, "SERIF : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(typeface.MONOSPACE, "MONOSPACE : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);

            escposPrinter.lineFeed(2);
            escposPrinter.printAndroidFont(Typeface.SANS_SERIF, "SANS : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.SANS_SERIF, true, "SANS BOLD : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.SANS_SERIF, true, false, "SANS BOLD : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.SANS_SERIF, false, true, "SANS ITALIC : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.SANS_SERIF, true, true, "SANS BOLD ITALIC : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.SANS_SERIF, true, true, true, "SANS B/I/U : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);

            escposPrinter.lineFeed(2);
            escposPrinter.printAndroidFont(Typeface.SERIF, "SERIF : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.SERIF, true, "SERIF BOLD : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.SERIF, true, false, "SERIF BOLD : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.SERIF, false, true, "SERIF ITALIC : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.SERIF, true, true, "SERIF BOLD ITALIC : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.SERIF, true, true, true, "SERIF B/I/U : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);

            escposPrinter.lineFeed(2);
            escposPrinter.printAndroidFont(Typeface.MONOSPACE, "MONOSPACE : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.MONOSPACE, true, "MONO BOLD : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.MONOSPACE, true, false, "MONO BOLD : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.MONOSPACE, false, true, "MONO ITALIC : 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.MONOSPACE, true, true, "MONO BOLD ITALIC: 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            escposPrinter.printAndroidFont(Typeface.MONOSPACE, true, true, true, "MONO B/I/U: 1234iwIW", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);

            escposPrinter.lineFeed(4);
            escposPrinter.cutPaper();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return 0;
    }

    public int Print_Multilingual() throws InterruptedException
    {
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
            rtn = escposPrinter.printerSts();
            // Do not check the paper near empty.
            // if( (rtn != 0) && (rtn != ESCPOSConst.STS_PAPERNEAREMPTY)) return rtn;
            // check the paper near empty.
            if( rtn != 0 )  return rtn;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return rtn;
        }

        try
        {
            escposPrinter.printAndroidFont("Korean Font", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            // Korean 100-dot size font in android device.
            escposPrinter.printAndroidFont(Koreandata, 512, 100, ESCPOSConst.LK_ALIGNMENT_CENTER);

            escposPrinter.printAndroidFont("Turkish Font", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            // Turkish 50-dot size font in android device.
            escposPrinter.printAndroidFont(Turkishdata, 512, 50, ESCPOSConst.LK_ALIGNMENT_CENTER);

            escposPrinter.printAndroidFont("Russian Font", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            // Russian 60-dot size font in android device.
            escposPrinter.printAndroidFont(Russiandata, 512, 60, ESCPOSConst.LK_ALIGNMENT_CENTER);

            escposPrinter.printAndroidFont("Arabic Font", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            // Arabic 100-dot size font in android device.
            escposPrinter.printAndroidFont(Arabicdata, 512, 100, ESCPOSConst.LK_ALIGNMENT_CENTER);

            escposPrinter.printAndroidFont("Greek Font", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            // Greek 60-dot size font in android device.
            escposPrinter.printAndroidFont(Greekdata, 512, 60, ESCPOSConst.LK_ALIGNMENT_CENTER);

            escposPrinter.printAndroidFont("Japanese Font", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            // Japanese 100-dot size font in android device.
            escposPrinter.printAndroidFont(Japanesedata, 512, 100, ESCPOSConst.LK_ALIGNMENT_CENTER);

            escposPrinter.printAndroidFont("GB2312 Font", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            // GB2312 100-dot size font in android device.
            escposPrinter.printAndroidFont(GB2312data, 512, 100, ESCPOSConst.LK_ALIGNMENT_CENTER);

            escposPrinter.printAndroidFont("BIG5 Font", 512, 24, ESCPOSConst.LK_ALIGNMENT_LEFT);
            // BIG5 100-dot size font in android device.
            escposPrinter.printAndroidFont(BIG5data, 512, 100, ESCPOSConst.LK_ALIGNMENT_CENTER);

            escposPrinter.lineFeed(4);
            escposPrinter.cutPaper();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return 0;
    }

    public int Print_PDF() throws InterruptedException
    {
        try
        {
            rtn = escposPrinter.printerSts();
            // Do not check the paper near empty.
            // if( (rtn != 0) && (rtn != ESCPOSConst.STS_PAPERNEAREMPTY)) return rtn;
            // check the paper near empty.
            if( rtn != 0 )  return rtn;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return rtn;
        }

        try
        {
            escposPrinter.printPDFFile("//sdcard//temp//test//PDF_Sample.pdf", LKPrint.LK_ALIGNMENT_CENTER, LKPrint.LK_PAPER_POS_3INCH, 2);
            escposPrinter.lineFeed(3);
            escposPrinter.cutPaper();

            //escposPrinter.printPDFFile("//sdcard//temp//test//PDF_Sample.pdf", LKPrint.LK_ALIGNMENT_LEFT, 500, 2);  //custom size
            //escposPrinter.lineFeed(3);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return rtn;
        }

        return 0;
    }
}
