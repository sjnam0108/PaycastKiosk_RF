package kr.co.bbmc.paycast.presentation.dialog;

import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import kr.co.bbmc.paycast.R;

public class CustomNewLockCodeDialog extends DialogFragment {
    private static String TAG = "CustomLockCodeDialog";
    private EditText mNewLockNum = null;
    private EditText mReLockNum = null;
    private Button mConfirmBtn = null;
    private Button mConcelBtn = null;
    private String title = "";
    private onNewLockClickListener mCallBack = null;


    public interface onNewLockClickListener {
        public void onCancelClick();
        public void onConfirmClick(String newlock);
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.custom_new_lock_dialog, container, false);
        if(title!=null) {
            getDialog().setTitle(title);
        }


        mNewLockNum = (EditText)v.findViewById(R.id.lockcode_num1_id);
        mReLockNum = (EditText)v.findViewById(R.id.lockcode_num2_id);
        mConcelBtn = (Button)v.findViewById(R.id.cconcel_id);
        mConcelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCallBack!=null)
                    mCallBack.onCancelClick();
            }
        });
        mConfirmBtn = (Button)v.findViewById(R.id.confirm_id);
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallBack!=null) {
                    String lock = mNewLockNum.getText().toString();
                    String reLock = mReLockNum.getText().toString();
                    if(lock.equals(reLock)) {
                        mCallBack.onConfirmClick(lock);
                    }
                    else
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Toast.makeText(getContext(),  R.string.str_not_match_new_lock_code,  Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        return v;
    }
    public void setTitle(String t)
    {
        this.title = t;
    }
    public void setCallBack( onNewLockClickListener callBack)
    {
        mCallBack=callBack;
    }
}
