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

import kr.co.bbmc.paycast.R;

public class OnPrintSettingDialog extends DialogFragment {
    private static String TAG = "AgentSettingDialog";
    private EditText mPrinterIp = null;
    private CheckBox mMasterMode = null;
    private Button mTestConnectBtn = null;
    //    private CheckBox mPlayerExe = null;
//    private EditText mPlayerMonPeriod = null;
    private Button mSaveBtn = null;
    private Button mCancelBtn = null;

    private String printIp = "";
    private boolean masterMode = false;
    private String title = null;
    private static printBtnClickListener mCallBack = null;


    public interface printBtnClickListener {
        public void onSaveClick(String host, String enable);

        public void onCancelClick();

    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.print_setting_layout, container, false);
        if (title != null) {
            getDialog().setTitle(title);
        }

        mPrinterIp = (EditText) v.findViewById(R.id.print_ip_id);
        mPrinterIp.setText(printIp);
        mMasterMode = (CheckBox) v.findViewById(R.id.set_master_prt_id);
        mMasterMode.setChecked(masterMode);

        mSaveBtn = (Button) v.findViewById(R.id.option_save_id);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallBack != null)
                    mCallBack.onSaveClick(mPrinterIp.getText().toString(), String.valueOf(mMasterMode.isChecked()));
                else
                    Log.e(TAG, "mSaveBtn.onSaveClick() is not exist!!");
            }
        });
        mCancelBtn = (Button) v.findViewById(R.id.option_cancel_id);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallBack != null)
                    mCallBack.onCancelClick();
                else
                    Log.e(TAG, "mCancelBtn.onCancelClick() is not exist!!");
            }
        });
        return v;
    }

    public void setPrinterParam(String serverIp, String masterMode) {
        this.printIp = serverIp;
        this.masterMode = Boolean.valueOf(masterMode);
    }

    public void setTitle(String t) {
        this.title = t;
    }

    public void setCallBack(printBtnClickListener callBack) {
        mCallBack = callBack;
    }

    public String getAgentServerIp() {
        if ((mPrinterIp == null) || (mPrinterIp.getText().toString().isEmpty()))
            return "";

        return mPrinterIp.getText().toString();
    }


    public boolean getPrintMasterMode() {
        if (mMasterMode == null)
            return false;
        return mMasterMode.isChecked();
    }
    public void upDateDialog() {
        mPrinterIp.setText(printIp);

    }
}
