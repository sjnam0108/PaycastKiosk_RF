package kr.co.bbmc.paycast.presentation.mainMenu;

import java.util.ArrayList;
import java.util.List;

import kr.co.bbmc.paycast.data.model.DataModel;

public class CancelOrderData {
    public String verificationYN ="";
    public String verifiMsg ="";
    public String tid = "";
    public String mid = "";
    public String totalindex = "";
    public String goodsAmt = "";
    public String orderNumber = "";
    public String orderDate = "";
    public String authCode = "";
    public String fnName = "";
    public String fnCd1 = "";
    public String fnName1 = "";
    public String storeOrderId = "";
    public List<DataModel> cancelList = new ArrayList<>();
}
