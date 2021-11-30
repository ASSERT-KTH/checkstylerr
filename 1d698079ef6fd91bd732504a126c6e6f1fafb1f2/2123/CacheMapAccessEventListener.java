/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;


/**
 * This is the interface to listen to cache map access events. The events are
 * cache hits and misses, and are dispatched through this interface
 *
 * @version        $Revision: 254 $
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 */
public interface CacheMapAccessEventListener extends CacheEventListener {
    /**
     * Event fired when an entry is accessed.
     * Use getEventType to differentiate between access events.
     */
    public void accessed(CacheMapAccessEvent event);
}
