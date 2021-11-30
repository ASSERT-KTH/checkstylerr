/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;


/**
 * Event handlers implement this so they can be notified when a cache
 * is created and also when it is destroyed. This allows event handlers
 * to load any configuration and/or resources they need on startup and
 * then release them again when the cache is shut down.
 *
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 *
 * @see com.opensymphony.oscache.base.events.CacheEventListener
 */
public interface LifecycleAware {
    /**
    * Called by the cache administrator class when a cache is instantiated.
    *
    * @param cache the cache instance that this listener is attached to.
    * @param config The cache's configuration details. This allows the event handler
    * to initialize itself based on the cache settings, and also to receive <em>additional</em>
    * settings that were part of the cache configuration but that the cache
    * itself does not care about. If you are using <code>cache.properties</code>
    * for your configuration, simply add any additional properties that your event
    * handler requires and they will be passed through in this parameter.
    *
    * @throws InitializationException thrown when there was a problem initializing the
    * listener. The cache administrator will log this error and disable the listener.
    */
    public void initialize(Cache cache, Config config) throws InitializationException;

    /**
    * Called by the cache administrator class when a cache is destroyed.
    *
    * @throws FinalizationException thrown when there was a problem finalizing the
    * listener. The cache administrator will catch and log this error.
    */
    public void finialize() throws FinalizationException;
}
