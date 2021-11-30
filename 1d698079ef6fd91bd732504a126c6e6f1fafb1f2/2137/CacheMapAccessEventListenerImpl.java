/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.extra;

import com.opensymphony.oscache.base.events.CacheMapAccessEvent;
import com.opensymphony.oscache.base.events.CacheMapAccessEventListener;
import com.opensymphony.oscache.base.events.CacheMapAccessEventType;

/**
 * Implementation of a CacheMapAccessEventListener. It uses the events to count
 * the cache hit and misses.
 * <p>
 * We are not using any synchronized so that this does not become a bottleneck.
 * The consequence is that on retrieving values, the operations that are
 * currently being done won't be counted.
 *
 * @version        $Revision: 254 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public class CacheMapAccessEventListenerImpl implements CacheMapAccessEventListener {
    /**
     * Hit counter
     */
    private int hitCount = 0;

    /**
     * Miss counter
     */
    private int missCount = 0;

    /**
     * Stale hit counter
     */
    private int staleHitCount = 0;

    /**
     * Constructor, empty for us
     */
    public CacheMapAccessEventListenerImpl() {
    }

    /**
     * Returns the cache's current hit count
     *
     * @return The hit count
     */
    public int getHitCount() {
        return hitCount;
    }

    /**
     * Returns the cache's current miss count
     *
     * @return The miss count
     */
    public int getMissCount() {
        return missCount;
    }

    /**
     * Returns the cache's current stale hit count
     */
    public int getStaleHitCount() {
        return staleHitCount;
    }

    /**
     * This method handles an event each time the cache is accessed
     *
     * @param event The event triggered when the cache was accessed
     */
    public void accessed(CacheMapAccessEvent event) {
        // Retrieve the event type and update the counters
        CacheMapAccessEventType type = event.getEventType();

        // Handles a hit event
        if (type == CacheMapAccessEventType.HIT) {
            hitCount++;
        }
        // Handles a stale hit event
        else if (type == CacheMapAccessEventType.STALE_HIT) {
            staleHitCount++;
        }
        // Handles a miss event
        else if (type == CacheMapAccessEventType.MISS) {
            missCount++;
        } else {
            // Unknown event!
            throw new IllegalArgumentException("Unknown Cache Map Access event received");
        }
    }

    /**
     * Resets all of the totals to zero
     */
    public void reset() {
        hitCount = 0;
        staleHitCount = 0;
        missCount = 0;
    }

    /**
     * Return the counters in a string form
     */
    public String toString() {
        return ("Hit count = " + hitCount + ", stale hit count = " + staleHitCount + " and miss count = " + missCount);
    }
}
