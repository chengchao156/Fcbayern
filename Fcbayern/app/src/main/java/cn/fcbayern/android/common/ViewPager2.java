package cn.fcbayern.android.common;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import cn.fcbayern.android.util.LogUtils;

/**
 *
 *  FIX IllegalArgumentException BUG for ViewPager.
 *
 */
public class ViewPager2 extends ViewPager {

    private boolean mCanScroll = true;

    public ViewPager2(Context context) {
        super(context);
    }

    public ViewPager2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mCanScroll) {
            return false;
        }
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            if (getAdapter() != null) {
                getAdapter().notifyDataSetChanged();
            }
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mCanScroll) {
            return false;
        }
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void setCanScroll(boolean can) {
        mCanScroll = can;
    }
}
