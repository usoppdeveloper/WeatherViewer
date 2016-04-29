package ru.buggy.weatherviewer.events.bus;

import ru.buggy.weatherviewer.data.ForecastWeatherData;

public class AddNewCityEvent extends AbstractEvent {
    private ForecastWeatherData mCityForecastWeatherData;

    public AddNewCityEvent(ForecastWeatherData cityForecastWeatherData) {
        mCityForecastWeatherData = cityForecastWeatherData;
    }

    public ForecastWeatherData getCityForecastWeatherData() {
        return mCityForecastWeatherData;
    }
}
