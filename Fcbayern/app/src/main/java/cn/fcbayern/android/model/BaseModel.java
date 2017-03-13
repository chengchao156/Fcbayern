package cn.fcbayern.android.model;

import org.json.JSONObject;

/**
 * Created by chenzhan on 15/5/26.
 */
public class BaseModel {

    public static final int MIX_ITEM = 0;
    public static final int NEWS_ITEM = 1;
    public static final int PHOTO_ITEM = 2;
    public static final int FOCUS_ITEM = 3;
    public static final int RANK_ITEM = 5;
    public static final int SCHEDULE_ITEM = 6;
    public static final int PLAYER_ITEM = 7;
    public static final int ADS_ITEM = 8;
    public static final int VIDEO_ITEM = 9;

    public static final int BAYERN_ID = 358;


    public int id;
    public int type = MIX_ITEM;
    public String time;

    public void parse(JSONObject object) {
        id = object.optInt("id", id);
        time = object.optString("date", time);
    }

    public boolean isEmpty() {
        return false;
    }

}
