package me.toddcarter.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum  EventPriority {


    /**
     *  For lowest importance event calls
     */
    LOWEST(0),
    /**
     * For low importance event calls
     */
    LOW(1),
    /**
     * For normal importance event calls
     */
    NORMAL(2),
    /**
     * For high importance event calls
     */
    HIGH(3),
    /**
     * For highest importance event calls
     */
    HIGHEST(4),
    /**
     * To monitor the outcome of the event
     * DO NOT USE THIS TO MAKE MODIFICATIONS
     */
    MONITOR(5);

    private int slot;

    EventPriority(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }
}

