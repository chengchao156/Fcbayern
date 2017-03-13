package cn.fcbayern.android.data;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.SparseArray;

import java.util.ArrayList;

import cn.fcbayern.android.model.BaseModel;
import cn.fcbayern.android.util.AppConfig;
import cn.fcbayern.android.util.DeviceUtils;
import cn.fcbayern.android.util.Global;
import cn.fcbayern.android.util.LogUtils;
import cn.fcbayern.android.util.image.ImageCache;
import cn.fcbayern.android.util.image.ImageLoader;

/**
 * Created by chenzhan on 15/5/26.
 */
public class DataManager {

    public final static String TAG = DataManager.class.getSimpleName();

    private static ImageLoader imageLoader = null;
    private static ImageLoader imageLoaderBig = null;

    private static final int THUMB_MEM_CACHE_SIZE_IN_KB = 6 << 10;

    private static final ArrayList<BaseModel> EMPTY = new ArrayList<>();

//    public final static int HOME_TYPE = 0;
//    public final static int NEWS_TYPE = 1;
//    public final static int PHOTO_TYPE = 2;
//    public final static int FOCUS_TYPE = 3;
//    public final static int MATCH_TYPE = 6;
//    public final static int TEAM_TYPE = 7;
//    public final static int HOME_ADS_TYPE = 8;
//    public final static int VIDEO_TYPE = 9;
//    public final static int LAST_MATCH_TYPE = 10;

    private static SparseArray<ArrayList<BaseModel>> datas = new SparseArray<>();

//    static {
//        datas.put(HOME_TYPE, new ArrayList<BaseModel>());
//        datas.put(NEWS_TYPE, new ArrayList<BaseModel>());
//        datas.put(PHOTO_TYPE, new ArrayList<BaseModel>());
//        datas.put(FOCUS_TYPE, new ArrayList<BaseModel>());
//        datas.put(MATCH_TYPE, new ArrayList<BaseModel>());
//        datas.put(TEAM_TYPE, new ArrayList<BaseModel>());
//        datas.put(HOME_ADS_TYPE, new ArrayList<BaseModel>());
//        datas.put(VIDEO_TYPE, new ArrayList<BaseModel>());
//        datas.put(LAST_MATCH_TYPE, new ArrayList<BaseModel>());
//    }

    public interface DataLoadListListener {
        void loadComplete(int errorCode, boolean fromCache, int operator, boolean append);
    }

    public interface DataLoadDetailListener {
        void loadComplete(int errorCode, boolean fromCache, int operator);
    }

    public static ImageLoader getImageLoader() {
        if (imageLoader == null) {
            LogUtils.v(TAG, "init(), getImageLoader, max heap = %d MB, remain heap = %d MB", DeviceUtils.getMaxHeapSizeInBytes() >> 20, DeviceUtils.getHeapRemainInBytes() >> 20);
            imageLoader = new ImageLoader(Global.sContext, 330, 440);
            ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(Global.sContext);
            cacheParams.memCacheSize = (int) Math.min(THUMB_MEM_CACHE_SIZE_IN_KB, (DeviceUtils.getHeapRemainInBytes() >> 10) * 0.15f);
            if (cacheParams.memCacheSize <= 0) {
                cacheParams.memCacheSize = THUMB_MEM_CACHE_SIZE_IN_KB;
            }
            LogUtils.v(TAG, "init(), getImageLoader, memCache size = %d KB", cacheParams.memCacheSize);
            cacheParams.diskCacheEnabled = false;
            cacheParams.memoryCacheEnabled = true;
            imageLoader.addImageCache(null, cacheParams);
        }
        return imageLoader;
    }

    public static ImageLoader getBigImageLoader() {
        if (imageLoaderBig == null) {
            int size = Math.min(DeviceUtils.getScreenWidth(Global.sContext), DeviceUtils.getScreenHeight(Global.sContext));
            imageLoaderBig = new ImageLoader(Global.sContext, size, size);
            ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(Global.sContext);
            cacheParams.memCacheSize = (int) Math.min(THUMB_MEM_CACHE_SIZE_IN_KB, (DeviceUtils.getHeapRemainInBytes() >> 10) * 0.2f);
            if (cacheParams.memCacheSize <= 0) {
                cacheParams.memCacheSize = THUMB_MEM_CACHE_SIZE_IN_KB;
            }
            LogUtils.v(TAG, "init(), getBigImageLoader, memCache size = %d KB", cacheParams.memCacheSize);
            cacheParams.diskCacheEnabled = false;
            cacheParams.memoryCacheEnabled = true;
            imageLoaderBig.addImageCache(null, cacheParams);
        }
        return imageLoaderBig;
    }

    public synchronized static ArrayList<BaseModel> getData(int type) {
        if (datas.get(type) == null) {
            return EMPTY;
        }
        return datas.get(type);
    }

    public synchronized static void addData(int key, ArrayList<BaseModel> model) {
        datas.put(key, model);
    }

    public synchronized static BaseModel findModel(int type, int id) {
        ArrayList<BaseModel> list = getData(type);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == id) {
                return list.get(i);
            }
        }
        return null;
    }

}
