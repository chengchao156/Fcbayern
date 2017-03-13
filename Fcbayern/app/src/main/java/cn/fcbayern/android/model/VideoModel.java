package cn.fcbayern.android.model;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by chenzhan on 15/5/26.
 */
public class VideoModel extends BaseModel {

    public String title;
    public String imageUrl;
    public String url;
    public String tags = "";

    @Override
    public void parse(JSONObject object) {
        super.parse(object);

        title = object.optString("title", title);
        imageUrl = object.optString("pic", imageUrl);
        url = object.optString("link", url);

        JSONArray obj = object.optJSONArray("tags");
        if (obj != null) {
            for (int i = 0; i < obj.length(); i++) {
                tags += obj.optString(i) + " ";
            }
        }
    }
}
