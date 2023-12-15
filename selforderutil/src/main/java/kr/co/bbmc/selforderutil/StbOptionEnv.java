package kr.co.bbmc.selforderutil;

public class StbOptionEnv {
    public boolean playerStart;
    public int monitorMins;

    public int stbId;
    public String stbName="";
    public int stbUdpPort;
    public int serverPort;
    public String stbServiceType="";
    public String serverHost="";
    public String serverUkid="";

    public boolean ftpActiveMode;
    public String ftpHost="";
    public int ftpPort;
    public String ftpUser="";
    public String ftpPassword="";

    public String scheduleName;
    public int stbStatus;       //0: 미확인, 2 : 장비꺼짐, 3:모니터 꺼짐, 4:플레이어 꺼짐, 5:스케줄 미지정, 6: 정상방송

    public String menuName="";
    public String storeName="";    //매장명
    public String storeAddr="";        //매장 주소
    public String storeBusinessNum=""; //매장사업자 번호
    public String storeTel="";         //매장 전화번호
    public String storeMerchantNum=""; //매장 가맹점 번호
    public String storeCatId="";       //kiosk 카드 단말기 cat id
    public String storeRepresent="";    //매장 대표자 명
    public String storeId="";    //매장 번호
    public String deviceId="";     //deviceId;
    public String operatingTime="";    //매장 영업시간
    public String introMsg="";         //매장소개글
    public String mainPrtEnable="true";     //true :main print kiosk, false : no main print kiosk
    public String mainPrtip="192.168.0.217";     //sewoo printer 초기 ip
    public String koEnable="true";     //kiosk enable
    public String atEnable = "init";
    public String openType = "O";   //o (영업중), c (영업종료)

    public void init(){
        playerStart = true;
        monitorMins = 3;
        stbId = -1;
        stbName ="";
        stbUdpPort = 11001;
        serverPort =80;
        stbServiceType = "S";
        serverHost = "m.paycast.co.kr";
        serverUkid = "BBMCSC";

        ftpActiveMode = false;
        ftpHost = "m.paycast.co.kr";
        ftpPort = 21;
        ftpUser = "paycast14";
        ftpPassword = "paycastfnd";
        stbStatus = 0;
        deviceId = "?";     //deviceId;

    }
}
