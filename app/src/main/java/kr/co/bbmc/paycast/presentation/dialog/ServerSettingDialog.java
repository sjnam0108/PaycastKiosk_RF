package kr.co.bbmc.paycast.presentation.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import kr.co.bbmc.paycast.R;

public class ServerSettingDialog extends DialogFragment {
    private static String TAG = "SettingDialog";
    private EditText mReceiptNum = null;
    private Button mCancelBtn = null;
    private TextView mChgLock = null;
    private TextView mQuitApp = null;
    private TextView mClosingSet = null;
    private Button mOrderCancelBtn = null;
    private TextView mServerSet = null;
    private TextView mPrintSet = null;

    private String serverIp = "";
    private String serverPort = "";
    private String serverSite = "";
    private boolean activeMode = false;
    private boolean playerExe = true;
    private String playerMonPeriod = "3";
    private String title = null;
    private static agentButtonClickListener mCallBack = null;


    public interface agentButtonClickListener {
        public void onPostonDetach();
        public void oSettingServer();
        public void onQuitApp();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.srv_setting_layout, container, false);
        if(title!=null) {
            getDialog().setTitle(title);
        }
        mServerSet = (TextView)v.findViewById(R.id.agent_setting_id);
        mServerSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCallBack!=null)
                    mCallBack.oSettingServer();
            }
        });
        mQuitApp = (TextView)v.findViewById(R.id.quit_app_id);
        mQuitApp.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(mCallBack!=null)
                    mCallBack.onQuitApp();
            }
        });
        return v;
    }
    public void setPlayerParam(boolean exePlayer, int period)
    {
        this.playerExe = exePlayer;
        this.playerMonPeriod = Integer.toString(period);
    }
    public void setServerParam(String serverIp, int port, String site)
    {
        this.serverIp = serverIp;
        this.serverPort = Integer.toString(port);
        this.serverSite = site;
    }
    public void setTitle(String t)
    {
        this.title = t;
    }
    public void setCallBack(agentButtonClickListener callBack)
    {
        mCallBack=callBack;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        if(mCallBack!=null)
            mCallBack.onPostonDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mCallBack!=null)
            mCallBack.onPostonDetach();

    }
}
