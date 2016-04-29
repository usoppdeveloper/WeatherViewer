package ru.buggy.weatherviewer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ru.buggy.weatherviewer.data.CityData;

public class CitiesDataAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<CityData> mCitiesData;

    public CitiesDataAdapter(Context context, ArrayList<CityData> citiesData) {
        mContext = context;
        mCitiesData = citiesData;
    }

    @Override
    public int getCount() {
        return mCitiesData.size();
    }

    @Override
    public Object getItem(int i) {
        return mCitiesData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = new TextView(mContext);
        }

        TextView tv = (TextView) view;
        if (tv.isSelected()) {
            tv.setBackgroundColor(Color.GRAY);
        }
        tv.setText(mCitiesData.get(position).getCityName());
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);


        return view;
    }
}
