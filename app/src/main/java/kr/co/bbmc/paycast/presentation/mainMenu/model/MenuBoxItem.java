package kr.co.bbmc.paycast.presentation.mainMenu.model;

public class MenuBoxItem implements Comparable<MenuBoxItem> {
    private String menuName;
    private String menuPrice;
    private String menuImage;
    private String menuImagepath;
    private String menuCatagory;

    public MenuBoxItem(String name, String price, String image, String imgPath, String catagory)
    {
        menuName = name;
        menuPrice = price;
        menuImage = image;
        menuImagepath = imgPath;
        menuCatagory = catagory;
    }
    public String getMenuName()
    {
        return menuName;
    }
    public String getImgPath()
    {
        return menuImagepath;
    }
    public String getImage() {
        return menuImage;
    }
    public String getMenuPrice() {
        return menuPrice;
    }
    public String getMenuCatagory() {
        return menuCatagory;
    }
    public int compareTo(MenuBoxItem o) {
        if(this.menuName != null)
            return this.menuName.toLowerCase().compareTo(o.getMenuName().toLowerCase());
        else
            throw new IllegalArgumentException();
    }
}
