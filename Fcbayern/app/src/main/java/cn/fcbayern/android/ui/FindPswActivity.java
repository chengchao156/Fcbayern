package cn.fcbayern.android.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import cn.fcbayern.android.R;
import cn.fcbayern.android.common.MyDialog;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.ToastUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FindPswActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout back;

    private EditText userName;
    private EditText email;
    private Button find;

    private Dialog dialog;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_psw);
        initView();
        setOnClick();
    }

    private void setOnClick() {
        back.setOnClickListener(this);
        find.setOnClickListener(this);
    }

    private void initView() {
        back = (RelativeLayout) this.findViewById(R.id.back);
        userName = (EditText) this.findViewById(R.id.etUserName);
        email = (EditText) this.findViewById(R.id.etEmail);
        find = (Button) this.findViewById(R.id.bt_find);
        View view = LayoutInflater.from(this).inflate(R.layout.loading_dialog, null);
        dialog = new MyDialog(this, R.style.dialog);
        dialog.setContentView(view);
        title = (TextView) view.findViewById(R.id.title);
    }

    private void findPassword() {

        OkHttpClient client = new OkHttpClient();

        String name = userName.getText().toString();
        String em = email.getText().toString();

        HashMap<String, String> params = new HashMap<>();
        params.put("action", "getRecode");
        params.put("username", name);
        params.put("email", em);

        Request request = new Request.Builder()
                .url(AddressUtils.USER_URL + NetworkOper.buildQueryParam("getRecode", 0, "", 0, params))
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
                                int code = object.getInt("code");
                                if (code == 0) {
                                    dialog.dismiss();
                                    Toast.makeText(FindPswActivity.this, "邮件已发送至邮箱", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else if (code == -2) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(FindPswActivity.this, "Email 格式有误");
                                } else if (code == -3) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(FindPswActivity.this, "Email有误");
                                } else if (code == -4) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(FindPswActivity.this, "用户不存在");
                                } else if (code == -5) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(FindPswActivity.this, "账号与邮箱不匹配");
                                } else if (code == -6) {
                                    dialog.dismiss();
                                    ToastUtils.showToast(FindPswActivity.this, "操作失败");
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

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.bt_find:
                if (userName.getText().toString().equals("")) {
                    ToastUtils.showToast(this, "请先输入用户名");
                } else if (email.getText().toString().equals("")) {
                    ToastUtils.showToast(this, "请输入邮箱地址");
                } else {
                    title.setText("操作进行中");
                    dialog.show();
                    findPassword();
                }
                break;
        }
    }
}
