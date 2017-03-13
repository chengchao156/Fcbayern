package cn.fcbayern.android.model;

import java.io.Serializable;

/**
 * Created by chengchao on 16/9/19.
 */
public class HomeScheduleBoaed implements Serializable{


    private String game_id;
    private String league_id;
    private String match_day;
    private String match_date_cn;
    private String home_id;
    private String home_name;
    private String home_score;

    private String away_id;
    private String away_name;
    private String away_score;
    private String half_score;
    private String game_status;

    private int news_link;
    private int album_link;

    private String relay_info;

    private String home_logo;
    private String away_logo;

    private String league_title;
    private int show_default;

    public HomeScheduleBoaed(String game_id, String league_id, String match_day, String match_date_cn, String home_id, String home_name, String home_score, String away_id, String away_name, String away_score, String half_score, String game_status, int news_link, int album_link, String relay_info, String home_logo, String away_logo, String league_title,int show_default) {
        this.game_id = game_id;
        this.league_id = league_id;
        this.match_day = match_day;
        this.match_date_cn = match_date_cn;
        this.home_id = home_id;
        this.home_name = home_name;
        this.home_score = home_score;
        this.away_id = away_id;
        this.away_name = away_name;
        this.away_score = away_score;
        this.half_score = half_score;
        this.game_status = game_status;
        this.news_link = news_link;
        this.album_link = album_link;
        this.relay_info = relay_info;
        this.home_logo = home_logo;
        this.away_logo = away_logo;
        this.league_title = league_title;
        this.show_default = show_default;
    }

    public int getShow_default() {
        return show_default;
    }

    public void setShow_default(int show_default) {
        this.show_default = show_default;
    }

    public String getGame_id() {
        return game_id;
    }

    public void setGame_id(String game_id) {
        this.game_id = game_id;
    }

    public String getLeague_id() {
        return league_id;
    }

    public void setLeague_id(String league_id) {
        this.league_id = league_id;
    }

    public String getMatch_day() {
        return match_day;
    }

    public void setMatch_day(String match_day) {
        this.match_day = match_day;
    }

    public String getMatch_date_cn() {
        return match_date_cn;
    }

    public void setMatch_date_cn(String match_date_cn) {
        this.match_date_cn = match_date_cn;
    }

    public String getHome_id() {
        return home_id;
    }

    public void setHome_id(String home_id) {
        this.home_id = home_id;
    }

    public String getHome_name() {
        return home_name;
    }

    public void setHome_name(String home_name) {
        this.home_name = home_name;
    }

    public String getHome_score() {
        return home_score;
    }

    public void setHome_score(String home_score) {
        this.home_score = home_score;
    }

    public String getAway_id() {
        return away_id;
    }

    public void setAway_id(String away_id) {
        this.away_id = away_id;
    }

    public String getAway_name() {
        return away_name;
    }

    public void setAway_name(String away_name) {
        this.away_name = away_name;
    }

    public String getAway_score() {
        return away_score;
    }

    public void setAway_score(String away_score) {
        this.away_score = away_score;
    }

    public String getHalf_score() {
        return half_score;
    }

    public void setHalf_score(String half_score) {
        this.half_score = half_score;
    }

    public String getGame_status() {
        return game_status;
    }

    public void setGame_status(String game_status) {
        this.game_status = game_status;
    }

    public int getNews_link() {
        return news_link;
    }

    public void setNews_link(int news_link) {
        this.news_link = news_link;
    }

    public int getAlbum_link() {
        return album_link;
    }

    public void setAlbum_link(int album_link) {
        this.album_link = album_link;
    }

    public String getRelay_info() {
        return relay_info;
    }

    public void setRelay_info(String relay_info) {
        this.relay_info = relay_info;
    }

    public String getHome_logo() {
        return home_logo;
    }

    public void setHome_logo(String home_logo) {
        this.home_logo = home_logo;
    }

    public String getAway_logo() {
        return away_logo;
    }

    public void setAway_logo(String away_logo) {
        this.away_logo = away_logo;
    }

    public String getLeague_title() {
        return league_title;
    }

    public void setLeague_title(String league_title) {
        this.league_title = league_title;
    }
}
