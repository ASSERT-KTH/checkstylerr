/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;


/**
 * The root event class for all cache events. Each subclasses of this class
 * classifies a particular type of cache event.
 *
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 * Date: 20-May-2003
 * Time: 15:25:02
 */
public abstract class CacheEvent {
    /**
     * An optional tag that can be attached to the event to specify the event's origin.
     */
    protected String origin = null;

    /**
     * No-argument constructor so subtypes can easily implement <code>Serializable</code>
     */
    public CacheEvent() {
    }

    /**
     * Creates a cache event object that came from the specified origin.
     *
     * @param origin A string that indicates where this event was fired from.
     * This value is optional; <code>null</code> can be passed in if an
     * origin is not required.
     */
    public CacheEvent(String origin) {
        this.origin = origin;
    }

    /**
     * Retrieves the origin of this event, if one was specified. This is most
     * useful when an event handler causes another event to fire - by checking
     * the origin the handler is able to prevent recursive events being
     * fired.
     */
    public String getOrigin() {
        return origin;
    }
}
