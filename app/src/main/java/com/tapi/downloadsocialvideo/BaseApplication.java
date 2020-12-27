package com.tapi.downloadsocialvideo;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.tapi.download.video.core.config.AppPreferences;


public class BaseApplication extends Application {
    private static BaseApplication sIntance;

    public static Context getAppContext() {
        return sIntance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sIntance = this;
        AppPreferences.INSTANCE.load(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
