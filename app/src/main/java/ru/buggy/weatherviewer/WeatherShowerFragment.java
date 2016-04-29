package ru.buggy.weatherviewer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import ru.buggy.weatherviewer.data.ForecastWeatherData;
import ru.buggy.weatherviewer.data.Storage;
import ru.buggy.weatherviewer.events.bus.UpdateWeatherShowerEvent;
import ru.buggy.weatherviewer.events.bus.bus.EventBus;

public class WeatherShowerFragment extends Fragment {
    private TextView mCityTv;
    private TextView mTemperatureTv;
    private TextView mWindTv;
    private TextView mTimeTv;
    private ListView mForecastWeatherListView;
    String[] displayStrings = new String[ForecastWeatherData.FORECAST_DAYS_NUMBER];
    ArrayAdapter<String> mAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getInstance().register(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentLayout = inflater.inflate(R.layout.weather_shower_fragment, null);

        mCityTv = (TextView) fragmentLayout.findViewById(R.id.city);
        mTemperatureTv = (TextView) fragmentLayout.findViewById(R.id.temperature);
        mWindTv = (TextView) fragmentLayout.findViewById(R.id.wind);
        mTimeTv = (TextView) fragmentLayout.findViewById(R.id.time);
        mForecastWeatherListView = (ListView) fragmentLayout.findViewById(R.id.forecast_weather_list_view);

        // setup list view
        mForecastWeatherListView.setEnabled(false);
        for (int i = 0; i < ForecastWeatherData.FORECAST_DAYS_NUMBER; i++) {
            displayStrings[i] = "";
        }
        mAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.forecast_list_item, displayStrings);
        mForecastWeatherListView.setAdapter(mAdapter);

        // update ui
        onUpdateWeatherShowerEvent(new UpdateWeatherShowerEvent(Storage.getStorage().getDefaultCityForecastData()));

        return fragmentLayout;
    }

    @Subscribe
    public void onUpdateWeatherShowerEvent(UpdateWeatherShowerEvent event) {
        ForecastWeatherData fwd = event.getForecastData();

        // update current weather ui
        String temperature = fwd.getTemperature() == ForecastWeatherData.NO_DATA_RECEIVED_VALUE ? "No data" : fwd.getTemperature() + " \u00B0C";
        String wind = fwd.getWindVelocity() == ForecastWeatherData.NO_DATA_RECEIVED_VALUE ? "No data" : fwd.getWindVelocity() + " m/s";
        String time = fwd.getUpdateTime() == 0 ? "No data" : getDateString(fwd.getUpdateTime());

        mCityTv.setText(fwd.getCityDescription());
        mTemperatureTv.setText(temperature);
        mWindTv.setText(wind);
        mTimeTv.setText(time);

        // update forecast weather ui
        String template = "%s,   %s °C, %s m/s";
        ArrayList<ForecastWeatherData> weekForecastData = fwd.getWeekForecastData();

        if (weekForecastData == null) {
            for (int i = 0; i < displayStrings.length; i++) {
                displayStrings[i] = "no data received";
            }
        } else {
            for (int i = 0; i < weekForecastData.size() && i <displayStrings.length; i++) {
                ForecastWeatherData curFwd = weekForecastData.get(i);
                displayStrings[i] = String.format(template, getDateString(curFwd.getUpdateTime()), curFwd.getTemperature()+"", curFwd.getWindVelocity()+"");
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    private String getDateString(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM, HH:mm", Locale.US);
        return sdf.format(new Date(timeMillis));
    }
}
