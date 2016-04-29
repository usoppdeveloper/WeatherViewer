package ru.buggy.weatherviewer.events.bus;

import org.json.JSONObject;

public class UpdateCurrentWeatherEvent extends AbstractEvent {
    JSONObject jo;

    public UpdateCurrentWeatherEvent(JSONObject jo) {
        this.jo = jo;
    }

    public JSONObject getData() {
        return jo;
    }
}
