package kr.co.bbmc.selforderutil;

import java.util.ArrayList;
import java.util.List;

public class CustomOptionData {
    public String reqTitle="";
    public List<CustomRequiredOptionData> requiredOptList = new ArrayList();    //CustomOptionRecyclerAdapter
    public String addTitle="";
    public List<CustomAddedOptionData> addOptList = new ArrayList();            //CustomAddOptionRecyclerAdapter
    public int refillCount = 0;
}
