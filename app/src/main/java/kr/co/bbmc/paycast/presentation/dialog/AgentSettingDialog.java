package kr.co.bbmc.paycast.presentation.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.Nullable;

import kr.co.bbmc.paycast.R;

public class AgentSettingDialog extends DialogFragment {
    private static String TAG = "AgentSettingDialog";
    private EditText mServerIp = null;
    private EditText mServerPort = null;
    private EditText mServerSite = null;
    private CheckBox mActiveMode = null;
    private Button mTestConnectBtn = null;
    //    private CheckBox mPlayerExe = null;
//    private EditText mPlayerMonPeriod = null;
    private Button mSaveBtn = null;
    private Button mCancelBtn = null;

    private String serverIp = "";
    private String serverPort = "";
    private String serverSite = "";
    private boolean activeMode = false;
    private boolean playerExe = true;
    private String playerMonPeriod = "3";
    private String title = null;
    private static agentButtonClickListener mCallBack = null;


    public interface agentButtonClickListener {
        public void onConnectClick(String host, String port, String site);
        public void onSaveClick(String host, String port, String site);
        public void onCancelClick();
        public void onPostonDetach();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.agent_setting_layout, container, false);
        if(title!=null) {
            getDialog().setTitle(title);
        }

        mServerIp = (EditText)v.findViewById(R.id.server_ip_id);
        mServerIp.setText(serverIp);
        mServerPort = (EditText)v.findViewById(R.id.server_port_id);
        mServerPort.setText(serverPort);
        mServerSite = (EditText)v.findViewById(R.id.server_site_id);
        mServerSite.setText(serverSite);
        mActiveMode = (CheckBox)v.findViewById(R.id.active_mode_id);
        mActiveMode.setChecked(activeMode);

        mTestConnectBtn = (Button)v.findViewById(R.id.test_connect_id);
        mTestConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallBack!=null) {
                    mCallBack.onConnectClick(mServerIp.getText().toString(), mServerPort.getText().toString(), mServerSite.getText().toString());
                }
                else
                    Log.e(TAG, "mCallBack.onConnectClick() is not exist!!");
            }
        });
        mSaveBtn = (Button)v.findViewById(R.id.option_save_id);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallBack!=null)
                    mCallBack.onSaveClick(mServerIp.getText().toString(), mServerPort.getText().toString(), mServerSite.getText().toString());
                else
                    Log.e(TAG, "mSaveBtn.onSaveClick() is not exist!!");
            }
        });
        mCancelBtn = (Button)v.findViewById(R.id.option_cancel_id);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallBack!=null)
                    mCallBack.onCancelClick();
                else
                    Log.e(TAG, "mCancelBtn.onCancelClick() is not exist!!");
            }
        });
        return v;
    }
    public void setPlayerParam(boolean exePlayer, int period)
    {
        this.playerExe = exePlayer;
        this.playerMonPeriod = Integer.toString(period);
    }
    public void setServerParam(String serverIp, int port, String site, boolean activeMode)
    {
        this.serverIp = serverIp;
        this.serverPort = Integer.toString(port);
        this.serverSite = site;
        this.activeMode = activeMode;
    }
    public void setTitle(String t)
    {
        this.title = t;
    }
    public void setCallBack(agentButtonClickListener callBack)
    {
        mCallBack=callBack;
    }
    public String getAgentServerIp()
    {
        if((mServerIp==null)||(mServerIp.getText().toString().isEmpty()))
            return "";

        return mServerIp.getText().toString();
    }
    public String getAgentServerSite()
    {
        if((mServerSite==null)||(mServerSite.getText().toString().isEmpty()))
            return "";
        return mServerSite.getText().toString();
    }
    public String getAgentServerPort()
    {
        if((mServerPort==null)||(mServerSite.getText().toString().isEmpty()))
            return "";
        return mServerPort.getText().toString();
    }
    public boolean getAgentActiveMode()
    {
        if(mActiveMode==null)
            return false;
        return mActiveMode.isChecked();
    }
    /*
        public boolean getAgentPlayerExe()
        {
            if(mPlayerExe==null)
                return false;
            return mPlayerExe.isChecked();
        }
        public String getAgentPlayerMonPeriod()
        {
            if((mPlayerMonPeriod==null)||(mPlayerMonPeriod.getText().toString().isEmpty()))
                return "";
            return mPlayerMonPeriod.getText().toString();
        }
    */
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
    public void upDateDialog()
    {
        mServerIp.setText(serverIp);
        mServerPort.setText(serverPort);
        mServerSite.setText(serverSite);

    }
}
