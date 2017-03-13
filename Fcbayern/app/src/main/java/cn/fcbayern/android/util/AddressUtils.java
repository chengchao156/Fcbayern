package cn.fcbayern.android.util;

/**
 * Created by chenzhan on 15/5/26.
 */
public class AddressUtils {

    //public static final String FOCUS_PHOTO_URL = "http://192.168.9.30:8080/app/focus";

    public static final String API_DOMAIN = AppConfig.DEBUG_SERVER ? "api.fcbayern.cn:8080" : "api.fcbayern.cn";

    public static final String DOMAIN = AppConfig.DEBUG_SERVER ? "192.168.9.30:8080" : "www.fcbayern.cn";

    public static final String NEWS_URL = "http://" + API_DOMAIN + "/news";

    public static final String PHOTO_URL = "http://" + API_DOMAIN + "/album";

    public static final String HOME_URL = "http://" + API_DOMAIN + "/home_data";

    public static final String MATCH_URL = "http://" + API_DOMAIN + "/match";

    public static final String TEAM_URL = "http://" + API_DOMAIN + "/team";

    public static final String VIDEO_URL = "http://" + API_DOMAIN + "/video";

    public static final String IMAGE_PREFIX = "http://7xj11p.com1.z0.glb.clouddn.com/";

    public static final String DETAIL_NEWS_URL = "http://" + DOMAIN + "/news/";

    public static final String DETAIL_PHOTO_URL = "http://" + DOMAIN + "/photo/album/%d.html";

    public static final String SHOP_URL = "http://fcb.tmall.hk/";

    public static final String CLUB_URL = "http://www.fcbayern.cn/club";

    public static final String APP_PARAM = "?app=1";

    public static final String USER_URL = "http://" + API_DOMAIN + "/user";

    public static final String OTHER_URL = "http://" + API_DOMAIN + "/other";

}
