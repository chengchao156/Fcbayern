package cn.fcbayern.android.model;

import java.io.Serializable;

/**
 * Created by chengchao on 16/9/19.
 */
public class HomeVideoBanner implements Serializable{

    private String title;
    private String content;
    private String pic;
    private String url;
    private int orderby;
    private String date;
    private int show_type;
    private int show_id;

    public HomeVideoBanner(String title, String content, String pic, String url, int orderby, String date, int show_type, int show_id) {
        this.title = title;
        this.content = content;
        this.pic = pic;
        this.url = url;
        this.orderby = orderby;
        this.date = date;
        this.show_type = show_type;
        this.show_id = show_id;
    }

    public int getOrderby() {
        return orderby;
    }

    public void setOrderby(int orderby) {
        this.orderby = orderby;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getShow_type() {
        return show_type;
    }

    public void setShow_type(int show_type) {
        this.show_type = show_type;
    }

    public int getShow_id() {
        return show_id;
    }

    public void setShow_id(int show_id) {
        this.show_id = show_id;
    }
}
