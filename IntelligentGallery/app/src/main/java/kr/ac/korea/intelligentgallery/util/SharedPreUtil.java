package kr.ac.korea.intelligentgallery.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import kr.ac.korea.IntelligentGallery;


public class SharedPreUtil implements OnSharedPreferenceChangeListener {

	public static SharedPreferences sharedPrefs = null;
	public static SharedPreferences.Editor sharedPrefsEditor = null;

	public static SharedPreUtil instance = null;

	public final static String GALLERY_SHARED_PRE = "Gallery_shared_pref";

	// 컨텐츠(앨범과 각 항목 들)에 대한 디비를 생성하는 작업(분류기 작업)이 최초인지 결정하는 것
	public final static String IS_NOT_FIRST_TIME_TO_START_APP = "Is_not_first_time_to_start_app";

	//정렬하기 순서 기억하는 sharedPreference
	public final static String ALBUM_ORDER_BY = "ALBUM_ORDER_BY";
	public final static String FOLDER_CATEGORY_ORDER_BY = "FOLDER_CATEGORY_ORDER_BY";

	public static SharedPreUtil getInstance() {
		if (instance == null) {
			instance = new SharedPreUtil();
		}
		return instance;
	}

	public SharedPreferences getSharedPrefs() {
		return sharedPrefs;
	}

	private SharedPreUtil() {
		sharedPrefs = IntelligentGallery.getContext().getSharedPreferences(GALLERY_SHARED_PRE, Context.MODE_PRIVATE);
		sharedPrefsEditor = sharedPrefs.edit();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		
	}

	public void putPreference(String key, boolean value) {
		sharedPrefsEditor.putBoolean(key, value);
		sharedPrefsEditor.commit();
	}
	
	public void putPreference(String key, String value) {
		sharedPrefsEditor.putString(key, value);
		sharedPrefsEditor.commit();
	}
	
	public void putPreference(String key, int value) {
		sharedPrefsEditor.putInt(key, value);
		sharedPrefsEditor.commit();
	}
	
	public boolean getBooleanPreference(String key){
		return sharedPrefs.getBoolean(key, false);
	}
	
	public String getStringPreference(String key){
		return sharedPrefs.getString(key, null);
	}
	
	public int getIntPreference(String key){
		return sharedPrefs.getInt(key, 0);
	}
	
	public void removePreference(String key){
		sharedPrefsEditor.remove(key);
		sharedPrefsEditor.commit();
	}
}
