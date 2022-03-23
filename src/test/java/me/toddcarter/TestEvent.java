package me.toddcarter;

import me.toddcarter.event.Cancellable;
import me.toddcarter.event.Event;
import me.toddcarter.event.HandlerList;

public final class TestEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public boolean test;
    public int priority = 0;
    public int expire = 0;
    public int filter = 0;
    public boolean expired;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
