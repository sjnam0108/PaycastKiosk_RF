package kr.co.bbmc.paycast;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

public class NumIndicator extends LinearLayout implements View.OnClickListener{
    Context mContext;
    LinearLayout mIndicatorNumLayout;
    TextView mIndicatorNumText;
    View mView;
    protected NumIndicatorListener mListener;

    public interface NumIndicatorListener {
        void onClickNumIndicator(int pos);
    }

    private final ViewPager.OnPageChangeListener mInternalPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Log.d("NumIdicator", "onPageSelected() position="+position);

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public NumIndicator(Context context, NumIndicatorListener listener) {
        super(context);
        mContext =context;
        mListener = listener;
        init();
    }
    private void init() {
        mView = setInflater();
        mIndicatorNumLayout = (LinearLayout) mView.findViewById(R.id.indicator_num_layout);
        mIndicatorNumText = (TextView) mView.findViewById(R.id.indicator_num_text);
        mIndicatorNumText.setOnClickListener(this);

    }

    protected View setInflater() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.indicator_layout, this);
    }

    public void setIndicatorNumText(String text, int color, int bg_res_id)
    {
        mIndicatorNumText.setTag(Integer.parseInt(text));
        mIndicatorNumText.setText(text);
        mIndicatorNumText.setTextColor(color);
        mIndicatorNumLayout.setBackgroundResource(bg_res_id);
        mIndicatorNumLayout.setTag(Integer.parseInt(text));
    }
    public void setIndicatorNumLayoutMargin(int left, int right)
    {
        LayoutParams layoutParams =(LayoutParams) mIndicatorNumLayout.getLayoutParams();
        layoutParams.leftMargin = left;
        layoutParams.rightMargin = right;
        mIndicatorNumLayout.setLayoutParams(layoutParams);
    }

    @Override
    public void onClick(View view) {
        Object tag = view.getTag();
//        if (tag != null)
        {
            int position = (int) tag;
            if(position > 0)
                position-=1;
            //Log.e("NumIndicator", " click position="+position);
            if(mListener!=null)
                mListener.onClickNumIndicator(position);
        }
    }
}
