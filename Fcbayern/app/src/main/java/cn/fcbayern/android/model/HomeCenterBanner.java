package cn.fcbayern.android.model;

import java.io.Serializable;

/**
 * Created by chengchao on 16/9/19.
 */
public class HomeCenterBanner implements Serializable{

    private String id;
    private String pic;
    private String url;

    public HomeCenterBanner(String id, String pic, String url) {
        this.id = id;
        this.pic = pic;
        this.url = url;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
