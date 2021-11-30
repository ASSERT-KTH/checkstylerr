/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

import com.opensymphony.oscache.base.algorithm.AbstractConcurrentReadCache;
import com.opensymphony.oscache.base.events.*;
import com.opensymphony.oscache.base.persistence.PersistenceListener;
import com.opensymphony.oscache.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

import javax.swing.event.EventListenerList;

/**
 * An AbstractCacheAdministrator defines an abstract cache administrator, implementing all
 * the basic operations related to the configuration of a cache, including assigning
 * any configured event handlers to cache objects.<p>
 *
 * Extend this class to implement a custom cache administrator.
 *
 * @version        $Revision: 425 $
 * @author        a href="mailto:mike@atlassian.com">Mike Cannon-Brookes</a>
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 * @author <a href="mailto:fabian.crabus@gurulogic.de">Fabian Crabus</a>
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public abstract class AbstractCacheAdministrator implements java.io.Serializable {
    private static transient final Log log = LogFactory.getLog(AbstractCacheAdministrator.class);

    /**
     * A boolean cache configuration property that indicates whether the cache
     * should cache objects in memory. Set this property to <code>false</code>
     * to disable in-memory caching.
     */
    public final static String CACHE_MEMORY_KEY = "cache.memory";

    /**
     * An integer cache configuration property that specifies the maximum number
     * of objects to hold in the cache. Setting this to a negative value will
     * disable the capacity functionality - there will be no limit to the number
     * of objects that are held in cache.
     */
    public final static String CACHE_CAPACITY_KEY = "cache.capacity";

    /**
     * A String cache configuration property that specifies the classname of
     * an alternate caching algorithm. This class must extend
     * {@link com.opensymphony.oscache.base.algorithm.AbstractConcurrentReadCache}
     * By default caches will use {@link com.opensymphony.oscache.base.algorithm.LRUCache} as
     * the default algorithm if the cache capacity is set to a postive value, or
     * {@link com.opensymphony.oscache.base.algorithm.UnlimitedCache} if the
     * capacity is negative (ie, disabled).
     */
    public final static String CACHE_ALGORITHM_KEY = "cache.algorithm";

    /**
     * A boolean cache configuration property that indicates whether the persistent
     * cache should be unlimited in size, or should be restricted to the same size
     * as the in-memory cache. Set this property to <code>true</code> to allow the
     * persistent cache to grow without bound.
     */
    public final static String CACHE_DISK_UNLIMITED_KEY = "cache.unlimited.disk";

    /**
     * The configuration key that specifies whether we should block waiting for new
     * content to be generated, or just serve the old content instead. The default
     * behaviour is to serve the old content since that provides the best performance
     * (at the cost of serving slightly stale data).
     */
    public final static String CACHE_BLOCKING_KEY = "cache.blocking";

    /**
     * A String cache configuration property that specifies the classname that will
     * be used to provide cache persistence. This class must extend {@link PersistenceListener}.
     */
    public static final String PERSISTENCE_CLASS_KEY = "cache.persistence.class";

    /**
     * A String cache configuration property that specifies if the cache persistence
     * will only be used in overflow mode, that is, when the memory cache capacity has been reached.
     */
    public static final String CACHE_PERSISTENCE_OVERFLOW_KEY = "cache.persistence.overflow.only";

    /**
     * A String cache configuration property that holds a comma-delimited list of
     * classnames. These classes specify the event handlers that are to be applied
     * to the cache.
     */
    public static final String CACHE_ENTRY_EVENT_LISTENERS_KEY = "cache.event.listeners";
    protected Config config = null;

    /**
     * Holds a list of all the registered event listeners. Event listeners are specified
     * using the {@link #CACHE_ENTRY_EVENT_LISTENERS_KEY} configuration key.
     */
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * The algorithm class being used, as specified by the {@link #CACHE_ALGORITHM_KEY}
     * configuration property.
     */
    protected String algorithmClass = null;

    /**
     * The cache capacity (number of entries), as specified by the {@link #CACHE_CAPACITY_KEY}
     * configuration property.
     */
    protected int cacheCapacity = -1;

    /**
     * Whether the cache blocks waiting for content to be build, or serves stale
     * content instead. This value can be specified using the {@link #CACHE_BLOCKING_KEY}
     * configuration property.
     */
    private boolean blocking = false;

    /**
     * Whether or not to store the cache entries in memory. This is configurable using the
     * {@link com.opensymphony.oscache.base.AbstractCacheAdministrator#CACHE_MEMORY_KEY} property.
     */
    private boolean memoryCaching = true;

    /**
     * Whether the persistent cache should be used immediately or only when the memory capacity
         * has been reached, ie. overflow only.
     * This can be set via the {@link #CACHE_PERSISTENCE_OVERFLOW_KEY} configuration property.
     */
    private boolean overflowPersistence;

    /**
     * Whether the disk cache should be unlimited in size, or matched 1-1 to the memory cache.
     * This can be set via the {@link #CACHE_DISK_UNLIMITED_KEY} configuration property.
     */
    private boolean unlimitedDiskCache;

    /**
     * Create the AbstractCacheAdministrator.
     * This will initialize all values and load the properties from oscache.properties.
     */
    protected AbstractCacheAdministrator() {
        this(null);
    }

    /**
     * Create the AbstractCacheAdministrator.
     *
     * @param p the configuration properties for this cache.
     */
    protected AbstractCacheAdministrator(Properties p) {
        loadProps(p);
        initCacheParameters();

        if (log.isDebugEnabled()) {
            log.debug("Constructed AbstractCacheAdministrator()");
        }
    }

    /**
     * Sets the algorithm to use for the cache.
     *
     * @see com.opensymphony.oscache.base.algorithm.LRUCache
     * @see com.opensymphony.oscache.base.algorithm.FIFOCache
     * @see com.opensymphony.oscache.base.algorithm.UnlimitedCache
     * @param newAlgorithmClass The class to use (eg.
     * <code>"com.opensymphony.oscache.base.algorithm.LRUCache"</code>)
     */
    public void setAlgorithmClass(String newAlgorithmClass) {
        algorithmClass = newAlgorithmClass;
    }

    /**
     * Indicates whether the cache will block waiting for new content to
     * be built, or serve stale content instead of waiting. Regardless of this
     * setting, the cache will <em>always</em> block if new content is being
     * created, ie, there's no stale content in the cache that can be served.
     */
    public boolean isBlocking() {
        return blocking;
    }

    /**
     * Sets the cache capacity (number of items). Administrator implementations
     * should override this method to ensure that their {@link Cache} objects
     * are updated correctly (by calling {@link AbstractConcurrentReadCache#setMaxEntries(int)}}}.
     *
     * @param newCacheCapacity The new capacity
     */
    protected void setCacheCapacity(int newCacheCapacity) {
        cacheCapacity = newCacheCapacity;
    }

    /**
     * Whether entries are cached in memory or not.
     * Default is true.
     * Set by the <code>cache.memory</code> property.
     *
     * @return Status whether or not memory caching is used.
     */
    public boolean isMemoryCaching() {
        return memoryCaching;
    }

    /**
     * Retrieves the value of one of the configuration properties.
     *
     * @param key The key assigned to the property
     * @return Property value, or <code>null</code> if the property could not be found.
     */
    public String getProperty(String key) {
        return config.getProperty(key);
    }

    /**
     * Indicates whether the unlimited disk cache is enabled or not.
     */
    public boolean isUnlimitedDiskCache() {
        return unlimitedDiskCache;
    }

    /**
     * Check if we use overflowPersistence
     *
     * @return Returns the overflowPersistence.
     */
    public boolean isOverflowPersistence() {
        return this.overflowPersistence;
    }

    /**
     * Sets the overflowPersistence flag
     *
     * @param overflowPersistence The overflowPersistence to set.
     */
    public void setOverflowPersistence(boolean overflowPersistence) {
        this.overflowPersistence = overflowPersistence;
    }

    /**
     * Retrieves an array containing instances all of the {@link CacheEventListener}
     * classes that are specified in the OSCache configuration file.
     */
    protected CacheEventListener[] getCacheEventListeners() {
        List classes = StringUtil.split(config.getProperty(CACHE_ENTRY_EVENT_LISTENERS_KEY), ',');
        CacheEventListener[] listeners = new CacheEventListener[classes.size()];

        for (int i = 0; i < classes.size(); i++) {
            String className = (String) classes.get(i);

            try {
                Class clazz = Class.forName(className);

                if (!CacheEventListener.class.isAssignableFrom(clazz)) {
                    log.error("Specified listener class '" + className + "' does not implement CacheEventListener. Ignoring this listener.");
                } else {
                    listeners[i] = (CacheEventListener) clazz.newInstance();
                }
            } catch (ClassNotFoundException e) {
                log.error("CacheEventListener class '" + className + "' not found. Ignoring this listener.", e);
            } catch (InstantiationException e) {
                log.error("CacheEventListener class '" + className + "' could not be instantiated because it is not a concrete class. Ignoring this listener.", e);
            } catch (IllegalAccessException e) {
                log.error("CacheEventListener class '" + className + "' could not be instantiated because it is not public. Ignoring this listener.", e);
            }
        }

        return listeners;
    }

    /**
     * If there is a <code>PersistenceListener</code> in the configuration
     * it will be instantiated and applied to the given cache object. If the
     * <code>PersistenceListener</code> cannot be found or instantiated, an
     * error will be logged but the cache will not have a persistence listener
     * applied to it and no exception will be thrown.<p>
     *
     * A cache can only have one <code>PersistenceListener</code>.
     *
     * @param cache the cache to apply the <code>PersistenceListener</code> to.
     *
     * @return the same cache object that was passed in.
     */
    protected Cache setPersistenceListener(Cache cache) {
        String persistenceClassname = config.getProperty(PERSISTENCE_CLASS_KEY);

        try {
            Class clazz = Class.forName(persistenceClassname);
            PersistenceListener persistenceListener = (PersistenceListener) clazz.newInstance();

            cache.setPersistenceListener(persistenceListener.configure(config));
        } catch (ClassNotFoundException e) {
            log.error("PersistenceListener class '" + persistenceClassname + "' not found. Check your configuration.", e);
        } catch (Exception e) {
            log.error("Error instantiating class '" + persistenceClassname + "'", e);
        }

        return cache;
    }

    /**
     * Applies all of the recognised listener classes to the supplied
     * cache object. Recognised classes are {@link CacheEntryEventListener}
     * and {@link CacheMapAccessEventListener}.<p>
     *
     * @param cache The cache to apply the configuration to.
     * @return cache The configured cache object.
     */
    protected Cache configureStandardListeners(Cache cache) {
        if (config.getProperty(PERSISTENCE_CLASS_KEY) != null) {
            cache = setPersistenceListener(cache);
        }

        if (config.getProperty(CACHE_ENTRY_EVENT_LISTENERS_KEY) != null) {
            // Grab all the specified listeners and add them to the cache's
            // listener list. Note that listeners that implement more than
            // one of the event interfaces will be added multiple times.
            CacheEventListener[] listeners = getCacheEventListeners();

            for (int i = 0; i < listeners.length; i++) {
                // Pass through the configuration to those listeners that require it
                if (listeners[i] instanceof LifecycleAware) {
                    try {
                        ((LifecycleAware) listeners[i]).initialize(cache, config);
                    } catch (InitializationException e) {
                        log.error("Could not initialize listener '" + listeners[i].getClass().getName() + "'. Listener ignored.", e);

                        continue;
                    }
                }

                if (listeners[i] instanceof CacheEntryEventListener) {
                    cache.addCacheEventListener(listeners[i]);
                } else if (listeners[i] instanceof CacheMapAccessEventListener) {
                    cache.addCacheEventListener(listeners[i]);
                }
            }
        }

        return cache;
    }

    /**
     * Finalizes all the listeners that are associated with the given cache object.
     * Any <code>FinalizationException</code>s that are thrown by the listeners will
     * be caught and logged.
     */
    protected void finalizeListeners(Cache cache) {
        // It's possible for cache to be null if getCache() was never called (CACHE-63)
        if (cache == null) {
            return;
        }

        Object[] listeners = cache.listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i + 1] instanceof LifecycleAware) {
                try {
                    ((LifecycleAware) listeners[i + 1]).finialize();
                } catch (FinalizationException e) {
                    log.error("Listener could not be finalized", e);
                }
            }
        }
    }

    /**
     * Initialize the core cache parameters from the configuration properties.
     * The parameters that are initialized are:
     * <ul>
     * <li>the algorithm class ({@link #CACHE_ALGORITHM_KEY})</li>
     * <li>the cache size ({@link #CACHE_CAPACITY_KEY})</li>
     * <li>whether the cache is blocking or non-blocking ({@link #CACHE_BLOCKING_KEY})</li>
     * <li>whether caching to memory is enabled ({@link #CACHE_MEMORY_KEY})</li>
     * <li>whether the persistent cache is unlimited in size ({@link #CACHE_DISK_UNLIMITED_KEY})</li>
     * </ul>
     */
    private void initCacheParameters() {
        algorithmClass = getProperty(CACHE_ALGORITHM_KEY);

        blocking = "true".equalsIgnoreCase(getProperty(CACHE_BLOCKING_KEY));

        String cacheMemoryStr = getProperty(CACHE_MEMORY_KEY);

        if ((cacheMemoryStr != null) && cacheMemoryStr.equalsIgnoreCase("false")) {
            memoryCaching = false;
        }

        unlimitedDiskCache = Boolean.valueOf(config.getProperty(CACHE_DISK_UNLIMITED_KEY)).booleanValue();
        overflowPersistence = Boolean.valueOf(config.getProperty(CACHE_PERSISTENCE_OVERFLOW_KEY)).booleanValue();

        String cacheSize = getProperty(CACHE_CAPACITY_KEY);

        try {
            if ((cacheSize != null) && (cacheSize.length() > 0)) {
                cacheCapacity = Integer.parseInt(cacheSize);
            }
        } catch (NumberFormatException e) {
            log.error("The value supplied for the cache capacity, '" + cacheSize + "', is not a valid number. The cache capacity setting is being ignored.");
        }
    }

    /**
     * Load the properties file from the classpath.
     */
    private void loadProps(Properties p) {
        config = new Config(p);
    }
}
