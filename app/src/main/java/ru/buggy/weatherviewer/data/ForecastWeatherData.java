package ru.buggy.weatherviewer.data;

import java.util.ArrayList;

public class ForecastWeatherData {
    public static int NO_DATA_RECEIVED_VALUE = 1_000_000;
    public static int FORECAST_DAYS_NUMBER = 7;

    private long mUpdateTime;
    private CityData mCityData;
    private int mTemperature;
    private  int mWindVelocity;
    private ArrayList<ForecastWeatherData> mWeekForecastData;

    public ForecastWeatherData(CityData cityData, int temperature, int windVelocity, long updateTime) {
        mUpdateTime = updateTime;
        mCityData = cityData;
        mTemperature = temperature;
        mWindVelocity = windVelocity;
    }

    public long getUpdateTime() {
        return mUpdateTime;
    }

    public CityData getCityData() {
        return mCityData;
    }

    public String getCityDescription() {
        return mCityData.getCityName();
    }

    public int getTemperature() {
        return mTemperature;
    }

    public int getWindVelocity() {
        return mWindVelocity;
    }

    public ArrayList<ForecastWeatherData> getWeekForecastData() {
        return mWeekForecastData;
    }

    public void setCityData(CityData cityData) {
        mCityData = cityData;
    }

    public void setTemperature(int temperature) {
        mTemperature = temperature;
    }

    public void setWindVelocity(int windVelocity) {
        mWindVelocity = windVelocity;
    }

    public void setUpdateTime(long updateTime) {
        this.mUpdateTime = updateTime;
    }

    public void updateChild(int index, int temperature, int windVelocity, long updateTime) {
        if (mWeekForecastData == null) {
            mWeekForecastData = new ArrayList<>();
            while (mWeekForecastData.size() < FORECAST_DAYS_NUMBER) {
                mWeekForecastData.add(new ForecastWeatherData(null, NO_DATA_RECEIVED_VALUE, NO_DATA_RECEIVED_VALUE, 0));
            }
        }

        mWeekForecastData.get(index).setUpdateTime(updateTime);
        mWeekForecastData.get(index).setTemperature(temperature);
        mWeekForecastData.get(index).setWindVelocity(windVelocity);
    }
}
