package ru.buggy.weatherviewer;

import android.app.Application;

import ru.buggy.weatherviewer.data.Storage;
import ru.buggy.weatherviewer.events.bus.bus.EventBus;

public class WeatherViewerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Event bus provider init
        EventBus.init();

        // Storage init
        new Storage(getApplicationContext());
        Storage.getStorage().startForecastDownloadDaemon();
    }
}
