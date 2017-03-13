package cn.fcbayern.android.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpDateSexActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout back;
    private TextView man;
    private TextView woman;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_up_date_sex);
        initView();
        setOnClick();
    }

    private void setOnClick() {
        back.setOnClickListener(this);
        man.setOnClickListener(this);
        woman.setOnClickListener(this);
    }

    private void initView() {
        back = (RelativeLayout) this.findViewById(R.id.back);
        man = (TextView) this.findViewById(R.id.man);
        woman = (TextView) this.findViewById(R.id.woman);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.man:
                update("男");
                break;
            case R.id.woman:
                update("女");
                break;
        }
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
        params.put("field_name", "gender");
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
                                        userInfo.setGender(str);
                                    }
                                    Toast.makeText(UpDateSexActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                                    UpDateSexActivity.this.finish();
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
