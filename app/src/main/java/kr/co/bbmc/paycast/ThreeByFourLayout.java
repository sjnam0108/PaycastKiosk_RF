package kr.co.bbmc.paycast;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

public class ThreeByFourLayout extends MenuLayout {
    /*
    mImageView = (ImageView)mView.findViewById(R.id.menu_image);
    mMainTextView = (TextView) mView.findViewById(R.id.section_label);
    mSubTextView = (TextView) mView.findViewById(R.id.price_label);
    */
    protected ThreeByFourLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ImageView setMenuImage(Context context, Drawable d, OnClickListener listener)
    {
        mImageView.setImageDrawable(d);
        if(listener!=null)
            setListener(listener);
        return mImageView;

    }
    public TextView setMenuTitle(Context context, String menuText, OnClickListener listener)
    {
        mMainTextView.setText(menuText);
        if(listener!=null)
            setListener(listener);
        return mMainTextView;

    }
    public TextView setMenuPrice(Context context, String priceText, OnClickListener listener)
    {
        mSubTextView.setText(priceText);
        if(listener!=null)
            setListener(listener);
        return mSubTextView;

    }

    @Override
    protected void setListener(OnClickListener listener) {
        super.setListener(listener);
    }
}
