package cn.fcbayern.android.common;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.fcbayern.android.MainApp;
import cn.fcbayern.android.R;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.ui.MainActivity;
import cn.fcbayern.android.ui.PersonInfoActivity;
import cn.fcbayern.android.util.AddressUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * <p>
 * Title: CustomDialog
 * </p>
 * <p>
 * Description:自定义Dialog（参数传入Dialog样式文件，Dialog布局文件）
 * </p>
 * <p>
 * Copyright: Copyright (c) 2014
 * </p>
 *
 * @author zihao
 * @version 1.01
 */
public class CustomDialog extends Dialog implements View.OnClickListener {

    /**
     * 布局文件
     **/
    int layoutRes;
    /**
     * 上下文对象
     **/
    Context context;
    /**
     * 确定按钮
     **/
    private Button confirmBtn;
    /**
     * 取消按钮
     **/
    private Button cancelBtn;


    public static final int TOAST_TIME = 1000;

    public CustomDialog(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * 自定义布局的构造方法
     *
     * @param context
     * @param resLayout
     */
    public CustomDialog(Context context, int resLayout) {
        super(context);
        this.context = context;
        this.layoutRes = resLayout;
    }


    /**
     * 自定义主题及布局的构造方法
     *
     * @param context
     * @param theme
     * @param resLayout
     */
    public CustomDialog(Context context, int theme, int resLayout) {
        super(context, theme);
        this.context = context;
        this.layoutRes = resLayout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 指定布局
        this.setContentView(layoutRes);
        // 根据id在布局中找到控件对象
        confirmBtn = (Button) findViewById(R.id.confirm_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        // 设置按钮的文本颜色
        confirmBtn.setTextColor(0xff1E90FF);
        cancelBtn.setTextColor(0xff1E90FF);
        // 为按钮绑定点击事件监听器
        confirmBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_btn:
                // 点击了确认按钮
                logout();
                break;
            case R.id.cancel_btn:
                // 点击了取消按钮
                this.dismiss();
                break;
        }
    }


    private void logout() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(AddressUtils.USER_URL + NetworkOper.buildQueryParam("logout", 0, "", 0, null))
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
                    try {
                        JSONObject object = new JSONObject(json);
                        int code = object.getInt("code");
                        if (code == 0) {
                            SharedPreferences preferences = getContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            SharedPreferences preferences1 = getContext().getSharedPreferences("isShow", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor1 = preferences1.edit();
                            editor.clear();
                            editor.commit();
                            editor1.clear();
                            editor1.commit();
                            MainApp.isLogin = false;
                            MainApp.isShow = true;
                            CustomDialog.this.dismiss();
                            getOwnerActivity().finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}