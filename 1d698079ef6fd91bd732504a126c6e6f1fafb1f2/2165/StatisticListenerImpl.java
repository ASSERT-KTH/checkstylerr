/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.extra;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.events.*;

/**
 * A simple implementation of a statistic reporter which uses the
 * CacheMapAccessEventListener, CacheEntryEventListener and ScopeEventListener.
 * It uses the events to count the cache hit and misses and of course the
 * flushes.
 * <p>
 * We are not using any synchronized so that this does not become a bottleneck.
 * The consequence is that on retrieving values, the operations that are
 * currently being done won't be counted.
 */
public class StatisticListenerImpl implements CacheMapAccessEventListener, CacheEntryEventListener, ScopeEventListener {

    private static transient final Log log = LogFactory.getLog(StatisticListenerImpl.class);

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
     * Hit counter sum
     */
    private int hitCountSum = 0;

    /**
     * Miss counter sum
     */
    private int missCountSum = 0;

    /**
     * Stale hit counter
     */
    private int staleHitCountSum = 0;

    /**
     * Flush hit counter
     */
    private int flushCount = 0;

    /**
     * Constructor, empty for us
     */
    public StatisticListenerImpl() {
        log.info("Creation of StatisticListenerImpl");
    }

    /**
     * This method handles an event each time the cache is accessed
     * 
     * @param event The event triggered when the cache was accessed
     * @see com.opensymphony.oscache.base.events.CacheMapAccessEventListener#accessed(CacheMapAccessEvent)
     */
    public void accessed(CacheMapAccessEvent event) {
        String result = "N/A";

        // Retrieve the event type and update the counters
        CacheMapAccessEventType type = event.getEventType();

        // Handles a hit event
        if (type == CacheMapAccessEventType.HIT) {
            hitCount++;
            result = "HIT";
        }
        // Handles a stale hit event
        else if (type == CacheMapAccessEventType.STALE_HIT) {
            staleHitCount++;
            result = "STALE HIT";
        }
        // Handles a miss event
        else if (type == CacheMapAccessEventType.MISS) {
            missCount++;
            result = "MISS";
        }

        if (log.isDebugEnabled()) {
            log.debug("ACCESS : " + result + ": " + event.getCacheEntryKey());
            log.debug("STATISTIC : Hit = " + hitCount + ", stale hit ="
                    + staleHitCount + ", miss = " + missCount);
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

        if (log.isInfoEnabled()) {
            log.info("FLUSH : " + info);
            log.info("STATISTIC SUM : " + "Hit = " + hitCountSum
                    + ", stale hit = " + staleHitCountSum + ", miss = "
                    + missCountSum + ", flush = " + flushCount);
        }

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
        // do nothing
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
        // do nothing
    }

    /**
     * Event fired when an entry is updated in the cache.
     * 
     * @param event CacheEntryEvent
     * @see com.opensymphony.oscache.base.events.CacheEntryEventListener#cacheEntryUpdated(CacheEntryEvent)
     */
    public void cacheEntryUpdated(CacheEntryEvent event) {
        // do nothing
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
     * Return the counters in a string form
     *
     * @return String
     */
    public String toString() {
        return "StatisticListenerImpl: Hit = " + hitCount + " / " + hitCountSum
                + ", stale hit = " + staleHitCount + " / " + staleHitCountSum
                + ", miss = " + missCount + " / " + missCountSum + ", flush = "
                + flushCount;
    }
}