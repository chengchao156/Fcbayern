package cn.fcbayern.android.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;

import cn.fcbayern.android.R;
import cn.fcbayern.android.util.Global;
import cn.fcbayern.android.util.Utils;
import cn.fcbayern.android.util.cache.CacheUtils;


/**
 * Created by chenzhan on 15/5/29.
 */
public class ActionWebFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = ActionWebFragment.class.getSimpleName();

    protected static final String SHOW_BAR = "show_bar";

    private ImageView mRefreshBtn;
    private boolean mIsloading;

    private WebView mWebView;

    protected static final String KEY_URL = "URL";
    protected static final String KEY_TITLE = "TITLE";  // toolbar title

    public static Fragment createInstance(String url, String title, boolean showBar) {

        if (TextUtils.isEmpty(url)) {
            return null;
        }

//        protectWebviewFromCache();
//        Intent intent = new Intent(context, ActionWebFragment.class);
//        intent.putExtra(KEY_URL, url + AddressUtils.APP_PARAM);
//        intent.putExtra(KEY_TITLE, title);
//        intent.putExtra(SHOW_BAR, showBar);

        Bundle bundle = new Bundle();
        bundle.putString(KEY_URL, url);
        bundle.putString(KEY_TITLE, title);
        bundle.putBoolean(SHOW_BAR, showBar);
        ActionWebFragment fragment = new ActionWebFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

//    protected void initUI() {
//
//        View barRoot = findViewById(R.id.bottom_actionbar);
//
//        boolean showBar = getIntent().getBooleanExtra(SHOW_BAR, false);
//        if (showBar) {
//            mRefreshBtn = (ImageView) findViewById(R.id.refresh);
//            mRefreshBtn.setOnClickListener(this);
//            findViewById(R.id.back).setOnClickListener(this);
//            findViewById(R.id.forward).setOnClickListener(this);
//        } else {
//            barRoot.setVisibility(View.GONE);
//        }
//
//    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void initWebView() {
        // 一旦在你的xml布局中引用了webview甚至没有使用过，都会阻碍重新进入Application之后对内存的gc。
        // 不能在xml中定义webview节点，而是在需要的时候动态生成。
        mWebView = new WebView(getActivity()) {
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

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mIsloading = true;
                if (mRefreshBtn != null) {
                    mRefreshBtn.setImageResource(R.drawable.ic_web_stop);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    mIsloading = false;
                    if (mRefreshBtn != null) {
                        mRefreshBtn.setImageResource(R.drawable.ic_web_refresh);
                    }
                }
            }
        });

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

        CookieManager.getInstance().setAcceptCookie(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_base_web, container, false);

        initWebView();

        ViewGroup contentView = (LinearLayout) root.findViewById(R.id.container);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentView.addView(mWebView, lp);

        View barRoot = root.findViewById(R.id.bottom_actionbar);

        boolean showBar = getArguments().getBoolean(SHOW_BAR, false);
        if (showBar) {
            mRefreshBtn = (ImageView) root.findViewById(R.id.refresh);
            mRefreshBtn.setOnClickListener(this);
            root.findViewById(R.id.back).setOnClickListener(this);
            root.findViewById(R.id.forward).setOnClickListener(this);
        } else {
            barRoot.setVisibility(View.GONE);
        }

        final String url = getArguments().getString(KEY_URL);
        root.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mWebView != null) {
                    mWebView.loadUrl(url);
                }
            }
        }, 100);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mWebView.getParent() instanceof ViewGroup) {
            ((ViewGroup) mWebView.getParent()).removeAllViews();
        }
        mWebView.destroy();
        mWebView = null;
        System.gc();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                }
                break;
            case R.id.forward:
                if (mWebView.canGoForward()) {
                    mWebView.goForward();
                }
                break;
            case R.id.refresh:
                if (mIsloading) {
                    mWebView.stopLoading();
                } else {
                    mWebView.reload();
                }
                break;
        }
    }
}

