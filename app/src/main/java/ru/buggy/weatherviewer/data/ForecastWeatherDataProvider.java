package ru.buggy.weatherviewer.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import ru.buggy.weatherviewer.Log;

public class ForecastWeatherDataProvider extends ContentProvider {
    // tables
    private static final String FORECAST_WEATHER_TABLE = "forecast";

    // fields
    public static final String ALL_DATA = "all_data";
    public static final String FORECAST_ID = "id";
    public static final String CITY_ID = "city_id";
   /* private static final String CITY_NAME = "city_name";
    private static final String UPDATE_TIME = "update_time";
    private static final String TEMPERATURE = "temperature";
    private static final String WIND = "wind";*/

    // uri
    private static final String AUTHORITY = "ru.buggy.weatherviewer";
    private static final String PATH = "forecastweatherdata";
    private  static final Uri FORECAST_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

    public static Uri getForecastContentUri() {
        return Uri.parse("content://" + AUTHORITY + "/" + PATH);
    }

    // uri matcher
    private static final int URI_FORECASTS = 1;
    private static final int URI_FORECAST_ID = 2;
    private static final String FORECAST_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PATH;
    private static final String FORECAST_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + PATH;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PATH, URI_FORECASTS);
        uriMatcher.addURI(AUTHORITY, PATH + "/#", URI_FORECAST_ID);
    }

    // db
    private static final String DB_NAME = "forecast_db";
    private static final int DB_VERSION = 1;
    DBHelper dbHelper;
    SQLiteDatabase db;

    private class DBHelper extends SQLiteOpenHelper {
        static final String DB_CREATE = "create table " + FORECAST_WEATHER_TABLE + "("
                + FORECAST_ID + " integer primary key autoincrement, "
                + CITY_ID + " text, " + ALL_DATA + " text" + ");";


        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    // --------------------------------------------------------------------------------------------

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new DBHelper(context);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case URI_FORECASTS:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = CITY_ID + " ASC";
                }
                break;
            case URI_FORECAST_ID:
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(FORECAST_WEATHER_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(),  FORECAST_CONTENT_URI);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_FORECASTS:
                return FORECAST_CONTENT_TYPE;
            case URI_FORECAST_ID:
                return FORECAST_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Log.e("insert, " + uri.toString());

        if (uriMatcher.match(uri) != URI_FORECASTS) {
            throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        db = dbHelper.getWritableDatabase();
        long rowID = db.insert(FORECAST_WEATHER_TABLE, null, contentValues);
        Log.e("row id: " + rowID);
        Uri resultUri = ContentUris.withAppendedId(FORECAST_CONTENT_URI, rowID);
        Log.e("result uri: " + resultUri);

        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d("delete, " + uri.toString());
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d("update, " + uri.toString());

        switch (uriMatcher.match(uri)) {
            case URI_FORECASTS:
                Log.d("URI_CONTACTS");
                break;

            case URI_FORECAST_ID:
                String id = values.getAsString(CITY_ID);
                Log.d("URI_CONTACTS_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = CITY_ID + " = " + id;
                } else {
                    selection = selection + " AND " + CITY_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.update(FORECAST_WEATHER_TABLE, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return cnt;
    }
}
