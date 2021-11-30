/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;


/**
 * This is an enumeration holding all the events that can
 * occur at the cache-wide level.
 *
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public class CachewideEventType {
    /**
     * Get an event type for a cache flush event.
     */
    public static final CachewideEventType CACHE_FLUSHED = new CachewideEventType();

    /**
     * Private constructor to ensure that no object of this type are
     * created externally.
     */
    private CachewideEventType() {
    }
}
