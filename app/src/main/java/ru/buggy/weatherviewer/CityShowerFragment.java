package ru.buggy.weatherviewer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import ru.buggy.weatherviewer.adapters.CitiesDataAdapter;
import ru.buggy.weatherviewer.data.CityData;
import ru.buggy.weatherviewer.data.ForecastWeatherData;
import ru.buggy.weatherviewer.data.Storage;
import ru.buggy.weatherviewer.events.bus.AddNewCityEvent;
import ru.buggy.weatherviewer.events.bus.CitySelectedEvent;
import ru.buggy.weatherviewer.events.bus.bus.EventBus;
import ru.buggy.weatherviewer.net.DownloadWeatherHelper;

public class CityShowerFragment extends Fragment {

    private EditText mCityEditText;
    private Button mAddCityButton;
    CitiesDataAdapter mCitiesDataAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getInstance().register(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentLayout = inflater.inflate(R.layout.city_shower_fragment, null);

        // get views
        ListView lv = (ListView) fragmentLayout.findViewById(R.id.cities_list_view);
        mCityEditText = (EditText) fragmentLayout.findViewById(R.id.add_city_edit_text);
        mAddCityButton = (Button) fragmentLayout.findViewById(R.id.add_city_button);

        // setup cities list view
        mCitiesDataAdapter = new CitiesDataAdapter(getActivity().getApplicationContext(), Storage.getStorage().getCitiesData());
        lv.setAdapter(mCitiesDataAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                itemClicked.setSelected(true);
                new CitySelectedEvent(Storage.getStorage().getCitiesData().get(position)).post();
            }
        });

        // setup add city button
        mAddCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(mCityEditText.getText().toString());
                final String cityName = mCityEditText.getText().toString();
                mAddCityButton.setEnabled(false);

                // make search request at the separate thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jo = DownloadWeatherHelper.searchCity(cityName);

                        if (jo != null) {
                            try {
                                int cityId = jo.getInt("id");

                                // check city already exist
                                if (Storage.getStorage().checkCityAlreadyExist(cityId)) {
                                    CityShowerFragment.this.getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String toastMessage = "City with name \"" + cityName + "\" already exist!";
                                            Toast.makeText(CityShowerFragment.this.getActivity(), toastMessage, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    mAddCityButton.setEnabled(true);
                                    return;
                                }

                                int temperature = (int)((JSONObject) jo.get("main")).getDouble("temp");
                                int windVelocity = ((JSONObject) jo.get("wind")).getInt("speed");

                                new AddNewCityEvent(new ForecastWeatherData(new CityData(cityName, cityId), temperature, windVelocity, System.currentTimeMillis())).post();
                            } catch (Exception e) {
                                Log.e(e.toString());
                                return;
                            }
                        } else {
                            CityShowerFragment.this.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String toastMessage = "Failed to find city " + cityName +
                                            ". Check the city name or internet connection.";
                                    Toast.makeText(CityShowerFragment.this.getActivity(), toastMessage, Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        // update ui
                        CityShowerFragment.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAddCityButton.setEnabled(true);
                                mCitiesDataAdapter.notifyDataSetChanged();
                            }
                        });

                    }
                }).start();
            }
        });

        return fragmentLayout;
    }
}
