package cn.fcbayern.android.util.cache;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cn.fcbayern.android.util.DeviceUtils;
import cn.fcbayern.android.util.LogUtils;
import cn.fcbayern.android.util.Utils;

/**
 * 缓存处理相关的工具类
 */
public final class CacheUtils {

    private static final String TAG = CacheUtils.class.getSimpleName();

    /**
     * Get a usable cache directory (external if available, internal otherwise).
     *
     * @param context The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        String path = null;
        if (DeviceUtils.isExternalStorageAvailable() && DeviceUtils.isExternalStorageSpaceEnough(DeviceUtils.MIN_STORAGE_SIZE)) {
            path = getExternalCacheDir(context).getPath();
            if (!DeviceUtils.isDirectoryWritable(path)) {
                path = null;
            }
        }

        if (TextUtils.isEmpty(path)) {
            path = context.getCacheDir().getPath();
        }

        LogUtils.v(TAG, "getDiskCacheDir(), cachePath = %s", path);
        return new File(path + File.separator + uniqueName);
    }

    private static String getCacheBaseDir(Context context) {
        return "Android/data/" + context.getPackageName() + "/cache";
    }

    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    private static File getExternalCacheDir(Context context) {
        File file = context.getExternalCacheDir();

        // Before Froyo we need to construct the external cache dir ourselves
        if (file == null) {
            file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + getCacheBaseDir(context));
        }
        return file;
    }

    /**
     * Check how much usable space is available at a given path.
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    @TargetApi(9)
    public static long getUsableSpace(File path) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
//            return path.getUsableSpace();
//        }
        try {
            StatFs stats = new StatFs(path.getPath());
//        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
            return DeviceUtils.getAvailableSize(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
