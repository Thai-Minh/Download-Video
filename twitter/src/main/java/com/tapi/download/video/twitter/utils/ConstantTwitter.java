package com.tapi.download.video.twitter.utils;

public class ConstantTwitter {
    //Create an developper account on twitter and get your api key
    public static final String TWITTER_KEY = "YbsTtOlXJZOY6G8yzXLR7fObk";
    public static final String TWITTER_SECRET = "aXwPL8r00rok639o2ayFNZDLE2Ujh9d6spDH3ImUV7ZTyERojn";

    //Permissions
    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static final String[] PERMISSION_STRORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static String[] USER_AGENT = new String[]{
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36",
    };

    public static final int REQUEST_CODE = 20;
    public static final int NOTI_IDENTIFIER = 3100;
    public static final int AUTO_REQUEST_CODE = 30;

    public static final String VIDEO_LOADED = "video_loaded";
    public static final String VIDEO_HANDLE = "video_handle";

    public static final String VIDEO = "twitter_video";
}
