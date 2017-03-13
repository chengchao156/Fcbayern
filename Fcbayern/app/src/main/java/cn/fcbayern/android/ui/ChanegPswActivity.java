package cn.fcbayern.android.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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

public class ChanegPswActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout back;

    private EditText oldPsw;
    private EditText newPsw;
    private EditText rePsw;

    private Button confirm;

    private String uid;
    private String callback;
    private String original_password;
    private String new_password;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chaneg_psw);
        initView();
        preferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    private void initView() {
        back = (RelativeLayout) this.findViewById(R.id.back);
        back.setOnClickListener(this);
        oldPsw = (EditText) this.findViewById(R.id.etOldPsw);
        newPsw = (EditText) this.findViewById(R.id.etNewPsw);
        rePsw = (EditText) this.findViewById(R.id.etReNewPsw);
        confirm = (Button) this.findViewById(R.id.bt_confirm);
        confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.bt_confirm:
                if (!newPsw.getText().toString().equals(rePsw.getText().toString())) {
                    ToastUtils.showToast(this, "两次新密码输入不一致");
                } else if (oldPsw.getText().toString().equals("")) {
                    ToastUtils.showToast(this, "原始密码不能为空");
                } else if (newPsw.getText().toString().equals("")) {
                    ToastUtils.showToast(this, "新密码不能为空");
                } else if (rePsw.getText().toString().equals("")) {
                    ToastUtils.showToast(this, "请再次确认新密码");
                } else {
                    changePsw();
                }
                break;
        }
    }

    private void changePsw() {
        OkHttpClient client = new OkHttpClient();

        HashMap<String, String> params = new HashMap<>();

        if (preferences != null) {
            uid = String.valueOf(preferences.getInt("uid", 0));
            callback = preferences.getString("call", "");
        }
        original_password = oldPsw.getText().toString();
        new_password = rePsw.getText().toString();

        params.put("action", "modify_password");
        params.put("uid", uid);
        params.put("callback_verify", callback);
        params.put("original_password", original_password);
        params.put("new_password", new_password);

        Request request = new Request.Builder()
                .url(AddressUtils.USER_URL + NetworkOper.buildQueryParam("modify_password", 0, "", 0, params))
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String json = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject object = new JSONObject(json);
                                String code = object.getString("code");
                                if (code.equals("0")) {
                                    JSONObject data = object.getJSONObject("data");
                                    String message = data.getString("message");
                                    Toast.makeText(ChanegPswActivity.this, message + "请重新登录验证", Toast.LENGTH_SHORT).show();
                                    editor.clear();
                                    MainApp.isLogin = false;
                                    editor.commit();
                                    SharedPreferences preferences1 = getSharedPreferences("isShow", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor1 = preferences1.edit();
                                    editor1.clear();
                                    editor1.commit();
                                    MainApp.isShow = true;
                                    Intent intent = new Intent(ChanegPswActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    ChanegPswActivity.this.finish();
                                } else if (code.equals("-1")) {
                                    ToastUtils.showToast(ChanegPswActivity.this, "没有找到用户");
                                } else if (code.equals("-4")) {
                                    ToastUtils.showToast(ChanegPswActivity.this, "原始密码不正确");
                                } else if (code.equals("-5")) {
                                    ToastUtils.showToast(ChanegPswActivity.this, "密码修改失败");
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
