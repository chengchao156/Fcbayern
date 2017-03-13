package cn.fcbayern.android.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.model.BaseModel;
import cn.fcbayern.android.parse.CommonParser;
import cn.fcbayern.android.parse.Parser;
import cn.fcbayern.android.parse.StandParser;
import cn.fcbayern.android.parse.TeamParser;

/**
 * Created by chenzhan on 15/5/27.
 */
public class JsonParser {

//    public static ArrayList<PhotoModel> parseFocusJson(String data) {
//
//        ArrayList<PhotoModel> models = new ArrayList<PhotoModel>();
//        try {
//            JSONArray array = new JSONArray(data);
//            for(int i = 0; i < array.length(); i++) {
//                PhotoModel model = new PhotoModel();
//                JSONObject object = array.optJSONObject(i);
//                model.id = object.optInt("id");
//                model.type = BaseModel.FOCUS_TYPE;
//                model.title = object.optString("title");
//                model.time = object.optString("time");
//                models.add(model);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return models;
//
//    }
//
//    public static ArrayList<NewsModel> parseNewsJson(String data) {
//
//        ArrayList<NewsModel> models = new ArrayList<NewsModel>();
//        try {
//            JSONArray array = new JSONArray(data);
//            for(int i = 0; i < array.length(); i++) {
//                NewsModel model = new NewsModel();
//                JSONObject object = array.optJSONObject(i);
//                model.id = object.optInt("id");
//                model.type = BaseModel.NEWS_TYPE;
//                model.title = object.optString("title");
//                model.time = object.optString("time");
//                model.imageUrl = object.optString("pic");
//                model.content = object.optString("content");
//                models.add(model);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return models;
//
//    }
//
//    public static ArrayList<PhotoModel> parsePhotoJson(String data) {
//
//        ArrayList<PhotoModel> models = new ArrayList<PhotoModel>();
//        try {
//            JSONArray array = new JSONArray(data);
//            for(int i = 0; i < array.length(); i++) {
//                PhotoModel model = new PhotoModel();
//                JSONObject object = array.optJSONObject(i);
//                model.id = object.optInt("id");
//                model.type = BaseModel.PHOTO_TYPE;
//                model.title = object.optString("title");
//                model.time = object.optString("time");
//
//                JSONArray thumbs = object.optJSONArray("thmub");
//                if (thumbs != null) {
//                    for (int j = 0; j < thumbs.length(); j++) {
//                        String url = thumbs.optString(j);
//                        if (!TextUtils.isEmpty(url)) {
//                            model.pics.add(url);
//                        }
//                    }
//                }
//                models.add(model);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return models;
//
//    }

    public static final String TYPE_KEY = "cont_type";
    public static final String CODE_KEY = "code";

    public static final String OBJ_KEY = "KEYS";
//    public static ArrayList<BaseModel> parseJson(String data, int type) {
//        ArrayList<BaseModel> models = new ArrayList<BaseModel>();
//        try {
//            JSONArray array = new JSONArray(data);
//            BaseModel model = null;
//            for (int i = 0; i < array.length(); i++) {
//                if (type == BaseModel.PHOTO_TYPE) {
//                    model = new PhotoModel();
//                } else if (type == BaseModel.NEWS_TYPE) {
//                    model = new NewsModel();
//                }
//                JSONObject object = array.optJSONObject(i);
//                model.parse(object);
//                models.add(model);
//            }
//        } catch (JSONException e) {
//
//        }
//        return models;
//    }
//
//    public static class JsonResult {
//        public ArrayList<BaseModel> list;
//        public String storeData;
//    }

//    public static boolean checkCode(String data) {
//        try {
//            JSONObject obj = new JSONObject(data);
//            int code = obj.optInt(CODE_KEY, -1);
//        } catch (JSONException e) {
//
//        }
//    }

    public static ArrayList<BaseModel> parseJson2(String data, int operator) {
        try {
            JSONObject obj = new JSONObject(data);
            int code = obj.optInt(CODE_KEY, -1);
            if (code != 0) {
                return null;
            }

            Parser parser;
            if (operator == NetworkOper.Req.STAND) {
                parser = new StandParser();
            } else if (operator == NetworkOper.Req.TEAM) {
                parser = new TeamParser();
            } else {
                parser = new CommonParser();
            }
            ArrayList<BaseModel> models = parser.parse(obj);
            return models;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

//    private static ArrayList<BaseModel> parseArrayData(JSONArray array, String key, int type) {
//        ArrayList<BaseModel> models = new ArrayList<BaseModel>();
//
//        BaseModel model = null;
//        for (int i = 0; i < array.length(); i++) {
//            JSONObject object = array.optJSONObject(i);
//            if (!TextUtils.isEmpty(key)) {
//                try {
//                    object.put(OBJ_KEY, key);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            int subType = object.optInt(TYPE_KEY, -1);
//            if (BaseModel.MIX_ITEM != type) {
//                subType = type;
//            }
//            switch (subType) {
//                case BaseModel.FOCUS_ITEM:
//                case BaseModel.PHOTO_ITEM:
//                    model = new PhotoModel();
//                    break;
//                case BaseModel.NEWS_ITEM:
//                    model = new NewsModel();
//                    break;
//                case BaseModel.SCHEDULE_ITEM:
//                    model = new MatchModel();
//                    break;
//                case BaseModel.PLAYER_ITEM:
//                    model = new PlayerModel();
//                    break;
//                case BaseModel.ADS_ITEM:
//                    model = new AdsModel();
//                    break;
//                case BaseModel.VIDEO_ITEM:
//                    model = new VideoModel();
//                    break;
//            }
//            model.parse(object);
//            model.type = subType;
//            models.add(model);
//        }
//        return models;
//    }

//    private static ArrayList<BaseModel> parseObjectData(JSONObject obj, int type) {
//        ArrayList<BaseModel> models = new ArrayList<BaseModel>();
//        Iterator<?> it = obj.keys();
//        while (it.hasNext()) {
//            String key = it.next().toString();
//            JSONArray array = obj.optJSONArray(key);
//            models.addAll(parseArrayData(array, key, type));
//        }
//        return models;
//    }

    public static int parseDetailJson(String data, BaseModel model) {
        try {
            JSONObject obj = new JSONObject(data);
            int code = obj.optInt(CODE_KEY, -1);
            if (code != 0) {
                return -1;
            }
            model.type = obj.optInt(TYPE_KEY, -1);
            if (obj.optJSONObject("data") == null) {
                model.parse(obj);
            } else {
                model.parse(obj.optJSONObject("data"));
            }
            return 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
