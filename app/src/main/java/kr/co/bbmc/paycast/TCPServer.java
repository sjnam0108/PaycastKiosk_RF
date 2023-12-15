package kr.co.bbmc.paycast;

import static kr.co.bbmc.paycast.App.APP;
import static kr.co.bbmc.paycast.ConstantKt.approvalNum;
import static kr.co.bbmc.paycast.ConstantKt.approveMoney;
import static kr.co.bbmc.paycast.ConstantKt.autoOnOff;
import static kr.co.bbmc.paycast.ConstantKt.installment;
import static kr.co.bbmc.paycast.ConstantKt.payCmdDate;
import static kr.co.bbmc.paycast.ConstantKt.paymentStatus;
import static kr.co.bbmc.paycast.ConstantKt.serviceFee;
import static kr.co.bbmc.paycast.ConstantKt.taxMoney;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kr.co.bbmc.paycast.presentation.payment.CustomPaymentActivity;

public class TCPServer implements Runnable {

    public static String TERMID =	"KIOSK1114915545";

    private static String TAG = "TCPServer";

    public final byte CMD_RX_TREMCHACK = 'a';
    public final byte CMD_RX_PAY = 'b';
    public final byte CMD_RX_PAYCANCEL = 'c';
    public final byte CMD_RX_SEARCH = 'd';
    public final byte CMD_RX_WAITNG = 'e';
    public final byte CMD_RX_UID = 'f';
    public final byte CMD_RX_RESET = 'r';

    public static int ServerPort = 1234;
    public static String ServerIP = "xxx.xxx.xxx.xxxx";
    public boolean mRun = false;
    private byte mServerMessage[];
    public OnMessageReceived mMessageListener = null;
    private OutputStream mBufferOut;
    private packetHader packetHader;
    private PacketData packetData;
    //private PlayerExternalVarApp externalVarApp;
    //    private boolean m_auto = true;
    private boolean bRxPacket = false;

    // from constant.kt
    public static int CARDIN_STATUS = 0x0001;
    public static int CARDOUT_STATUS = 0x0000;
    public static int MS_CARD = 0x1000;
    public static int RF_CARD = 0x0100;

    // CMD 설정
    public String CMD_TX_TREMCHACK = "A";
    public String CMD_TX_PAY = "B";
    public String CMD_TX_PAYCANCEL = "C";
    public String CMD_TX_SEARCH = 	"D";
    public static String CMD_TX_WAITNG = 	"E";
    public String CMD_TX_UID = "F";
    public String CMD_TX_RESET = "R";
    
    // 패킷 설정
    public static int PACKET_HADER_SIZE = 34;
    public static int PACKET_DATA_SIZE = 57;

    //
    public static byte SOH =	0x01;
    public static byte STX =	0x02;
    public static byte ETX =	0x03;
    public static byte EOT =	0x04;
    public static byte ENQ =	0x05;
    public static byte ACK =	0x06;	//정상
    public static byte SYN =	0x16;
    public static byte CR = 	0x0d;
    public static byte LF = 	0x0a;
    public static byte NACK =	0x15;	//오류

    private String origdate = "000000";  //8 digits
    private String origtime = "000000";  //6 digits

    private void onPrintCardTransInfo() {
        String termIdStr = new String(packetHader.TermID);
        Log.d(TAG,"키오스크 ID : "+termIdStr);
        termIdStr = new String(packetHader.DateTime);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        Date myDate = null;
        try
        {
            myDate = dateFormat.parse(termIdStr);
        } catch(ParseException e)
        {
            e.printStackTrace();
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd/hh:mm:ss");
        String finalDate = timeFormat.format(myDate);
        Log.d(TAG,"시간 : "+finalDate);
        termIdStr =new String(packetHader.JobCode);
        Log.d(TAG,"업무 코드 : "+termIdStr);
        termIdStr =String.valueOf(packetHader.BodyLen);
        if(packetHader.BodyLen >0)
        {
            Log.d(TAG, "### DATA ###");
            Log.d(TAG, "데이터 길이 : " + termIdStr);

            if ((packetHader.JobCode == CMD_TX_PAYCANCEL.getBytes()) || (packetHader.JobCode[0] == CMD_RX_PAYCANCEL)) {

                if (packetData.CancleType == '1')
                    Log.d(TAG, "요청전문 취소");
                else
                    Log.d(TAG, "마지막 거래 취소");

                if (packetData.Type == '1')
                    Log.d(TAG, "신용 승인 취소");
                else
                    Log.d(TAG, "현금영수증 취소");
            } else {
                if (packetData.Type == '1')
                    Log.d(TAG, "신용 승인");
                else
                    Log.d(TAG, "현금영수증");
            }
            try {
                termIdStr = new String(packetData.Fare, "EUC-KR");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "거래 금액 : " + termIdStr + "원");

            try {
                termIdStr = new String(packetData.Tax, "EUC-KR");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "세금 : " + termIdStr + "원");

            try {
                termIdStr = new String(packetData.ServiceFee, "EUC-KR");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "봉사료 : " + termIdStr + "원");

            try {
                termIdStr = new String(packetData.Installment, "EUC-KR");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "할부개월 : " + termIdStr);


            if (packetData.bSign == '1')
                Log.d(TAG, "서명 여부 : 비서명");
            else
                Log.d(TAG, "서명 여부 : 서명");

            if ((packetHader.JobCode[0] == CMD_TX_PAYCANCEL.getBytes()[0]) || (packetHader.JobCode[0] == CMD_RX_PAYCANCEL)) {
                termIdStr = new String(packetData.ApproveCode);
                Log.d(TAG, "승인 번호 : " + termIdStr);

                termIdStr = new String(packetData.OrgDateTime);
                Log.d(TAG, "원거래일자 : " + termIdStr);

                termIdStr = new String(packetData.OrgDateHour);
                Log.d(TAG, "원거래시간 : " + termIdStr);
            }
        }
        Log.d(TAG,"###############################");
    }

    private byte[] onMakeTransBufCmd(byte[] sBuf, String message) {
        sBuf[0]=packetData.Type;
        System.arraycopy(packetData.Fare,0,sBuf,1,10);
        System.arraycopy(packetData.Tax,0,sBuf,11,8);
        System.arraycopy(packetData.ServiceFee,0,sBuf,19,8);
        System.arraycopy(packetData.Installment,0,sBuf,27,2);
        sBuf[29]=packetData.bSign;
        System.arraycopy(packetData.ApproveCode,0,sBuf,30,12);
        System.arraycopy(packetData.OrgDateTime,0,sBuf,42,8);
        System.arraycopy(packetData.OrgDateHour,0,sBuf,50,6);
        return sBuf;
    }

    private byte[] onMakeTransBufCmdCancel(byte[] sBuf, String message) {
        sBuf[0] = packetData.CancleType;
        sBuf[1] = packetData.Type;
        System.arraycopy(packetData.Fare, 0, sBuf, 2, 10);
        System.arraycopy(packetData.Tax, 0, sBuf, 12, 8);
        System.arraycopy(packetData.ServiceFee, 0, sBuf, 20, 8);
        System.arraycopy(packetData.Installment, 0, sBuf, 28, 2);
        sBuf[30] = packetData.bSign;
        System.arraycopy(packetData.ApproveCode, 0, sBuf, 31, 12);
        System.arraycopy(packetData.OrgDateTime, 0, sBuf, 43, 8);
        System.arraycopy(packetData.OrgDateHour, 0, sBuf, 51, 6);
        return sBuf;
    }

    private void onMakePacketCmdPayCancel(String message) {
        packetHader.BodyLen = PACKET_DATA_SIZE;
        if (ConstantKt.cancleType != '1') ConstantKt.cancleType = '2';

        packetData.CancleType = ConstantKt.cancleType;
        packetData.Type = '1';

        byte[] tempmoney = String.valueOf(approveMoney).getBytes();
        byte[] tempBytes = new byte[10];
        Arrays.fill(tempBytes, (byte) '0');

        System.arraycopy(tempBytes, 0, packetData.Fare, 0, tempBytes.length);
        System.arraycopy(tempmoney, 0, packetData.Fare, tempBytes.length - tempmoney.length, tempmoney.length);

        tempmoney = String.valueOf(taxMoney).getBytes();
        tempBytes = new byte[8];
        Arrays.fill(tempBytes, (byte) '0');
        System.arraycopy(tempBytes, 0, packetData.Tax, 0, tempBytes.length);
        System.arraycopy(tempmoney, 0, packetData.Tax, tempBytes.length - tempmoney.length, tempmoney.length);

        tempmoney = String.valueOf(serviceFee).getBytes();
        tempBytes = new byte[8];
        Arrays.fill(tempBytes, (byte) '0');
        System.arraycopy(tempBytes, 0, packetData.ServiceFee, 0, tempBytes.length);
        System.arraycopy(tempmoney, 0, packetData.ServiceFee, tempBytes.length - tempmoney.length, tempmoney.length);

        tempmoney = String.valueOf(installment).getBytes();
        tempBytes = new byte[2];
        Arrays.fill(tempBytes, (byte) '0');
        System.arraycopy(tempBytes, 0, packetData.Installment, 0, tempBytes.length);
        System.arraycopy(tempmoney, 0, packetData.Installment, tempBytes.length - tempmoney.length, tempmoney.length);

        packetData.bSign = '1';

        Arrays.fill(packetData.ApproveCode, (byte) 0x00);
        tempmoney = approvalNum.getBytes();
        tempBytes = new byte[12];
        Arrays.fill(tempBytes, (byte) ' ');
        System.arraycopy(tempBytes, 0, packetData.ApproveCode, 0, tempBytes.length);
        System.arraycopy(tempmoney, 0, packetData.ApproveCode, tempBytes.length - tempmoney.length, tempmoney.length);

        tempmoney = origdate.getBytes();
        tempBytes = new byte[8];
        Arrays.fill(tempBytes, (byte) '0');
        System.arraycopy(tempBytes, 0, packetData.OrgDateTime, 0, tempBytes.length);
        System.arraycopy(tempmoney, 0, packetData.OrgDateTime, tempBytes.length - tempmoney.length, tempmoney.length);

        tempmoney = origtime.getBytes();
        tempBytes = new byte[6];
        Arrays.fill(tempBytes, (byte) '0');
        System.arraycopy(tempBytes, 0, packetData.OrgDateHour, 0, tempBytes.length);
        System.arraycopy(tempmoney, 0, packetData.OrgDateHour, tempBytes.length - tempmoney.length, tempmoney.length);
    }
    private void onMakePacketCmdPay(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        packetHader.BodyLen = PACKET_DATA_SIZE - 27;
        packetData.Type = '1';
        Date currentTime = Calendar.getInstance().getTime();

        payCmdDate = sdf.format(currentTime);
        byte[] tempmoney = String.valueOf(approveMoney).getBytes();
        byte[] tempBytes = new byte[10];
        Arrays.fill(tempBytes, (byte) '0');

        System.arraycopy(tempBytes, 0, packetData.Fare, 0, tempBytes.length);
        System.arraycopy(tempmoney, 0, packetData.Fare, tempBytes.length - tempmoney.length, tempmoney.length);

        tempmoney = String.valueOf(taxMoney).getBytes();
        tempBytes = new byte[8];
        Arrays.fill(tempBytes, (byte) '0');
        System.arraycopy(tempBytes, 0, packetData.Tax, 0, tempBytes.length);
        System.arraycopy(tempmoney, 0, packetData.Tax, tempBytes.length - tempmoney.length, tempmoney.length);

        tempmoney = String.valueOf(serviceFee).getBytes();
        tempBytes = new byte[8];
        Arrays.fill(tempBytes, (byte) '0');
        System.arraycopy(tempBytes, 0, packetData.ServiceFee, 0, tempBytes.length);
        System.arraycopy(tempmoney, 0, packetData.ServiceFee, tempBytes.length - tempmoney.length, tempmoney.length);

        tempmoney = String.valueOf(installment).getBytes();
        tempBytes = new byte[2];
        Arrays.fill(tempBytes, (byte) '0');
        System.arraycopy(tempBytes, 0, packetData.Installment, 0, tempBytes.length);
        System.arraycopy(tempmoney, 0, packetData.Installment, tempBytes.length - tempmoney.length, tempmoney.length);

        packetData.bSign = '1';
    }
    public void setServerParam(OnMessageReceived listener, String ip, int port)
    {
        this.mMessageListener = listener;
        this.ServerPort = port;
        this.ServerIP = ip;
        this.mRun = false;
        packetHader = new packetHader();
        packetData = new PacketData();
    }

    @Override
    public void run() {

        try {
            System.out.println("S: Connecting...");
            ServerSocket serverSocket = new ServerSocket(ServerPort);
            byte	cst=0;
            byte  buff[] = new byte[256];
            int	rxLen=0;
            int cnt;
            byte 	bcc= 0;

            while (true) {
                Socket client = serverSocket.accept();
//                System.out.println("S: Receiving...");
                try {
                    mBufferOut = client.getOutputStream();

//                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    InputStream in = client.getInputStream();
                    int charsRead = 0;
                    int offset = 0;
                    byte[] readbuffer = new byte[1024]; //choose your buffer size if you need other than 1024

                    mRun = true;

                    while (mRun)
                    {
                        charsRead = in.read(readbuffer);
                        Log.d(TAG, "TCP SERVER="+charsRead);
                        rxLen = 0;
                        offset = 0;
                        while((charsRead-offset)>0) {
//                            Log.e(TAG, "read offset="+offset+" buffer="+readbuffer[offset]);
                            if(cst==1) {								//헤더
                                buff[rxLen++] = (byte)readbuffer[offset];
                                bcc ^= readbuffer[offset];
                                if(rxLen >= PACKET_HADER_SIZE) {
                                    if(rxLen>=packetHader.TermID.length)
                                        System.arraycopy(buff, 0, packetHader.TermID, 0, packetHader.TermID.length);
                                    if(rxLen>=(packetHader.TermID.length+packetHader.DateTime.length))
                                        System.arraycopy(buff, packetHader.TermID.length, packetHader.DateTime, 0, packetHader.DateTime.length);
                                    if(rxLen>=(packetHader.TermID.length+packetHader.DateTime.length+packetHader.JobCode.length))
                                        System.arraycopy(buff, packetHader.TermID.length+packetHader.DateTime.length, packetHader.JobCode, 0, packetHader.JobCode.length);
                                    if(rxLen> (packetHader.TermID.length+packetHader.DateTime.length+packetHader.JobCode.length))
                                    {
                                        packetHader.ResponCode = buff[packetHader.TermID.length + packetHader.DateTime.length + 1];
//                                        packetHader.BodyLen = buff[packetHader.TermID.length + packetHader.DateTime.length + 2]; //little endian
                                        packetHader.BodyLen = (int)buff[packetHader.TermID.length + packetHader.DateTime.length + 2]&0xff; //little endian

//                                        packetHader.BodyLen = buff[packetHader.TermID.length + packetHader.DateTime.length + 3]<<8; //little endian
//                                        packetHader.BodyLen |= buff[packetHader.TermID.length + packetHader.DateTime.length + 2];   //little endian
                                        Log.d(TAG, "packetHader.BodyLen="+packetHader.BodyLen);

                                    }
                                    if(packetHader.BodyLen != 0) {
                                        Arrays.fill(buff, (byte) 0x00);
                                        rxLen=0;
                                    }
                                    cst++;
                                }
                            } else if(cst==2) {						// DATA + ETX
                                bcc ^= readbuffer[offset];
                                if(rxLen+1 <= packetHader.BodyLen)
                                    buff[rxLen++] = (byte)readbuffer[offset];
                                else if(readbuffer[offset] == ETX)
                                    cst++;
                                else {
                                    //strLog.Format(_T("  >>>>>  ETX ERROR[%02X]!"), data);
                                    //DrawLog(strLog);
                                    //SendCommond(NACK);
                                    //SetTimer(5, 1000, NULL);
                                    cst = 0;
                                }
                            } else if(cst==3) {						//BCC
                                bRxPacket = false;
                                if(bcc==readbuffer[offset]) {
                                    if(autoOnOff)
                                        sendMessage(ACK);
                                    onPCComHandle(buff);
                                } else {
                                    if(autoOnOff == false);
                                    else {
                                        //strLog.Format(_T("  >>>>>  BCC ERROR[%02X :: %02X]!"), data, bcc);
                                        //DrawLog(strLog);
                                        sendMessage(NACK);
                                        //	SetTimer(5, 1000, NULL);
                                    }
                                }
                                cst = 0;
                            } else {
                                if(readbuffer[offset]==STX) {
                                    cst++;
                                    bcc = (byte)readbuffer[offset];
                                    Arrays.fill(buff, (byte) 0x00);
                                    rxLen=0;
                                } else if(readbuffer[offset]==ACK) {
                                    //KillTimer(5);
                                    //DrawLog(_T("  >>>>>  통신 전송 완료!!"));
                                } else if(readbuffer[offset]==NACK) {
                                    //DrawLog(_T("  >>>>>  통신 전송 오류!!"));
                                    //SetTimer(5, 1000, NULL);
                                    ;
                                }
                            }

                            offset += 1;
                        }


                        charsRead = 0;
                        offset = 0;
                        mServerMessage = new byte[rxLen];
                        System.arraycopy(buff, 0, mServerMessage, 0, rxLen);
/*
                        if (mServerMessage != null && mMessageListener != null && rxLen>0) {
                            //call the method messageReceived from MyActivity class
                            mMessageListener.messageReceived(mServerMessage);
                        }
*/
                    }

//                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
//                    out.println("Server Received " + str);
                } catch (Exception e) {
                    System.out.println("1 S: Error");
                    e.printStackTrace();
                    client.close();
                } finally {
                    client.close();
                    System.out.println("S: Done.");
                }
            }
        } catch (Exception e) {
            System.out.println("2 S: Error");
            e.printStackTrace();
        }
    }
    public interface OnMessageReceived {
        public void messageReceived(Intent i, byte jobcode);
    }
    public void sendMessage(final String message) {
        Runnable runnable = () -> {
            Log.d(TAG, "sendMessage... run " + message);
            if (mBufferOut != null) {
                Log.d(TAG, "Sending: " + message);
//                        mBufferOut.println(message + "\r\n");
                byte[] sBuf = new byte[256];
                byte[] termId = TERMID.getBytes();
                Arrays.fill(packetHader.TermID, (byte) 0x00);
                System.arraycopy(termId, 0, packetHader.TermID, 0,termId.length);
//                    packetHader.TermID = TERMID.getBytes();
                Log.d(TAG, "packetHader.TermID.length="+packetHader.TermID.length);
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
                String str_date = df.format(new Date());
                packetHader.DateTime = str_date.getBytes();
                packetHader.JobCode = message.getBytes();
                packetHader.ResponCode = 0x00;
                int	sLen=0;

                if(message.equals(CMD_TX_TREMCHACK)||message.equals(CMD_TX_SEARCH)||
                        message.equals(CMD_TX_WAITNG)||message.equals(CMD_RX_TREMCHACK)||
                        message.equals(CMD_RX_SEARCH)||message.equals(CMD_RX_WAITNG)||
                        message.equals(CMD_TX_UID)||message.equals(CMD_TX_RESET))
                {
                    packetHader.BodyLen = 0;
                }
                else if(message.equals(CMD_TX_PAY)||message.equals(CMD_RX_PAY)) {
                    onMakePacketCmdPay(message);
                }
                else if(message.equals(CMD_TX_PAYCANCEL)||message.equals(CMD_RX_PAYCANCEL))
                {
                    onMakePacketCmdPayCancel(message);
                }
                sLen = packetHader.BodyLen;

                if(message.equals(CMD_TX_PAYCANCEL)||message.equals(CMD_RX_PAYCANCEL)){
                    sBuf = onMakeTransBufCmdCancel(sBuf, message);
                }
                else {
                    sBuf = onMakeTransBufCmd(sBuf, message);
                }

                Log.d(TAG, "#######장치체크 [%d]#######");

                int len = 0, i;
                byte buf[]=new byte[126];
                byte rbcc;

                buf[len++] = STX;
                rbcc = STX;
                for(i=0; i<packetHader.TermID.length; i++) {
                    buf[len] = packetHader.TermID[i];
                    rbcc ^= buf[len++];
                }
                //rbcc ^= buf[len++];
                for(i=0; i<14; i++) {
                    buf[len] = packetHader.DateTime[i];
                    rbcc ^= buf[len++];
                }
                buf[len] = packetHader.JobCode[0];
                rbcc ^= buf[len++];
                buf[len] = 0x00;
                rbcc ^= buf[len++];		//업무코드
                buf[len] = (byte)((sLen) & 0xff);
                rbcc ^= buf[len++];		//응답 코드
                buf[len] = (byte)((sLen) >> 8);
                rbcc ^= buf[len++];		//body 길이

                for(i=0;i<sLen;i++) {
                    buf[len] = sBuf[i];
                    rbcc ^= buf[len++];
                }

                buf[len] = ETX;
                rbcc ^= buf[len++];
                buf[len++] = rbcc;
//                    send(ah_socket,(char*)&buf[0], len, 0);
                byte sendBuff[] = new byte[len];
                for(int s = 0; s<len; s++) {
                    sendBuff[s] = buf[s];
                    //Log.d(TAG, "sendbuff["+s+"]="+sendBuff[s]);
                }
                onPrintCardTransInfo();
                try {
                    mBufferOut.write(sendBuff);
                    mBufferOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.setName(message);
        thread.start();

//        mThreadList.add(thread);
    }
    public void sendMessage(final byte message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "sendMessage.byte.. run " + message);
                if (mBufferOut != null) {
                    Log.d(TAG, "Sending: " + message);
                    byte len = 1;
                    byte sendBuff[] = new byte[len];
                    sendBuff[0] = message;

                    try {
                        mBufferOut.write(sendBuff);
                        mBufferOut.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.setName(String.valueOf(message));
        thread.start();

//        mThreadList.add(thread);
    }
    void onPCComHandle(byte data[])
    {
        int	rxLen=0;
        String strtmp;
        String tempS = null;
        byte cardNum[] = new byte[16];
        byte date[] = new byte[14];
        byte account[] = new byte[10];
        byte balance[] = new byte[8];
        byte tax[] = new byte[8];
        byte serviceFee[] = new byte[8];
        byte installment[] = new byte[2];
        byte approveNum[] = new byte[12];

        byte uniqueNum[] = new byte[12];
        byte franchisee[] = new byte[15];
        byte terminalNum[] = new byte[14];
        byte issuer[] = new byte[4];
        byte issuerName[] = new byte[16];
        byte acquirer[] = new byte[4];
        byte acquirerName[] = new byte[16];
        byte answercode[] = new byte[4];
        byte answermsg[] = new byte[30];

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss", Locale.KOREA);
        String sTime = df.format(new Date());
//        Intent sendIntent = new Intent(externalVarApp.ACTION_CMD_CARDREADER);
        Bundle b = new Bundle();

        Intent sendIntent = new Intent(App.APP.getApplicationContext(), CustomPaymentActivity.class);

//        KillTimer(2);

        b.putInt("respon error code", packetHader.ResponCode);
        if(packetHader.ResponCode != 0x00) {	// 응답 코드 에러
            sendIntent.putExtras(b);
            APP.getApplicationContext().sendBroadcast(sendIntent);
            if(packetHader.ResponCode != 0x01) {
                Log.d(TAG, "미등록 단말기 ID");
                //DrawLog(_T("미등록 단말기 ID"));

            }
            else if(packetHader.ResponCode != 0x02) {
                Log.d(TAG,"전송 일시 오류");
            }
            else if(packetHader.ResponCode != 0x03) {
                Log.d(TAG,"정의되지 않은 job Cord");
            }
            else if(packetHader.ResponCode != 0x04) {
                Log.d(TAG,"Date 길이 오류");
            }
            else if(packetHader.ResponCode != 0x05)
                Log.d(TAG,"ETX 오류");
            else if(packetHader.ResponCode != 0x06)
            {
                Log.d(TAG,"BCC 오류");
            }
            return;
        }

        Log.d(TAG,"#####################");
        String termIdStr = new String(packetHader.TermID);
        Log.d(TAG,"단말기ID : "+termIdStr);
        Log.d(TAG,"시간 : "+ sTime);
        String jobStr = new String(packetHader.JobCode);
        Log.d(TAG,"업무 코드 : "+ jobStr);
        Log.d(TAG,"데이터 길이 : "+ packetHader.BodyLen);


        b.putInt("jobcode", packetHader.JobCode[0]);

        if(packetHader.JobCode[0]=='@')    //0X64= '@'
        {

            Log.d(TAG, "이벤트 :"+String.valueOf(data[0]));
            b.putInt("event", data[0]);
            switch (data[0])
            {
                case 0x4D:  //M MS 카드인식 시
                    paymentStatus |= MS_CARD;
                    break;
                case 0x52:  //R RF 카드인식 시
                    paymentStatus |= RF_CARD;
                    break;
                case 0x49:  //I IC 카드인식 시
                    paymentStatus |= CARDIN_STATUS;
                    break;
                case 0x4F:  //I IC 카드 제거 시
                    paymentStatus |= CARDOUT_STATUS;
                    Log.d(TAG, "이벤트 :"+String.valueOf(data[0]));
                    break;
                case 0x46:  //I IC FALLBACK 발생 시
                    break;
                default:
                    break;
            }
//            strLog.Format(_T("이벤트 : %c"), data[0]);
//            DrawLog(strLog);
        }
        switch(packetHader.JobCode[0]) {
            case CMD_RX_TREMCHACK:
                if (data[0] == 'N') {
                    Log.d(TAG, "카드 모듈 미설치");
                    b.putString("card module", "N");
                } else if (data[0] == 'O') {
                    Log.d(TAG, "카드 모듈 정상");
                    b.putString("card module", "O");
                } else {
                    Log.d(TAG, "카드 모듈 오류");
                    b.putString("card module", "E");
                }

                if (data[1] == 'O') {
                    Log.d(TAG, "RF 상태 정상");
                    b.putString("RF module", "O");
                } else {
                    Log.d(TAG, "RF 모듈 오류");
                    b.putString("RF module", "E");
                }
                if (data[2] == 'N')
                {
                    Log.d(TAG, "VAN 서버연결 미설정");
                    b.putString("VAN Server connect", "E");
                }
                else if (data[2]  == 'O') {
                    Log.d(TAG,"VAN 서버연결 상태 정상");
                    b.putString("VAN Server connect", "O");
                }
                else if (data[2]  == 'X') {
                    Log.d(TAG, "VAN 서버연결 디바이스 연결");
                    b.putString("VAN Server connect", "X");
                }
                else {
                    Log.d(TAG,"VAN 서버 연결 실패");
                    b.putString("VAN Server connect", "E");
                }
                if(data[3]  == 'N') {
                    Log.d(TAG, "연동 서버연결 미설정");
                    b.putString("inter Server connect", "N");
                }
                else if (data[3]  == 'O')
                {
                    Log.d(TAG,"연동 서버연결 상태 정상");
                    b.putString("inter Server connect", "O");
                }
                else if (data[3]  == 'X') {
                    Log.d(TAG,"연동 서버연결 디바이스 연결");
                    b.putString("inter Server connect", "X");
                }
                else {
                    Log.d(TAG, "연동 서버 연결 실패");
                    b.putString("inter Server connect", "E");
                }
                break;
            case CMD_RX_SEARCH:
                Log.d(TAG,"### DATA ###");
                if(data[0] == '1')
                {
                    Log.d(TAG,"거래 매체 : IC 카드");
                    b.putString("Trading medium", "IC");
                }
                else if(data[0] == '2') {
                    Log.d(TAG, "거래 매체 : MS 카드");
                    b.putString("Trading medium", "MS");
                }
                else if(data[0] == '3') {
                    Log.d(TAG, "거래 매체 : RF 카드");
                    b.putString("Trading medium", "RF");
                }
                else {
                    Log.d(TAG, "거래 매체 : KEYIN, 현금영수증");
                    b.putString("Trading medium", "CA");
                }

                Log.d(TAG,"카드 종류 : ");
                switch(data[1]) {
                    case 'T':
                        Log.d(TAG,"티머니");
                        b.putString("card kind", "T Money");
                        break;
                    case 'E':
                        Log.d(TAG,"캐시비");
                        b.putString("card kind", "cashbee");
                        break;
                    case 'M':
                        Log.d(TAG,"마이비");
                        b.putString("card kind", "mybee");
                        break;
                    case 'U':
                        Log.d(TAG,"유페이");
                        b.putString("card kind", "youpay");
                        break;
                    case 'H':
                        Log.d(TAG,"한페이");
                        b.putString("card kind", "hanpay");
                        break;
                    case 'K':
                        Log.d(TAG,"코레일");
                        b.putString("card kind", "korail");
                        break;
                    case 'P':
                        Log.d(TAG,"후불");
                        b.putString("card kind", "postpay");
                        break;
                    default:
                        b.putString("card kind", "unknown");
                        Log.d(TAG,"알수없음["+ data[50]+"]");
                        break;
                }
                System.arraycopy(data, 6, cardNum, 0, 16);
                tempS = new String(cardNum);
                Log.d(TAG,"카드 번호 : "+tempS);
                b.putString("card number", tempS);

                System.arraycopy(data, 22, date, 0, 14);
                tempS = new String(date);
                Log.d(TAG, "직전 거래일시 : "+tempS);
                b.putString("trading day", tempS);

                System.arraycopy(data, 36, account, 0, 8);
                tempS = new String(account);
                Log.d(TAG, "직전 거래 금액 : "+tempS+"원");
                b.putString("trading amount", tempS);

                System.arraycopy(data, 44, balance, 0, 8);
                tempS = new String(balance);
                Log.d(TAG, "카드 잔액 : "+tempS+"원");
                b.putString("card balance", tempS);

                Log.d(TAG, "거래 구분 : ");
                if(data[52]=='0') {
                    Log.d(TAG,"거래내역 없음");
                    b.putString("deal division", "0");
                }
                else if(data[52]=='O')
                {
                    Log.d(TAG,"마지막 거래 : 결제");
                    b.putString("deal division", "O");
                }
                else
                {
                    Log.d(TAG,"마지막 거래 : 취소");
                    b.putString("deal division", "C");
                }
                break;
            case CMD_RX_PAY :
            case CMD_RX_PAYCANCEL:
                Log.d(TAG,"### DATA ###");
                if(data[0] != 'X'){
                    if(data[0] == '1')
                    {
                        Log.d(TAG,"신용 승인");
                        b.putString("payment method", "1");
                    }
                    else if(data[0] == '2')
                    {
                        Log.d(TAG,"현금 영수증");
                        b.putString("payment method", "2");
                    }
                    else
                    {
                        Log.d(TAG,"선불카드");
                        b.putString("payment method", "3");
                    }

                    if(packetHader.JobCode[0]==CMD_RX_PAYCANCEL) {
                        Log.d(TAG," 취소");
                        b.putString("payment method", "c");
                    }

                    if(data[1] == '1') {
                        Log.d(TAG," [IC 거래]");
                        b.putString("Trading medium", "IC");
                    }
                    else if(data[1] == '2') {
                        Log.d(TAG, " [MS 거래]");
                        b.putString("Trading medium", "MS");
                    }
                    else if(data[1] == '3') {
                        Log.d(TAG, " [RF 거래]");
                        b.putString("Trading medium", "RF");
                    }
                    else {
                        Log.d(TAG," [KEYIN, 현금영수증");
                        b.putString("Trading medium", "CA");
                    }


                    System.arraycopy(data, 6, cardNum, 0, 16);
                    tempS = new String(cardNum);
                    Log.d(TAG,"카드 번호 : "+tempS);
                    b.putString("card number", tempS);

                    System.arraycopy(data, 22, account, 0, 10);
                    tempS = new String(account);
                    Log.d(TAG, "거래 금액 : "+tempS+"원");
                    b.putString("trading amount", tempS);

                    System.arraycopy(data, 32, tax, 0, 8);
                    tempS = new String(tax);
                    Log.d(TAG, "세 금 : "+tempS+"원");
                    b.putString("tax amount", tempS);

                    System.arraycopy(data, 40, serviceFee, 0, 8);
                    tempS = new String(serviceFee);
                    Log.d(TAG, "봉사료 : "+tempS+"원");
                    b.putString("service fee", tempS);

                    System.arraycopy(data, 48, installment, 0, 2);
                    tempS = new String(installment);
                    Log.d(TAG, "할부개월 : "+tempS+"개월");
                    b.putString("Installment month", tempS);

                    System.arraycopy(data, 50, approveNum, 0, 12);
                    tempS = new String(approveNum);
                    Log.d(TAG, "승인 번호 : "+tempS);
                    b.putString("Approval number", tempS);

                    switch(data[50]) {
                        case 'T':
                            Log.d(TAG,"티머니");
                            b.putString("card kind", "T Money");
                            break;
                        case 'E':
                            Log.d(TAG,"캐시비");
                            b.putString("card kind", "cashbee");
                            break;
                        case 'M':
                            Log.d(TAG, "마이비");
                            b.putString("card kind", "mybee");
                            break;
                        case 'U':
                            Log.d(TAG, "유페이");
                            b.putString("card kind", "youpay");
                            break;
                        case 'H':
                            Log.d(TAG, "한페이");
                            b.putString("card kind", "hanpay");
                            break;
                        case 'K':
                            Log.d(TAG, "코레일");
                            b.putString("card kind", "korail");
                            break;
                        case 'P':
                            Log.d(TAG, "후불");
                            b.putString("card kind", "postpay");
                            break;
                        default:
                            Log.d(TAG, "알수없음["+data[50]+"]");
                            b.putString("card kind", "unknown");
                            break;
                    }
                    System.arraycopy(data, 56, balance, 0, 6);
                    tempS = new String(balance);
                    Log.d(TAG, "[잔액 : "+tempS+"원]");
                    b.putString("card balance", tempS);

                    System.arraycopy(data, 62, date, 0, 14);
                    tempS = new String(date);
                    Log.d(TAG, "매출 시간 : "+tempS);
                    b.putString("trading day", tempS);

                    System.arraycopy(data, 76, uniqueNum, 0, 12);
                    tempS = new String(uniqueNum);
                    Log.d(TAG, "거래 고유 번호 : "+tempS);
                    b.putString("Transaction number", tempS);

                    System.arraycopy(data, 88, franchisee, 0, 15);
                    tempS = new String(franchisee);
                    tempS = tempS.replace(" ","");
                    Log.d(TAG, "가맹점 번호  : "+tempS);
                    b.putString("chain number", tempS);


                    System.arraycopy(data, 103, terminalNum, 0, 14);
                    tempS = new String(terminalNum);
                    Log.d(TAG, "단말기번호  : "+tempS);
                    b.putString("set number", tempS);

                    System.arraycopy(data, 117, issuer, 0, 4);
                    tempS = new String(issuer);
                    Log.d(TAG, "발급사 : "+tempS);
                    b.putString("issueer", tempS);

//                    strLog += MakeStringLog(&data[121], 16);
//                    DrawLog(strLog);
                    System.arraycopy(data, 121, issuerName, 0, 16);
                    try {
                        tempS = new String(issuerName,"EUC-KR");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "발급사명 : "+tempS);
                    b.putString("issueerName", tempS);

                    System.arraycopy(data, 137, acquirer, 0, 4);
                    tempS = new String(acquirer);
                    Log.d(TAG, "매입사 : "+tempS);
                    b.putString("acquirer", tempS);

                    System.arraycopy(data, 141, acquirerName, 0, 16);
                    try {
                        tempS = new String(acquirerName, "EUC-KR");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "매입사명 : "+tempS);
                    b.putString("acquirerName", tempS);

//                    strLog += MakeStringLog(&data[141], 16);
//                    DrawLog(strLog);
                } else {
                    b.putString("payment method", "X");
                    System.arraycopy(data, 117, answercode, 0, 4);
                    try {
                        tempS = new String(answercode, "EUC-KR");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG,"@@@ 거래 거절@@@");
                    Log.d(TAG,"응답코드 : ["+tempS+"]");
                    b.putString("refuse answercode", tempS);

                    System.arraycopy(data, 120, answermsg, 0, 30);
                    try {
                        tempS = new String(answermsg, "EUC-KR");
                        b.putString("answercode", tempS);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG,"응답 메세지 : "+tempS);
                }
                break;
            case CMD_RX_UID : {
                byte[] tempdata = new byte[8];
                System.arraycopy(data, 0, tempdata, 0, 8);
                String cardUid = null;
                try {
                    cardUid = new String(tempdata, "EUC-KR");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "카드 UID : " + cardUid);
                b.putString("card uid", cardUid);
                //strLog.Format(_T("카드 UID : %c%c%c%c%c%c%c%c%c%c"), data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                //DrawLog(strLog);
            }
                break;
        }
        sendIntent.putExtras(b);
        if(mMessageListener!=null)
            mMessageListener.messageReceived(sendIntent, packetHader.JobCode[0]);
        else
        {
            Log.e(TAG, "mMessageListener callback error");
        }
//        externalVarApp.mAppContext.startActivity(sendIntent);

        Log.d(TAG,"##########################");
        //DrawLog(_T("#####################"));
    }

    class packetHader {
        byte	TermID[] = new byte[16];     //16byte
        byte	DateTime[] = new byte[14];   //14byte
        byte	JobCode[] = new byte[1];
        byte	ResponCode;
        int 	BodyLen;
    }   //PACKET_HADER_SIZE is origin source 34 byte
    class PacketData {
        byte	CancleType;			// 취소구분코드 '1' 승인번호 검색 취소 '2' 마지막 거래취소
        byte	Type;				// 거래 구분. '1' 신용승인, '2' 현금영수증
        byte[]	Fare = new byte[10];			// 거래 금액, 원거래 금액 + 세금 + 봉사료
        byte[]	Tax = new byte[8];				// 세금
        byte[]	ServiceFee =new byte[8];		// 봉사료
        byte[]	Installment =new byte[2];		// 할부 개월
        byte	bSign;				// 서명 여부 '1' 비 서명, '2' 서명
        byte[]	ApproveCode = new byte[12];	// 승인 번호
        byte[]	OrgDateTime = new byte[8];		// 원 거래일시...	YYYYMMDD
        byte[]	OrgDateHour = new byte[6];	      // 원 거래시간...hhmmss
    }   //57 byte

}


