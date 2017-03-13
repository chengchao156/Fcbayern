package cn.fcbayern.android.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.fcbayern.android.R;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.ui.ActionWebActivity;
import cn.fcbayern.android.ui.MainActivity;
import cn.fcbayern.android.ui.NewsWebActivity;
import cn.fcbayern.android.ui.PhotoActivity;
import cn.fcbayern.android.ui.WebActivity;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.DeviceUtils;
import cn.jpush.android.api.JPushInterface;

/**
 * Created by chenzhan on 15/8/5.
 */
public class PushReceiver extends BroadcastReceiver {

    private static final String TAG = PushReceiver.class.getSimpleName();

    private static final String KEY_URL = "url";
    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";
    private static final String KEY_TITLE = "title";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_VERSION = "version";
    private static final String KEY_OPEN_MAIN = "main";

    private static int NOTIFICATION_ID = 0x10;

    @Override
    public void onReceive(Context context, Intent intent) {

//        Bundle bundle = intent.getExtras();

        if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {

            processExtra(intent);
            String version = intent.getStringExtra(KEY_VERSION);
            if (TextUtils.isEmpty(version)) {
                sendNotification(context, intent);
            } else if (DeviceUtils.getVersionName(context).equals(version)) {
                sendNotification(context, intent);
            }

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
//            LogUtils.d(TAG, "[MyReceiver] 接收到推送下来的通知");
//            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
//            LogUtils.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
//            LogUtils.d(TAG, "[MyReceiver] 用户点击打开了通知");

            boolean openMain = intent.getBooleanExtra(KEY_OPEN_MAIN, false);

            if (openMain) {
                Intent i = new Intent(context, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(i);
            } else {
                String url = intent.getStringExtra(KEY_URL);
                String type = intent.getStringExtra(KEY_TYPE);
                int id = intent.getIntExtra(KEY_ID, 0);
                String title = intent.getStringExtra(KEY_TITLE);

                if (!TextUtils.isEmpty(url)) {
                    if ("video".equals(type)) {
                        ActionWebActivity.browse(context, url, "", title, true);
                    } else {
                        WebActivity.browse(context, url, title, -1);
                    }
                } else {
                    if ("news".equals(type)) {
                        NewsWebActivity.browse(context, AddressUtils.DETAIL_NEWS_URL + id, context.getString(R.string.news), -1);
                    } else if ("photos".equals(type)) {
                        Intent i = new Intent(context, PhotoActivity.class);
                        i.putExtra(PhotoActivity.KEY_CONTENT, id);
                        i.putExtra(PhotoActivity.KEY_TYPE, NetworkOper.Req.HOME);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                    } else {
                        Intent i = new Intent(context, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(i);
                    }
                }
            }

        }

    }

    public void sendNotification(Context context, Intent i) {

        Intent intent = new Intent();
        intent.putExtras(i);
        intent.setAction(JPushInterface.ACTION_NOTIFICATION_OPENED);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
        builder.setSmallIcon(R.drawable.ic_logo);
//        builder.setColor(0xFF0000);
        builder.setOngoing(true);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
//        builder.setNumber(3);

        builder.setContentTitle(i.getStringExtra(KEY_TITLE));
        builder.setContentText(i.getStringExtra(KEY_CONTENT));
        builder.setTicker(i.getStringExtra(KEY_TITLE));

        builder.setDefaults(Notification.DEFAULT_SOUND);
//        builder.setSubText("Tap to view documentation about notifications.");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID++, builder.build());
    }

    private void processExtra(Intent intent) {

        String extra = intent.getExtras().getString(JPushInterface.EXTRA_EXTRA);
        intent.putExtra(KEY_OPEN_MAIN, "{}".equals(extra));

        try {

            JSONObject object = new JSONObject(extra);
            String url = object.optString(KEY_URL);
            String type = object.optString(KEY_TYPE);
            int id = object.optInt(KEY_ID);
            String title = object.optString(KEY_TITLE);
            String version = object.optString(KEY_VERSION);
            String content = object.optString(KEY_CONTENT);

            intent.putExtra(KEY_URL, url);
            intent.putExtra(KEY_TYPE, type);
            intent.putExtra(KEY_ID, id);
            intent.putExtra(KEY_TITLE, title);
            intent.putExtra(KEY_VERSION, version);
            intent.putExtra(KEY_CONTENT, content);

        } catch (JSONException e) {
            e.printStackTrace();
            intent.putExtra(KEY_OPEN_MAIN, true);
        }
    }

}
