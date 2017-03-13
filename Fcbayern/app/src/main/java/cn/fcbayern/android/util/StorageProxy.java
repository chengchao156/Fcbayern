package cn.fcbayern.android.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.os.Build;
import android.os.storage.StorageManager;
import android.text.TextUtils;

/**
 * Created by chenzhan on 15-3-26.
 */
public class StorageProxy {

    private static final String TAG = StorageProxy.class.getSimpleName();

    private static final String VOLD_FSTAB_FILE = "/etc/vold.fstab";
    private static final String READ_COMMAND = "cat " + VOLD_FSTAB_FILE;
    private static final String DEV_MOUNT = "dev_mount";
    private static final String COLON = ":";

    /*
     * 用于3.0以下机器读取设备存储状态
     */
    private static ArrayList<String> readEtcFstabFile() {
        try {
            Process p = Runtime.getRuntime().exec(READ_COMMAND);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            ArrayList<String> list = new ArrayList<String>(5);
            while ((line = br.readLine()) != null) {
                LogUtils.d(TAG, "readEtcFstabFile. content = " + line);
                // Exclude lines start with # sign.
                if (line.startsWith("#") || !line.contains(DEV_MOUNT)) {
                    continue;
                }
                String[] words = line.split("\\s+");
                if (words != null && words.length >= 3) {
                    list.add(removePathColon(words[2]));
                }
            }
            return list;
        } catch (IOException e) {
            LogUtils.e(TAG, "Error occured, while readEtcFstabFile.", e);
            return null;
        }
    }

    /*
     * 3.0 以上机器有效
     */
    private static ArrayList<String> getExternalStoragePathEx(Context context) {
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        // 获取sdcard的路径：外置和内置
        Class smclass = sm.getClass();
        ArrayList<String> list = new ArrayList<String>(5);
        String[] paths = null;
        try {
            Method method = smclass.getMethod("getVolumePaths", new Class[0]);
            paths = (String[]) method.invoke(sm, new Object[0]);
            LogUtils.d(TAG, "paths.size = " + paths.length);
            for (String path : paths) {
                LogUtils.d(TAG, "path = " + path);
                list.add(removePathColon(path));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "error, ", e);
            return null;
        }
    }

    public static ArrayList<String> getAvailableStorage(Context context) {
        ArrayList<String> list;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            list = getExternalStoragePathEx(context);// >= 3.0
        else
            list = readEtcFstabFile();// < 3.0
        if (list == null || list.size() == 0) {
            return null;
        }
        // check if path is valid.
        Iterator<String> iter = list.iterator();
        while (iter.hasNext()) {
            String path = iter.next();
            File file = new File(path);
            if (file == null || !file.exists()) {
                iter.remove();
            }
        }
        return list;
    }

    // Remove path colon. For example. /mnt/sdcard-ext:none:lun1
    private static String removePathColon(String path) {
        if (TextUtils.isEmpty(path)) {
            return path;
        }
        int index = path.indexOf(COLON);
        if (index != -1) {
            return path.substring(0, index);
        }
        return path;
    }
}
