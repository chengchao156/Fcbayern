package cn.fcbayern.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by chengchao on 16/9/20.
 */
public class Rank implements Parcelable{

    private String team_group;
    private int team_id;
    private String name_zh;
    private String known_name_zh;
    private int played;
    private int rank_index;
    private int win;
    private int draw;
    private int lost;
    private int hits;
    private int miss;
    private int difference;
    private int score;
    private int avg_goal_hit;
    private int avg_goal_lost;
    private int avg_goal_win;
    private int avg_score;
    private String promotion_id;
    private String promotion_name;
    private String team_logo;
    private int isHomeTeam;

    public Rank(String team_group, int team_id, String name_zh, String known_name_zh, int played, int rank_index, int win, int draw, int lost, int hits, int miss, int difference, int score, int avg_goal_hit, int avg_goal_lost, int avg_goal_win, int avg_score, String promotion_id, String promotion_name, String team_logo, int isHomeTeam) {
        this.team_group = team_group;
        this.team_id = team_id;
        this.name_zh = name_zh;
        this.known_name_zh = known_name_zh;
        this.played = played;
        this.rank_index = rank_index;
        this.win = win;
        this.draw = draw;
        this.lost = lost;
        this.hits = hits;
        this.miss = miss;
        this.difference = difference;
        this.score = score;
        this.avg_goal_hit = avg_goal_hit;
        this.avg_goal_lost = avg_goal_lost;
        this.avg_goal_win = avg_goal_win;
        this.avg_score = avg_score;
        this.promotion_id = promotion_id;
        this.promotion_name = promotion_name;
        this.team_logo = team_logo;
        this.isHomeTeam = isHomeTeam;
    }

    public String getTeam_group() {
        return team_group;
    }

    public void setTeam_group(String team_group) {
        this.team_group = team_group;
    }

    public int getTeam_id() {
        return team_id;
    }

    public void setTeam_id(int team_id) {
        this.team_id = team_id;
    }

    public String getName_zh() {
        return name_zh;
    }

    public void setName_zh(String name_zh) {
        this.name_zh = name_zh;
    }

    public String getKnown_name_zh() {
        return known_name_zh;
    }

    public void setKnown_name_zh(String known_name_zh) {
        this.known_name_zh = known_name_zh;
    }

    public int getPlayed() {
        return played;
    }

    public void setPlayed(int played) {
        this.played = played;
    }

    public int getRank_index() {
        return rank_index;
    }

    public void setRank_index(int rank_index) {
        this.rank_index = rank_index;
    }

    public int getWin() {
        return win;
    }

    public void setWin(int win) {
        this.win = win;
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public int getLost() {
        return lost;
    }

    public void setLost(int lost) {
        this.lost = lost;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public int getMiss() {
        return miss;
    }

    public void setMiss(int miss) {
        this.miss = miss;
    }

    public int getDifference() {
        return difference;
    }

    public void setDifference(int difference) {
        this.difference = difference;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getAvg_goal_hit() {
        return avg_goal_hit;
    }

    public void setAvg_goal_hit(int avg_goal_hit) {
        this.avg_goal_hit = avg_goal_hit;
    }

    public int getAvg_goal_lost() {
        return avg_goal_lost;
    }

    public void setAvg_goal_lost(int avg_goal_lost) {
        this.avg_goal_lost = avg_goal_lost;
    }

    public int getAvg_goal_win() {
        return avg_goal_win;
    }

    public void setAvg_goal_win(int avg_goal_win) {
        this.avg_goal_win = avg_goal_win;
    }

    public int getAvg_score() {
        return avg_score;
    }

    public void setAvg_score(int avg_score) {
        this.avg_score = avg_score;
    }

    public String getPromotion_id() {
        return promotion_id;
    }

    public void setPromotion_id(String promotion_id) {
        this.promotion_id = promotion_id;
    }

    public String getPromotion_name() {
        return promotion_name;
    }

    public void setPromotion_name(String promotion_name) {
        this.promotion_name = promotion_name;
    }

    public String getTeam_logo() {
        return team_logo;
    }

    public void setTeam_logo(String team_logo) {
        this.team_logo = team_logo;
    }

    public int getIsHomeTeam() {
        return isHomeTeam;
    }

    public void setIsHomeTeam(int isHomeTeam) {
        this.isHomeTeam = isHomeTeam;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
