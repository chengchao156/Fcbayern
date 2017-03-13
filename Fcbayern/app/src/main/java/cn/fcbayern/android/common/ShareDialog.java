package cn.fcbayern.android.common;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.BaseShareContent;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import org.w3c.dom.Text;

import cn.fcbayern.android.R;
import cn.fcbayern.android.util.LogUtils;

/**
 * Created by chenzhan on 15/6/3.
 */
public class ShareDialog extends AppCompatDialog implements View.OnClickListener {


    private ShareHelper mShareHelper;

    protected ShareDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    public ShareDialog(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    public ShareDialog(Context context) {
        this(context, 0);
    }

    private void init(Context context) {
        mShareHelper = new ShareHelper(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View container = LayoutInflater.from(getContext()).inflate(R.layout.layout_share, null);
        setContentView(container);
        container.setOnClickListener(this);

        setClickListener(container);
    }

    private void setClickListener(View view) {
        if(view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View v = group.getChildAt(i);
                if (v instanceof ImageView) {
                    v.setOnClickListener(this);
                } else if (v instanceof ViewGroup) {
                    setClickListener(v);
                }
            }
        }
    }

    public void setShareData(String title, String imageUrl, String content, String url) {
        mShareHelper.setShareData(title, imageUrl, content, url);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.share_pengyou:
                mShareHelper.sharePost(SHARE_MEDIA.WEIXIN_CIRCLE);
                break;
            case R.id.share_qq:
                mShareHelper.sharePost(SHARE_MEDIA.QQ);
                break;
            case R.id.share_qzone:
                mShareHelper.sharePost(SHARE_MEDIA.QZONE);
                break;
            case R.id.share_sina:
                mShareHelper.sharePost(SHARE_MEDIA.SINA);
                break;
            case R.id.share_wechat:
                mShareHelper.sharePost(SHARE_MEDIA.WEIXIN);
                break;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mShareHelper.clear();
    }

    static class ShareHelper {

        private static final String QZONE_APPID = "1104603685";
        private static final String QZONE_APPKEY = "HUogPie8Ivup5vPQ";

        private static final String WX_APPID = "wxe74f684db173a3a1";
        private static final String WX_APPKEY = "d6f483f6a1fc95064a4bba7f88642c56";


        private UMSocialService mController;
        private Context mContext;

        private String mUrl;
        private String mImageUrl;
        private String mTitle;
        private String mContent;

        public ShareHelper(Context context) {
            mContext = context;
            mController = UMServiceFactory.getUMSocialService("com.umeng.share");
            mController.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.SINA);
            mController.getConfig().setDefaultShareLocation(false);
            mController.getConfig().closeToast();
        }

        public void setShareData(String title, String imageUrl, String content, String url) {
            mUrl = url;
            mImageUrl = imageUrl;
            mTitle = title;
            mContent = content;
        }

        public void sharePost(SHARE_MEDIA type) {

            BaseShareContent shareContent = null;

            UMImage mUMImgBitmap;
            if (TextUtils.isEmpty(mImageUrl)) {
                mUMImgBitmap = new UMImage(mContext, R.drawable.ic_logo);
            } else {
                mUMImgBitmap = new UMImage(mContext, mImageUrl);
            }

            if (type == SHARE_MEDIA.SINA) {
                mController.getConfig().setSsoHandler(new SinaSsoHandler());
                shareContent = new SinaShareContent(mUMImgBitmap);
                shareContent.setShareContent(mTitle + mUrl);
                shareContent.setTargetUrl(mUrl);

            } else if (type == SHARE_MEDIA.QZONE) {
                QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler((Activity) mContext, QZONE_APPID, QZONE_APPKEY);
                qZoneSsoHandler.addToSocialSDK();
                shareContent = new QZoneShareContent(mUMImgBitmap);
                shareContent.setTitle(mTitle);
                shareContent.setShareContent(mTitle + mUrl);
                shareContent.setTargetUrl(mUrl);

            } else if (type == SHARE_MEDIA.WEIXIN) {

                // 添加微信平台，参数1为当前Activity, 参数2为用户申请的AppID, 参数3为点击分享内容跳转到的目标url
                // UMWXHandler wxHandler =
                // mController.getConfig().supportWXPlatform(
                // this, appID, shareURL);
                // wxHandler.setWXTitle(shareWX);
                // mController.getConfig().setSsoHandler(wxHandler);

                UMWXHandler wxHandler = new UMWXHandler(mContext,WX_APPID,WX_APPKEY);
                wxHandler.addToSocialSDK();
                shareContent = new WeiXinShareContent();
                shareContent.setTitle(mTitle);
                shareContent.setTargetUrl(mUrl);
                shareContent.setShareContent(mTitle + mUrl);
                shareContent.setShareMedia(mUMImgBitmap);

            } else if (type == SHARE_MEDIA.WEIXIN_CIRCLE) {
                // 支持微信朋友圈
                // UMWXHandler circleHandler = mController.getConfig()
                // .supportWXCirclePlatform(this, appID, shareURL);
                // circleHandler.setCircleTitle(shareCicle);
                UMWXHandler wxCircleHandler = new UMWXHandler(mContext, WX_APPID,WX_APPKEY);
                wxCircleHandler.setToCircle(true);
                wxCircleHandler.addToSocialSDK();
                wxCircleHandler.setTargetUrl(mUrl);
                shareContent = new CircleShareContent(mUMImgBitmap);
                shareContent.setTitle(mTitle);
                shareContent.setShareContent(mTitle + mUrl);
                shareContent.setTargetUrl(mUrl);

            } else if (type == SHARE_MEDIA.QQ) {
                UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler((Activity)mContext, QZONE_APPID, QZONE_APPKEY);
                qqSsoHandler.addToSocialSDK();
                shareContent = new QQShareContent(mUMImgBitmap);
                shareContent.setTitle(mTitle);
                shareContent.setShareContent(mTitle + mUrl);
                shareContent.setTargetUrl(mUrl);
            }

            LogUtils.e("Share", "Share param title = " + mTitle + ", content = " + mContent
                    + ", url = " + mUrl + ", ImageUrl = " + mImageUrl);

            mController.setShareMedia(shareContent);// 现在只绑定一个平台的消息

            mController.postShare(mContext, type, new SocializeListeners.SnsPostListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onComplete(SHARE_MEDIA platform, int eCode, SocializeEntity entity) {
                    LogUtils.e("Share", "Share Complete platform = " + platform + ", code = " + eCode);
                }
            });
        }

        public void clear() {
            mController.getConfig().cleanListeners();
        }

    }
}
