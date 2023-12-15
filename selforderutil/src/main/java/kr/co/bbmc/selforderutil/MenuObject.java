package kr.co.bbmc.selforderutil;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MenuObject {
    public String productId;
    public String seq;
    public String name;
    public String price;
    public String imagefile;
    public String description;
    public String popular = "false";
    public String newmenu = "false";
    public String soldout = "false";
    public String refill = "none";
    public List<MenuOptionData> optMenusList = new ArrayList<>();

    @NonNull
    @Override
    public String toString() {
        return "productId: " + productId
                + ", name: " + name
                + ", price: " + price
                + ", imagefile: " + imagefile
                + ", description: " + description
                + ", popular: " + popular
                + ", description: " + description
                + ", newmenu: " + newmenu
                + ", soldout: " + soldout
                + ", refill: " + refill
                + ", optMenusList: " + optMenusList.size();
    }
}
