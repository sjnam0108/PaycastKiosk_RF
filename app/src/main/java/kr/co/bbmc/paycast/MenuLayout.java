package kr.co.bbmc.paycast;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuLayout extends FrameLayout {
    protected Context mContext;
    protected View mView;
    protected ImageView mImageView;
    protected TextView mMainTextView;
    protected TextView mSubTextView;
    protected FrameLayout mlayout;

    public MenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        mContext = context;
        init();
    }
    private void init() {
        mView = setInflater();
        mlayout = (FrameLayout)mView.findViewById(R.id.each_menu_layout);
        mImageView = (ImageView)mView.findViewById(R.id.menu_image);
        mMainTextView = (TextView) mView.findViewById(R.id.section_label);
        mSubTextView = (TextView) mView.findViewById(R.id.price_label);
    }

    protected View setInflater(){
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.menu_layout, this);
    }
    public void setPriceText(String text){
        if( mSubTextView.getVisibility() == GONE){
            mSubTextView.setVisibility(VISIBLE);
        }
        mSubTextView.setText(text);
    }
    protected void setListener(OnClickListener listener){
        mlayout.setOnClickListener(listener);
    }

}
