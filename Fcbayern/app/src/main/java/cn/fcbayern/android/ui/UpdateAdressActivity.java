package cn.fcbayern.android.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import cn.fcbayern.android.MainApp;
import cn.fcbayern.android.R;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.ToastUtils;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateAdressActivity extends AdressActivity implements View.OnClickListener, OnWheelChangedListener {

    private RelativeLayout back;
    private RelativeLayout choose;

    private TextView adress;

    private LinearLayout date;
    private WheelView mViewProvince;
    private WheelView mViewCity;

    private TextView mBtnConfirm;
    private TextView cancle;

    private Button commit;
    private Animation animation;
    private Animation animation2;

    private boolean isshowing = false;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_adress);
        initView();
        setUpListener();
        setUpData();
    }

    private void initView() {
        back = (RelativeLayout) this.findViewById(R.id.back);
        back.setOnClickListener(this);
        choose = (RelativeLayout) this.findViewById(R.id.chooseAdress);
        choose.setOnClickListener(this);
        mViewProvince = (WheelView) findViewById(R.id.id_province);
        mViewCity = (WheelView) findViewById(R.id.id_city);

        mBtnConfirm = (TextView) findViewById(R.id.btn_confirm);

        date = (LinearLayout) this.findViewById(R.id.Adress);
        cancle = (TextView) this.findViewById(R.id.cancle);

        animation = AnimationUtils.loadAnimation(this, R.anim.test_translate);
        animation2 = AnimationUtils.loadAnimation(this, R.anim.test_translate2);

        adress = (TextView) this.findViewById(R.id.adress);
        commit = (Button) this.findViewById(R.id.commit);
    }

    private void setUpListener() {
        mViewProvince.addChangingListener(this);
        mViewCity.addChangingListener(this);
        mBtnConfirm.setOnClickListener(this);
        cancle.setOnClickListener(this);
        commit.setOnClickListener(this);
    }

    private void setUpData() {
        initProvinceDatas();
        mViewProvince.setViewAdapter(new ArrayWheelAdapter<>(this, mProvinceDatas));
        mViewProvince.setVisibleItems(7);
        mViewCity.setVisibleItems(7);
        updateCities();
        updateAreas();
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if (wheel == mViewProvince) {
            updateCities();
        } else if (wheel == mViewCity) {
            updateAreas();
        }
    }

    private void updateAreas() {
        int pCurrent = mViewCity.getCurrentItem();
        mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[pCurrent];
        String[] areas = mDistrictDatasMap.get(mCurrentCityName);

        if (areas == null) {
            areas = new String[]{""};
        }
    }

    private void updateCities() {
        int pCurrent = mViewProvince.getCurrentItem();
        mCurrentProviceName = mProvinceDatas[pCurrent];
        String[] cities = mCitisDatasMap.get(mCurrentProviceName);
        if (cities == null) {
            cities = new String[]{""};
        }
        mViewCity.setViewAdapter(new ArrayWheelAdapter<>(this, cities));
        mViewCity.setCurrentItem(0);
        updateAreas();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.chooseAdress:
                if (isshowing) {
                    date.startAnimation(animation2);
                    date.setVisibility(View.GONE);
                    isshowing = false;
                } else {
                    date.setAnimation(animation);
                    date.setVisibility(View.VISIBLE);
                    isshowing = true;
                }

                break;
            case R.id.cancle:
                date.startAnimation(animation2);
                date.setVisibility(View.GONE);
                isshowing = false;
                break;
            case R.id.btn_confirm:
                adress.setText(mCurrentProviceName + "-" + mCurrentCityName);
                date.startAnimation(animation2);
                date.setVisibility(View.GONE);
                break;
            case R.id.commit:
                if (adress.getText().toString().equals("")) {
                    ToastUtils.showToast(this, " 请先选择地址");
                } else {
                    update(adress.getText().toString());
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isshowing) {
                date.startAnimation(animation2);
                date.setVisibility(View.GONE);
                return true;
            }
            if (isshowing) {
                date.startAnimation(animation2);
                date.setVisibility(View.GONE);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void update(final String str) {

        OkHttpClient client = new OkHttpClient();

        HashMap<String, String> params = new HashMap<>();
        String uid = "";
        String call = "";

        preferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);

        final MainApp.UserInfo userInfo = MainApp.UserInfo.getUserInfo();

        if (preferences != null) {
            uid = String.valueOf(preferences.getInt("uid", 0));
            call = preferences.getString("call", "");
        }

        params.put("action", "profile_update");
        params.put("uid", uid);
        params.put("callback_verify", call);
        params.put("field_name", "area");
        params.put("field_value", str);


        Request request = new Request.Builder()
                .url(AddressUtils.USER_URL + NetworkOper.buildQueryParam("profile_update", 0, "", 0, params))
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String json = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject object = new JSONObject(json);
                                String code = object.getString("code");
                                if (code.equals("0")) {
                                    if (userInfo != null) {
                                        userInfo.setArea(str);
                                    }
                                    Toast.makeText(UpdateAdressActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                                    UpdateAdressActivity.this.finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }
}
