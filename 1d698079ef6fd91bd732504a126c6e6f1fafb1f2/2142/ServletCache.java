/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.CacheEntry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

/**
 * A simple extension of Cache that implements a session binding listener,
 * and deletes it's entries when unbound
 *
 * @author        <a href="mailto:mike@atlassian.com">Mike Cannon-Brookes</a>
 * @author        <a href="mailto:tgochenour@peregrine.com">Todd Gochenour</a>
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @version        $Revision: 339 $
 */
public final class ServletCache extends Cache implements HttpSessionBindingListener, Serializable {
    private static transient final Log log = LogFactory.getLog(ServletCache.class);

    /**
     * The admin for this cache
     */
    private ServletCacheAdministrator admin;

    /**
     * The scope of that cache.
     */
    private int scope;

    /**
     * Create a new ServletCache
     *
     * @param admin The ServletCacheAdministrator to administer this ServletCache.
     * @param scope The scope of all entries in this hashmap
     */
    public ServletCache(ServletCacheAdministrator admin, int scope) {
        super(admin.isMemoryCaching(), admin.isUnlimitedDiskCache(), admin.isOverflowPersistence());
        setScope(scope);
        this.admin = admin;
    }

    /**
     * Create a new Cache
     *
     * @param admin The CacheAdministrator to administer this Cache.
     * @param algorithmClass The class that implement an algorithm
     * @param limit The maximum cache size in number of entries
     * @param scope The cache scope
     */
    public ServletCache(ServletCacheAdministrator admin, String algorithmClass, int limit, int scope) {
        super(admin.isMemoryCaching(), admin.isUnlimitedDiskCache(), admin.isOverflowPersistence(), admin.isBlocking(), algorithmClass, limit);
        setScope(scope);
        this.admin = admin;
    }

    /**
     * Get the cache scope
     *
     * @return The cache scope
     */
    public int getScope() {
        return scope;
    }

    private void setScope(int scope) {
        this.scope = scope;
    }

    /**
     * When this Cache is bound to the session, do nothing.
     *
     * @param event The SessionBindingEvent.
     */
    public void valueBound(HttpSessionBindingEvent event) {
    }

    /**
     * When the users's session ends, all listeners are finalized and the
     * session cache directory is deleted from disk.
     *
     * @param event The event that triggered this unbinding.
     */
    public void valueUnbound(HttpSessionBindingEvent event) {
        if (log.isInfoEnabled()) {
        	// CACHE-229: don't access the session's id, because this can throw an IllegalStateException
            log.info("[Cache] Unbound from session " + event.getSession() + " using name " + event.getName());
        }

        admin.finalizeListeners(this);
        clear();
    }

    /**
     * Indicates whether or not the cache entry is stale. This overrides the
     * {@link Cache#isStale(CacheEntry, int, String)} method to take into account any
     * flushing that may have been applied to the scope that this cache belongs to.
     *
     * @param cacheEntry     The cache entry to test the freshness of.
     * @param refreshPeriod  The maximum allowable age of the entry, in seconds.
     * @param cronExpiry     A cron expression that defines fixed expiry dates and/or
     * times for this cache entry.
     *
     * @return <code>true</code> if the entry is stale, <code>false</code> otherwise.
     */
    protected boolean isStale(CacheEntry cacheEntry, int refreshPeriod, String cronExpiry) {
        return super.isStale(cacheEntry, refreshPeriod, cronExpiry) || admin.isScopeFlushed(cacheEntry, scope);
    }
}
