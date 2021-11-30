/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

import java.io.Serializable;

/**
 * Interface that allows custom code to be called when checking to see if a cache entry
 * has expired. This is useful when the rules that determine when content needs refreshing
 * are beyond the base funtionality offered by OSCache.
 *
 * @version        $Revision: 254 $
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 */
public interface EntryRefreshPolicy extends Serializable {
    /**
     * Indicates whether the supplied <code>CacheEntry</code> needs to be refreshed.
     * This will be called when retrieving an entry from the cache - if this method
     * returns <code>true</code> then a <code>NeedsRefreshException</code> will be
     * thrown.
     *
     * @param entry The cache entry that is being tested.
     * @return <code>true</code> if the content needs refreshing, <code>false</code> otherwise.
     *
     * @see NeedsRefreshException
     * @see CacheEntry
     */
    public boolean needsRefresh(CacheEntry entry);
}
