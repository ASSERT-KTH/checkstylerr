/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web.filter;

import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.EntryRefreshPolicy;
import com.opensymphony.oscache.base.NeedsRefreshException;

/**
 * Checks if a cache filter entry has expired.
 * This is useful when expires header are used in the response.
 *
 * @version $Revision: 411 $
 * @author <a href="mailto:ltorunski [ AT ] t-online.de">Lars Torunski</a>
 */
public class ExpiresRefreshPolicy implements EntryRefreshPolicy {
    
    /** the refresh period (in milliseconds) of a certain cache filter*/
    private long refreshPeriod;

    /**
     * Constructor ExpiresRefreshPolicy.
     *
     * @param refreshPeriod the refresh period in seconds
     */
    public ExpiresRefreshPolicy(int refreshPeriod) {
        this.refreshPeriod = refreshPeriod * 1000L;
    }
    
    /**
     * Indicates whether the supplied <code>CacheEntry</code> needs to be refreshed.
     * This will be called when retrieving an entry from the cache - if this method
     * returns <code>true</code> then a <code>NeedsRefreshException</code> will be
     * thrown.
     *
     * @param entry The cache entry which is ignored.
     * @return <code>true</code> if the content needs refreshing, <code>false</code> otherwise.
     *
     * @see NeedsRefreshException
     * @see CacheEntry
     */
    public boolean needsRefresh(CacheEntry entry) {
        
        long currentTimeMillis = System.currentTimeMillis();
        
        if ((refreshPeriod >= 0) && (currentTimeMillis >= (entry.getLastUpdate() + refreshPeriod))) {
            return true;
        } else if (entry.getContent() instanceof ResponseContent) {
            ResponseContent responseContent = (ResponseContent) entry.getContent();
            return currentTimeMillis >= responseContent.getExpires();
        } else {
            return false;
        }
        
    }

    /**
     * @return the refreshPeriod in seconds
     * @since 2.4
     */
    public long getRefreshPeriod() {
        return refreshPeriod / 1000;
    }

    /**
     * @param refreshPeriod the refresh period in seconds
     * @since 2.4
     */
    public void setRefreshPeriod(long refreshPeriod) {
        this.refreshPeriod = refreshPeriod * 1000L;
    }
    
}
