/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;


/**
 * This is the interface to listen to scope events. The events are
 * scope flushed and all scope flushed, and are dispatched thru this interface
 *
 * @version        $Revision: 254 $
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 */
public interface ScopeEventListener extends CacheEventListener {
    /**
     * Event fired when a specific or all scopes are flushed.
     * Use getEventType to differentiate between the two.
     */
    public void scopeFlushed(ScopeEvent event);
}
