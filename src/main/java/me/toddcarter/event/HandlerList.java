package me.toddcarter.event;

import me.toddcarter.subscription.Subscription;

import java.util.*;

public class HandlerList {

    /**
     * The list of handlers.
     */
    private volatile Subscription<?>[] handlers = null;

    /**
     * Unbaked list of handlers.
     */
    private final EnumMap<EventPriority, ArrayList<Subscription<?>>> handlerslots;

    /**
     * List of all handlers
     */
    private static final ArrayList<HandlerList> allLists = new ArrayList<>();

    /**
     * Bake all handler lists
     */
    public static void bakeAll() {
        synchronized (allLists) {
            for (HandlerList h : allLists) {
                h.bake();
            }
        }
    }

    /**
     * Unregister all listeners
     */
    public static void unregisterAll() {
        synchronized (allLists) {
            for (HandlerList h : allLists) {
                synchronized (h) {
                    for (List<Subscription<?>> list : h.handlerslots.values()) {
                        list.clear();
                    }
                    h.handlers = null;
                }
            }
        }
    }

    /**
     * Unregister a specific listener from all handlers
     *
     * @param subscription listener to unregister
     */
    public static void unregisterAll(Subscription<?> subscription) {
        synchronized (allLists) {
            for (HandlerList h : allLists) {
                h.unregister(subscription);
            }
        }
    }

    /**
     * Create a new handler list and initialize using EventPriority.
     * <p>
     * The HandlerList is then added to meta-list for use in bakeAll()
     */
    public HandlerList() {
        handlerslots = new EnumMap<EventPriority, ArrayList<Subscription<?>>>(EventPriority.class);
        for (EventPriority o : EventPriority.values()) {
            handlerslots.put(o, new ArrayList<Subscription<?>>());
        }
        synchronized (allLists) {
            allLists.add(this);
        }
    }

    /**
     * Register a new listener
     *
     * @param subscription listener to register
     */
    public synchronized void register(Subscription<?> subscription) {
        if (handlerslots.get(subscription.getPriority()).contains(subscription))
            throw new IllegalStateException("This listener is already registered to priority " + subscription.getPriority().toString());
        handlers = null;
        handlerslots.get(subscription.getPriority()).add(subscription);
    }

    /**
     * Register a collection of new listeners
     *
     * @param subscriptions listeners to register
     */
    public void registerAll(Collection<Subscription<?>> subscriptions) {
        for (Subscription<?> listener : subscriptions) {
            register(listener);
        }
    }

    /**
     * Remove a specific listener
     *
     * @param listener listener to remove
     */
    public synchronized void unregister(Subscription<?> listener) {
        boolean changed = false;
        for (List<Subscription<?>> list : handlerslots.values()) {
            for (ListIterator<Subscription<?>> i = list.listIterator(); i.hasNext();) {
                if (i.next().equals(listener)) {
                    i.remove();
                    changed = true;
                }
            }
        }
        if (changed) handlers = null;
    }

    /**
     * Bake HashMap and ArrayLists to 2d array
     */
    public synchronized void bake() {
        if (handlers != null) return; // don't re-bake when still valid
        List<Subscription<?>> entries = new ArrayList<Subscription<?>>();
        for (Map.Entry<EventPriority, ArrayList<Subscription<?>>> entry : handlerslots.entrySet()) {
            entries.addAll(entry.getValue());
        }
        handlers = entries.toArray(new Subscription<?>[entries.size()]);
    }

    /**
     * Get the baked registered listeners
     *
     * @return the array of registered listeners
     */
    public Subscription<?>[] getRegisteredListeners() {
        Subscription<?>[] handlers;
        while ((handlers = this.handlers) == null) bake(); // This prevents fringe cases of returning null
        return handlers;
    }

    /**
     * Get a list of all handler lists for every event type
     *
     * @return the list of all handler lists
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<HandlerList> getHandlerLists() {
        synchronized (allLists) {
            return (ArrayList<HandlerList>) allLists.clone();
        }
    }
}