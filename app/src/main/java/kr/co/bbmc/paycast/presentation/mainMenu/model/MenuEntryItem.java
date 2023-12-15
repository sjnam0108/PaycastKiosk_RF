package kr.co.bbmc.paycast.presentation.mainMenu.model;

public class MenuEntryItem implements Comparable<MenuEntryItem> {
    private String menuName;
    private String menuPrice;
    private String image;
    private String path;
    private boolean newMenu;
    private boolean popMenu;

    @Override
    public int compareTo( MenuEntryItem menuEntryItem) {
        return 0;
    }
    public MenuEntryItem(String menuName, String menuPrice, String image, String path, boolean newMenu, boolean popMenu)
    {
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.image = image;
        this.path= path;
        this.newMenu = newMenu;
        this.popMenu = popMenu;
    }
    public String getMenuName()
    {
        return menuName;
    }
    public String getMenuPrice()
    {
        return menuPrice;
    }
    public String getMenuImagePath()
    {
        return path;
    }
    public boolean isPopMenu()
    {
        return popMenu;
    }
    public boolean isNewMenu()
    {
        return newMenu;
    }
}


