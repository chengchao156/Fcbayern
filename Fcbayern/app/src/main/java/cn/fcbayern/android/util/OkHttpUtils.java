package cn.fcbayern.android.util;

import java.util.Arrays;
import java.util.HashMap;

import okhttp3.Request;

/**
 * Author：Mr.Cheng
 * Date: 2016/9/14 10 :10
 * Email:1121360659@qq.com
 */
public class OkHttpUtils {

    /**
     * 构建sign参数值
     *
     * @param msg
     * @return
     */
    private static String buildSign(String msg) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(msg.replace("=", "").replace("&", "").replace("?", ""));
        buffer.append(AppConfig.APIKEY_HUPU);
        return MD5Utils.stringMD5(buffer.toString());
    }

    public static Request okHttpGet(String action) {

        String load = "http://" + AddressUtils.API_DOMAIN + "/" + action + "?" + "sign" + "=" + buildQueryParam(action) + "&"
                + "deviceId" + "=" + DeviceUtils.getImei(Global.sContext) + "&" + "platform" + "=" + "android" + "&" + "version" + "=" + DeviceUtils.getVersionName(Global.sContext);

        Request request = new Request.Builder()
                .url(load)
                .get()
                .build();

        return request;
    }

    /**
     * 组建sign字符串
     *
     * @param action
     * @return
     */
    private static String buildQueryParam(String action) {

        HashMap<String, String> values = new HashMap<>();

        values.put("action", action);
        values.put("deviceId", DeviceUtils.getImei(Global.sContext));
        values.put("platform", "android");
        values.put("version", DeviceUtils.getVersionName(Global.sContext));

        Object[] keys = values.keySet().toArray();
        Arrays.sort(keys);

        StringBuffer buffer = new StringBuffer("?");
        for (int i = 0; i < keys.length; i++) {
            buffer.append(String.valueOf(keys[i])).append("=").append(values.get(keys[i])).append("&");
        }
        String sign = buildSign(buffer.toString());

        return sign;
    }


}
