package cn.fcbayern.android.data;

import java.util.ArrayList;

import cn.fcbayern.android.R;

/**
 * Created by chenzhan on 15/5/25.
 */
public class NaviMenuData {

    public static ArrayList<NaviMenuData> modelsList = new ArrayList<>();

    static {
        modelsList.add(new NaviMenuData(R.string.home, "Home", R.id.navi_home, R.drawable.navi_home_icon));
        modelsList.add(new NaviMenuData(R.string.news, "News", R.id.navi_news, R.drawable.navi_news_icon));
        modelsList.add(new NaviMenuData(R.string.photos, "Photos", R.id.navi_photos, R.drawable.navi_photos_icon));
        modelsList.add(new NaviMenuData(R.string.video, "Video", R.id.navi_video, R.drawable.navi_video_icon));
        modelsList.add(new NaviMenuData(R.string.shop, "Fan Shop", R.id.navi_shop, R.drawable.navi_shop_icon));
        modelsList.add(new NaviMenuData(R.string.schedule, "Match", R.id.navi_match, R.drawable.navi_schedule_icon));
        modelsList.add(new NaviMenuData(R.string.stand, "Standings", R.id.navi_stand, R.drawable.navi_stand_icon));
        modelsList.add(new NaviMenuData(R.string.team, "Team", R.id.navi_team, R.drawable.navi_team_icon));
        modelsList.add(new NaviMenuData(R.string.club, "Club", R.id.navi_club, R.drawable.navi_club_icon));
        modelsList.add(new NaviMenuData(R.string.settings, "Settings", R.id.navi_settings, R.drawable.navi_settings_icon));
    }

    public int strRes;
    public int iconRes;
    public String alias;
    public int id;

    public NaviMenuData(int titleRes, String alias, int id, int iconRes) {
        this.strRes = titleRes;
        this.iconRes = iconRes;
        this.alias = alias;
        this.id = id;
    }

}
