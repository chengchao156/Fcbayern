package cn.fcbayern.android.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.List;

import cn.fcbayern.android.R;
import cn.fcbayern.android.common.ShareDialog;
import cn.fcbayern.android.data.DataManager;
import cn.fcbayern.android.model.NewsModel;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.DialogUtils;


/**
 * Created by chenzhan on 15/5/29.
 */
public class NewsWebActivity extends WebActivity implements DataManager.DataLoadDetailListener {

    private static final String TAG = NewsWebActivity.class.getSimpleName();

    protected static final String KEY_TYPE = "TYPE";

    private int mFrom;
    private NewsModel mModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mFrom = extras.getInt(KEY_TYPE);
        }
        super.onCreate(savedInstanceState);
    }

    public static void browse(Context context, String url, String title, int from) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        protectWebviewFromCache();
        Intent intent = new Intent(context, NewsWebActivity.class);
        intent.putExtra(KEY_URL, url + AddressUtils.APP_PARAM);
        intent.putExtra(KEY_TITLE, title);
        intent.putExtra(KEY_TYPE, from);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected WebViewClient initWebViewClient() {
        return new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                view.loadUrl(ERROR_PAGE);
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(NewsWebActivity.this);
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
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mUrl = url;
                return super.shouldOverrideUrlLoading(view, url);
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case android.support.v7.appcompat.R.id.home:
                finish();
                break;
            case R.id.action_share:
                Uri uri = Uri.parse(mUrl);
                List<String> list = uri.getPathSegments();
                String id = list.get(list.size() - 1);
                mModel = mModel == null ? (NewsModel) DataManager.findModel(mFrom, Integer.parseInt(id)) : mModel;
                if (mModel != null) {
                    ShareDialog dialog = DialogUtils.createShareDialog(this);
                    dialog.setShareData(mModel.title, AddressUtils.IMAGE_PREFIX + mModel.imageUrl, "", mUrl);
                    dialog.show();
                } else {
                    mModel = new NewsModel();
                    mModel.id = Integer.parseInt(id);
                    NetworkOper.getDetail(NetworkOper.Req.NEWS, mModel, this);
                }
                break;
        }
        return true;
    }

    @Override
    public void loadComplete(int errorCode, boolean fromCache, int operator) {

        if (mModel != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShareDialog dialog = DialogUtils.createShareDialog(NewsWebActivity.this);
                    dialog.setShareData(mModel.title, AddressUtils.IMAGE_PREFIX + mModel.imageUrl, "", mUrl);
                    dialog.show();
                }
            });
        }
    }
}

