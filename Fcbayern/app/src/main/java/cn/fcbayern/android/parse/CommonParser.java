package cn.fcbayern.android.parse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.fcbayern.android.model.AdsModel;
import cn.fcbayern.android.model.BaseModel;
import cn.fcbayern.android.model.MatchModel;
import cn.fcbayern.android.model.NewsModel;
import cn.fcbayern.android.model.PhotoModel;
import cn.fcbayern.android.model.PlayerModel;
import cn.fcbayern.android.model.VideoModel;
import cn.fcbayern.android.util.JsonParser;

/**
 * Created by chenzhan on 15/5/26.
 */
public class CommonParser implements Parser {

    @Override
    public ArrayList<BaseModel> parse(JSONObject obj) {

        ArrayList<BaseModel> objs = new ArrayList<>();

        JSONArray array = obj.optJSONArray("data");
        int outType = obj.optInt(JsonParser.TYPE_KEY, -1);
        BaseModel model = null;
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            int subType = object.optInt(JsonParser.TYPE_KEY, -1);
            if (BaseModel.MIX_ITEM != outType) {
                subType = outType;
            }
            switch (subType) {
                case BaseModel.FOCUS_ITEM:
                case BaseModel.PHOTO_ITEM:
                    model = new PhotoModel();
                    break;
                case BaseModel.NEWS_ITEM:
                    model = new NewsModel();
                    break;
                case BaseModel.SCHEDULE_ITEM:
                    model = new MatchModel();
                    break;
                case BaseModel.PLAYER_ITEM:
                    model = new PlayerModel();
                    break;
                case BaseModel.ADS_ITEM:
                    model = new AdsModel();
                    break;
                case BaseModel.VIDEO_ITEM:
                    model = new VideoModel();
                    break;
            }
            model.parse(object);
            model.type = subType;
            objs.add(model);
        }
        return objs;

    }
}
