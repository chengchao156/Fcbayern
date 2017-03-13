package cn.fcbayern.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cn.fcbayern.android.data.DataManager;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.R;
import cn.fcbayern.android.common.ShareDialog;
import cn.fcbayern.android.common.ViewPager2;
import cn.fcbayern.android.model.BaseModel;
import cn.fcbayern.android.model.PhotoModel;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.DialogUtils;
import uk.co.senab.photoview.PhotoView;

public class PhotoActivity extends AppCompatActivity implements DataManager.DataLoadDetailListener {

    private ViewPager2 mViewPager;
    private PhotoPagerAdapter mAdapter;

    public static final String KEY_CONTENT = "CONTENT";
    public static final String KEY_TYPE = "TYPE";

    private PhotoModel mModel = new PhotoModel();

    private TextView mPhotoTitle;
    private TextView mIndex;
    private TextView main_title;
    private View mFooter;

    private int mFrom;
    private int mPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        Intent intent = getIntent();
        int id = intent.getIntExtra(KEY_CONTENT, -1);
        mFrom = intent.getIntExtra(KEY_TYPE, -1);
        if (id < 0 || mFrom < 0) {
            finish();
            return;
        }

        mFooter = findViewById(R.id.footer);
        mPhotoTitle = (TextView) findViewById(R.id.photo_title);
        mIndex = (TextView) findViewById(R.id.index);

        main_title = (TextView) findViewById(R.id.main_title);

        mViewPager = (ViewPager2) findViewById(R.id.container);
        mAdapter = new PhotoPagerAdapter();
        mViewPager.setAdapter(mAdapter);

        for (int i = 0; i < DataManager.getData(mFrom).size(); i++) {
            if (DataManager.getData(mFrom).get(i).id == id) {
                mModel = (PhotoModel) DataManager.getData(mFrom).get(i);
                mPos = i;
                break;
            }
        }

        mModel.id = id;

        initData();

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position >= mModel.pics.size()) {
                    mViewPager.setCanScroll(false);
                    mFooter.setVisibility(View.GONE);
                    mViewPager.postDelayed(mGotoNext, 300);
                } else {
                    setFooter(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private Runnable mGotoNext = new Runnable() {
        @Override
        public void run() {
            ArrayList<BaseModel> list = DataManager.getData(mFrom);
            for (int i = mPos + 1; i < list.size(); i++) {
                if (list.get(i).type == BaseModel.PHOTO_ITEM) {
                    mModel = (PhotoModel) DataManager.getData(mFrom).get(i);
                    mPos = i;
                    initData();
                    break;
                }
            }
        }
    };

    private void initData() {

        if (mModel.pics.size() <= 0) {
            NetworkOper.getDetail(NetworkOper.Req.PHOTO, mModel, PhotoActivity.this);
        } else {
            mViewPager.setCanScroll(true);
            mAdapter.setContent(mModel);
            mViewPager.setAdapter(mAdapter);
        }
    }

    private void setFooter(int position) {
        if (mModel.titles.size()!=0) {
            main_title.setText(mModel.title);
            mFooter.setVisibility(View.VISIBLE);
            mPhotoTitle.setText(mModel.titles.get(0));
            if (position >= 0) {
                mPhotoTitle.setText(mModel.titles.get(position));
                mIndex.setText(Html.fromHtml("<font color=\"#FF0000\">" + (position + 1) + "</font>/" + mModel.pics.size()));
            } else {

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_share:
                ShareDialog dialog = DialogUtils.createShareDialog(this);
                dialog.setShareData(mModel.title, AddressUtils.IMAGE_PREFIX + mModel.imageUrl, "", String.format(AddressUtils.DETAIL_PHOTO_URL, mModel.id));
                dialog.show();
                break;
            case android.R.id.home:
            case android.support.v7.appcompat.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void loadComplete(int errorCode, boolean fromCache, int type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCanScroll(true);
                mAdapter.setContent(mModel);
                mViewPager.setAdapter(mAdapter);
                setFooter(0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        DataManager.getBigImageLoader().clearMemCache();
        super.onDestroy();
    }

    class PhotoPagerAdapter extends PagerAdapter {

        private PhotoModel mModel = null;

        public void setContent(PhotoModel model) {
            mModel = model;
        }

        @Override
        public int getCount() {
            if (mModel == null) {
                return 0;
            }
            return mModel.pics.size() + 1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (getCount() == position + 1) {
                View view = LayoutInflater.from(container.getContext()).inflate(R.layout.layout_photo_next, null);
                container.addView(view);
                return view;
            } else {
                PhotoView view = new PhotoView(container.getContext());
                container.addView(view);
                DataManager.getBigImageLoader().loadImage(AddressUtils.IMAGE_PREFIX + mModel.pics.get(position), view);
                return view;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
