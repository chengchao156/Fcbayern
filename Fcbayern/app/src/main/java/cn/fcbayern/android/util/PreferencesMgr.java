package cn.fcbayern.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * SharedPreferences管理类
 * */
public class PreferencesMgr {

	public static final String PREFS_PUSH_KEY = "pref_key_push";
	public static final String CACHE_KEY = "pref_key_cache";
	public static final String VERSION_KEY = "pref_key_version";
	public static final String CHANGE_PSW = "change_psw";

	private static SharedPreferences sPrefs;

	public static void init(Context context) {
		if (sPrefs == null) {
			sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		}
	}
	
	public static int getInt(String key,int defaultValue) {
		return sPrefs.getInt(key, defaultValue);
	}

	public static void setInt(String key,int value) {
		sPrefs.edit().putInt(key, value).apply();
	}

	public static boolean getBoolean(String key,boolean defaultValue) {
		return sPrefs.getBoolean(key, defaultValue);
	}

	public static void setBoolean(String key,boolean value) {
		sPrefs.edit().putBoolean(key, value).apply();
	}
	
	public static String getString(String key,String defaultValue) {
		return sPrefs.getString(key, defaultValue);
	}

	public static void setString(String key,String value) {
		sPrefs.edit().putString(key, value).apply();
	}
	
	public static void clearAll(){
		sPrefs.edit().clear().apply();
	}
}
