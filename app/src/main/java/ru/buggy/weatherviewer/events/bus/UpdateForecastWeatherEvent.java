package ru.buggy.weatherviewer.events.bus;

import org.json.JSONObject;

public class UpdateForecastWeatherEvent extends AbstractEvent {
    JSONObject jo;

    public UpdateForecastWeatherEvent(JSONObject jo) {
        this.jo = jo;
    }

    public JSONObject getData() {
        return jo;
    }
}
