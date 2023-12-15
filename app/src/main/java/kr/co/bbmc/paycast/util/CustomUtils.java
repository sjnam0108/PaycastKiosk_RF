package kr.co.bbmc.paycast.util;

import java.util.ArrayList;

import kr.co.bbmc.paycast.ConstantKt;
import kr.co.bbmc.selforderutil.StbOptionEnv;

public class CustomUtils {
    public static ArrayList onUpdateStoreInfo(StbOptionEnv stbOpt) {

        ArrayList sellerInfolist = new ArrayList();
        if(stbOpt.storeName!=null)
            sellerInfolist.add(new String("상호: "+stbOpt.storeName));
        if(stbOpt.storeBusinessNum!=null)
            sellerInfolist.add(new String("사업자번호: "+stbOpt.storeBusinessNum));
        if(stbOpt.storeRepresent!=null)
            sellerInfolist.add(new String("대표자: "+stbOpt.storeRepresent));
        if(stbOpt.storeTel!=null)
            sellerInfolist.add(new String("전화: "+stbOpt.storeTel));
        if(stbOpt.storeAddr!=null)
            sellerInfolist.add(new String("주 소: "+stbOpt.storeAddr));
/*
        if (stbOpt.koEnable != null)
            sellerInfolist.add(new String("koEnabled" +stbOpt.koEnable));
        if (stbOpt.atEnabled != null)
            sellerInfolist.add(new String("atEnabled" + stbOpt.atEnabled));
*/
        return sellerInfolist;
    }

    static public void setMsgNotifyNumber(String tel) {
        String telnum = "";
        if((tel!=null)&&(!tel.isEmpty()))
            telnum = tel.replaceAll("-", "");
        ConstantKt.mTelephone = telnum;
    }

    public StbOptionEnv mStbOpt; //selforderutil
    public boolean getKioskAtEnable() {
        if(mStbOpt.atEnable.equalsIgnoreCase("false"))
            return false;
        return true;
    }

    public boolean getKioskEnable() {
        if ((mStbOpt == null) || (mStbOpt.koEnable == null))
            return true;
        if (mStbOpt.koEnable.equalsIgnoreCase("false"))
            return false;
        return true;
    }

    public void setInitOrderNum(String value) {
//        int num = Integer.valueOf(value);
        int num = Integer.parseInt(value);
        if((num >= 0) && (num < 99999))
            ConstantKt.INIT_ORDER_NUM = num;
        else
            ConstantKt.INIT_ORDER_NUM = 0;
        ConstantKt.MAX_ORDER_NUM = 100 + ConstantKt.INIT_ORDER_NUM;
    }
}
