package ru.buggy.weatherviewer.events.bus.bus;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * Event bus wrapper to post messages to Otto bus in main thread
 */
public class EventBus {

    private static EventBus instance;
    private final Bus bus = new Bus();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    /**
     * EventBus instance should be retrieved via {@link #getInstance()}
     */
    private EventBus() {

    }

    /**
     * Gets the instance of the bus
     */
    public static EventBus getInstance() {
        return instance;
    }

    /**
     * Initialization of the Bus
     * EventBus should be initialize before the usage
     */
    public static void init() {
        instance = new EventBus();
    }

    /**
     * Posts event to the bus in the main thread
     */
    public void post(Object event) {
        mainThreadHandler.post(new PostEventTask(event));
    }

    /**
     * Register the bus listener
     */
    public void register(Object listener) {
        bus.register(listener);
    }

    /**
     * Unregister the bus listener
     */
    public void unregister(Object listener) {
        bus.unregister(listener);
    }

    /**
     * Post event to the bus
     */
    private final class PostEventTask implements Runnable {

        private final Object message;

        private PostEventTask(Object postMessage) {
            message = postMessage;
        }

        @Override
        public void run() {
            bus.post(message);
        }
    }
}
