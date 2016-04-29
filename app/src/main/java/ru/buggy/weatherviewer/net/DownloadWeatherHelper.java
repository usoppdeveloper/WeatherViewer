package ru.buggy.weatherviewer.net;

import android.content.Context;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.buggy.weatherviewer.Log;

public class DownloadWeatherHelper {
    private final static int CONNECTION_TIMEOUT_MS = 10_000;
    private final static String API_KEY = "ceb0685e363f1ae2592779a68af43539";
    private final static String SEARCH_CITY_REQUEST = "http://api.openweathermap.org/data/2.5/find?q=%s&units=metric";
    private static final String GET_CURRENT_WEATHER_REQUEST = "http://api.openweathermap.org/data/2.5/group?id=%s&units=metric";
    private static final String GET_FORECAST_WEATHER_REQUEST = "http://api.openweathermap.org/data/2.5/forecast/daily?id=%d&cnt=7&units=metric";

    public static JSONObject searchCity(String cityName) {
        if (cityName == null || cityName.isEmpty()) {
            return null;
        }

        try {
            // setup connection
            URL url = new URL(String.format(SEARCH_CITY_REQUEST, cityName.replace(" ", "+")));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            connection.addRequestProperty("x-api-key", API_KEY);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            // read data
            StringBuffer stringBuffer = new StringBuffer(1024);
            String tmp;
            while((tmp = reader.readLine()) != null) {
                stringBuffer.append(tmp).append("\n");
            }
            reader.close();

            // make json
            JSONObject jsonData = new JSONObject(stringBuffer.toString());

            // check data
            if (jsonData.getInt("cod") != 200) {
                return null;
            }

            return jsonData.getJSONArray("list").getJSONObject(0);
        } catch(Exception e){
            Log.e(e.toString());
            return null;
        }
    }

    public static JSONObject getCurrentWeather(Integer[] citiesIds){
        if (citiesIds == null || citiesIds.length == 0) {
            return null;
        }

        try {
            // make cities list string
            String citiesList = "" + citiesIds[0];
            for (int i = 1; i < citiesIds.length; i++) {
                citiesList += "," + citiesIds[i];
            }

            // setup connection
            URL url = new URL(String.format(GET_CURRENT_WEATHER_REQUEST, citiesList));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            connection.addRequestProperty("x-api-key", API_KEY);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            // read data
            StringBuffer stringBuffer = new StringBuffer(1024);
            String tmp="";
            while((tmp = reader.readLine()) != null) {
                stringBuffer.append(tmp).append("\n");
            }
            reader.close();

            // make json
            JSONObject jsonData = new JSONObject(stringBuffer.toString());

            // check data
            if(jsonData.getInt("cnt") == 0){
                return null;
            }

            return jsonData;
        } catch(Exception e){
            Log.e(e.toString());
            return null;
        }
    }

    public static JSONObject getForecastWeather(int cityId) {
        try {
            // setup connection
            URL url = new URL(String.format(GET_FORECAST_WEATHER_REQUEST, cityId));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            connection.addRequestProperty("x-api-key", API_KEY);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            // read data
            StringBuffer stringBuffer = new StringBuffer(1024);
            String tmp="";
            while((tmp = reader.readLine()) != null) {
                stringBuffer.append(tmp).append("\n");
            }
            reader.close();

            // make json
            JSONObject jsonData = new JSONObject(stringBuffer.toString());

            // check data
            if (jsonData.getInt("cod") != 200) {
                return null;
            }

            return jsonData;
        } catch(Exception e){
            Log.e(e.toString());
            return null;
        }
    }

}
