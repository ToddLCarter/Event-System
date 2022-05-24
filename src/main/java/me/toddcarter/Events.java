package me.toddcarter;

import me.toddcarter.event.Event;
import me.toddcarter.event.EventPriority;
import me.toddcarter.eventbus.EventBus;
import me.toddcarter.subscription.Subscription;
import me.toddcarter.subscription.SubscriptionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class Events {

    private static final EventBus eventBus = new EventBus();


    /**
     * Initialise a new subscription builder.
     *
     * @param eventClass the event class to subscribe to
     * @param priority   the priority of the subscription
     */
    public static <T extends Event> SubscriptionBuilder<T> subscribe(Class<T> eventClass , EventPriority priority) {
        return new SubscriptionBuilder<>(eventClass, priority);
    }

    /**
     * Initialise a new subscription builder.
     *
     * @param eventClass the event class to subscribe to
     */
    public static <T extends Event> SubscriptionBuilder<T> subscribe(Class<T> eventClass) {
        return new SubscriptionBuilder<>(eventClass, EventPriority.NORMAL);
    }

    /**
     * Submit the event on a new async thread.
     *
     * @param event the event to call
     */
    public static void callAsync(@NotNull Event event) {
        CompletableFuture.runAsync(() -> eventBus.call(event, true));
    }

    /**
     * Submit the event on the current thread.
     *
     * @param event the event to call
     */
    public static void callSync(@NotNull Event event) {
        eventBus.call(event, false);
    }

    /**
     * Register a new listener.
     *
     * @param listener the listener to register
     */
    public static <T extends Event> void registerListener(@NotNull Subscription<T> listener) {
        eventBus.registerListener(listener);
    }

    /**
     * Unregister a listener.
     *
     * @param listener the listener to unregister
     */
    public static <T extends Event> void unregisterListener(@NotNull Subscription<T> listener) {
        eventBus.unregisterListener(listener);
    }
}
