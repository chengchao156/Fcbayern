package cn.fcbayern.android.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.fcbayern.android.R;
import cn.fcbayern.android.common.ShareDialog;
import cn.fcbayern.android.util.DialogUtils;
import cn.fcbayern.android.util.Global;
import cn.fcbayern.android.util.LogUtils;
import cn.fcbayern.android.util.Utils;
import cn.fcbayern.android.util.cache.CacheUtils;


/**
 * Created by chenzhan on 15/5/29.
 */
public class WebActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final String TAG = WebActivity.class.getSimpleName();
    protected static final String ERROR_PAGE = "file:///android_asset/html/error_page.html";

    protected WebView mWebView;
    protected LinearLayout mContentView;

    protected WebChromeClient mChromeClient;
    protected WebViewClient mWebViewClient;

    //    protected WebInteractListener mWebListener;
    protected static final String KEY_URL = "URL";
    protected static final String KEY_TITLE = "TITLE";  // toolbar title
    protected static final String KEY_URL_TITLE = "URL_TITLE";

    public ValueCallback<Uri> mUploadMessage;
    public final static int FILE_CHOOSER_RESULT_CODE = 1;

    protected String mUrl;
    protected String mTitle;
    protected String mUrlTitle;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mWebView != null) {
            mWebView.requestFocus();
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // flag是0x01000000是FLAG_HARDWARE_ACCELERATED，在确实4.0机型上flash可以播放
        if (Utils.hasHoneycomb()) {
            getWindow().setFlags(0x01000000, 0x01000000);
        }

        setContentView(getContentLayoutId());

        TextView titleView = (TextView) findViewById(R.id.title);
        ImageView imageView = (ImageView) findViewById(R.id.search);

        if (imageView != null) {
            imageView.setVisibility(View.GONE);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_back);

        initUI();

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            mUrl = extras.getString(KEY_URL);
            mTitle = extras.getString(KEY_TITLE);
            mUrlTitle = extras.getString(KEY_URL_TITLE);
            titleView.setText(mTitle);
            mContentView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mWebView != null) {
                        mWebView.loadUrl(mUrl);
                    }
                }
            }, 100);
        } else {
            finish();
        }
    }

    protected int getContentLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onResume() {
        super.onResume();
        callHiddenWebViewMethod("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 诡异啊，在cmwap上要这样写才能拉到数据
        if (mWebView != null) {
            callHiddenWebViewMethod("onPause");
        }
    }

    @Override
    protected void onDestroy() {

        if (mWebView != null) {
            // WebView中包含ZoomButtonsController，当web.getSettings().setBuiltInZoomControls(true);，用户一旦触摸屏幕就会出现缩放控制图标。这个图标过上几秒会自动消失，但在3.0系统以上，如果图标自动消失前退出当前Activity的话，就会发生ZoomButton找不到依附的Window而造成程序崩溃.
            // 解决办法很简单就是在Activity的onDestory方法中调用web.setVisibility(View.GONE);方法，手动将其隐藏。
            mWebView.setVisibility(View.GONE);
            mWebView.stopLoading();
//            mWebView.clearCache(false);
            mWebView.removeAllViews();
            mContentView.removeAllViews();

            mWebView.freeMemory();

            mWebView.destroy();
            mWebView = null;
            System.gc();
        }

        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void initUI() {

        mContentView = (LinearLayout) findViewById(R.id.container);
        // 一旦在你的xml布局中引用了webview甚至没有使用过，都会阻碍重新进入Application之后对内存的gc。
        // 不能在xml中定义webview节点，而是在需要的时候动态生成。
        mWebView = new WebView(this) {

            @Override
            public boolean performClick() {
                try {
                    return super.performClick();
                } catch (Exception e) {
                    return false;
                }
            }
        };

        mWebView.setBackgroundColor(Color.WHITE);
        // API level < 17的设备有漏洞：
        // WebView上默认添加"searchBoxJavaBridge_"到mJavaScriptObjects中，有可能通过用户信任的客户端获取SD卡的数据。
        if (Utils.hasHoneycomb()) {
            // 删除默认添加的"searchBoxJavaBridge_"
            mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        }

        mWebView.setVisibility(View.VISIBLE);
        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContentView.addView(mWebView, lp);

        mWebViewClient = initWebViewClient();
        mWebView.setWebViewClient(mWebViewClient);

        mChromeClient = initChromeClient();
        mWebView.setWebChromeClient(mChromeClient);

        mWebView.setOnTouchListener(this);
        mWebView.setAlwaysDrawnWithCacheEnabled(true);
        mWebView.setDrawingCacheEnabled(true);
        mWebView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

        mWebView.freeMemory();

        System.gc();// 进来就清除内存保证视频播放

        final WebSettings setting = mWebView.getSettings();
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

        mWebView.requestFocus();
        mWebView.setFocusableInTouchMode(true);
        mWebView.setDownloadListener(new DownloadListener() {

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

    protected WebViewClient initWebViewClient() {
        return new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(WebActivity.this);
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
        };
    }

    protected WebChromeClient initChromeClient() {
        return new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                LogUtils.d(TAG, "WebActivityBase - chromeClient - onConsoleMessage()");
//                if (mWebListener != null) {
//                    if (consoleMessage != null && !TextUtils.isEmpty(consoleMessage.message())) {
//                        return mWebListener.onGetConsoleLog(consoleMessage.message().trim());
//                    }
//                }
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }

            //The undocumented magic method override
            //IDEA will swear at you if you try to put @Override here
            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {

                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILE_CHOOSER_RESULT_CODE);

            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FILE_CHOOSER_RESULT_CODE);
            }

            //For Android 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILE_CHOOSER_RESULT_CODE);

            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!mWebView.hasFocus()) {
            mWebView.requestFocus();
            mWebView.requestFocusFromTouch();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用隐藏的WebView方法 <br />
     * 说明：WebView完全退出swf的方法，停止声音的播放。
     *
     * @param name
     */
    private void callHiddenWebViewMethod(String name) {
        if (mWebView != null) {
            try {
                Method method = WebView.class.getMethod(name);
                method.invoke(mWebView); // 调用
            } catch (NoSuchMethodException e) { // 没有这样的方法
                LogUtils.i("No such method: " + name, e.toString());
            } catch (IllegalAccessException e) { // 非法访问
                LogUtils.i("Illegal Access: " + name, e.toString());
            } catch (InvocationTargetException e) { // 调用的目标异常
                LogUtils.d("Invocation Target Exception: " + name, e.toString());
            }
        }
    }

    public static void browse(Context context, String url, String title, int from, int requestCode) {
        if (url == null) {
            return;
        }
        protectWebviewFromCache();
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra(KEY_URL, url);
        intent.putExtra(KEY_TITLE, title);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setData(Uri.parse(url));
        if (requestCode >= 0 && context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, requestCode);
        } else {
            context.startActivity(intent);
        }
    }

    public static void browse(Context context, String url, String title, int from) {
        browse(context, url, title, from, -1);
    }

    /**
     * 一系列处理保护避免webviewcrash
     */
    public static void protectWebviewFromCache() {
        // 剪切版为空时进html文本框粘贴crash
        try { // 某些机型会np crash
            ClipboardManager clipboardManager = (ClipboardManager) Global.sContext.getSystemService(Context.CLIPBOARD_SERVICE);
            if (TextUtils.isEmpty(clipboardManager.getText())) {
                clipboardManager.setText("");
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString(), e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case android.support.v7.appcompat.R.id.home:
                finish();
                break;
            case R.id.action_share:
                ShareDialog dialog = DialogUtils.createShareDialog(this);
                dialog.setShareData(mUrlTitle, "", "", mUrl);
                dialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}

