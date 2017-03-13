package cn.fcbayern.android.network;

import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.hupu.alienhttp.AlienHttpListener;
import com.hupu.alienhttp.AlienHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cn.fcbayern.android.MainApp;
import cn.fcbayern.android.R;
import cn.fcbayern.android.data.DataManager;
import cn.fcbayern.android.model.BaseModel;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.AppConfig;
import cn.fcbayern.android.util.DeviceUtils;
import cn.fcbayern.android.util.FileUtils;
import cn.fcbayern.android.util.Global;
import cn.fcbayern.android.util.JsonParser;
import cn.fcbayern.android.util.MD5Utils;
import cn.fcbayern.android.util.Utils;

/**
 * Created by chenzhan on 15/5/26.
 */
public class NetworkOper {

    public static class Error {
        public static final int OK = 0;
        public static final int TIME_OUT = -1;
        public static final int DATA_ERROR = -2;
        public static final int NETWORK_ERROR = -3;
        public static final int NO_MORE = -4;
    }


    public static class Req {
        public static final int HOME = 0;
        public static final int FOCUS = 1;
        public static final int NEWS = 2;
        public static final int PHOTO = 3;
        public static final int SCHEDULE = 4;
        public static final int TEAM = 5;
        public static final int LAST_SCHEDULE = 6;
        public static final int HOME_ADS = 7;
        public static final int VIDEO = 8;
        public static final int STAND = 9;
    }

    private static final String GETLIST_ACTION = "get_list_2016";
    private static final String GET_HOME_ACTION = "get_mix_list";
    private static final String GET_FOCUS_ACTION = "get_focus";
    private static final String GET_DETAIL_ACTION = "get_detail";
    private static final String GET_SCHEDULE_ACTION = "schedules";
    private static final String GET_LAST_SCHEDULE_ACTION = "lastSchedules";
    private static final String GET_ADS_ACTION = "get_mid_adv";
    private static final String GET_RANK_ACTION = "team_rank";

    private static String buildSign(String msg) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(msg.replace("=", "").replace("&", "").replace("?", ""));
        buffer.append(AppConfig.APIKEY_HUPU);
        return MD5Utils.stringMD5(buffer.toString());
    }

    // the param key need keep sort
    public static String buildQueryParam(String action, int limit, String lastTime, int id, HashMap<String, String> params) {

        HashMap<String, String> values = new HashMap<>();
        values.put("action", action);
        values.put("deviceId", DeviceUtils.getImei(Global.sContext));
        if (id > 0) values.put("id", String.valueOf(id));
        if (limit > 0) values.put("limit", String.valueOf(limit));
        values.put("platform", "android");
        values.put("version", DeviceUtils.getVersionName(Global.sContext));
        if (!TextUtils.isEmpty(lastTime)) values.put("last_time", "%s");
        if (params != null) {
            values.putAll(params);
        }

        Object[] keys = values.keySet().toArray();
        Arrays.sort(keys);

        StringBuffer buffer = new StringBuffer("?");
        for (int i = 0; i < keys.length; i++) {
            buffer.append(String.valueOf(keys[i])).append("=").append(values.get(keys[i])).append("&");
        }

        String sign = "";
        String queryStrWithoutSign = "";
        if (!TextUtils.isEmpty(lastTime)) {
            String toSign = String.format(buffer.toString(), lastTime);
            sign = buildSign(toSign);
            try {
                String encodeTime = URLEncoder.encode(lastTime, "UTF-8");
                queryStrWithoutSign = String.format(buffer.toString(), encodeTime);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            sign = buildSign(buffer.toString());
            queryStrWithoutSign = buffer.toString();
        }

        return queryStrWithoutSign + "sign=" + sign;
    }

    private static String getUrl(int type) {
        String url = "";
        switch (type) {
            case Req.NEWS:
                url = AddressUtils.NEWS_URL;
                break;
            case Req.HOME:
            case Req.HOME_ADS:
            case Req.FOCUS:
                url = AddressUtils.HOME_URL;
                break;
            case Req.PHOTO:
                url = AddressUtils.PHOTO_URL;
                break;
            case Req.SCHEDULE:
            case Req.LAST_SCHEDULE:
            case Req.STAND:
                url = AddressUtils.MATCH_URL;
                break;
            case Req.TEAM:
                url = AddressUtils.TEAM_URL;
                break;
            case Req.VIDEO:
                url = AddressUtils.VIDEO_URL;
                break;
        }
        return url;
    }

    private static String getAction(int type) {
        String action = "";
        switch (type) {
            case Req.NEWS:
            case Req.VIDEO:
                action = GETLIST_ACTION;
                break;
            case Req.TEAM:
            case Req.PHOTO:
                action = "get_list";
                break;
            case Req.FOCUS:
                action = GET_FOCUS_ACTION;
                break;
            case Req.HOME:
                action = GET_HOME_ACTION;
                break;
            case Req.SCHEDULE:
                action = GET_SCHEDULE_ACTION;
                break;
            case Req.LAST_SCHEDULE:
                action = GET_LAST_SCHEDULE_ACTION;
                break;
            case Req.HOME_ADS:
                action = GET_ADS_ACTION;
                break;
            case Req.STAND:
                action = GET_RANK_ACTION;
                break;
        }
        return action;
    }

    public static void getList(final int operator, final DataManager.DataLoadListListener listener, final boolean onlyNet) {
        MainApp.postBg(new Runnable() {
            @Override
            public void run() {
                if (!onlyNet && loadCache(operator)) {
                    listener.loadComplete(NetworkOper.Error.OK, true, operator, false);
                } else {
                    getList(operator, null, "", 0, listener);
                }
            }
        });

    }

    public static void getList(final int type, int start, String lastTime, int limit, final DataManager.DataLoadListListener listener) {
        HashMap<String, String> params = new HashMap<>();
        params.put("last_id", String.valueOf(start));
        getList(type, params, lastTime, limit, listener);
    }

    public static final String CACHE_HOME_NAME = "homes";
    public static final String CACHE_NEWS_NAME = "news";
    public static final String CACHE_PHOTOS_NAME = "photos";
    public static final String CACHE_FOCUS_NAME = "focus_photo";
    public static final String CACHE_VIDEO_NAME = "video";


    public static String getCacheName(int operator) {
        String name = "";
        switch (operator) {
            case Req.NEWS:
                name = CACHE_NEWS_NAME;
                break;
            case Req.FOCUS:
                name = CACHE_FOCUS_NAME;
                break;
            case Req.HOME:
                name = CACHE_HOME_NAME;
                break;
            case Req.PHOTO:
                name = CACHE_PHOTOS_NAME;
                break;
            case Req.VIDEO:
                name = CACHE_VIDEO_NAME;
                break;
        }
        return name;
    }

    private static boolean loadCache(final int operator) {

        String data = FileUtils.readFile(Global.sContext, getCacheName(operator));
        ArrayList<BaseModel> list = JsonParser.parseJson2(data, operator);
        if (list != null && !list.isEmpty()) {
            DataManager.addData(operator, list);
            return true;
        } else {
            return false;
        }
    }

    public static void getList(final int operator, /*final SparseArray<Integer> starts,*/final HashMap<String, String> params, final String lastTime, int limit, final DataManager.DataLoadListListener listener) {

//        String time = lastTime;
//        if (!TextUtils.isEmpty(lastTime)) {
//            List<BaseModel> list = DataManager.getDataList(type);
//            time = list.get(list.size() - 1).time;
//        }
//        int startId = start;
//        if (start > 0) {
//            List<BaseModel> list = DataManager.getDataList(type);
//            startId = list.get(list.size() - 1).id;
//        }

        String url = getUrl(operator) + buildQueryParam(getAction(operator), limit, lastTime, 0, params);

        final boolean isAppend = !(TextUtils.isEmpty(lastTime) && params == null);

        new AlienHttpUtil().send(url, null, new AlienHttpListener() {

            @Override
            public void onFailed(Exception e) {
                e.printStackTrace();
                listener.loadComplete(Error.NETWORK_ERROR, false, operator, isAppend);
                showToast(R.string.net_err);
            }

            @Override
            public void onProgress(long l, long l1) {

            }

            @Override
            public void onSuccess(byte[] bytes, String s) {
                String json = new String(bytes);

                ArrayList<BaseModel> result = JsonParser.parseJson2(json, operator);
                if (result == null) {
                    listener.loadComplete(Error.DATA_ERROR, false, operator, isAppend);
                    return;
                } else if (result.isEmpty()) {
                    listener.loadComplete(Error.NO_MORE, false, operator, isAppend);
                    showToast(R.string.no_more_err);
                    return;
                }

                ArrayList<BaseModel> list = DataManager.getData(operator);

                boolean isAppend = true;
                if (TextUtils.isEmpty(lastTime) && params == null) {
                    isAppend = false;
                }

                storeToFile(json, getCacheName(operator), isAppend);

                if (!isAppend) {
                    DataManager.addData(operator, result);
                } else {
                    list.addAll(result);
                }
                listener.loadComplete(Error.OK, false, operator, isAppend);

            }
        });
    }

    public static void getDetail(final int operator, final BaseModel model, final DataManager.DataLoadDetailListener listener) {
        getDetail(operator, model, null, listener);
    }

    public static void getDetail(final int operator, final BaseModel model, HashMap<String, String> params, final DataManager.DataLoadDetailListener listener) {

        String url = getUrl(operator) + buildQueryParam(GET_DETAIL_ACTION, 0, "", model.id, params);
        new AlienHttpUtil().send(url, null, new AlienHttpListener() {

            @Override
            public void onFailed(Exception e) {
                e.printStackTrace();
                listener.loadComplete(Error.NETWORK_ERROR, false, operator);
            }

            @Override
            public void onProgress(long l, long l1) {

            }

            @Override
            public void onSuccess(byte[] bytes, String s) {
                String json = new String(bytes);

                int ret = JsonParser.parseDetailJson(json, model);
                if (ret != 0) {
                    listener.loadComplete(Error.DATA_ERROR, false, operator);
                    return;
                }

                listener.loadComplete(Error.OK, false, operator);
            }
        });
    }

    private static void storeToFile(String json, String fileName, boolean isAppend) {
        if (TextUtils.isEmpty(fileName)) return;
        String path = FileUtils.getCachePath(fileName);
        if (!isAppend) {
            FileUtils.writeFile(Global.sContext, json, path, false);
        } else {
            File file = new File(path);
            if (file.exists()) {
                long length = file.length();
                if (length > 2) {
                    try {
                        JSONObject jobj = new JSONObject(json);
                        String writeStr = Utils.stringToUnicode(jobj.optString("data"));
                        if (writeStr.length() > 2) {
                            writeStr = writeStr.substring(1, writeStr.length() - 1);
                            writeStr = "," + writeStr + "]}";
                            FileUtils.accessWriteFile(Global.sContext, writeStr, path, length - 2);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void showToast(final int error) {
        new Handler(Global.sContext.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Global.sContext, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
//    public static void getHomeFocus(final DataManager.DataLoadListener listener) {
//        new AlienHttpUtil().send(AddressUtils.FOCUS_PHOTO_URL, null, new AlienHttpListener() {
//
//            @Override
//            public void onFailed(Exception e) {
//
//            }
//
//            @Override
//            public void onProgress(long l, long l1) {
//
//            }
//
//            @Override
//            public void onSuccess(byte[] bytes, String s) {
//                String json = new String(bytes);
//                FileUtils.writeFile(Global.sContext, json, DataManager.CACHE_FOCUS_NAME, false);
//                ArrayList<BaseModel> newList = JsonParser.parseJson(json, BaseModel.PHOTO_TYPE);
//                DataManager.focusList.clear();
//                DataManager.focusList.addAll(newList);
//                listener.loadComplete(0);
//            }
//        });
//
//    }
//
//    public static void getHomeItems(final DataManager.DataLoadListener listener) {
//
//        new AlienHttpUtil().send(AddressUtils.HOME_URL, null, new AlienHttpListener() {
//
//            @Override
//            public void onFailed(Exception e) {
//
//            }
//
//            @Override
//            public void onProgress(long l, long l1) {
//
//            }
//
//            @Override
//            public void onSuccess(byte[] bytes, String s) {
//                String json = new String(bytes);
//                FileUtils.writeFile(Global.sContext, json, DataManager.CACHE_HOME_NAME, false);
//                ArrayList<BaseModel> newList = JsonParser.parseJson(json);
//                DataManager.homeList.clear();
//                DataManager.homeList.addAll(newList);
//                listener.loadComplete(0);
//            }
//        });
//    }
//
//    public static void getNews(int start, int limit, final DataManager.DataLoadListener listener) {
//        String url = AddressUtils.NEWS_URL + buildQueryParam(NEWS_GETLIST_ACTION, limit, start, 0);
//        new AlienHttpUtil().send(url, null, new AlienHttpListener() {
//
//            @Override
//            public void onFailed(Exception e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onProgress(long l, long l1) {
//
//            }
//
//            @Override
//            public void onSuccess(byte[] bytes, String s) {
//                String json = new String(bytes);
//                //FileUtils.writeFile(Global.sContext, json, DataManager.CACHE_NEWS_NAME, false);
//                ArrayList<BaseModel> newList = JsonParser.parseJson2(json);
//                DataManager.newsList.clear();
//                DataManager.newsList.addAll(newList);
//                listener.loadComplete(0);
//            }
//        });
//    }
//
//    public static void getPhotos(final DataManager.DataLoadListener listener) {
//        new AlienHttpUtil().send(AddressUtils.PHOTO_URL, null, new AlienHttpListener() {
//
//            @Override
//            public void onFailed(Exception e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onProgress(long l, long l1) {
//
//            }
//
//            @Override
//            public void onSuccess(byte[] bytes, String s) {
//                String json = new String(bytes);
//                FileUtils.writeFile(Global.sContext, json, DataManager.CACHE_PHOTOS_NAME, false);
//                ArrayList<BaseModel> newList = JsonParser.parseJson(json, BaseModel.PHOTO_TYPE);
//                DataManager.photoList.clear();
//                DataManager.photoList.addAll(newList);
//                listener.loadComplete(0);
//            }
//        });
//    }


}
