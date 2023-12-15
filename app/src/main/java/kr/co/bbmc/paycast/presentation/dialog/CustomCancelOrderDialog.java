package kr.co.bbmc.paycast.presentation.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.bbmc.paycast.presentation.mainMenu.CancelOrderData;
import kr.co.bbmc.paycast.R;

public class CustomCancelOrderDialog  extends DialogFragment {
    private static String TAG = "CustomLockCodeDialog";
    private Button mConfirmBtn = null;
    private Button mCancelBtn = null;
    private String title = "";
    private onCustomCancelBtnClickListener mCallBack = null;
    private TextView mOrderNumId;
    private TextView mPaymentDateId;
    private TextView mPaymentMoneyId;
    private TextView mApprovalNumId;
    private CancelOrderData mCancelData;

    public interface onCustomCancelBtnClickListener {
        public void onConfirmClick(CancelOrderData info);
        public void onCancelClick();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.custom_cancel_order_dialog, container, false);
        if(title!=null) {
            getDialog().setTitle(title);
        }

        mOrderNumId = (TextView)v.findViewById(R.id.order_number_id);
        mPaymentDateId = (TextView)v.findViewById(R.id.payment_date_id);
        mPaymentMoneyId = (TextView)v.findViewById(R.id.payment_money_id);
        mApprovalNumId = (TextView)v.findViewById(R.id.approval_number_id);

        mOrderNumId.setText(String.format("%s : %s", getString(R.string.str_order_number), mCancelData.orderNumber));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = null;
        try {
            date = sdf.parse(mCancelData.orderDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf = new SimpleDateFormat("yyyy년MM월dd일 HH:mm:ss");

        mPaymentDateId.setText(String.format("%s : %s", getString(R.string.str_payment_date), sdf.format(date)));

        DecimalFormat decimalFormat = new DecimalFormat("#,###");

        String amount = decimalFormat.format(Float.valueOf(mCancelData.goodsAmt));
        mPaymentMoneyId.setText(String.format("%s : %s원", getString(R.string.str_payment_money), amount));
        mApprovalNumId.setText(String.format("%s : %s", getString(R.string.str_approval_number), mCancelData.authCode));

        mConfirmBtn = (Button)v.findViewById(R.id.lock_confirm_id);
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCancelData!=null) {
                    mCallBack.onConfirmClick(mCancelData);
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
    public void setCallBack( onCustomCancelBtnClickListener callBack)
    {
        mCallBack=callBack;
    }
    public void onSetCancelOrderData(CancelOrderData canceldata)
    {
        mCancelData = canceldata;
    }
}
