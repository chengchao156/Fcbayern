package cn.fcbayern.android.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chengchao on 16/9/28.
 */
public class PhotoBean implements Serializable{

    private String id;
    private String type;
    private String pic;
    private String title;
    private String content;
    private String date;

    private List<String>thumb;


    public PhotoBean(String id, String pic, String title, String content, String date, List<String> thumb) {
        this.id = id;
        this.pic = pic;
        this.title = title;
        this.content = content;
        this.date = date;

        this.thumb = thumb;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getThumb() {
        return thumb;
    }

    public void setThumb(List<String> thumb) {
        this.thumb = thumb;
    }
}
