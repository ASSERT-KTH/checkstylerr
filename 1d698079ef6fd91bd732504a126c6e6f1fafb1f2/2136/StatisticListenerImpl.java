/*
 * Copyright (c) 2002-2007 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.extra;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.events.CacheEntryEvent;
import com.opensymphony.oscache.base.events.CacheEntryEventListener;
import com.opensymphony.oscache.base.events.CacheGroupEvent;
import com.opensymphony.oscache.base.events.CacheMapAccessEvent;
import com.opensymphony.oscache.base.events.CacheMapAccessEventListener;
import com.opensymphony.oscache.base.events.CacheMapAccessEventType;
import com.opensymphony.oscache.base.events.CachePatternEvent;
import com.opensymphony.oscache.base.events.CachewideEvent;
import com.opensymphony.oscache.base.events.ScopeEvent;
import com.opensymphony.oscache.base.events.ScopeEventListener;
import com.opensymphony.oscache.extra.ScopeEventListenerImpl;

/**
 * A simple implementation of a statistic reporter which uses the
 * event listeners. It uses the events to count the cache hit and
 * misses and of course the flushes.
 * <p>
 * We are not using any synchronized so that this does not become a bottleneck.
 * The consequence is that on retrieving values, the operations that are
 * currently being done won't be counted.
 */
public class StatisticListenerImpl implements CacheMapAccessEventListener,
        CacheEntryEventListener, ScopeEventListener {

    /**
     * Hit counter.
     */
    private static int hitCount = 0;

    /**
     * Miss counter.
     */
    private static int missCount = 0;

    /**
     * Stale hit counter.
     */
    private static int staleHitCount = 0;

    /**
     * Hit counter sum.
     */
    private static int hitCountSum = 0;

    /**
     * Miss counter sum.
     */
    private static int missCountSum = 0;

    /**
     * Stale hit counter.
     */
    private static int staleHitCountSum = 0;

    /**
     * Flush hit counter.
     */
    private static int flushCount = 0;
    
    /**
     * Miss counter sum.
     */
    private static int entriesAdded = 0;

    /**
     * Stale hit counter.
     */
    private static int entriesRemoved = 0;

    /**
     * Flush hit counter.
     */
    private static int entriesUpdated = 0;

    /**
     * Constructor, empty for us.
     */
    public StatisticListenerImpl() {

    }

    /**
     * This method handles an event each time the cache is accessed.
     * 
     * @param event
     *            The event triggered when the cache was accessed
     * @see com.opensymphony.oscache.base.events.CacheMapAccessEventListener#accessed(CacheMapAccessEvent)
     */
    public void accessed(CacheMapAccessEvent event) {
        // Retrieve the event type and update the counters
        CacheMapAccessEventType type = event.getEventType();

        // Handles a hit event
        if (type == CacheMapAccessEventType.HIT) {
            hitCount++;
        } else if (type == CacheMapAccessEventType.STALE_HIT) { // Handles a
                                                                // stale hit
                                                                // event
            staleHitCount++;
        } else if (type == CacheMapAccessEventType.MISS) { // Handles a miss
                                                            // event
            missCount++;
        }
    }

    /**
     * Logs the flush of the cache.
     * 
     * @param info the string to be logged.
     */
    private void flushed(String info) {
        flushCount++;

        hitCountSum += hitCount;
        staleHitCountSum += staleHitCount;
        missCountSum += missCount;

        hitCount = 0;
        staleHitCount = 0;
        missCount = 0;
    }

    /**
     * Event fired when a specific or all scopes are flushed.
     * 
     * @param event ScopeEvent
     * @see com.opensymphony.oscache.base.events.ScopeEventListener#scopeFlushed(ScopeEvent)
     */
    public void scopeFlushed(ScopeEvent event) {
        flushed("scope " + ScopeEventListenerImpl.SCOPE_NAMES[event.getScope()]);
    }

    /**
     * Event fired when an entry is added to the cache.
     * 
     * @param event CacheEntryEvent
     * @see com.opensymphony.oscache.base.events.CacheEntryEventListener#cacheEntryAdded(CacheEntryEvent)
     */
    public void cacheEntryAdded(CacheEntryEvent event) {
        entriesAdded++;
    }

    /**
     * Event fired when an entry is flushed from the cache.
     * 
     * @param event CacheEntryEvent
     * @see com.opensymphony.oscache.base.events.CacheEntryEventListener#cacheEntryFlushed(CacheEntryEvent)
     */
    public void cacheEntryFlushed(CacheEntryEvent event) {
        // do nothing, because a group or other flush is coming
        if (!Cache.NESTED_EVENT.equals(event.getOrigin())) {
            flushed("entry " + event.getKey() + " / " + event.getOrigin());
        }
    }

    /**
     * Event fired when an entry is removed from the cache.
     * 
     * @param event CacheEntryEvent
     * @see com.opensymphony.oscache.base.events.CacheEntryEventListener#cacheEntryRemoved(CacheEntryEvent)
     */
    public void cacheEntryRemoved(CacheEntryEvent event) {
        entriesRemoved++;
    }

    /**
     * Event fired when an entry is updated in the cache.
     * 
     * @param event CacheEntryEvent
     * @see com.opensymphony.oscache.base.events.CacheEntryEventListener#cacheEntryUpdated(CacheEntryEvent)
     */
    public void cacheEntryUpdated(CacheEntryEvent event) {
        entriesUpdated++;
    }

    /**
     * Event fired when a group is flushed from the cache.
     * 
     * @param event CacheGroupEvent
     * @see com.opensymphony.oscache.base.events.CacheEntryEventListener#cacheGroupFlushed(CacheGroupEvent)
     */
    public void cacheGroupFlushed(CacheGroupEvent event) {
        flushed("group " + event.getGroup());
    }

    /**
     * Event fired when a key pattern is flushed from the cache.
     * 
     * @param event CachePatternEvent
     * @see com.opensymphony.oscache.base.events.CacheEntryEventListener#cachePatternFlushed(CachePatternEvent)
     */
    public void cachePatternFlushed(CachePatternEvent event) {
        flushed("pattern " + event.getPattern());
    }

    /**
     * An event that is fired when an entire cache gets flushed.
     * 
     * @param event CachewideEvent
     * @see com.opensymphony.oscache.base.events.CacheEntryEventListener#cacheFlushed(CachewideEvent)
     */
    public void cacheFlushed(CachewideEvent event) {
        flushed("wide " + event.getDate());
    }

    /**
     * Return the counters in a string form.
     * 
     * @return String
     */
    public String toString() {
        return "StatisticListenerImpl: Hit = " + hitCount + " / " + hitCountSum
                + ", stale hit = " + staleHitCount + " / " + staleHitCountSum
                + ", miss = " + missCount + " / " + missCountSum + ", flush = "
                + flushCount + ", entries (added, removed, updates) = " 
                + entriesAdded + ", " + entriesRemoved + ", " + entriesUpdated;
    }

    /**
     * @return Returns the entriesAdded.
     */
    public int getEntriesAdded() {
        return entriesAdded;
    }

    /**
     * @return Returns the entriesRemoved.
     */
    public int getEntriesRemoved() {
        return entriesRemoved;
    }

    /**
     * @return Returns the entriesUpdated.
     */
    public int getEntriesUpdated() {
        return entriesUpdated;
    }

    /**
     * @return Returns the flushCount.
     */
    public int getFlushCount() {
        return flushCount;
    }

    /**
     * @return Returns the hitCount.
     */
    public int getHitCount() {
        return hitCount;
    }

    /**
     * @return Returns the hitCountSum.
     */
    public int getHitCountSum() {
        return hitCountSum;
    }

    /**
     * @return Returns the missCount.
     */
    public int getMissCount() {
        return missCount;
    }

    /**
     * @return Returns the missCountSum.
     */
    public int getMissCountSum() {
        return missCountSum;
    }

    /**
     * @return Returns the staleHitCount.
     */
    public int getStaleHitCount() {
        return staleHitCount;
    }

    /**
     * @return Returns the staleHitCountSum.
     */
    public int getStaleHitCountSum() {
        return staleHitCountSum;
    }
}
