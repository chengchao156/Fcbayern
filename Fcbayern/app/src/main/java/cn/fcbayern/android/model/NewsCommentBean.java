package cn.fcbayern.android.model;

import java.io.Serializable;

/**
 * Created by chengchao on 16/9/29.
 */
public class NewsCommentBean implements Serializable{

    private int id;
    private int cont_id;
    private int uid;
    private String username;
    private String avatar;
    private String content;
    private long time;
    private int good;
    private int gooded;

    public NewsCommentBean(int id, int cont_id, int uid, String username, String avatar, String content, long time, int good, int gooded) {
        this.id = id;
        this.cont_id = cont_id;
        this.uid = uid;
        this.username = username;
        this.avatar = avatar;
        this.content = content;
        this.time = time;
        this.good = good;
        this.gooded = gooded;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCont_id() {
        return cont_id;
    }

    public void setCont_id(int cont_id) {
        this.cont_id = cont_id;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getGood() {
        return good;
    }

    public void setGood(int good) {
        this.good = good;
    }

    public int getGooded() {
        return gooded;
    }

    public void setGooded(int gooded) {
        this.gooded = gooded;
    }
}
