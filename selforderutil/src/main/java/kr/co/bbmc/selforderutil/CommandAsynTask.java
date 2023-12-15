package kr.co.bbmc.selforderutil;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CommandAsynTask extends AsyncTask <String, String, String> {
    private static final String TAG = "CommandAsynTask";
    private static int MAX_CONNECTION_TIMEOUT = 10000;  //10초
    private static int MAX_SOCKET_TIMEOUT = 10000;  //10초

    private boolean LOG = false;
    //private AgentExternalVarApp mAgentExterVarApp;
    private List<CommandObject> newcommandList = null;
    private List<CommandObject> commandList = null;
    private StbOptionEnv stbOpt;
    private Context context;

    public void setApplication(List<CommandObject> newcommandList, List<CommandObject> commandList, StbOptionEnv stbOpt, Context context)
    {
        this.newcommandList = newcommandList;
        this.commandList = commandList;
        this.stbOpt = stbOpt;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        List<CommandObject> delCommandList = new ArrayList<CommandObject>();
        if (newcommandList.size() > 0) {
            int size = newcommandList.size();
            for (int i = 0; i < size; size--) {
                commandList.add(newcommandList.get(i));
                newcommandList.remove(i);
            }
        }
        Log.d(TAG, "CommandAsynTask() commandList.SIZE=" + commandList.size());
        if (commandList.size() > 0) {
            for (CommandObject command : commandList) {
                String result = executeCommand(command);
                String reqUrl = ServerReqUrl.getServerRcCommandUrl(stbOpt, context);
                command.result = result;

                URL Url = null; // URL화 한다.
                try {
                    Url = new URL(reqUrl + "?" + "rcCmdId=" + command.rcCommandid + "&result=" + command.result);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    continue;
                }
                HttpURLConnection conn = null; // URL을 연결한 객체 생성.
                try {
                    conn = (HttpURLConnection) Url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                    conn.disconnect();
                    continue;
                }
                try {
                    conn.setRequestMethod("GET"); // get방식 통신
                } catch (ProtocolException e) {
                    e.printStackTrace();
                    conn.disconnect();
                    continue;
                }
                conn.setDoInput(true); // 읽기모드 지정
                conn.setConnectTimeout(MAX_CONNECTION_TIMEOUT);        // 통신 타임아웃
                conn.setReadTimeout(MAX_SOCKET_TIMEOUT);

                int resCode = -1;

                try {
                    resCode = conn.getResponseCode();
                } catch (IOException e) {
                    e.printStackTrace();
                    conn.disconnect();
                    continue;
                }
                if(resCode==HttpURLConnection.HTTP_OK || resCode == HttpURLConnection.HTTP_CREATED)
                {
                    InputStream is = null; //input스트림 개방
                    try {
                        is = conn.getInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                        conn.disconnect();
                        continue;
                    }

                    StringBuilder builder = new StringBuilder(); //문자열을 담기 위한 객체
                    BufferedReader reader = null; //문자열 셋 세팅
                    try {
                        reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        conn.disconnect();
                        continue;
                    }
                    String line = null;

                    while (true) {
                        try {
                            if (!((line = reader.readLine()) != null)) break;
                        } catch (IOException e) {
                            e.printStackTrace();
                            conn.disconnect();
                            continue;
                        }
                        builder.append(line + "\n");
                    }
                    if(LOG) {
                        Log.d(TAG, "명령실행보고  url=" + String.valueOf(Url));
                        Log.e(TAG, "CommandAsynTask rcCmdId=" + command.rcCommandid + " command="+command.command+" result=" + command.result);
                    }
                    delCommandList.add(command);
                }
            }
        }
        for (CommandObject command : delCommandList) {
            commandList.remove(command);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
//        mCommandAsynTask = null;
    }
    private String executeCommand(CommandObject ci) {
        String result = "F";
        Log.e(TAG, "executeCommand() L84 error command="+ci.command);
/*
        if (ci != null) {
            String playerStatusCode = getPlayerAgentCheck(false);
            boolean playerControlMode = true;
            switch (ci.command) {
                case "UpdatePlayer.bbmc":              // 성공: P
                    if((ci.prameter!=null)&&(!ci.prameter.isEmpty()))
                        result = executeUpdatePlayer(playerControlMode, ci.prameter.replace("version=", ""));
                    else
                        result = executeUpdatePlayer(playerControlMode, "");
                    break;
                case "SetConfig.bbmc":                 // 성공: S
                    result = executeSetConfig(playerControlMode, ci.prameter);
                    Log.e(TAG, "CommandAsynTask SetConfig.bbmc() RESULT="+result);
                    break;
                case "MonitorOn.bbmc":                 // 성공: S
                    result = executeOnMonitor(playerControlMode);
                    break;
                case "MonitorOff.bbmc":                // 성공: S
                    result = executeOffMonitor(playerControlMode);
                    break;
                case "PowerOff.bbmc":                  // 성공: S
                    result = executeOffPower(playerControlMode);
                    break;
                case "Reboot.bbmc":                    // 성공: S
                    result = executeReboot(playerControlMode);
                    break;
                case "HideEmergenText.bbmc":           // 성공: S
                    result = executeHideEmergenText(playerControlMode);
                    break;
                case "SyncContent.bbmc":               // 성공: S(다운로드 대상 항목이 0), P(다운로드 대상 항목 존재 시)
                    Log.e(TAG, "executeCommand() SyncContent.bbmc executeSyncContent()");
                    result = executeSyncContent(ci.rcCommandid);
                    break;
                case "UploadCapture.bbmc":             // 성공: P
                    result = executeUploadCapture();
                    break;
                case "UploadCaptures.bbmc":            // 성공: P
                    result = executeUploadCaptures();
                    break;
                case "UploadTrackFile.bbmc":           // 성공: S(업로드 대상 항목이 0), P(업로드 대상 항목 존재 시)
                    result = executeUploadTrackFile();
                    break;
                case "UploadLogFile.bbmc":             // 성공: S(업로드 대상 항목이 0), P(업로드 대상 항목 존재 시)
                    result = executeUploadLogFile();
                    break;
                case "UploadDebugFile.bbmc":             // 성공: S(업로드 대상 항목이 0), P(업로드 대상 항목 존재 시)
                    result = executeUploadDebugFile();
                    break;
                case "UploadTodayFile.bbmc":             // 성공: S(업로드 대상 항목이 0), P(업로드 대상 항목 존재 시)
                    result = executeUploadTodayFile();
                    break;
                case "PowerOnWol.bbmc":                // 성공: S
                    result = "S";
                    break;
                case "DeleteTrackFile.bbmc":           // 성공: S
                    result = executeDeleteTrackFile(ci.prameter.replace("file=", ""));
                    break;
                case "RestartPlayer.bbmc":             // 성공: S
                    result = executeRestartPlayer(playerControlMode, playerStatusCode);
                    break;
                case "ShowEmergenText.bbmc":           // 성공: S
                    result = executeShowEmergenText(playerControlMode, ci.prameter);
                    break;
                case "DeleteAllSchedule.bbmc":         // 성공: S
                    result = executeDeleteAllSchedule(playerControlMode);
                    break;
                case "Debug10Mins.bbmc":                // 성공: S
                    result = executeDebug10Mins(playerControlMode);
                    break;
                // jason:restartagent: 에이전트 재시작(2015/08/31)
                case "RestartAgent.bbmc":
                    Utils.LOG(getString(R.string.Log_RCRestartAgent));
                    Utils.LOG("");

                    result = "P";
                    isAgentRestartRequired = true;
                    reStartAgent();
                    break;
                //-
                // jason:checkresourcecmd: 리소스 체크 명령(2015/09/10)
                case "CheckResources.bbmc":            // 성공: S
                    result = executeCheckResources();
                    break;
                //-
                case "SetSchedule.bbmc":
                    result = executeSetConfig(playerControlMode, ci.prameter);
                    break;

                default:
                    break;
            }

            switch (ci.command) {
                case "MonitorOn.bbmc":
                case "MonitorOff.bbmc":
                case "RestartPlayer.bbmc":
                case "DeleteAllSchedule.bbmc":
                    //case "SetConfig.bbmc":
                case "PowerOnWol.bbmc":
                    reportStbStatusToServer();
                    break;
                default:
                    break;
            }
        }
*/
        return result;
    }

}
