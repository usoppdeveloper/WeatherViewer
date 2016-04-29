package ru.buggy.weatherviewer;

public class Log {
    private final static String LOG_TAG = "weather_viewer_tag";

    public static void d(String s) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(LOG_TAG, s);
        }
    }

    public static void e(String s) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(LOG_TAG, s);
        }
    }
}
