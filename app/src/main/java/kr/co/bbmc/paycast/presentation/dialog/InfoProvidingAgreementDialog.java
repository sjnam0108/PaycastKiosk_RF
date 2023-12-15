package kr.co.bbmc.paycast.presentation.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;

import kr.co.bbmc.paycast.R;

public class InfoProvidingAgreementDialog extends DialogFragment implements View.OnClickListener{
    private static String TAG = "InfoProvidingAgreementDialog";
    private String title = "";
    private Button mCloseBtn = null;
    private Button mCloseBottomBtn = null;
    private onClickListener mCallBack = null;


    public interface onClickListener {
        public void onCloseBtnClick();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info_providing_agreement_dialog_layout, container, false);
        if(title!=null) {
            getDialog().setTitle(title);
        }
        mCloseBtn = (Button)v.findViewById(R.id.info_providing_btn_id);
        mCloseBtn.setOnClickListener(this);
        mCloseBottomBtn = (Button)v.findViewById(R.id.info_providing_bottom_btn_id);
        mCloseBottomBtn.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        if(view.equals(mCloseBtn)||view.equals(mCloseBottomBtn))
        {
            if(mCallBack!=null)
                mCallBack.onCloseBtnClick();
        }
    }
    public void setCallBack( onClickListener callBack)
    {
        mCallBack=callBack;
    }

}
