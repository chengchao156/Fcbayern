package cn.fcbayern.android.parse;

import org.json.JSONObject;

import java.util.ArrayList;

import cn.fcbayern.android.model.BaseModel;

/**
 * Created by chenzhan on 15/7/23.
 */
public interface Parser {

//    public ArrayList<BaseModel> objs = new ArrayList<>();
//
//    public boolean isEmpty() {
//        return objs.isEmpty();
//    }

    public ArrayList<BaseModel> parse(JSONObject obj);

//    public BaseModel get(int index) {
//        return objs.get(index);
//    }
//
//    public int size() {
//        return objs.size();
//    }
//
//    public void add(ArrayList<BaseModel> models) {
//        objs.addAll(models);
//    }
//
//    public ArrayList<BaseModel> getAll() {
//        return objs;
//    }

}
