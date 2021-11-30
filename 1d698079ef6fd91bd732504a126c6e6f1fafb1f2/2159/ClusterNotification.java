/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.plugins.clustersupport;

import java.io.Serializable;

/**
 * A notification message that holds information about a cache event. This
 * class is <code>Serializable</code> to allow it to be sent across the
 * network to other machines running in a cluster.
 *
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 * @author $Author: dres $
 * @version $Revision: 254 $
 */
public class ClusterNotification implements Serializable {
    /**
     * Specifies a notification message that indicates a particular cache key
     * should be flushed.
     */
    public static final int FLUSH_KEY = 1;

    /**
     * Specifies a notification message that indicates an entire cache group
     * should be flushed.
     */
    public static final int FLUSH_GROUP = 2;

    /**
     * Specifies a notification message that indicates all entries in the cache
     * that match the specified pattern should be flushed.
     */
    public static final int FLUSH_PATTERN = 3;

    /**
     * Specifies a notification message indicating that an entire cache should
     * be flushed.
     */
    public static final int FLUSH_CACHE = 4;

    /**
     * Any additional data that may be required
     */
    protected Serializable data;

    /**
     * The type of notification message.
     */
    protected int type;

    /**
     * Creates a new notification message object to broadcast to other
     * listening nodes in the cluster.
     *
     * @param type       The type of notification message. Valid types are
     *                   {@link #FLUSH_KEY} and {@link #FLUSH_GROUP}.
     * @param data       Specifies the object key or group name to flush.
     */
    public ClusterNotification(int type, Serializable data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Holds any additional data that was required
     */
    public Serializable getData() {
        return data;
    }

    /**
     * The type of notification message.
     */
    public int getType() {
        return type;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("type=").append(type).append(", data=").append(data);

        return buf.toString();
    }
}
