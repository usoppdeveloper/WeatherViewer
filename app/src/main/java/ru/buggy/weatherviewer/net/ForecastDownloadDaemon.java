package ru.buggy.weatherviewer.net;

import java.util.TimerTask;

import ru.buggy.weatherviewer.Log;

public class ForecastDownloadDaemon extends TimerTask {
    private static int UPDATE_CURRENT_FORECAST_PERIOD_SEC = 30;

    private CurrentWeatherReceiverThread mCurrentWeatherReceiverThread = new CurrentWeatherReceiverThread();
    private ForecastWeatherReceiverThread mForecastWeatherReceiverThread = new ForecastWeatherReceiverThread();
    private static int mSyncRemaining = 3;

    public void run() {
        mSyncRemaining--;

        if (mSyncRemaining <= 0) {
            mSyncRemaining = UPDATE_CURRENT_FORECAST_PERIOD_SEC;

            try {
                if (!mCurrentWeatherReceiverThread.isAlive()) {
                    mCurrentWeatherReceiverThread = new CurrentWeatherReceiverThread();
                    mCurrentWeatherReceiverThread.start();
                }

                if (!mForecastWeatherReceiverThread.isAlive()) {
                    mForecastWeatherReceiverThread = new ForecastWeatherReceiverThread();
                    mForecastWeatherReceiverThread.start();
                }

                Log.d("(Daemon) is active");
            } catch (Exception e) {
                Log.e(e.toString());
            }
        }
    }

    public void forceUpdate() {
        mSyncRemaining = 3;
    }
}
