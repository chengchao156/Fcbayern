package cn.fcbayern.android.common;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;

import cn.fcbayern.android.R;

/**
 * Created by chenzhan on 15/6/9.
 */
public class CustomLoadingLayout extends LoadingLayout {

    public CustomLoadingLayout(Context context, PullToRefreshBase.Mode mode, PullToRefreshBase.Orientation scrollDirection) {
        super(context, mode, scrollDirection);

        LayoutInflater.from(context).inflate(R.layout.layout_refresh_header, this);

        mInnerLayout = (FrameLayout) findViewById(R.id.fl_inner);
        mHeaderImage = (ImageView) findViewById(R.id.pull_to_refresh_image);
        mHeaderProgress = new ProgressBar(context);

        mHeaderText = (TextView) findViewById(R.id.pull_to_refresh_text);
        mSubHeaderText = new TextView(context);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mInnerLayout.getLayoutParams();
        switch (mode) {
            case PULL_FROM_END:
                lp.gravity = scrollDirection == PullToRefreshBase.Orientation.VERTICAL ? Gravity.TOP : Gravity.LEFT;

                // Load in labels
                setPullLabel(context.getString(R.string.end_pull_to_refresh_label));
                setReleaseLabel(context.getString(R.string.end_release_to_refresh_label));
                setRefreshingLabel(context.getString(R.string.end_refreshing_label));
                break;

            case PULL_FROM_START:
            default:
                lp.gravity = scrollDirection == PullToRefreshBase.Orientation.VERTICAL ? Gravity.BOTTOM : Gravity.RIGHT;

                // Load in labels
                setPullLabel(context.getString(R.string.start_pull_to_refresh_label));
                setReleaseLabel(context.getString(R.string.start_release_to_refresh_label));
                setRefreshingLabel(context.getString(R.string.start_refreshing_label));
                break;
        }

        setLoadingDrawable(context.getResources().getDrawable(R.drawable.ani_loading));

        reset();
    }

    @Override
    protected int getDefaultDrawableResId() {
        return 0;
    }

    @Override
    protected void onLoadingDrawableSet(Drawable imageDrawable) {

    }

    @Override
    protected void onPullImpl(float scaleOfLayout) {

    }

    @Override
    protected void pullToRefreshImpl() {
        mHeaderImage.setVisibility(View.GONE);
    }

    @Override
    protected void refreshingImpl() {
        mHeaderImage.setVisibility(View.VISIBLE);
    }

    @Override
    protected void releaseToRefreshImpl() {

    }

    @Override
    protected void resetImpl() {

    }
}
