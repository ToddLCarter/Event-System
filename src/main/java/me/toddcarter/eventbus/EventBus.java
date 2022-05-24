package me.toddcarter.eventbus;

import me.toddcarter.event.Event;
import me.toddcarter.event.HandlerList;
import me.toddcarter.subscription.Subscription;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class EventBus {


    /**
     * Register a new listener
     *
     * @param subscription the listener to register
     *
     */
    public void registerListener(@NotNull Subscription<? extends Event> subscription) {
        try {
            final Class<? extends Event> eventClass = subscription.getEventClass().asSubclass(Event.class);
            Method method = eventClass.getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            HandlerList hl = (HandlerList) method.invoke(null);
            hl.register(subscription);
        } catch (NoSuchMethodException| InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unregister a listener
     *
     * @param subscription the listener to unregister
     *
     */
    public void unregisterListener(@NotNull Subscription<? extends Event> subscription) {
        try {
            final Class<? extends Event> eventClass = subscription.getEventClass().asSubclass(Event.class);
            Method method = eventClass.getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            HandlerList hl = (HandlerList) method.invoke(null);
            hl.unregister(subscription);
        } catch (NoSuchMethodException| InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * Post an event to the event bus
     *
     * @param event the event to post
     *
     */
    public void call(@NotNull Event event, boolean async) {
        if(async) {
            if (Thread.holdsLock(this)) {
                throw new IllegalStateException("Cannot fire event from within synchronized block");
            }
            fireEvent(event);
        }else{
            fireEvent(event);
        }
    }

    private void fireEvent(Event event) {
        HandlerList handlers = event.getHandlers();
        Subscription<?>[] listeners = handlers.getRegisteredListeners();

        for (Subscription<?> registration : listeners) {
            try {
                registration.execute(event);
            } catch (Throwable ex) {
                //do nothing
            }
        }
    }
}