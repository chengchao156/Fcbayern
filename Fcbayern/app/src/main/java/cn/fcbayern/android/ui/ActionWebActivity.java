package cn.fcbayern.android.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.text.TextUtils;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;


import cn.fcbayern.android.R;
import cn.fcbayern.android.util.AddressUtils;


/**
 * Created by chenzhan on 15/5/29.
 */
public class ActionWebActivity extends WebActivity implements View.OnClickListener {

    private static final String TAG = ActionWebActivity.class.getSimpleName();

    protected static final String SHOW_BAR = "show_bar";

    private ImageView mRefreshBtn;
    private boolean mIsloading;

    public static void browse(Context context, String url, String title, String urlTitle, boolean showBar) {

        if (TextUtils.isEmpty(url)) {
            return;
        }
        protectWebviewFromCache();
        Intent intent = new Intent(context, ActionWebActivity.class);
        intent.putExtra(KEY_URL, url + AddressUtils.APP_PARAM);
        intent.putExtra(KEY_TITLE, title);
        intent.putExtra(SHOW_BAR, showBar);
        intent.putExtra(KEY_URL_TITLE, urlTitle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void initUI() {
        super.initUI();

        View barRoot = findViewById(R.id.bottom_actionbar);

        boolean showBar = getIntent().getBooleanExtra(SHOW_BAR, false);
        if (showBar) {
            mRefreshBtn = (ImageView) findViewById(R.id.refresh);
            mRefreshBtn.setOnClickListener(this);
            findViewById(R.id.back).setOnClickListener(this);
            findViewById(R.id.forward).setOnClickListener(this);
        } else {
            barRoot.setVisibility(View.GONE);
        }

    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_base_web;
    }

    @Override
    protected WebViewClient initWebViewClient() {
        return new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (handler != null) {
                    handler.cancel();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mIsloading = true;
                mRefreshBtn.setImageResource(R.drawable.ic_web_stop);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        };
    }

    protected WebChromeClient initChromeClient() {
        return new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    mIsloading = false;
                    mRefreshBtn.setImageResource(R.drawable.ic_web_refresh);
                }
            }
        };
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_share:
//                break;
//        }
//        return true;
//    }

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

