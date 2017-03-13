package cn.fcbayern.android.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.umeng.analytics.MobclickAgent;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.fcbayern.android.MainApp;
import cn.fcbayern.android.R;
import cn.fcbayern.android.common.SpinnerProgressDialog;
import cn.fcbayern.android.data.DataManager;
import cn.fcbayern.android.data.NaviMenuData;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.DataReport;

public class MainActivity extends AppCompatActivity implements NaviFragment.NaviSelListener, View.OnClickListener {

    private SlidingMenu mSlideMenu;
    private TextView mTitleView;
    private ImageView search;
    private NaviFragment mNaviFragment;

    private ImageView dismiss;
    private ImageView wanshan;

    private boolean isShow = false;
    private PopupWindow mPopWindow;
    private SpinnerProgressDialog mDialog;

    private RelativeLayout pop;
    private LinearLayout container;
    Animation up;
    Animation down;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        search = (ImageView) this.findViewById(R.id.search);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SerachActivity.class);
                startActivity(intent);
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mTitleView = (TextView) findViewById(R.id.title);

        mSlideMenu = new SlidingMenu(this);
        mSlideMenu.setMode(SlidingMenu.LEFT_RIGHT);
        mSlideMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mSlideMenu.setBehindOffsetRes(R.dimen.sliding_offset);
        mSlideMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);

        mSlideMenu.setMenu(R.layout.layout_left_menu);
        mSlideMenu.setSecondaryMenu(R.layout.layout_right_menu);

        toolbar.setNavigationIcon(R.drawable.ic_navi);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlideMenu.toggle();
                DataReport.report(MainActivity.this, DataReport.NAV_EVENTID, DataReport.NAV_KEY, DataReport.NAV_VAL);
            }
        });

        mNaviFragment = NaviFragment.createInstance(MainActivity.this);
        getSupportFragmentManager().beginTransaction().replace(R.id.left_menu_container, mNaviFragment).commit();

        MatchCenterFragment rightFragment = MatchCenterFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.right_menu_container, rightFragment).commit();

        Fragment home = getSupportFragmentManager().findFragmentByTag(String.valueOf(R.id.navi_home));
        if (home == null) {
            home = MainFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, home, String.valueOf(R.id.navi_home)).commit();
        }
        mSlideMenu.getContent().setBackgroundColor(Color.WHITE);
        getWindow().setBackgroundDrawable(null);
        preferences = getSharedPreferences("isShow", Context.MODE_PRIVATE);
        editor = preferences.edit();
        pop = (RelativeLayout) this.findViewById(R.id.pop);
        dismiss = (ImageView) pop.findViewById(R.id.iv_disMiss);
        wanshan = (ImageView) pop.findViewById(R.id.wanshan);
        dismiss.setOnClickListener(this);
        wanshan.setOnClickListener(this);
        container = (LinearLayout) this.findViewById(R.id.container);
        up = AnimationUtils.loadAnimation(this, R.anim.test_translate);
        down = AnimationUtils.loadAnimation(this, R.anim.test_translate2);

        MainApp.UserInfo userInfo = MainApp.UserInfo.getUserInfo();
        if (preferences != null && userInfo != null) {
            MainApp.isShow = preferences.getBoolean("isshow", true);
            if (MainApp.isLogin && userInfo.isperfect() && MainApp.isShow) {
                pop.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pop.startAnimation(up);
                        pop.setVisibility(View.VISIBLE);
                    }
                }, 1000);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_match) {
            mSlideMenu.showSecondaryMenu();
            DataReport.report(this, DataReport.NAV_EVENTID, DataReport.GAME_CENTER_KEY, DataReport.GAME_CENTER_VAL);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNaviItemClick(final int pos) {
        mSlideMenu.toggle();
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                jumpToModule(pos);
            }
        }, 200);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNaviSel();
        MobclickAgent.onResume(this);
        MainApp.UserInfo userInfo = MainApp.UserInfo.getUserInfo();
        if (preferences != null && userInfo != null) {
            MainApp.isShow = preferences.getBoolean("isshow", true);
            if (MainApp.isLogin && userInfo.isperfect() && MainApp.isShow) {
                pop.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pop.startAnimation(up);
                        pop.setVisibility(View.VISIBLE);
                        isShow = true;
                    }
                }, 1000);
            }
        }
        if (isShow) {
            pop.setVisibility(View.GONE);
            isShow = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        pop.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        DataManager.getImageLoader().clearMemCache();
        super.onDestroy();
    }

    private void checkNaviSel() {
        List<Fragment> list = getSupportFragmentManager().getFragments();
        for (Fragment f : list) {
            if (!TextUtils.isEmpty(f.getTag()) && !f.isHidden()) {
                String id = f.getTag();
                for (int i = 0; i < NaviMenuData.modelsList.size(); i++) {
                    NaviMenuData model = NaviMenuData.modelsList.get(i);
                    if (model.id == Integer.valueOf(id)) {
                        mNaviFragment.setNaviSel(i);
                        return;
                    }
                }
            }
        }
    }

    private void jumpToModule(int pos) {

        int id = NaviMenuData.modelsList.get(pos).id;
        naviDataReport(id);
        int titleId = NaviMenuData.modelsList.get(pos).strRes;

        mSlideMenu.clearIgnoredViews();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(id));
        if (fragment == null) {
            switch (id) {
                case R.id.navi_home:
                    fragment = MainFragment.newInstance();
                    break;
                case R.id.navi_news:
                    fragment = new NewsFragment();
                    break;
                case R.id.navi_photos:
                    fragment = new PhotosFragment();
                    break;
                case R.id.navi_settings:
                    fragment = new SettingFragment();
                    break;
                case R.id.navi_team:
                    fragment = new TeamFragment();
                    break;
                case R.id.navi_video:
                    fragment = new VideoFragment();
                    break;
                case R.id.navi_stand:
                    fragment = new RankFragment();
                    break;
                case R.id.navi_match:
                    fragment = new ScheduleFragment();
                    break;
                case R.id.navi_shop:
                    fragment = ActionWebFragment.createInstance(AddressUtils.SHOP_URL, getString(titleId), true);
                    break;
                case R.id.navi_club:
                    fragment = ActionWebFragment.createInstance(AddressUtils.CLUB_URL + AddressUtils.APP_PARAM, getString(titleId), false);
                    break;
            }
            transaction.add(R.id.container, fragment, String.valueOf(id));
        }
        mTitleView.setText(titleId);
        List<Fragment> list = getSupportFragmentManager().getFragments();
        for (Fragment f : list) {
            if (!TextUtils.isEmpty(f.getTag())) {
                if (f != fragment) {
                    transaction.hide(f);
                } else {
                    transaction.show(f);
                }
            }
        }
        transaction.commit();
    }

    private void naviDataReport(int id) {
        // data report
        switch (id) {
            case R.id.navi_home:
                DataReport.report(this, DataReport.NAV_EVENTID, DataReport.NAV_KEY, DataReport.NAV_INDEX_VAL);
                break;
            case R.id.navi_news:
                DataReport.report(this, DataReport.NAV_EVENTID, DataReport.NAV_KEY, DataReport.NAV_NEWS_VAL);
                break;
            case R.id.navi_photos:
                DataReport.report(this, DataReport.NAV_EVENTID, DataReport.NAV_KEY, DataReport.NAV_PHOTOS_VAL);
                break;
            case R.id.navi_settings:
                DataReport.report(this, DataReport.NAV_EVENTID, DataReport.NAV_KEY, DataReport.NAV_SETTING_VAL);
                break;
            case R.id.navi_shop:
                DataReport.report(this, DataReport.NAV_EVENTID, DataReport.NAV_KEY, DataReport.NAV_SHOP_VAL);
                break;
            case R.id.navi_team:
                DataReport.report(this, DataReport.NAV_EVENTID, DataReport.NAV_KEY, DataReport.NAV_TEAM_VAL);
                break;
            case R.id.navi_stand:
                DataReport.report(this, DataReport.NAV_EVENTID, DataReport.NAV_KEY, DataReport.NAV_STAND_VAL);
                break;
            case R.id.navi_club:
                DataReport.report(this, DataReport.NAV_EVENTID, DataReport.NAV_KEY, DataReport.NAV_CLUB_VAL);
                break;
            case R.id.navi_video:
                DataReport.report(this, DataReport.NAV_EVENTID, DataReport.NAV_KEY, DataReport.NAV_VIDEO_VAL);
                break;
            case R.id.navi_match:
                DataReport.report(this, DataReport.NAV_EVENTID, DataReport.NAV_KEY, DataReport.NAV_MATCH_VAL);
                break;
        }
    }

    public void showLoadingDlg() {
        mDialog = new SpinnerProgressDialog(this);
        mDialog.setMessage(R.string.end_refreshing_label);
        mDialog.setCancelable(false);
        try {
            mDialog.show();
        } catch (Exception e) {
        }
    }

    public void dismissDlg() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
        } catch (Exception e) {
        }
    }

    public SlidingMenu getSlideMenu() {
        return mSlideMenu;
    }

    private boolean isExit = false;

    @Override
    public void onBackPressed() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {
            finish();
            System.exit(0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_disMiss:
                pop.startAnimation(down);
                pop.setVisibility(View.GONE);
                MainApp.isShow = false;
                editor.putBoolean("isshow", MainApp.isShow);
                editor.commit();
                break;
            case R.id.wanshan:
                pop.startAnimation(down);
                pop.setVisibility(View.GONE);
                pop.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, PersonInfoActivity.class);
                        startActivity(intent);
                        MainApp.isShow = false;
                        editor.putBoolean("isshow", MainApp.isShow);
                        editor.commit();
                    }
                }, 500);
                break;
        }
    }
}
