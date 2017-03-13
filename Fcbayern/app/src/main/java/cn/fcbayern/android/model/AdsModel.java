package cn.fcbayern.android.model;

import org.json.JSONObject;

/**
 * Created by chenzhan on 15/5/26.
 */
public class AdsModel extends BaseModel {

    public String pic;
    public String url;

    @Override
    public void parse(JSONObject object) {
        super.parse(object);

        pic = object.optString("pic", pic);
        url = object.optString("url", url);
    }
}
