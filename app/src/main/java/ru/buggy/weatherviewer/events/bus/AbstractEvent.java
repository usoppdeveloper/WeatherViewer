package ru.buggy.weatherviewer.events.bus;

import ru.buggy.weatherviewer.events.bus.bus.EventBus;

/**
 * Base class for all events
 */
public abstract class AbstractEvent {

    AbstractEvent() { }

    /**
     * Submits the event
     */
    public final void post() {
        EventBus.getInstance().post(this);
    }

}

