package cn.fcbayern.android.parse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import cn.fcbayern.android.model.BaseModel;
import cn.fcbayern.android.model.PlayerModel;

/**
 * Created by chenzhan on 15/5/26.
 */
public class TeamParser implements Parser {

    @Override
    public ArrayList<BaseModel> parse(JSONObject object) {

        ArrayList<BaseModel> players = new ArrayList<>();

        object = object.optJSONObject("data");

        if (object != null) {
            Iterator<?> it = object.keys();
            while (it.hasNext()) {

                String key = it.next().toString();
                JSONArray array = object.optJSONArray(key);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject o = array.optJSONObject(i);
                    PlayerModel model = new PlayerModel();
                    model.parse(o);
                    model.position = key;
                    players.add(model);
                }

            }
        }

        return players;
    }
}
