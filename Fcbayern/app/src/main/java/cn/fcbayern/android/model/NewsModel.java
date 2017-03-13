package cn.fcbayern.android.model;

import org.json.JSONObject;

/**
 * Created by chenzhan on 15/5/26.
 */
public class NewsModel extends BaseModel {

    public String content;
    public String title;
    public String imageUrl;

    @Override
    public void parse(JSONObject object) {
        super.parse(object);

        title = object.optString("title", title);
        imageUrl = object.optString("pic", imageUrl);
        content = object.optString("content", content);
    }
}
