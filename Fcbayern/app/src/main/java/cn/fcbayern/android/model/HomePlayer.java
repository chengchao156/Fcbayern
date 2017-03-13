package cn.fcbayern.android.model;

import java.io.Serializable;

/**
 * Created by chengchao on 16/9/19.
 */
public class HomePlayer implements Serializable{

    private String name;
    private String player_id;
    private String number;
    private String pic;
    private int is_coach;

    public HomePlayer(String name, String player_id, String number, String pic,int is_coach) {
        this.name = name;
        this.player_id = player_id;
        this.number = number;
        this.pic = pic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(String player_id) {
        this.player_id = player_id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public int getIs_coach() {
        return is_coach;
    }

    public void setIs_coach(int is_coach) {
        this.is_coach = is_coach;
    }
}
