package cn.fcbayern.android.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;

/**
 * Created by chenzhan on 15/6/9.
 */
public class PullRefreshListView2 extends PullToRefreshListView {
    public PullRefreshListView2(Context context) {
        super(context);
    }

    public PullRefreshListView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected LoadingLayout createLoadingLayout(Context context, Mode mode, TypedArray attrs) {
        return new CustomLoadingLayout(context, mode, Orientation.VERTICAL);
    }
}
