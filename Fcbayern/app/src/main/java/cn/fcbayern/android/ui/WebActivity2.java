package cn.fcbayern.android.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.fcbayern.android.R;
import cn.fcbayern.android.common.ObservableWebView;
import cn.fcbayern.android.common.ShareDialog;
import cn.fcbayern.android.util.DialogUtils;
import cn.fcbayern.android.util.Global;
import cn.fcbayern.android.util.Utils;
import cn.fcbayern.android.util.cache.CacheUtils;

public class WebActivity2 extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout back;
    private TextView title;

    private ImageView goBack;
    private ImageView goForword;
    private ImageView goRefresh;
    private ImageView share;
    private RelativeLayout bottom;

    private ObservableWebView webView;
    private String url;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web2);
        initView();
        initUI();
        url = getIntent().getStringExtra("url");
        //支持javascript
        webView.getSettings().setJavaScriptEnabled(true);
        // 设置可以支持缩放
        webView.getSettings().setSupportZoom(false);
        // 设置出现缩放工具
        if (android.os.Build.VERSION.SDK_INT >= 11) {
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
            public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                webView.loadUrl(url);
                return super.shouldOverrideKeyEvent(view, event);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(WebActivity2.this);
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
        webView.setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback() {
                                               @Override
                                               public void onScroll(int dx, int dy) {
                                                   if (dy <= -10 || dy == 1) {
                                                       bottom.setVisibility(View.VISIBLE);
                                                   } else if (dy >= 10) {
                                                       bottom.setVisibility(View.GONE);
                                                   }
                                               }
                                           }

        );
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
        webView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (mimeType != null && mimeType.contains("mp4")) {
                    intent.setDataAndType(Uri.parse(url), mimeType);
                } else {
                    intent.setData(Uri.parse(url));
                }
                try {
                    startActivity(Intent.createChooser(intent, "选择"));
                    finish();
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        CookieManager.getInstance().setAcceptCookie(true);
    }

    private void initView() {
        back = (RelativeLayout) this.findViewById(R.id.back);
        back.setOnClickListener(this);
        webView = (ObservableWebView) this.findViewById(R.id.details);
        title = (TextView) this.findViewById(R.id.title);
        progress = (ProgressBar) this.findViewById(R.id.progress);
        goBack = (ImageView) this.findViewById(R.id.goBack);
        goForword = (ImageView) this.findViewById(R.id.goForword);
        goRefresh = (ImageView) this.findViewById(R.id.goRefresh);
        bottom = (RelativeLayout) this.findViewById(R.id.bottomView);
        share = (ImageView) this.findViewById(R.id.share);
        share.setOnClickListener(this);
        goBack.setOnClickListener(this);
        goForword.setOnClickListener(this);
        goRefresh.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.goBack:
                webView.goBack();
                break;
            case R.id.goForword:
                webView.goForward();
                break;
            case R.id.goRefresh:
                webView.reload();
                break;
            case R.id.share:
                ShareDialog dialog = DialogUtils.createShareDialog(this);
                dialog.setShareData("", "", "", url);
                dialog.show();
                break;
        }
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
