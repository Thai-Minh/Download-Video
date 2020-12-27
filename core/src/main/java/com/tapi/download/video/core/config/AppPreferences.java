package com.tapi.download.video.core.config;

import android.content.Context;
import android.content.SharedPreferences;

public enum AppPreferences {
    INSTANCE;

    public final static String NAME = "download_social_video";

    private SharedPreferences preferences;


    public void load(Context context) {
        preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public void setListenerChange(SharedPreferences.OnSharedPreferenceChangeListener listener){
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public String getString(String key, String defValue) {
        return preferences.getString(key, defValue);
    }

    public void putString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    public int getInt(String key, int defValue) {
        return preferences.getInt(key, defValue);
    }

    public void putInt(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    public long getLong(String key, long defValue) {
        return preferences.getLong(key, defValue);
    }

    public void putLong(String key, long value) {
        preferences.edit().putLong(key, value).apply();
    }

    public float getFloat(String key, float defValue) {
        return preferences.getFloat(key, defValue);
    }

    public void putFloat(String key, float value) {
        preferences.edit().putFloat(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    public void putBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }
}
