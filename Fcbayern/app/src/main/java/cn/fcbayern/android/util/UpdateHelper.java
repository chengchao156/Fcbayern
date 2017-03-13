package cn.fcbayern.android.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.HashMap;


/**
 * Created by chenzhan on 15/7/30.
 */
public class UpdateHelper {

    private static SparseArray<Long> lastUpdateTime = new SparseArray<>();

    private final static int INTERVAL = 5 * 60 * 1000;

    public static void checkRefresh(Context activity, int tag, Runnable runnable) {
        final long last = lastUpdateTime.get(tag) == null ? 0 : lastUpdateTime.get(tag);
        if ((System.currentTimeMillis() - last) >  INTERVAL) {
            ((Activity) activity).getWindow().getDecorView().postDelayed(runnable, 400);
        }
    }

    public static void record(int tag) {
        lastUpdateTime.put(tag, System.currentTimeMillis());
    }
}
