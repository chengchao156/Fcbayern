package cn.fcbayern.android.ui;

import android.support.v4.app.Fragment;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import cn.fcbayern.android.data.DataManager;
import cn.fcbayern.android.util.LogUtils;
import cn.fcbayern.android.util.UpdateHelper;

/**
 * Created by chenzhan on 15/7/30.
 */
public class RefreshFragment extends Fragment implements DataManager.DataLoadListListener {

    protected PullToRefreshListView mListView;
    protected int mMainOperator = -1;

    protected Runnable mRefresh = new Runnable() {
        @Override
        public void run() {
            refreshData();
        }
    };

    protected void checkRefreshList() {
        UpdateHelper.checkRefresh(getActivity(), hashCode(), mRefresh);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            if(getActivity() != null) {
                checkRefreshList();
            }
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if(getActivity() != null) {
//            checkRefreshList();
//        }
//    }

    @Override
    public void loadComplete(int errorCode, boolean fromCache, int operator, boolean append) {

        if (mMainOperator == operator) {
            if (!fromCache) {
                if (!append) {
                    UpdateHelper.record(hashCode());
                }
            } else {
                UpdateHelper.checkRefresh(getActivity(), hashCode(), mRefresh);
            }
        }
    }

    public void refreshData() {
        if (mListView != null) {
            mListView.setRefreshing(true);
        }
    }
}
