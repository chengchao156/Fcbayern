package cn.fcbayern.android.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import cn.fcbayern.android.MainApp;
import cn.fcbayern.android.R;
import cn.fcbayern.android.common.MyDialog;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.ToastUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout back;

    private EditText uesrName;
    private EditText psw;

    private TextView regist;
    private TextView findPsw;
    private Button login;

    private static int uid;
    private static String avatar;
    private static String userN;

    private Dialog dialog;
    private TextView title;

    private HashMap<String, String> info = new HashMap<>();
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        setOnClick();
        preferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
    }

    private void setOnClick() {
        back.setOnClickListener(this);
        regist.setOnClickListener(this);
        findPsw.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    private void initView() {
        back = (RelativeLayout) this.findViewById(R.id.back);
        regist = (TextView) this.findViewById(R.id.regist);
        findPsw = (TextView) this.findViewById(R.id.findPsw);
        login = (Button) this.findViewById(R.id.bt_login);
        uesrName = (EditText) this.findViewById(R.id.etUserName);
        psw = (EditText) this.findViewById(R.id.etPassword);
        View view = LayoutInflater.from(this).inflate(R.layout.loading_dialog,null);
        dialog = new MyDialog(this,R.style.dialog);
        dialog.setContentView(view);
        title = (TextView) view.findViewById(R.id.title);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.regist:
                //跳转到注册界面
                Intent regist = new Intent(this, RegistActivity.class);
                startActivity(regist);
                this.finish();
                break;
            case R.id.findPsw:
                //跳转到找回密码界面
                Intent findPsw = new Intent(this, FindPswActivity.class);
                startActivity(findPsw);
                this.finish();
                break;
            case R.id.bt_login:
                if (String.valueOf(uesrName.getText()).equals("")) {
                    ToastUtils.showToast(this,"用户名不能为空");
                } else if (String.valueOf(psw.getText()).equals("")) {
                    ToastUtils.showToast(this,"密码不能为空");
                } else {
                    title.setText("登录中请稍后");
                    dialog.show();
                    login();
                }
                break;
        }
    }

    private void login() {

        OkHttpClient client = new OkHttpClient();

        final String username = String.valueOf(uesrName.getText());
        String password = String.valueOf(psw.getText());

        FormBody body = new FormBody.Builder()
                .add("action", "login")
                .add("username", username)
                .add("password", password)
                .build();
        if (client.connectTimeoutMillis()>10000){
            dialog.dismiss();
            ToastUtils.showToast(this,"网络连接超时");
        }

        final HashMap<String, String> params = new HashMap<>();
        params.put("action", "login");
        params.put("username", username);
        params.put("password", password);


        Request request = new Request.Builder()
                .url(AddressUtils.USER_URL + NetworkOper.buildQueryParam("login", 0, "", 0, params))
                .post(body)
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
                                    dialog.dismiss();
                                    JSONObject data = object.getJSONObject("data");
                                    uid = data.getInt("uid");
                                    userN = data.getString("username");
                                    String emai = data.getString("email");
                                    String reg_date = data.getString("reg_date");
                                    avatar = data.getString("avatar");
                                    String gender = data.getString("gender");
                                    String birt = data.getString("birth");
                                    String area = data.getString("area");
                                    String callback = data.getString("callback_verify");

                                    info.put("uid", String.valueOf(uid));
                                    info.put("userN", userN);
                                    info.put("emai", emai);
                                    info.put("reg_date", reg_date);
                                    info.put("avatar", avatar);
                                    info.put("gender", gender);
                                    info.put("birt", birt);
                                    info.put("area", area);
                                    info.put("callback", callback);

                                    if(birt.equals("未设置")||gender.equals("未设置")||area.equals("未设置")){
                                        MainApp.isShow = true;
                                    }else {
                                        MainApp.isShow = false;
                                    }

                                    MainApp.UserInfo userInfo = MainApp.UserInfo.getUserInfo();
                                    userInfo.setUserInfo(info);

                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt("uid", uid);
                                    editor.putString("avatar",avatar);
                                    editor.putString("userN",userN);
                                    editor.putBoolean("isLogin", true);
                                    editor.putString("call",callback);

                                    editor.commit();
                                    MainApp.isLogin =true;
                                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                    LoginActivity.this.finish();
                                } else if (code.equals("-1")) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(LoginActivity.this,"填写信息不完整");
                                } else if (code.equals("-2")) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(LoginActivity.this,"用户不存在");
                                } else if (code.equals("-3")) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(LoginActivity.this,"密码错误");
                                } else if (code.equals("-4")) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(LoginActivity.this,"发生未知错误");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {

                }
            }
        });
    }

}