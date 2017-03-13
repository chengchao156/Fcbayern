package cn.fcbayern.android.model;

import org.json.JSONObject;

import cn.fcbayern.android.R;
import cn.fcbayern.android.util.DateUtils;
import cn.fcbayern.android.util.JsonParser;

/**
 * Created by chenzhan on 15/5/26.
 */
public class PlayerModel extends BaseModel {

    public String name;
    public String nameEn;
    public String number;
    public String birthday;
    public String birthPlace;
    public String position;
    public String imageUrl;
    public String edu;
    public String zodiac;
    public String height;
    public String weight;
    public String family;
    public String shoeSize;
    public String desc;
    public String posName;
    public String title;

    public int imageH;
    public int imageW;

    @Override
    public void parse(JSONObject object) {
        super.parse(object);

        name = object.optString("name", name);
        nameEn = object.optString("name_en", nameEn);
        number = object.optString("No", number);
        birthday = object.optString("birthday", birthday);

        imageUrl = object.optString("pic", imageUrl);

        birthPlace = object.optString("birthplace", birthPlace);
        edu = object.optString("edu", edu);
        zodiac = object.optString("zodiac", zodiac);
        height = object.optString("height", height);
        weight = object.optString("weight", weight);
        family = object.optString("family", family);
        shoeSize = object.optString("shoesize", shoeSize);
        desc = object.optString("desc", desc);
        title = object.optString("title", title);

        posName = object.optString("type", posName);

        imageH = object.optInt("pic_height", imageH);
        imageW = object.optInt("pic_width", imageW);
    }

    public static boolean isCoach(int type) {
        return type == 5;
    }

    public static int getPlayerType(int type) {
        switch (type) {
            case 1:
                return R.string.player_type_keeper;
            case 2:
                return R.string.player_type_back;
            case 3:
                return R.string.player_type_center;
            case 4:
                return R.string.player_type_kick;
            case 5:
                return R.string.player_type_coach;
        }
        return 0;
    }
}
