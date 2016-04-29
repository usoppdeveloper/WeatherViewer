package ru.buggy.weatherviewer.events.bus;

import ru.buggy.weatherviewer.data.ForecastWeatherData;

public class UpdateWeatherShowerEvent extends AbstractEvent {
    private ForecastWeatherData mForecastData;

    public UpdateWeatherShowerEvent(ForecastWeatherData forecastData) {
        mForecastData = forecastData;
    }

    public ForecastWeatherData getForecastData() {
        return mForecastData;
    }
}
