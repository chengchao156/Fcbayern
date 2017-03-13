package cn.fcbayern.android.parse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import cn.fcbayern.android.model.BaseModel;
import cn.fcbayern.android.model.RankModel;

/**
 * Created by chenzhan on 15/5/26.
 */
public class StandParser implements Parser {

    @Override
    public ArrayList<BaseModel> parse(JSONObject object) {

//        super.parse(object);
        ArrayList<BaseModel> teams = new ArrayList<>();

        object = object.optJSONObject("data");
        JSONObject obj = object.optJSONObject("leagues");
        JSONObject rank = object.optJSONObject("rank");

        if (rank != null) {
            RankModel.leagues.clear();
            Iterator<?> it = rank.keys();
            while (it.hasNext()) {

                String key = it.next().toString();
                String leagueName = obj.optString(key);
                RankModel.leagues.add(leagueName);
//                ArrayList<TeamModel> list = teams.get(leagueName);
//                if (list == null) {
//                    list = new ArrayList<>();
//                    teams.put(leagueName, list);
//                }

                JSONArray array = rank.optJSONArray(key);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject o = array.optJSONObject(i);
                    RankModel model = new RankModel();
                    model.parse(o);
                    model.type = BaseModel.RANK_ITEM;
                    model.league = leagueName;
                    teams.add(model);
                }

            }
        }

        return teams;

    }
}
