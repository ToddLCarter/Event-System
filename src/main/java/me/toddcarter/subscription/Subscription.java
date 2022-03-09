package me.toddcarter.subscription;

import me.toddcarter.Events;
import me.toddcarter.event.Event;
import me.toddcarter.event.EventPriority;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Subscription<T extends Event> {

    private final Class<T> eventClass;
    private final EventPriority priority;

    private final List<Predicate<? super T>> filters;
    private final List<Predicate<Subscription<T>>> expiries;
    private final Consumer<? super T> handler;
    private final BiConsumer<? super T, Throwable> exceptionHandler;

    private final AtomicLong initTime = new AtomicLong(0);
    private AtomicLong callCount = new AtomicLong(0);
    private AtomicBoolean active = new AtomicBoolean(true);

    public Subscription(Class<T> eventClass, EventPriority priority, List<Predicate<? super T>> filters, List<Predicate<Subscription<T>>> expiries, Consumer<? super T> handler, BiConsumer<? super T, Throwable> exceptionHandler) {
        this.eventClass = eventClass;
        this.priority = priority;
        this.filters = filters;
        this.expiries = expiries;
        this.handler = handler;
        this.exceptionHandler = exceptionHandler;
        this.initTime.set(System.currentTimeMillis());
        this.callCount.set(0);
        this.active.set(true);
    }


    public synchronized final void execute(Event event) {
        if (!this.active.get()) {
            Events.unregisterListener(this);
            return;
        }

        T castedEvent = this.eventClass.cast(event);

        //check the expiries
        for(Predicate<Subscription<T>> expiries : this.expiries) {
            if(expiries.test(this)) {
                Events.unregisterListener(this);
                this.active.set(false);
                return;
            }
        }

        try {
            //check the filters
            for(Predicate<? super T> filters : this.filters) {
                if(!filters.test(castedEvent)) {
                    return;
                }
            }

            //call the handler
            this.handler.accept(castedEvent);

            //increment the call count
           this.callCount.incrementAndGet();

        } catch (Throwable t) {
            this.exceptionHandler.accept(castedEvent, t);
        }
    }

    @Nonnull
    public final Class<? extends Event> getEventClass() {
        return this.eventClass;
    }

    public final boolean isActive() {
        return this.active.get();
    }

    public final EventPriority getPriority() { return this.priority; }

    public final long getCallCounter() {
        return this.callCount.get();
    }

    public final long getRegisterTime() {
        return this.initTime.get();
    }

}
