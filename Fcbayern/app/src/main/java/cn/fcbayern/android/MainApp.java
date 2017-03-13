package cn.fcbayern.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hupu.alienhttp.AlienHttpConfig;
import com.hupu.alienhttp.AlienHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.ExampleUtil;
import cn.fcbayern.android.util.Global;
import cn.fcbayern.android.util.PreferencesMgr;
import cn.fcbayern.android.util.ToastUtils;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static cn.fcbayern.android.data.DataManager.TAG;

/**
 * Created by chenzhan on 15/5/26.
 */
public class MainApp extends Application {

    private static HandlerThread handlerThread;
    private static Handler bgHandler;
    public static boolean isLogin = false;
    public static boolean isShow = true;

    private HashMap<String, String> Info = new HashMap<>();
    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        Global.sContext = this;
        PreferencesMgr.init(this);
        AlienHttpUtil.init(new AlienHttpConfig());

        JPushInterface.setDebugMode(false);    // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);            // 初始化 JPush

        if (!PreferencesMgr.getBoolean(PreferencesMgr.PREFS_PUSH_KEY, true)) {
            JPushInterface.stopPush(this);
        }

        handlerThread = new HandlerThread("BG_T");
        handlerThread.start();
        bgHandler = new Handler(handlerThread.getLooper());
        preferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        if (preferences != null) {
            initInfo();
        }
//        setAlias();
    }

    private void setAlias() {
        String alias = "thisisalisa";
        if (TextUtils.isEmpty(alias)) {
            Toast.makeText(getApplicationContext(),"123131", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ExampleUtil.isValidTagAndAlias(alias)) {
            Toast.makeText(getApplicationContext(),"empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // 调用 Handler 来异步设置别名
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, alias));
    }

    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs ;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    Log.i(TAG, logs);
                    // 建议这里往 SharePreference 里写一个成功设置的状态。成功设置一次后，以后不必再次设置了。
                    break;
                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    Log.i(TAG, logs);
                    // 延迟 60 秒来调用 Handler 设置别名
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                    break;
                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e(TAG, logs);
            }
            ExampleUtil.showToast(logs, getApplicationContext());
        }
    };

    private static final int MSG_SET_ALIAS = 1001;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    Log.d(TAG, "Set alias in handler.");
                    // 调用 JPush 接口来设置别名。
                    JPushInterface.setAliasAndTags(getApplicationContext(),
                            (String) msg.obj,
                            null,
                            mAliasCallback);
                    break;
                default:
                    Log.i(TAG, "Unhandled msg - " + msg.what);
            }
        }
    };


    private void initInfo() {

        OkHttpClient client = new OkHttpClient();

        HashMap<String, String> params = new HashMap<>();
        int uid = preferences.getInt("uid", 0);
        params.put("uid", String.valueOf(uid));

        Request request = new Request.Builder()
                .url(AddressUtils.USER_URL + NetworkOper.buildQueryParam("profile", 0, "", 0, params))
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
                        String code = object.getString("code");
                        if (code.equals("0")) {
                            UserInfo userInfo = UserInfo.getUserInfo();
                            JSONObject data = object.getJSONObject("data");
                            String uid = data.getString("uid");
                            String userN = data.getString("username");
                            String emai = data.getString("email");
                            String reg_date = data.getString("reg_date");
                            String avatar = data.getString("avatar");
                            String gender = data.getString("gender");
                            String birt = data.getString("birth");
                            String area = data.getString("area");

                            Info.put("uid", uid);
                            Info.put("userN", userN);
                            Info.put("emai", emai);
                            Info.put("reg_date", reg_date);
                            Info.put("avatar", avatar);
                            Info.put("gender", gender);
                            Info.put("birt", birt);
                            Info.put("area", area);
                            Info.put("callback", null);

                            if (gender.equals("未设置")) {
                                userInfo.setIsperfect(true);
                            } else if (birt.equals("未设置")) {
                                userInfo.setIsperfect(true);
                            } else if (area.equals("未设置")) {
                                userInfo.setIsperfect(true);
                            } else {
                                userInfo.setIsperfect(false);
                            }
                            userInfo.setUserInfo(Info);
                        } else {
                            MainApp.isLogin = false;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    ToastUtils.showToast(getApplicationContext(),"登录异常请重新登录");
                }
            }
        });

    }

    public static void postBg(Runnable runnable) {
        bgHandler.post(runnable);
    }

    public static class UserInfo {

        public static UserInfo userInfo;

        public UserInfo() {
        }

        public static synchronized UserInfo getUserInfo() {

            if (userInfo == null) {
                userInfo = new UserInfo();
            }
            return userInfo;
        }

        private String uid;
        private String username;
        private String email;
        private String reg_date;
        private String avatar;
        private String gender;
        private String birth;
        private String area;
        private String callback_verify;

        public String getCallback_verify() {
            return callback_verify;
        }

        public void setCallback_verify(String callback_verify) {
            this.callback_verify = callback_verify;
        }

        private boolean isperfect;

        public boolean isperfect() {
            return isperfect;
        }

        public void setIsperfect(boolean isperfect) {
            this.isperfect = isperfect;
        }

        public void setUserInfo(HashMap<String, String> info) {
            setUid(info.get("uid"));
            setUsername(info.get("userN"));
            setEmail(info.get("emai"));
            setReg_date(info.get("reg_date"));
            setAvatar(info.get("avatar"));
            setGender(info.get("gender"));
            setBirth(info.get("birt"));
            setArea(info.get("area"));
            if (info.get("callback") != null) {
                setCallback_verify(info.get("callback"));
            }

            if (info.get("gender").equals("未设置")) {
                userInfo.setIsperfect(true);
            } else if (info.get("birt").equals("未设置")) {
                userInfo.setIsperfect(true);
            } else if (info.get("area").equals("未设置")) {
                userInfo.setIsperfect(true);
            } else {
                userInfo.setIsperfect(false);
            }
        }


        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getReg_date() {
            return reg_date;
        }

        public void setReg_date(String reg_date) {
            this.reg_date = reg_date;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getBirth() {
            return birth;
        }

        public void setBirth(String birth) {
            this.birth = birth;
        }

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }


    }

}
