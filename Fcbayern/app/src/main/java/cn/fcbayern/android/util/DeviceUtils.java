package cn.fcbayern.android.util;


import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 与设备参数读取相关的工具类
 */
public class DeviceUtils {
    private static final String TAG = DeviceUtils.class.getSimpleName();

    public static final int MOBILE_NETWORK_2G = 1;
    public static final int MOBILE_NETWORK_3G = 2;
    public static final int MOBILE_NETWORK_4G = 3;
    public static final int MOBILE_NETWORK_UNKNOWN = 4;
    public static final int MOBILE_NETWORK_DISCONNECT = 5;

    /** 网络环境 */
    public static final int NET_NONE = 0; // 无网
    public static final int NET_WIFI = 1; // WIFI
    public static final int NET_2G = 2; // 2G
    public static final int NET_3G = 3; // 3G
    public static final int NET_4G = 4; // 3G
    public static final int NET_OTHER = 5; // 其他

    private static int sTotalMemory = 0;
    private static long sTotalInternalMemory = 0;
    private static long sMaxCpuFreq = 0;
    private static int sCpuCount = 0;

    // 适配低端手机的相关参数
    private static final long SMALL_DISPLAYPIXELS = 480 * 320;
    private static final long NORMAL_MIN_CPU = 800000;
    private static final long NORMAL_MIN_MEMORY = 512;
    private static final long NORMAL_MIN_INTERNAL_MEMORY = 512;

    public static final int MIN_STORAGE_SIZE = 50 * 1024 * 1024; // 50MB

//    public static String ROOT = "";
    public static String ROOT;
    public static String DIRECTORY;
    static {
//        generateDirectory();
        ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
        DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Bayern";
    	/*try {
			ROOT = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
			DIRECTORY = ROOT + "/Camera";
    	} catch (Exception e) {
			ROOT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ROOT";
			DIRECTORY = ROOT + "/Camera";
		} catch (java.lang.NoSuchFieldError error) {
			ROOT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ROOT";
			DIRECTORY = ROOT + "/Camera";
		}*/
    }
    public static String generateAppDataDir(String root) {
        return root + File.separator + "Bayern";
    }

    public static void generateDirectory() {
        //DIRECTORY = PrefsUtils.getDefaultPrefs().getString(PrefsUtils.PREFS_KEY_STORAGE, "");

        if (TextUtils.isEmpty(DIRECTORY)) {
            ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
            DIRECTORY = generateAppDataDir(ROOT);
        } else {
            int lastSlash = DIRECTORY.lastIndexOf("/");
            if (lastSlash > 0) {
                ROOT = DIRECTORY.substring(0, lastSlash);
            }
        }

        File file = new File(DIRECTORY);
        try {
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkSdcard() {
        generateDirectory();

        //1. Use the system directory first
        try {
            long size = getAvailableSize(new StatFs(DIRECTORY));
            boolean isWritable = false;
            if (size > 0) {
                isWritable = isDirectoryWritable(DIRECTORY);
            }
            LogUtils.v(TAG, "checkSdcard(), DIRECTORY = %s, size = %d, writable = %b", DIRECTORY, size, isWritable);
            if (isWritable && size >= MIN_STORAGE_SIZE) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //2. Then use the manual detected mounts
        List<String> mounts = StorageProxy.getAvailableStorage(Global.sContext);
        long maxSize = 0;
        String maxMount = null;

        try {
            if (mounts != null && mounts.size() > 0) {
                for (String mount : mounts) {
                    long size = getAvailableSize(new StatFs(mount));
                    boolean isWritable = false;
                    if (size > 0) {
                        isWritable = isDirectoryWritable(mount);
                    }
                    LogUtils.v(TAG, "checkSdcard(), mount = %s, size = %d, writable = %b", mount, size, isWritable);
                    if (isWritable && size > maxSize) {
                        maxSize = size;
                        maxMount = mount;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtils.v(TAG, "checkSdcard(), maxMount = %s, maxSize = %d", maxMount, maxSize);

        if (maxSize >= MIN_STORAGE_SIZE && !TextUtils.isEmpty(maxMount)) {
            ROOT = maxMount;
            DIRECTORY = generateAppDataDir(maxMount);
            return true;
        }
        return false;
    }

    public static boolean checkJniLibsFolder(Context context) {
        File file = context.getFilesDir();
        if (file != null) {
            String path = file.getAbsolutePath().replace("files", "lib");
            file = new File(path);
            if (file != null && file.exists() && file.isDirectory()) {
                String[] libs = file.list();
                if (libs != null && libs.length > 0) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * 获取IMEI号
     * @param context
     * @return
     */
    public static String getImei(Context context) {
        try {
            TelephonyManager telephonyManager=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telephonyManager.getDeviceId();
            if (null != imei) return imei;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "0";
    }

    /**
     * 获取app vesionName
     * @param context
     * @return
     */
    public static String getVersionName(Context context){
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch(Exception e){

        }
        return "";
    }
    
    public static int getVersionInt(Context context){
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String versionName = packageInfo.versionName;
            int index = versionName.lastIndexOf(".");
            versionName = versionName.substring(0, index);
            versionName = versionName.replace(".", "");
            return Integer.valueOf(versionName);
        } catch(Exception e){

        }
        return -1;
    }

    public static int getVersionCode(Context context){
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch(Exception e){

        }
        return 0;
    }

    /**
     * 获取系统号 add by zhenhaiwu
     * @return
     */
    public static String getOSVersion(){
    	return  Build.VERSION.RELEASE;
    }
    
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {

        } else {
            final NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                return true;
            }
        //获取所有网络连接信息
//            NetworkInfo[] info = connectivity.getAllNetworkInfo();
//            if (info != null) {//逐一查找状态为已连接的网络
//                for (int i = 0; i < info.length; i++) {
//                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
//                        return true;
//                    }
//                }
//            }
        }
        return false;
    }
    
    public static int getScreenWidth(Context context) {
    	DisplayMetrics display = context.getResources().getDisplayMetrics();
    	return display.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics display = context.getResources().getDisplayMetrics();
        return display.heightPixels;
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * @return The number of cores, or 1 if failed to get result
     */
    public static int getNumCores() {
        if (sCpuCount > 0) {
            return sCpuCount;
        }
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if(Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            if (files != null) {
                sCpuCount = files.length;
            } else {
                sCpuCount = 1;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            //Default to return 1 core
            sCpuCount = 1;
        }
        LogUtils.d("DeviceUtils", "sCpuCount:" + sCpuCount);
        return sCpuCount;
    }

    //手机CPU主频大小
    public static long getMaxCpuFreq() {
        if (sMaxCpuFreq > 0) {
            return sMaxCpuFreq;
        }
        ProcessBuilder cmd;
        String cpuFreq = "";
        try {
            String[] args = { "/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                cpuFreq = cpuFreq + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            cpuFreq = "";
        }
        cpuFreq = cpuFreq.trim();
        if (cpuFreq == null || cpuFreq.length() == 0) {
            // 某些机器取到的是空字符串，如：OPPO U701
            sMaxCpuFreq = 1;
        } else {
            try {
                sMaxCpuFreq = Long.parseLong(cpuFreq);
            } catch (NumberFormatException e) {
                sMaxCpuFreq = 1;
                e.printStackTrace();
            }
        }
        LogUtils.d("DeviceUtils", "sMaxCpuFreq:" + sMaxCpuFreq);
        return sMaxCpuFreq;
    }

    /**
     * 获取本地ip地址 add by zhenhaiwu
     * @return
     */
    public static String getLocalIpAddress() {
        try {
            if (NetworkInterface.getNetworkInterfaces() != null) {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    if (intf != null) {
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress()
                                    && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
                                if (!inetAddress.getHostAddress().equals("null")
                                        && inetAddress.getHostAddress() != null) {
                                    return inetAddress.getHostAddress().trim();
                                }
                            }
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * 检查是否开通GPS定位或网络定位
     */
    public static boolean isLocationEnabled(Context context) {
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 返回是否为2/3G的移动网络
     *
     * @return
     */
    public static boolean isMobileNetwork(Context context) {
        if (context == null) return false;
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }

        NetworkInfo mobile = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobile != null && mobile.isAvailable() && mobile.isConnected()) {
            return true;
        }
        return false;
    }

    public static int checkMobileNetwork(Context context) {
        if (context == null) {
        	return MOBILE_NETWORK_UNKNOWN;
        }
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return MOBILE_NETWORK_UNKNOWN;
        }

        NetworkInfo mobile = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobile != null && mobile.isAvailable() && mobile.isConnected()) {
            int subType = mobile.getSubtype();
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return MOBILE_NETWORK_2G;
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return MOBILE_NETWORK_3G;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return MOBILE_NETWORK_4G;
                default:
                    return MOBILE_NETWORK_UNKNOWN;
            }
        } else {
            return MOBILE_NETWORK_DISCONNECT;
        }
    }

    public static boolean isWifiNetwork(Context context) {
        if (context == null) return false;
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }
        NetworkInfo wifi = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi != null && wifi.isAvailable() && wifi.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * 返回网络类型
     *
     * @return
     */
    public static int getNetworkState() {
        int net = NET_OTHER;
        try {
            if (!DeviceUtils.isNetworkAvailable(Global.sContext)) {
                // 当前未联网
                net = NET_NONE;
            } else if (DeviceUtils.isWifiNetwork(Global.sContext)) {
                // 当前为WIFI
                net = NET_WIFI;
            } else {
                switch (DeviceUtils.checkMobileNetwork(Global.sContext)) {
                    case DeviceUtils.MOBILE_NETWORK_2G:
                        net = NET_2G;
                        break;
                    case DeviceUtils.MOBILE_NETWORK_3G:
                        net = NET_3G;
                        break;
                    case DeviceUtils.MOBILE_NETWORK_4G:
                        net = NET_4G;
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return net;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getTotalSize(StatFs statFs) {
        long availableBytes;
        if (Utils.hasJellyBeanMR2()) {
            availableBytes = statFs.getTotalBytes();
        } else {
            availableBytes = (long) statFs.getBlockCount() * (long) statFs.getBlockSize();
        }
        return availableBytes;
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getAvailableSize(StatFs statFs) {
        long availableBytes;
        if (Utils.hasJellyBeanMR2()) {
            availableBytes = statFs.getAvailableBytes();
        } else {
            availableBytes = (long) statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
        }
        return availableBytes;
    }
    
    /**
     * The external storage status is OK.
     *
     * @return
     */
    public static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable();
    }

    /**
     * The external storage empty size is enough
     *
     * @param fileSize
     * @return
     */
    public static boolean isExternalStorageSpaceEnough(long fileSize) {
        File sdcard = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(sdcard.getAbsolutePath());
        return !(getAvailableSize(statFs) <= fileSize);
    }

    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     *         otherwise.
     */
    public static boolean isExternalStorageRemovable() {
        return Environment.isExternalStorageRemovable();
    }
    
    public static long getTotalInternalMemory() {
        if (sTotalInternalMemory > 0) {
            return sTotalInternalMemory;
        }
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());
        sTotalInternalMemory = getTotalSize(statFs);
        LogUtils.d("DeviceUtils", "sTotalInternalMemory:" + sTotalInternalMemory);
        return sTotalInternalMemory;
    }
    
    public static long getTotalInternalMemoryInMb() {
        return getTotalInternalMemory() >> 20;
    }

    /**
     * Get the external app files directory.
     *
     * @param context The context to use
     * @return The external files dir
     */
    // 使用下面的getExternalFilesDir()
    private static File getExternalFilesDir(Context context) {
        File file = context.getExternalFilesDir(null);

        // Before Froyo we need to construct the external cache dir ourselves
        if (file == null) {
            final String filesDir = "/Android/data/" + context.getPackageName() + "/files/";
            file = new File(Environment.getExternalStorageDirectory().getPath() + filesDir);
        }
        return file;
    }

    public static File getExternalFilesDir(Context context, String folder) {
        String path = null;
        if (DeviceUtils.isExternalStorageAvailable() && DeviceUtils.isExternalStorageSpaceEnough(DeviceUtils.MIN_STORAGE_SIZE)) {
            path = getExternalFilesDir(context).getPath();
            if (!DeviceUtils.isDirectoryWritable(path)) {
                path = null;
            }
        }

        if (TextUtils.isEmpty(path)) {
            path = !TextUtils.isEmpty(DeviceUtils.ROOT) ? (DeviceUtils.ROOT + File.separator + getFileBaseDir(context)) :
                    context.getFilesDir().getPath();
        }

        File file = new File(path + File.separator + folder);
        try {
            if (file.exists() && file.isFile()) {
                file.delete();
            }

            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }

    private static String getFileBaseDir(Context context) {
        return "Android/data/" + context.getPackageName() + "/files";
    }

    public static String generateFilePath(String title) {
        return DIRECTORY + '/' + title + ".jpg";
    }
    
    public static String generateFilePath(String dir, String title) {
        if (TextUtils.isEmpty(dir)) {
            return DIRECTORY + '/' + title + ".jpg";
        } else {
            return dir + '/' + title + ".jpg";
        }
    }

    public static int getTotalMemoryInMb() {
        return getTotalMemoryInKb() >> 10;
    }

    public static int getTotalMemoryInKb() {
        if (sTotalMemory > 0) {
            return sTotalMemory;
        }
        String str1 = "/proc/meminfo";
        String str2 = "";
        String[] arrayOfString;
        FileReader fr = null;
        BufferedReader localBufferedReader = null;
        try {
            fr = new FileReader(str1);
            localBufferedReader = new BufferedReader(fr, 8192);
            if ((str2 = localBufferedReader.readLine()) != null) {

                arrayOfString = str2.split("\\s+");
                sTotalMemory = Integer.valueOf(arrayOfString[1]).intValue();
                LogUtils.d("DeviceUtils", "sTotalMemory:" + sTotalMemory);
            }
        } catch (IOException e) {

        } finally {
            try {
                if(fr != null) {
                    fr.close();
                }
                if(localBufferedReader != null) {
                    localBufferedReader.close();
                }
            } catch (IOException e) {
            }
        }
        return sTotalMemory;
    }

    public static int getFreeMemoryInKb() {
        String str1 = "/proc/meminfo";
        String str2 = "";
        String[] arrayOfString;
        FileReader fr = null;
        BufferedReader localBufferedReader = null;
        int freeMem = 0;
        try {
            fr = new FileReader(str1);
            localBufferedReader = new BufferedReader(fr, 8192);
            int line = 0;
            while ((str2 = localBufferedReader.readLine()) != null) {
                if (++line == 2) {
                    LogUtils.v(TAG, "getFreeMemory(), %s", str2);
                    arrayOfString = str2.split("\\s+");
                    freeMem = Integer.valueOf(arrayOfString[1]).intValue();
                    LogUtils.v(TAG, "getFreeMemory(), free mem in kb = %d", freeMem);
                    freeMem = freeMem;
                    LogUtils.v(TAG, "getFreeMemory(), free mem in mb = %d", freeMem);
                    break;
                }
            }
        } catch (IOException e) {

        } finally {
            try {
                if(fr != null) {
                    fr.close();
                }
                if(localBufferedReader != null) {
                    localBufferedReader.close();
                }
            } catch (IOException e) {
            }
        }
        return freeMem;
    }

    /**
     * 获取当前手机系统的最大的可用堆栈大小
     *
     * @return
     */
    public static long getMaxHeapSizeInBytes() {
        long max = Runtime.getRuntime().maxMemory();
        try {
            ActivityManager am = (ActivityManager)Global.sContext.getSystemService(Context.ACTIVITY_SERVICE);
            LogUtils.v(TAG, "getMaxHeapSizeInBytes(), memoryClass = %d", am.getMemoryClass());
            long memoryClass = am.getMemoryClass() << 20;
            LogUtils.v(TAG, "getMaxHeapSizeInBytes(), memoryClass = %d", memoryClass);
            if (max > memoryClass) {
                max = memoryClass;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return max;
    }

    /**
     * 获取当前已经分配的堆栈的比例
     *
     * @return
     */
    public static float getHeapAllocatePercent() {
        long heapAllocated = Runtime.getRuntime().totalMemory();
        long heapMax = Runtime.getRuntime().maxMemory();
        return Math.round(heapAllocated * 10000.0f / heapMax) / 100.0f;
    }

    /**
     * 获取当前已经使用的堆栈占总堆栈的比例
     *
     * @return
     */
    public static float getHeapUsedPercent() {
        long heapUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long heapMax = getMaxHeapSizeInBytes();
        return Math.round(heapUsed * 10000.0f / heapMax) / 100.0f;
    }

    /**
     * 获取整个max heap里面除去已经使用的heap还可以再分配的heap大小
     *
     * @return
     */
    public static long getHeapRemainInBytes() {
        return getMaxHeapSizeInBytes() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    }

    /**
     * 获取进程list
     * @return
     */
    private static List<ActivityManager.RunningAppProcessInfo> getProcesses() {
        // Returns a list of application processes that are running on the
        // device
        ActivityManager activityManager = (ActivityManager) Global.sContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();

        return appProcesses == null ? null : appProcesses;
    }

    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public static boolean isAppOnForeground() {
        List<ActivityManager.RunningAppProcessInfo> appProcesses = getProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = Global.sContext.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
//            LogUtils.v(TAG, "process name = %s, importance = %d", appProcess.processName, appProcess.importance);
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String pkg : appProcess.pkgList) {
//                    LogUtils.v(TAG, "process name = %s, pkg = %s", appProcess.processName, pkg);
                    if (pkg.equals(packageName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 当前执行是否在主进程中
     * @return
     */
    public static boolean isDuringMainProcess() {
        List<ActivityManager.RunningAppProcessInfo> appProcesses = getProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = Global.sContext.getPackageName();
        final int pid = android.os.Process.myPid();

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.pid == pid && appProcess.processName.equals(packageName)) {
                //LogUtils.v(TAG, "appProcess.processName = %s", appProcess.processName);
                return true;
            }
        }
        return false;
    }

    public static boolean isDirectoryWritable(String directory) {
        File file = new File(directory);
        if (file.exists() && !file.isDirectory()) { // file is file, not folder
            return false;
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        if (file.isDirectory()) {
            try {
                File temp = new File(file.getAbsolutePath() + File.separator + "test_temp.txt");
                if (temp.exists()) {
                    temp.delete();
                }
                temp.createNewFile();
                temp.delete();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    /**
     * 获取语言信息
     * 
     * @return
     */
    public static String getLanguage() {
        try {
            Locale locale = Locale.getDefault();
            if (locale == null) {
                return "";
            }
            return locale.getLanguage() + "_" + locale.getCountry();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
