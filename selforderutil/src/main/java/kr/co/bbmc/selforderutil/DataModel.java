package kr.co.bbmc.selforderutil;

import java.util.ArrayList;
import java.util.List;

public class DataModel {

    public String productId;
    public String text;
    public String drawable;
    public String color;
    public String itemprice;
    public String price;
    public int count = 1;
    public boolean refill = false;
    public boolean popular = false;
    public boolean newmenu = false;
    public boolean soldoutmenu = false;
    public boolean isPackage = false;
    public String rfcount="";
    public String paOrdId="";
    public String rfTel="";
    public String description="";
    public List<CustomOptionData> optionList = new ArrayList<>();

    public DataModel(String pId, String t, String d, String c, String p , boolean pop, boolean nm, boolean sm, boolean rm, String des)
    {
        productId = pId;
        text=t;
        drawable=d;
        color=c;
        itemprice = p;
        count = 1;
        popular = pop;
        newmenu = nm;
        soldoutmenu =sm;
        refill = rm;
        description = des;
    }
    public DataModel(String pId, String t, String d, String c, String p , String des, List<CustomOptionData> oList)
    {
        productId = pId;
        text=t;
        drawable=d;
        color=c;
        itemprice = p;
        count = 1;
        popular = false;
        newmenu = false;
        soldoutmenu =false;
        description = des;
        optionList = oList;
    }

    public DataModel(String pId, String t, String d, String c, String p, int itemcount, String des)
    {
        productId = pId;
        text=t;
        drawable=d;
        color=c;
        itemprice = p;
        count = itemcount;
        popular = false;
        newmenu = false;
        soldoutmenu = false;
        description = des;
    }
    public DataModel(String pId, String t, String d, String c, String p, int itemcount, boolean pop, boolean nm, String des )
    {
        productId = pId;
        text=t;
        drawable=d;
        color=c;
        itemprice = p;
        count = itemcount;
        popular = pop;
        newmenu = nm;
        soldoutmenu = false;
        description = des;
    }

    public DataModel() {
        productId = "";
        text ="";
        drawable ="";
        color ="";
        itemprice ="";
        price ="";
        count = 1;
        popular = false;
        newmenu = false;
        soldoutmenu = false;
        isPackage = false;
        description = "";
        optionList = new ArrayList<>();
    }

}
