/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.plugins.clustersupport;

import com.opensymphony.oscache.base.*;
import com.opensymphony.oscache.base.events.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

/**
 * Implementation of a CacheEntryEventListener. It broadcasts the flush events
 * across a cluster to other listening caches. Note that this listener cannot
 * be used in conjection with session caches.
 *
 * @version        $Revision: 254 $
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public abstract class AbstractBroadcastingListener implements CacheEntryEventListener, LifecycleAware {
    private final static Log log = LogFactory.getLog(AbstractBroadcastingListener.class);

    /**
     * The name to use for the origin of cluster events. Using this ensures
     * events are not fired recursively back over the cluster.
     */
    protected static final String CLUSTER_ORIGIN = "CLUSTER";
    protected Cache cache = null;

    public AbstractBroadcastingListener() {
        if (log.isInfoEnabled()) {
            log.info("AbstractBroadcastingListener registered");
        }
    }

    /**
     * Event fired when an entry is flushed from the cache. This broadcasts
     * the flush message to any listening nodes on the network.
     */
    public void cacheEntryFlushed(CacheEntryEvent event) {
        if (!Cache.NESTED_EVENT.equals(event.getOrigin()) && !CLUSTER_ORIGIN.equals(event.getOrigin())) {
            if (log.isDebugEnabled()) {
                log.debug("cacheEntryFlushed called (" + event + ")");
            }

            sendNotification(new ClusterNotification(ClusterNotification.FLUSH_KEY, event.getKey()));
        }
    }

    /**
     * Event fired when an entry is removed from the cache. This broadcasts
     * the remove method to any listening nodes on the network, as long as
     * this event wasn't from a broadcast in the first place.
     */
    public void cacheGroupFlushed(CacheGroupEvent event) {
        if (!Cache.NESTED_EVENT.equals(event.getOrigin()) && !CLUSTER_ORIGIN.equals(event.getOrigin())) {
            if (log.isDebugEnabled()) {
                log.debug("cacheGroupFushed called (" + event + ")");
            }

            sendNotification(new ClusterNotification(ClusterNotification.FLUSH_GROUP, event.getGroup()));
        }
    }

    public void cachePatternFlushed(CachePatternEvent event) {
        if (!Cache.NESTED_EVENT.equals(event.getOrigin()) && !CLUSTER_ORIGIN.equals(event.getOrigin())) {
            if (log.isDebugEnabled()) {
                log.debug("cachePatternFushed called (" + event + ")");
            }

            sendNotification(new ClusterNotification(ClusterNotification.FLUSH_PATTERN, event.getPattern()));
        }
    }

    public void cacheFlushed(CachewideEvent event) {
        if (!Cache.NESTED_EVENT.equals(event.getOrigin()) && !CLUSTER_ORIGIN.equals(event.getOrigin())) {
            if (log.isDebugEnabled()) {
                log.debug("cacheFushed called (" + event + ")");
            }

            sendNotification(new ClusterNotification(ClusterNotification.FLUSH_CACHE, event.getDate()));
        }
    }

    // --------------------------------------------------------
    // The remaining events are of no interest to this listener
    // --------------------------------------------------------
    public void cacheEntryAdded(CacheEntryEvent event) {
    }

    public void cacheEntryRemoved(CacheEntryEvent event) {
    }

    public void cacheEntryUpdated(CacheEntryEvent event) {
    }

    public void cacheGroupAdded(CacheGroupEvent event) {
    }

    public void cacheGroupEntryAdded(CacheGroupEvent event) {
    }

    public void cacheGroupEntryRemoved(CacheGroupEvent event) {
    }

    public void cacheGroupRemoved(CacheGroupEvent event) {
    }

    public void cacheGroupUpdated(CacheGroupEvent event) {
    }

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
    public void initialize(Cache cache, Config config) throws InitializationException {
        this.cache = cache;
    }

    /**
     * Handles incoming notification messages. This method should be called by the
     * underlying broadcasting implementation when a message is received from another
     * node in the cluster.
     *
     * @param message The incoming cluster notification message object.
     */
    public void handleClusterNotification(ClusterNotification message) {
        if (cache == null) {
            log.warn("A cluster notification (" + message + ") was received, but no cache is registered on this machine. Notification ignored.");

            return;
        }

        if (log.isInfoEnabled()) {
            log.info("Cluster notification (" + message + ") was received.");
        }

        switch (message.getType()) {
            case ClusterNotification.FLUSH_KEY:
                cache.flushEntry((String) message.getData(), CLUSTER_ORIGIN);
                break;
            case ClusterNotification.FLUSH_GROUP:
                cache.flushGroup((String) message.getData(), CLUSTER_ORIGIN);
                break;
            case ClusterNotification.FLUSH_PATTERN:
                cache.flushPattern((String) message.getData(), CLUSTER_ORIGIN);
                break;
            case ClusterNotification.FLUSH_CACHE:
                cache.flushAll((Date) message.getData(), CLUSTER_ORIGIN);
                break;
            default:
                log.error("The cluster notification (" + message + ") is of an unknown type. Notification ignored.");
        }
    }

    /**
     * Called when a cluster notification message is to be broadcast. Implementing
     * classes should use their underlying transport to broadcast the message across
     * the cluster.
     *
     * @param message The notification message to broadcast.
     */
    abstract protected void sendNotification(ClusterNotification message);
}
