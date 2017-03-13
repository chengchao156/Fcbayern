package cn.fcbayern.android.util;

import android.content.res.Configuration;
import android.util.Log;

public class LogUtils {

    private static final int LOG_MODE_NONE = 0x00;
    private static final int LOG_MODE_LOGCAT = 0x01;
    private static final int LOG_MODE_FILE = 0x02;

    private static final int DEFAULT_MODE = LOG_MODE_LOGCAT | LOG_MODE_FILE;

    private static final Configuration sConfiguration = new Configuration();

    public static boolean isLogcatEnable() {
        return (DEFAULT_MODE & LOG_MODE_LOGCAT) == LOG_MODE_LOGCAT;
    }

    public static boolean isLogToFileEnable() {
        return (DEFAULT_MODE & LOG_MODE_FILE) == LOG_MODE_FILE;
    }

    public static void v(String tag, String msg, Object...args) {
        if (AppConfig.DEBUG) {
            msg = getFormatMessage(tag, msg, args);
            if (isLogcatEnable()) Log.v(tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr, Object...args) {
        if (AppConfig.DEBUG) {
            msg = getFormatMessage(tag, msg, args);
            if (isLogcatEnable()) Log.v(tag, msg, tr);
        }
    }

    public static void d(String tag, String msg, Object...args) {
        if (AppConfig.DEBUG) {
            msg = getFormatMessage(tag, msg, args);
            if (isLogcatEnable()) Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr, Object...args) {
        if (AppConfig.DEBUG) {
            msg = getFormatMessage(tag, msg, args);
            if (isLogcatEnable()) Log.d(tag, msg, tr);
        }
    }

    public static void i(String tag, String msg, Object...args) {
        if (AppConfig.DEBUG) {
            msg = getFormatMessage(tag, msg, args);
            if (isLogcatEnable()) Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr, Object...args) {
        if (AppConfig.DEBUG) {
            msg = getFormatMessage(tag, msg, args);
            if (isLogcatEnable()) Log.i(tag, msg, tr);
        }
    }

    public static void w(String tag, String msg, Object...args) {
        if (AppConfig.DEBUG) {
            msg = getFormatMessage(tag, msg, args);
            if (isLogcatEnable()) Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr, Object...args) {
        if (AppConfig.DEBUG) {
            msg = getFormatMessage(tag, msg, args);
            if (isLogcatEnable()) Log.w(tag, msg, tr);
        }
    }

    public static void e(String tag, String msg, Object...args) {
        if (AppConfig.DEBUG) {
            msg = getFormatMessage(tag, msg, args);
            if (isLogcatEnable()) Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr, Object...args) {
        if (AppConfig.DEBUG) {
            msg = getFormatMessage(tag, msg, args);
            if (isLogcatEnable()) Log.e(tag, msg, tr);
        }
    }

    private static String getFormatMessage(String tag, String msg, Object...args) {
        return /*"[thread - " + Thread.currentThread().getName() + "] "*/ genenateLogPrefix(tag) + (args == null ? msg : String.format(sConfiguration.locale, msg, args));
    }

    /**
     * 生成Log日志的前缀信息。如下格式：
     * 当前线程名+文件名+行号+方法名
     *
     * @param simpleClassName
     * @return
     */
    private static String genenateLogPrefix(String simpleClassName) {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return "";
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().endsWith(simpleClassName)) {
                return "[" + Thread.currentThread().getName() + "][" + st.getFileName() + ":" + st.getLineNumber() + "] " + "[" + st.getMethodName() + "] ";
            }
        }
        return "[" + Thread.currentThread().getName() + "]";
    }

}
