package kr.co.bbmc.paycast.presentation.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import kr.co.bbmc.paycast.R;

public class CustomLockCodeDialog extends DialogFragment {
    private static String TAG = "CustomLockCodeDialog";
    private EditText mLockNum = null;
    private Button mConfirmBtn = null;
    private Button mCancelBtn = null;
    private String title = "";
    private onCustomLockClickListener mCallBack = null;




    public interface onCustomLockClickListener {
        public void onConfirmClick(String s);
        public void onCancelClick();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.custom_lock_dialog, container, false);
        if(title!=null) {
            getDialog().setTitle(title);
        }

        mLockNum = (EditText)v.findViewById(R.id.lockcode_num1_id);
        mConfirmBtn = (Button)v.findViewById(R.id.lock_confirm_id);
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallBack!=null) {
                    String lock = mLockNum.getText().toString();
                    if((lock!=null)&&(!lock.isEmpty()))
                        mCallBack.onConfirmClick(lock);
                }
            }
        });
        mCancelBtn = (Button)v.findViewById(R.id.lock_cancel_id);
        mCancelBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(mCallBack!=null)
                    mCallBack.onCancelClick();
            }
        });

        return v;
    }
    public void setTitle(String t)
    {
        this.title = t;
    }
    public void setCallBack( onCustomLockClickListener callBack)
    {
        mCallBack=callBack;
    }

}
