package cn.fcbayern.android.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import cn.fcbayern.android.R;
import cn.fcbayern.android.data.DataManager;
import cn.fcbayern.android.model.PlayerModel;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.AddressUtils;

public class PlayerDetailActivity extends AppCompatActivity implements DataManager.DataLoadDetailListener {


    public static final String ID_KEY = "id";
    public static final String ID_POS = "coach";

    private PlayerModel mModel = new PlayerModel();

    private View mRoot;
    private LinearLayout mIntroLayout;
    private TextView mDescView;
    private ImageView mPhotoView;
    private TextView mNumberView;
    private TextView mNameView;
    private TextView mNameEnView;
    private TextView mPosView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        int playerId = getIntent().getIntExtra(ID_KEY, 0);
        boolean isCoach = getIntent().getBooleanExtra(ID_POS, false);
        if (playerId > 0) {
            mRoot = findViewById(R.id.content);
            mDescView = (TextView) findViewById(R.id.experience);
            mPhotoView = (ImageView) findViewById(R.id.image);
            mNumberView = (TextView) findViewById(R.id.number);
            mNameView = (TextView) findViewById(R.id.name);
            mNameEnView = (TextView) findViewById(R.id.name_en);
            mPosView = (TextView) findViewById(R.id.type);
            mIntroLayout = (LinearLayout) findViewById(R.id.intro_container);

            mModel.id = playerId;
            HashMap<String, String> params = new HashMap<>();
            params.put("is_coach", isCoach ? "1" : "0");
            NetworkOper.getDetail(NetworkOper.Req.TEAM, mModel, params, this);
        } else {
            finish();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
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
                mRoot.setVisibility(View.VISIBLE);
                if(mModel.desc.equals("")){
                    mDescView.setText("该球员暂无介绍");
                }else {
                    mDescView.setText(mModel.desc);
                }
                mNumberView.setText(mModel.number);
                mNameView.setText(mModel.name);
                mNameEnView.setText(mModel.nameEn);
                mPosView.setText(TextUtils.isEmpty(mModel.posName) ? mModel.title : mModel.posName);

                if (!TextUtils.isEmpty(mModel.birthday)) {
                    View view = getLayoutInflater().inflate(R.layout.list_item_intro, null);
                    ((TextView)view.findViewById(R.id.key)).setText(R.string.birthday);
                    ((TextView)view.findViewById(R.id.value)).setText(mModel.birthday);
                    mIntroLayout.addView(view);
                }
                if (!TextUtils.isEmpty(mModel.birthPlace)) {
                    View view = getLayoutInflater().inflate(R.layout.list_item_intro, null);
                    ((TextView)view.findViewById(R.id.key)).setText(R.string.birthplace);
                    ((TextView)view.findViewById(R.id.value)).setText(mModel.birthPlace);
                    mIntroLayout.addView(view);
                }
                if (!TextUtils.isEmpty(mModel.zodiac)) {
                    View view = getLayoutInflater().inflate(R.layout.list_item_intro, null);
                    ((TextView)view.findViewById(R.id.key)).setText(R.string.zodiac);
                    ((TextView)view.findViewById(R.id.value)).setText(mModel.zodiac);
                    mIntroLayout.addView(view);
                }
                if (!TextUtils.isEmpty(mModel.height)) {
                    View view = getLayoutInflater().inflate(R.layout.list_item_intro, null);
                    ((TextView)view.findViewById(R.id.key)).setText(R.string.bodyvalue);
                    ((TextView)view.findViewById(R.id.value)).setText(mModel.height + "/" + mModel.weight + "/" + mModel.shoeSize);
                    mIntroLayout.addView(view);
                }
                if (!TextUtils.isEmpty(mModel.family)) {
                    View view = getLayoutInflater().inflate(R.layout.list_item_intro, null);
                    ((TextView)view.findViewById(R.id.key)).setText(R.string.marry);
                    ((TextView)view.findViewById(R.id.value)).setText(mModel.family);
                    mIntroLayout.addView(view);
                }
                if (!TextUtils.isEmpty(mModel.edu)) {
                    View view = getLayoutInflater().inflate(R.layout.list_item_intro, null);
                    ((TextView)view.findViewById(R.id.key)).setText(R.string.edu);
                    ((TextView)view.findViewById(R.id.value)).setText(mModel.edu);
                    mIntroLayout.addView(view);
                }

                int width = mPhotoView.getLayoutParams().height * mModel.imageW / mModel.imageH;
                ViewGroup.LayoutParams lp = mPhotoView.getLayoutParams();
                lp.width = width;

                DataManager.getImageLoader().loadImage(AddressUtils.IMAGE_PREFIX + mModel.imageUrl, mPhotoView);
            }
        });
    }

}
