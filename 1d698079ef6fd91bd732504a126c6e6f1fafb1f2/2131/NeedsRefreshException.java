/*
 * Copyright (c) 2002-2007 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

/**
 * This exception is thrown when retrieving an item from cache and it is
 * expired.
 * Note that for fault tolerance purposes, it is possible to retrieve the
 * current cached object from the exception.
 *
 * <p>January, 2004 - The OSCache developers are aware of the fact that throwing
 * an exception for a perfect valid situation (cache miss) is design smell. This will
 * be removed in the near future, and other means of refreshing the cache will be
 * provided.</p>
 *
 * @author        <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @version        $Revision: 433 $
 */
public final class NeedsRefreshException extends Exception {

    /**
     * Current object in the cache
     */
    private Object cacheContent = null;
    
    /**
     * Create a NeedsRefreshException
     */
    public NeedsRefreshException(String message, Object cacheContent) {
        super(message);
        this.cacheContent = cacheContent;
    }

    /**
     * Create a NeedsRefreshException
     */
    public NeedsRefreshException(Object cacheContent) {
        super();
        this.cacheContent = cacheContent;
    }
    
    /**
     * Retrieve current object in the cache
     */
    public Object getCacheContent() {
        return cacheContent;
    }

}
