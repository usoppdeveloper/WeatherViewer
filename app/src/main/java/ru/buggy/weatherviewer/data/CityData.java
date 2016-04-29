package ru.buggy.weatherviewer.data;

public class CityData {
    private String mCityName;
    private int mCityId;

    public CityData(String cityName, int cityId) {
        mCityId = cityId;
        mCityName = cityName;
    }

    public String getCityName() {
        return mCityName;
    }

    public int getCityId() {
        return mCityId;
    }
}
