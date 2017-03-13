package cn.fcbayern.android.util;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

/**
 * Created by chenzhan on 15/6/30.
 */
public class DataReport {

    public final static String NAV_EVENTID = "Topbar";
    public final static String NAV_KEY = "MainNav";
    public final static String NAV_VAL = "Nav";
    public final static String NAV_INDEX_VAL = "TabIndex";
    public final static String NAV_NEWS_VAL = "TabNews";
    public final static String NAV_PHOTOS_VAL = "TabPhotos";
    public final static String NAV_SETTING_VAL = "TabSettings";
    public final static String NAV_TEAM_VAL = "TabTeam";
    public final static String NAV_SHOP_VAL = "TabShop";
    public final static String NAV_VIDEO_VAL = "TabVideos";
    public final static String NAV_MATCH_VAL = "TabFixtures";
    public final static String NAV_STAND_VAL = "TabStandings";
    public final static String NAV_CLUB_VAL = "TabClub";

    public final static String GAME_CENTER_KEY = "GameCenter";
    public final static String GAME_CENTER_VAL = "TabGameCenter";

    public final static String INDEX_EVENTID = "Index";
    public final static String INDEX_KEY = "List";
    public final static String INDEX_NEWS_VAL = "News";
    public final static String INDEX_PHOTO_VAL = "Photos";
    public final static String INDEX_REFRESH_VAL = "RefreshList";
    public final static String INDEX_LOAD_VAL = "LoadingList";

    public final static String NEWS_EVENTID = "News";
    public final static String NEWS_KEY = "NewsList";
    public final static String NEWS_NEWS_VAL = "News";
    public final static String NEWS_REFRESH_VAL = "RefreshNewsList";
    public final static String NEWS_LOAD_VAL = "LoadingNewsList";

    public final static String PHOTO_EVENTID = "Photos";
    public final static String PHOTO_KEY = "PhotosList";
    public final static String PHOTO_PHOTOS_VAL = "Photos";
    public final static String PHOTO_REFRESH_VAL = "RefreshPhotosList";
    public final static String PHOTO_LOAD_VAL = "LoadingPhotosList";

    public final static String VIDEO_EVENTID = "Videos";
    public final static String VIDEO_KEY = "VideosList";
    public final static String VIDEO_VIDEO_VAL = "Videos";
    public final static String VIDEO_REFRESH_VAL = "RefreshVideosList";
    public final static String VIDEO_LOAD_VAL = "LoadingVideosList";

    public static void report(Context context, String eventId, String key, String value) {
        HashMap<String, String> kv = new HashMap<>();
        kv.put(key, value);
        MobclickAgent.onEvent(context, eventId, kv);
    }

}
