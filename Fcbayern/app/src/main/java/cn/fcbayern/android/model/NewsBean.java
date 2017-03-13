package cn.fcbayern.android.model;

import java.io.Serializable;

/**
 * Created by chengchao on 16/9/27.
 */
public class NewsBean implements Serializable{

    private String id;
    private String pic;
    private String title;
    private String content;
    private String date;

    public NewsBean(String id, String pic, String title, String content, String date) {
        this.id = id;
        this.pic = pic;
        this.title = title;
        this.content = content;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
