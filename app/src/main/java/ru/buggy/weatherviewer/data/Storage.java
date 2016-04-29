package ru.buggy.weatherviewer.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TreeSet;

import ru.buggy.weatherviewer.events.bus.AddNewCityEvent;
import ru.buggy.weatherviewer.events.bus.UpdateCurrentWeatherEvent;
import ru.buggy.weatherviewer.events.bus.UpdateForecastWeatherEvent;
import ru.buggy.weatherviewer.net.ForecastDownloadDaemon;
import ru.buggy.weatherviewer.Log;
import ru.buggy.weatherviewer.events.bus.CitySelectedEvent;
import ru.buggy.weatherviewer.events.bus.UpdateWeatherShowerEvent;
import ru.buggy.weatherviewer.events.bus.bus.EventBus;

public class Storage {
    private static int DEFAULT_CITY_INDEX = 0;

    private static Storage mStorage;

    private Context mContext;
    private ForecastDownloadDaemon mForecastDownloadDaemon;
    private ArrayList<CityData> mCitiesData = new ArrayList<>();
    private Map<Integer, ForecastWeatherData> mForecastData = new HashMap<>();
    private int mSelectedCityId;

    public Storage(Context context) {
        mStorage = this;
        mContext = context;
        initDefaultCitiesData();
        mSelectedCityId = mCitiesData.get(DEFAULT_CITY_INDEX).getCityId();

        EventBus.getInstance().register(this);
    }

    public static Storage getStorage() {
        return mStorage;
    }

    public ForecastWeatherData getDefaultCityForecastData() {
        return mForecastData.get(mCitiesData.get(DEFAULT_CITY_INDEX).getCityId());
    }

    private void initDefaultCitiesData() {
        CityData moscowCity = new CityData("Moscow", 5601538);
        CityData spbCity = new CityData("Saint-Petersburg", 498817);

        ForecastWeatherData moscowData = getForecastWeatherDataFromDb(5601538);
        ForecastWeatherData spbData = getForecastWeatherDataFromDb(498817);
        if (moscowData == null) {
            moscowData = new ForecastWeatherData(moscowCity, ForecastWeatherData.NO_DATA_RECEIVED_VALUE, ForecastWeatherData.NO_DATA_RECEIVED_VALUE, 0);
        }
        if (spbData == null) {
            spbData = new ForecastWeatherData(moscowCity, ForecastWeatherData.NO_DATA_RECEIVED_VALUE, ForecastWeatherData.NO_DATA_RECEIVED_VALUE, 0);
        }

        addCityForecastData(moscowCity, moscowData);
        addCityForecastData(spbCity, spbData);
    }

    public synchronized ArrayList<CityData> getCitiesData() {
        return mCitiesData;
    }

    public void startForecastDownloadDaemon() {
        mForecastDownloadDaemon = new ForecastDownloadDaemon();
        new Timer().schedule(mForecastDownloadDaemon, 0, 1000);
    }

    public Integer[] getCitiesIds() {
        Set<Integer> keySet = mForecastData.keySet();
        return keySet.toArray(new Integer[keySet.size()]);
    }

    public boolean checkCityAlreadyExist(int cityId) {
        return mForecastData.containsKey(cityId);
    }

    private void addCityForecastData(CityData cityData, ForecastWeatherData forecastWeatherData) {
        mCitiesData.add(cityData);
        mForecastData.put(cityData.getCityId(), forecastWeatherData);
    }

    // ----- DB ----------------------------------------------------------------------------------

    private String getStringForecastDataFromDb(int cityId) {
        // query
        String data = null;
        String selection = ForecastWeatherDataProvider.CITY_ID + " = " + cityId;
        Cursor cursor = mContext.getContentResolver().query(ForecastWeatherDataProvider.getForecastContentUri(),
                null, selection, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int allDataIndex = cursor.getColumnIndex(ForecastWeatherDataProvider.ALL_DATA);
                if (allDataIndex > 0) {
                    data = cursor.getString(allDataIndex);
                }
            }
            cursor.close();
        }

        Log.d("DB, query result: " + data);
        return data;
    }

    private ForecastWeatherData getForecastWeatherDataFromDb(int cityId) {
        String currentWeather = getStringForecastDataFromDb(cityId);
        if (currentWeather == null) {
            return null;
        }

        ForecastWeatherData fwd = stringToForecastWeatherData(currentWeather);

        for (int i=0; i < ForecastWeatherData.FORECAST_DAYS_NUMBER; i++) {
            String curWfdStr = getStringForecastDataFromDb(cityId*10 + i);
            if (curWfdStr != null) {
                ForecastWeatherData curFwd = stringToForecastWeatherData(curWfdStr);
                if (curFwd != null) {
                    fwd.updateChild(i, curFwd.getTemperature(), curFwd.getWindVelocity(), curFwd.getUpdateTime());
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        return fwd;
    }

    private synchronized void putForecastDataToDb(int cityId, ForecastWeatherData forecastWeatherData) {
        if (getStringForecastDataFromDb(cityId) != null) {
            // update
            ContentValues cv = new ContentValues();
            cv.put(ForecastWeatherDataProvider.CITY_ID, "" + cityId);
            cv.put(ForecastWeatherDataProvider.ALL_DATA, forecastWeatherDataToString(forecastWeatherData));
            Uri uri = ContentUris.withAppendedId(ForecastWeatherDataProvider.getForecastContentUri(), 2);
            int count = mContext.getContentResolver().update(uri, cv, null, null);
            Log.d("DB, update count = " + count);
        } else {
            // insert
            ContentValues cv = new ContentValues();
            cv.put(ForecastWeatherDataProvider.CITY_ID, "" + cityId);
            cv.put(ForecastWeatherDataProvider.ALL_DATA, forecastWeatherDataToString(forecastWeatherData));
            Uri newUri = mContext.getContentResolver().insert(ForecastWeatherDataProvider.getForecastContentUri(), cv);
            Log.d("DB, insert uri : " + newUri);
        }
    }

    private String forecastWeatherDataToString(ForecastWeatherData fwd) {
        String template = "%s+%s+%s+%s+%s";
        return String.format(template,
                fwd.getUpdateTime(),
                fwd.getCityData().getCityId(),
                fwd.getCityData().getCityName(),
                fwd.getTemperature(),
                fwd.getWindVelocity());
    }

    private ForecastWeatherData stringToForecastWeatherData(String str) {
        Log.d(str);
        String params[] = str.split("\\+");

        if (params.length != 5) {
            return null;
        }

        Long updateTime = Long.parseLong(params[0]);
        int cityId = Integer.parseInt(params[1]);
        String cityName = params[2];
        int temperature = Integer.parseInt(params[3]);
        int wind = Integer.parseInt(params[4]);

        ForecastWeatherData fwd = new ForecastWeatherData(new CityData(cityName, cityId), temperature, wind, updateTime);
        return fwd;
    }

    // ----- Events -------------------------------------------------------------------------------

    @Subscribe
    public void onNewCitySelectedEvent(CitySelectedEvent event) {
        Log.e("you clicked: " + event.getCityData().getCityName());
        mSelectedCityId = event.getCityData().getCityId();
        new UpdateWeatherShowerEvent(mForecastData.get(mSelectedCityId)).post();
    }

    @Subscribe
    public void onAddNewCityEvent(AddNewCityEvent event) {
        ForecastWeatherData fwd = event.getCityForecastWeatherData();
        addCityForecastData(fwd.getCityData(), fwd);

        // save data to db
        putForecastDataToDb(fwd.getCityData().getCityId(), fwd);

        // force daemon to update data
        mForecastDownloadDaemon.forceUpdate();
    }

    @Subscribe
    public void onUpdateCurrentWeatherEvent(UpdateCurrentWeatherEvent event) {
        Log.d(event.getData().toString());

        // parse json
        try {
            JSONArray ja = event.getData().getJSONArray("list");
            for (int i = 0; i < ja.length(); i++) {
                JSONObject curJo = ja.getJSONObject(i);
                int cityId = curJo.getInt("id");
                int temperature = (int) Math.round(curJo.getJSONObject("main").getDouble("temp"));
                int windVelocity = curJo.getJSONObject("wind").getInt("speed");

                // update data
                ForecastWeatherData curFwd = mForecastData.get(cityId);
                if (curFwd != null) {
                    curFwd.setTemperature(temperature);
                    curFwd.setWindVelocity(windVelocity);
                    curFwd.setUpdateTime(System.currentTimeMillis());
                }

                // save data to db
                putForecastDataToDb(cityId, curFwd);
            }
        } catch (Exception e) {
            Log.e(e.toString());
        }

        // update ui
        new UpdateWeatherShowerEvent(mForecastData.get(mSelectedCityId)).post();
    }

    @Subscribe
    public void onUpdateForecastWeatherEvent(UpdateForecastWeatherEvent event) {
        Log.d(event.getData().toString());

        // parse json
        try {
            int cityId = event.getData().getJSONObject("city").getInt("id");
            JSONArray ja = event.getData().getJSONArray("list");

            for (int i = 0; i < ja.length(); i++) {
                JSONObject curJo = ja.getJSONObject(i);
                int temperature = (int) Math.round(curJo.getJSONObject("temp").getDouble("eve"));
                int windVelocity = (int) Math.round(curJo.getDouble("speed"));

                // update data
                ForecastWeatherData curFwd = mForecastData.get(cityId);
                if (curFwd != null) {
                    curFwd.updateChild(i, temperature, windVelocity, System.currentTimeMillis());

                    // put data to db
                    putForecastDataToDb(cityId*10 + i, new ForecastWeatherData(curFwd.getCityData(), temperature, windVelocity, System.currentTimeMillis()));
                }
            }
        } catch (Exception e) {
            Log.e(e.toString());
        }

        // update ui
        new UpdateWeatherShowerEvent(mForecastData.get(mSelectedCityId)).post();
    }
}
