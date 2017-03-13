package cn.fcbayern.android.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
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
import cn.fcbayern.android.common.ObservableWebView;
import cn.fcbayern.android.common.ShareDialog;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.DialogUtils;
import cn.fcbayern.android.util.Global;
import cn.fcbayern.android.util.ToastUtils;
import cn.fcbayern.android.util.Utils;
import cn.fcbayern.android.util.cache.CacheUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewsWebActivity2 extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout back;
    private TextView title;

    private ImageView share;
    private RelativeLayout bottom;
    private LinearLayout talk;
    private TextView count;

    private ObservableWebView webView;
    private String url;
    private String nid;
    private ProgressBar progress;
    private EditText etComment;

    private String comment_count;
    private String share_link;
    private String share_title;
    private String share_pic;

    private Dialog dia;
    private TextView dialogTitle;

    private Button commit;
    private PopupWindow popupWindow;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsweb2);
        initView();
        url = getIntent().getStringExtra("url");
        nid = getIntent().getStringExtra("nid");
        initUI();
        initDetails();//支持javascript
        webView.getSettings().setJavaScriptEnabled(true);
        // 设置可以支持缩放
        webView.getSettings().setSupportZoom(false);
        // 设置出现缩放工具
        if (Build.VERSION.SDK_INT >= 11) {
            webView.getSettings().setDisplayZoomControls(false);
        } else {
        }
        //扩大比例的缩放
        webView.getSettings().setUseWideViewPort(true);
        //自适应屏幕
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.loadUrl(url);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.equals("http://fcbayern:news_more_commnet/")) {
                    Intent intent = new Intent(NewsWebActivity2.this, CommentActivity.class);
                    intent.putExtra("nid", nid);
                    startActivity(intent);
                } else if (url.contains("fcbayern:vote_share&vid=")) {
                    if (MainApp.isLogin) {
                        String id = url.substring(url.length() - 1);
                        OkHttpClient client = new OkHttpClient();
                        HashMap<String, String> params = new HashMap<>();
                        params.put("action", "get_vote");
                        params.put("id", id);
                        Request request = new Request.Builder()
                                .url(AddressUtils.OTHER_URL + NetworkOper.buildQueryParam("get_vote", 0, "", 0, params))
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
                                                    JSONObject data = object.getJSONObject("data");
                                                    String share_title = data.getString("share_title");
                                                    String share_pic = data.getString("share_pic");
                                                    String share_link = data.getString("share_link");
                                                    ShareDialog dialog = DialogUtils.createShareDialog(NewsWebActivity2.this);
                                                    dialog.setShareData(share_title, share_pic, "", share_link);
                                                    dialog.show();
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        ToastUtils.showToast(NewsWebActivity2.this, "请先登录后再分享");
                    }
                } else if (url.contains("fcbayern:tag_news_list")) {
                    Intent intent = new Intent(NewsWebActivity2.this, TagNewsActivity.class);
                    String[] split = url.split("=");
                    intent.putExtra("tag_id", split[split.length - 1]);
                    startActivity(intent);
                }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(NewsWebActivity2.this);
                builder.setMessage("是否忽略证书继续加载？");
                builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progress.setVisibility(View.GONE);
                } else {
                    if (View.GONE == progress.getVisibility()) {
                        progress.setVisibility(View.VISIBLE);
                    }
                    progress.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void initUI() {

        webView.setBackgroundColor(Color.WHITE);
        // API level < 17的设备有漏洞：
        // WebView上默认添加"searchBoxJavaBridge_"到mJavaScriptObjects中，有可能通过用户信任的客户端获取SD卡的数据。
        if (Utils.hasHoneycomb()) {
            // 删除默认添加的"searchBoxJavaBridge_"
            webView.removeJavascriptInterface("searchBoxJavaBridge_");
        }

        webView.setVisibility(View.VISIBLE);
        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        webView.setAlwaysDrawnWithCacheEnabled(true);
        webView.setDrawingCacheEnabled(true);
        webView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

        webView.freeMemory();

        System.gc();// 进来就清除内存保证视频播放

        final WebSettings setting = webView.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setJavaScriptCanOpenWindowsAutomatically(true);
        setting.setNeedInitialFocus(true);
        // zoom control
        setting.setBuiltInZoomControls(false);
        setting.setSupportZoom(false);

        setting.setUseWideViewPort(true);
        // Use LOAD_CACHE_ELSE_NETWORK instead of LOAD_DEFAULT(res have to be not expired) for multi process
        setting.setCacheMode(WebSettings.LOAD_DEFAULT);


        setting.setLoadWithOverviewMode(true);
        setting.setPluginState(WebSettings.PluginState.ON);
        setting.setDomStorageEnabled(true);

        //启用地理定位
        setting.setGeolocationEnabled(true);
        //设置定位的数据库路径
        setting.setGeolocationDatabasePath(CacheUtils.getDiskCacheDir(Global.sContext, "geo").getAbsolutePath());

        webView.requestFocus();
        webView.setFocusableInTouchMode(true);
        CookieManager.getInstance().setAcceptCookie(true);
    }

    private void initView() {
        back = (RelativeLayout) this.findViewById(R.id.back);
        back.setOnClickListener(this);
        webView = (ObservableWebView) this.findViewById(R.id.details);
        title = (TextView) this.findViewById(R.id.title);
        progress = (ProgressBar) this.findViewById(R.id.progress);
        bottom = (RelativeLayout) this.findViewById(R.id.bottomView);
        share = (ImageView) this.findViewById(R.id.share);
        share.setOnClickListener(this);
        talk = (LinearLayout) this.findViewById(R.id.talk);
        talk.setOnClickListener(this);
        count = (TextView) this.findViewById(R.id.comment_count);
        commit = (Button) this.findViewById(R.id.commitComment);
        commit.setOnClickListener(this);
        etComment = (EditText) this.findViewById(R.id.etComment);
        View view = LayoutInflater.from(this).inflate(R.layout.loading_dialog, null);
        dia = new MyDialog(this, R.style.dialog);
        dia.setContentView(view);
        dialogTitle = (TextView) view.findViewById(R.id.title);
    }

    private void initDetails() {

        OkHttpClient client = new OkHttpClient();

        HashMap<String, String> params = new HashMap<>();
        params.put("action", "get_detail");
        params.put("id", nid);
        Request request = new Request.Builder()
                .url(AddressUtils.NEWS_URL + NetworkOper.buildQueryParam("get_detail", 0, "", 0, params))
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
                                    comment_count = data.getString("comment_count");
                                    share_title = data.getString("share_title");
                                    share_pic = data.getString("share_pic");
                                    share_link = data.getString("share_link");
                                    count.setText(comment_count);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.share:
                ShareDialog dialog = DialogUtils.createShareDialog(this);
                dialog.setShareData(share_title, share_pic, "", share_link);
                dialog.show();
                break;
            case R.id.talk:
                Intent comment = new Intent(this, CommentActivity.class);
                comment.putExtra("nid", nid);
                startActivity(comment);
                break;
            case R.id.cancle:
                popupWindow.dismiss();
                break;
            case R.id.confirm:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                popupWindow.dismiss();
                break;
            case R.id.commitComment:
                if (MainApp.isLogin) {
                    if (etComment.getText().toString().equals("")) {
                        ToastUtils.showToast(this, "请先输入评论内容");
                    } else {
                        dialogTitle.setText("评论提交中");
                        dia.show();
                        commitComment();
                    }
                } else {
                    setPopWindow();
                }
                break;
        }
    }

    private void commitComment() {

        preferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);

        String uid = "";
        String callback = "";

        OkHttpClient client = new OkHttpClient();

        HashMap<String, String> params = new HashMap<>();
        if (preferences != null) {
            uid = String.valueOf(preferences.getInt("uid", 0));
            callback = preferences.getString("call", "");
        }
        params.put("action", "add_comment");
        params.put("nid", nid);
        params.put("uid", uid);
        params.put("callback_verify", callback);
        params.put("content", etComment.getText().toString());

        Request request = new Request.Builder()
                .url(AddressUtils.NEWS_URL + NetworkOper.buildQueryParam("add_comment", 0, "", 0, params))
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
                                    dia.dismiss();
                                    Toast.makeText(NewsWebActivity2.this, " 评论成功", Toast.LENGTH_SHORT).show();
                                    comment_count = String.valueOf(Integer.valueOf(comment_count) + 1);
                                    count.setText(comment_count);
//                                    webView.postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            webView.reload();
//                                        }
//                                    }, 2500);
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

    private void setPopWindow() {
        popupWindow = new PopupWindow(LayoutInflater.from(this).inflate(R.layout.comment_popupwindow, null),
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        View contentView = popupWindow.getContentView();
        TextView cancale = (TextView) contentView.findViewById(R.id.cancle);
        TextView confirm = (TextView) contentView.findViewById(R.id.confirm);
        cancale.setOnClickListener(this);
        confirm.setOnClickListener(this);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_newsweb2, null);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            // WebView中包含ZoomButtonsController，当web.getSettings().setBuiltInZoomControls(true);，用户一旦触摸屏幕就会出现缩放控制图标。这个图标过上几秒会自动消失，但在3.0系统以上，如果图标自动消失前退出当前Activity的话，就会发生ZoomButton找不到依附的Window而造成程序崩溃.
            // 解决办法很简单就是在Activity的onDestory方法中调用web.setVisibility(View.GONE);方法，手动将其隐藏。
            webView.setVisibility(View.GONE);
            webView.stopLoading();
            webView.removeAllViews();
            webView.removeAllViews();
            webView.freeMemory();
            webView.destroy();
            webView = null;
            System.gc();
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
