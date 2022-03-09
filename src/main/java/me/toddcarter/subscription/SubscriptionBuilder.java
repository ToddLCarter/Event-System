package me.toddcarter.subscription;

import me.toddcarter.Events;
import me.toddcarter.event.Cancellable;
import me.toddcarter.event.Event;
import me.toddcarter.event.EventPriority;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SubscriptionBuilder<T extends Event> {

    private final Class<T> eventClass;
    private EventPriority priority;

    private List<Predicate<? super T>> filters;
    private List<Predicate<Subscription<T>>> expiries;
    private Consumer<? super T> handler;
    private BiConsumer<? super T, Throwable> exceptionHandler;

    /**
     * Constructor for the SubscriptionBuilder.
     *
     * @param eventClass the event class to subscribe to
     * @param priority   the priority of the subscription
     *
     */
    public SubscriptionBuilder(Class<T> eventClass , EventPriority priority) {
        Objects.requireNonNull(eventClass, "eventClass");
        Objects.requireNonNull(priority, "priority");
        this.eventClass = eventClass;
        this.priority = priority;
        this.filters = new ArrayList<>();
        this.expiries = new ArrayList<>();
        this.handler = event -> {};
        this.exceptionHandler = (event, throwable) -> { throwable.printStackTrace(); };
    }

    /**
     * Adds a filter to the subscription.
     *
     * @param filter the filter to add
     */
    public SubscriptionBuilder<T> filter(Predicate<? super T> filter) {
        Objects.requireNonNull(filter, "filter");
        filters.add(filter);
        return this;
    }

    /**
     * Do not execute if the event was cancelled by another listener.
     */
    public SubscriptionBuilder<T> ignoreCancelled() {
        filter(event -> {
           if(event instanceof Cancellable) {
               return !((Cancellable) event).isCancelled();
           }
           return true;
        });
        return this;
    }

    /**
     * Adds an expiry condition to the subscription.
     *
     * @param expiry the expiry to add
     */
    public SubscriptionBuilder<T> expireIf(Predicate<Subscription<T>> expiry) {
        Objects.requireNonNull(expiry, "expiry");
        expiries.add(expiry);
        return this;
    }

    /**
     * Limit the amount of times the listener can be called
     *
     * @param maxCalls the max amount of times the listener can be called
     */
    public SubscriptionBuilder<T> expireAfter(Integer maxCalls) {
        Objects.requireNonNull(maxCalls, "maxCalls");
        Preconditions.checkArgument(maxCalls >= 1, "maxCalls < 1");
        return expireIf(subscription -> subscription.getCallCounter() >= maxCalls);
    }

    /**
     * Limit how long the listener can be executed
     *
     * @param time the time
     * @param timeUnit the time unit
     */
    public SubscriptionBuilder<T> expireAfter(Integer time, TimeUnit timeUnit) {
        Objects.requireNonNull(time, "time");
        Objects.requireNonNull(timeUnit, "timeUnit");
        Objects.requireNonNull(timeUnit, "timeUnit");
        Preconditions.checkArgument(time >= 1, "time < 1");
        long expiry = timeUnit.toMillis(time) + System.currentTimeMillis();
        return expireIf(subscription -> expiry < subscription.getRegisterTime());
    }

    /**
     * Adds a handler for when an exception is thrown by the listener.
     *
     * @param exceptionHandler the handler to add
     */
    public SubscriptionBuilder<T> onError(BiConsumer<? super T, Throwable> exceptionHandler) {
        Objects.requireNonNull(exceptionHandler, "exceptionHandler");
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    /**
     * Adds a handler for when an event is received.
     *
     * @param handler the handler to add
     */
    public Subscription<T> handler(Consumer<? super T> handler) {
        Objects.requireNonNull(handler, "handler");
        this.handler = handler;
        Subscription<T> subscription = new Subscription<>(eventClass, priority, filters, expiries, handler, exceptionHandler);
        Events.registerListener(subscription);
        return subscription;
    }
}
