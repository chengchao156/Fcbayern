package cn.fcbayern.android.model;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chenzhan on 15/5/26.
 */
public class PhotoModel extends BaseModel {

    public ArrayList<String> thumbsPic = new ArrayList<String>();
    public ArrayList<String> pics = new ArrayList<String>();
    public ArrayList<String> titles = new ArrayList<String>();
    public String title;
    public String imageUrl;
    public String url;

    @Override
    public void parse(JSONObject object) {
        super.parse(object);

        title = object.optString("title", title);
        imageUrl = object.optString("pic", imageUrl);
        url = object.optString("url", url);

        JSONArray thumbs = object.optJSONArray("thmub");
        if (thumbs != null) {
            for (int j = 0; j < thumbs.length(); j++) {
                String url = thumbs.optString(j);
                if (!TextUtils.isEmpty(url)) {
                    thumbsPic.add(url);
                }
            }
        }

        JSONArray picList = object.optJSONArray("data");
        if (picList != null) {
            for (int j = 0; j < picList.length(); j++) {
                JSONObject obj = picList.optJSONObject(j);
                String url = obj.optString("pic");
                String title = obj.optString("title");
                if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(title)) {
                    pics.add(url);
                    titles.add(title);
                }
            }
        }

    }
}
