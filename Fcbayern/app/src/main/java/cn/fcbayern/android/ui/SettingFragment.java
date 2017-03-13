package cn.fcbayern.android.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v4.preference.PreferenceFragment;
import android.view.View;

import java.io.File;

import cn.fcbayern.android.MainApp;
import cn.fcbayern.android.data.DataManager;
import cn.fcbayern.android.R;
import cn.fcbayern.android.common.MyPreference;
import cn.fcbayern.android.util.DeviceUtils;
import cn.fcbayern.android.util.DialogUtils;
import cn.fcbayern.android.util.FileUtils;
import cn.fcbayern.android.util.Global;
import cn.fcbayern.android.util.IOUtils;
import cn.fcbayern.android.util.PreferencesMgr;
import cn.fcbayern.android.util.ToastUtils;
import cn.fcbayern.android.util.cache.CacheUtils;
import cn.jpush.android.api.JPushInterface;

/**
 * Created by chenzhan on 15/5/26.
 */
public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private MyPreference mCachePref;
    private CheckBoxPreference mPushPref;
    private MyPreference changePsw;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mCachePref = (MyPreference) findPreference(PreferencesMgr.CACHE_KEY);
        mCachePref.setValue(String.valueOf(getCacheSize()) + "MB");
        mCachePref.setActionFlagVisible(View.VISIBLE);

        changePsw = (MyPreference) findPreference(PreferencesMgr.CHANGE_PSW);

        MyPreference versionPref = (MyPreference) findPreference(PreferencesMgr.VERSION_KEY);
        versionPref.setValue(DeviceUtils.getVersionName(Global.sContext));
        versionPref.setActionFlagVisible(View.INVISIBLE);

        mPushPref = (CheckBoxPreference) findPreference(PreferencesMgr.PREFS_PUSH_KEY);
        boolean push = PreferencesMgr.getBoolean(PreferencesMgr.PREFS_PUSH_KEY, true);
        mPushPref.setChecked(push);
        mPushPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            getView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCachePref.setValue(String.valueOf(getCacheSize()) + "MB");
                }
            }, 200);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if (PreferencesMgr.CACHE_KEY.equals(key)) {
            DialogUtils.createCommonDialog(getActivity(),
                    getActivity().getResources().getString(R.string.clear_cache_tips),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clearCache(new Runnable() {
                                @Override
                                public void run() {
                                    mCachePref.setValue("0K");
                                }
                            });
                        }
                    }).show();

        }else if(PreferencesMgr.CHANGE_PSW.endsWith(key)){
            if(MainApp.isLogin){
                Intent intent = new Intent(getActivity(),ChanegPswActivity.class);
                startActivity(intent);
            }else {
                ToastUtils.showToast(getActivity(),"请在登录后修改");
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private static float getCacheSize() {
        float size = FileUtils.getDirSize(new File(Global.sContext.getFilesDir().getPath()));
        size += FileUtils.getDirSize(CacheUtils.getDiskCacheDir(Global.sContext, "http"));
        return (float) Math.round(size / 1024f / 1024f * 100) / 100;
    }

    private void clearCache(Runnable retJob) {
        IOUtils.deleteAllFilesOfDir(new File(Global.sContext.getFilesDir().getPath()));
        DataManager.getImageLoader().clearAllCache();
        getActivity().runOnUiThread(retJob);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (PreferencesMgr.PREFS_PUSH_KEY.equals(preference.getKey())) {
            mPushPref.setChecked((boolean) newValue);
            if ((boolean)newValue) {
                JPushInterface.resumePush(getActivity().getApplicationContext());
            } else {
                JPushInterface.stopPush(getActivity().getApplicationContext());
            }
        }
        return false;
    }
}
