package cn.fcbayern.android.model;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chenzhan on 15/7/23.
 */
public class RankModel extends BaseModel {

    public String name;
    public String league;
    public int teamId;
    public int rankIndex;
    public String teamLogo;

    public String win;
    public String draw;
    public String lost;

    public String hits;
    public String miss;

    public String score;

    public static ArrayList<String> leagues = new ArrayList<>();

    @Override
    public void parse(JSONObject object) {
        super.parse(object);

        name = object.optString("known_name_zh", name);
        teamId = object.optInt("team_id", teamId);
        rankIndex = object.optInt("rank_index", rankIndex);
        teamLogo = object.optString("team_logo", teamLogo);

        win = object.optString("win", win);
        draw = object.optString("draw", draw);
        lost = object.optString("lost", lost);

        hits = object.optString("hits", hits);
        miss = object.optString("miss", miss);

        score = object.optString("score", score);

    }
}
