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
import android.widget.TextView;

import androidx.annotation.Nullable;

import kr.co.bbmc.paycast.R;

public class SettingDialog extends DialogFragment {
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
        //public void onConnectClick();
        public void onOrderCancelClick(String cancelNum);
        public void onCancelClick();
        public void onPostonDetach();
        public void onChangeLockCode();
        public void oSettingServer();
        public void onQuitApp();
        public void onClosingSettlement();
        public void oSettingMainPrt();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.setting_layout, container, false);
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
        mChgLock = (TextView)v.findViewById(R.id.chg_lock_id);
        mChgLock.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(mCallBack!=null)
                    mCallBack.onChangeLockCode();
            }
        });
        mClosingSet = (TextView)v.findViewById(R.id.closing_set_id);
        mClosingSet.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(mCallBack!=null)
                    mCallBack.onClosingSettlement();
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
        mPrintSet = (TextView)v.findViewById(R.id.print_setting_id);
        mPrintSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCallBack!=null)
                    mCallBack.oSettingMainPrt();
            }
        });
        mReceiptNum = (EditText)v.findViewById(R.id.receipt_num_id);
        //mReceiptNum.setText(serverIp);


        mOrderCancelBtn = (Button)v.findViewById(R.id.order_cancel_id);
        mOrderCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallBack!=null) {
                    String num = mReceiptNum.getText().toString();
                    mCallBack.onOrderCancelClick(num);
                }
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
