package ru.buggy.weatherviewer.net;

import org.json.JSONObject;

import ru.buggy.weatherviewer.Log;
import ru.buggy.weatherviewer.data.Storage;
import ru.buggy.weatherviewer.events.bus.UpdateForecastWeatherEvent;

public class ForecastWeatherReceiverThread extends Thread {
    public void run() {
        Integer[] cityIds = Storage.getStorage().getCitiesIds();

        for (int i=0; i < cityIds.length; i++) {
            JSONObject jo = DownloadWeatherHelper.getForecastWeather(cityIds[i]);

            if (jo != null) {
                new UpdateForecastWeatherEvent(jo).post();
            } else {
                Log.e("null!");
            }
        }
    }
}
