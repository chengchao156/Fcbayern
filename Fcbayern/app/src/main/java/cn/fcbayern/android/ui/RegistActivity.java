package cn.fcbayern.android.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

public class RegistActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout back;

    private EditText userName;
    private EditText Email;
    private EditText passWord;
    private TextView login;
    private Button regist;

    private Dialog dialog;
    private TextView title;
    private TextView service;
    private CheckBox check;

    private HashMap<String, String> info = new HashMap<>();

    private static int uid;
    private static String avatar;
    private static String userN;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        initView();
        setOnClick();
        preferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
    }

    private void setOnClick() {
        back.setOnClickListener(this);
        login.setOnClickListener(this);
        regist.setOnClickListener(this);
        service.setOnClickListener(this);
    }

    private void initView() {
        back = (RelativeLayout) this.findViewById(R.id.back);
        login = (TextView) this.findViewById(R.id.tvLogin);
        login.setText(Html.fromHtml("已有账户？" + "<u>" + "立即登陆" + "</u>"));
        regist = (Button) this.findViewById(R.id.bt_Regist);
        userName = (EditText) this.findViewById(R.id.etUserName);
        Email = (EditText) this.findViewById(R.id.etEmail);
        passWord = (EditText) this.findViewById(R.id.etPsw);
        View view = LayoutInflater.from(this).inflate(R.layout.loading_dialog, null);
        dialog = new MyDialog(this, R.style.dialog);
        dialog.setContentView(view);
        title = (TextView) view.findViewById(R.id.title);
        service = (TextView) this.findViewById(R.id.service);
        service.setText(Html.fromHtml("我已阅读并同意"+"<font color='#dd410b'>网站服务及隐私协议</font>"));
        check = (CheckBox) this.findViewById(R.id.check);
        check.setChecked(true);
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.tvLogin:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_Regist:
                if (String.valueOf(userName.getText()).equals("")) {
                    ToastUtils.showToast(this, "用户名不能为空");
                } else if (String.valueOf(passWord.getText()).equals("")) {
                    ToastUtils.showToast(this, "密码不能为空");
                } else if (String.valueOf(Email.getText()).equals("")) {
                    ToastUtils.showToast(this, "邮箱不能为空");
                } else if (!check.isChecked()) {
                    ToastUtils.showToast(this, "请仔细阅读服务和协议");
                } else {
                    title.setText("注册中请稍后");
                    dialog.show();
                    regist();
                }
                break;
            case R.id.service:
                Intent service = new Intent(this,ServiceActivity.class);
                startActivity(service);
                break;
        }
    }

    private void regist() {

        OkHttpClient client = new OkHttpClient();

        String username = String.valueOf(userName.getText());
        String password = String.valueOf(passWord.getText());
        String email = String.valueOf(Email.getText());

        FormBody body = new FormBody.Builder()
                .add("action", "register")
                .add("username", username)
                .add("password", password)
                .add("email", email)
                .build();

        HashMap<String, String> params = new HashMap<>();
        params.put("action", "register");
        params.put("username", username);
        params.put("password", password);
        params.put("email", email);

        Request request = new Request.Builder()
                .url(AddressUtils.USER_URL + NetworkOper.buildQueryParam("register", 0, "", 0, params))
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

                                    if (birt.equals("未设置") || gender.equals("未设置") || area.equals("未设置")) {
                                        MainApp.isShow = true;
                                    } else {
                                        MainApp.isShow = false;
                                    }

                                    MainApp.UserInfo userInfo = MainApp.UserInfo.getUserInfo();
                                    userInfo.setUserInfo(info);
                                    MainApp.isLogin = true;

                                    Toast.makeText(RegistActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt("uid", uid);
                                    editor.putBoolean("isLogin", true);
                                    editor.putString("call", callback);
                                    editor.commit();

                                    finish();
                                } else if (code.equals("-1")) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(RegistActivity.this, "填写信息不完整");
                                } else if (code.equals("-2")) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(RegistActivity.this, "用户已存在");
                                } else if (code.equals("-3")) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(RegistActivity.this, "用户名不合法");
                                } else if (code.equals("-4")) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(RegistActivity.this, "包含要允许注册的词语");
                                } else if (code.equals("-6")) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(RegistActivity.this, "Email格式有误");
                                } else if (code.equals("-7")) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(RegistActivity.this, "Email不允许被注册");
                                } else if (code.equals("-8")) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(RegistActivity.this, "该Emain已经被注册");
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
