package cn.fcbayern.android.util.image;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Image loader for loading image asynchronized
 * <br/>
 *
 * 混合模式的Image Loader类，支持同时load本地或者在线的Image
 * <br/>
 * 1. asset资源：以assets://开头
 * 2. 存储卡资源：以/开头，如/sdcard/xxx.jpg
 * 3. 在线资源：以http://或者https://开头
 */
public class ImageLoader extends ImageFetcher {
    private static final String TAG = ImageLoader.class.getSimpleName();

    public ImageLoader(Context context, int imageWidth, int imageHeight) {
        super(context, imageWidth, imageHeight);
    }

    public ImageLoader(Context context, int imageSize) {
        super(context, imageSize);
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        if (data instanceof Integer) { // decode resource
            int resId = (Integer)data;
            return BitmapUtils.decodeSampledBitmapFromResource(mResources, resId, mImageWidth, mImageHeight, getImageCache());
        } else if (data instanceof String) { // decode file: assets or sdcard; decode url: TODO
        	//return loadFile((String)data);
            return super.processBitmap(data);
        }
        return null;
    }
    
//    private Bitmap loadFile(String data) {
//        String file = data;
//        if (file.startsWith(Utils.RES_PREFIX_ASSETS)) { // default
//            LogUtils.v(TAG, "processBitmap(), data = %s is assets resource", file);
//            file = file.substring(Utils.RES_PREFIX_ASSETS.length());
//            LogUtils.v(TAG, "processBitmap(), old file = %s", file);
//            file = FileUtils.checkAssetsPhoto(mContext, file);
//            LogUtils.v(TAG, "processBitmap(), new file = %s", file);
//            return BitmapUtils.decodeSampleBitmapFromAssets(mContext, file, mImageWidth, mImageHeight, getImageCache());
//        } else if (file.startsWith(Utils.RES_PREFIX_STORAGE)) { // sdcard files
//            LogUtils.v(TAG, "processBitmap(), data = %s is storage resource", data);
//            file = FileUtils.checkPhoto(file);
//            return BitmapUtils.decodeSampledBitmapFromFile(file, mImageWidth, mImageHeight, getImageCache());
//        } else if (file.startsWith(Utils.RES_PREFIX_HTTP) || file.startsWith(Utils.RES_PREFIX_HTTPS)) {
//            LogUtils.v(TAG, "processBitmap(), data = %s is online resource", data);
//            return super.processBitmap(data);
//        }
//        return null;
//    }


}
