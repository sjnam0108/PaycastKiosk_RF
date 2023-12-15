package kr.co.bbmc.selforderutil;

import java.util.ArrayList;
import java.util.List;

public class MenuOptionData {
    public String menuOptId ="";
    public String menuOptName ="";
    public String menuGubun ="";   //"0" : 필수정보, "1": 추가정보
    public String menuOptSeq ="";
    public List<CustomOptionData> optList = new ArrayList<CustomOptionData>();
}
