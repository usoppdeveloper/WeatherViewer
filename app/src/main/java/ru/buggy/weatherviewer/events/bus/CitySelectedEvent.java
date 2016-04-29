package ru.buggy.weatherviewer.events.bus;


import ru.buggy.weatherviewer.data.CityData;

public class CitySelectedEvent extends AbstractEvent {
    private CityData mCityData;

    public CitySelectedEvent(CityData cityData) {
        mCityData = cityData;
    }

    public CityData getCityData() {
        return mCityData;
    }
}
