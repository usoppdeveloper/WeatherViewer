package ru.buggy.weatherviewer.net;

import org.json.JSONObject;

import ru.buggy.weatherviewer.Log;
import ru.buggy.weatherviewer.data.Storage;
import ru.buggy.weatherviewer.events.bus.UpdateCurrentWeatherEvent;

public class CurrentWeatherReceiverThread extends Thread {
    public void run() {
        JSONObject jo = DownloadWeatherHelper.getCurrentWeather(Storage.getStorage().getCitiesIds());

        if (jo != null) {
            new UpdateCurrentWeatherEvent(jo).post();
        } else {
            Log.e("null!");
        }
    }
}
