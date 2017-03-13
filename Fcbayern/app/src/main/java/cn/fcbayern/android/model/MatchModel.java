package cn.fcbayern.android.model;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chenzhan on 15/5/26.
 */
public class MatchModel extends BaseModel {

    public String hostName;
    public String awayName;
    public String hostIconUrl;
    public String awayIconUrl;
    public String hostScore;
    public String awayScore;
    public String halfScore;
    public long matchTime;
    public String leagueName;
    public int leagueId;
    public int newsId;
    public int albumId;
    public String liveInfo;

    public final static ArrayList<String> leagueList = new ArrayList<>();
    static {
        leagueList.add("德甲");
        leagueList.add("欧冠");
        leagueList.add("德国杯");
        leagueList.add("其他");
    }

    @Override
    public void parse(JSONObject object) {
        super.parse(object);

        id = object.optInt("game_id", id);

        hostName = object.optString("home_name", hostName);
        awayName = object.optString("away_name", awayName);
        hostIconUrl = object.optString("home_logo", hostIconUrl);
        awayIconUrl = object.optString("away_logo", awayIconUrl);
        hostScore = object.optString("home_score", hostScore);
        awayScore = object.optString("away_score", awayScore);
        halfScore = object.optString("half_score", halfScore);

        matchTime = object.optLong("match_date_cn", matchTime);
        leagueName = object.optString("league_title", leagueName);
        leagueId = object.optInt("league_id", leagueId);

        newsId = object.optInt("news_link", newsId);
        albumId = object.optInt("album_link", albumId);
        liveInfo = object.optString("relay_info", liveInfo);
    }
}
