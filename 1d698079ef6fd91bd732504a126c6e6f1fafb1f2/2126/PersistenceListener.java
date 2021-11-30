/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.persistence;

import com.opensymphony.oscache.base.Config;

import java.util.Set;

/**
 * Defines the methods that are required to persist cache data.
 * To provide a custom persistence mechanism you should implement this
 * interface and supply the fully-qualified classname to the cache via
 * the <code>cache.persistence.class</code> configuration property.
 *
 * @version        $Revision: 254 $
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 */
public interface PersistenceListener {
    /**
     * Verify if an object is currently stored in the persistent cache.
     *
     * @param key The cache key of the object to check.
     */
    public boolean isStored(String key) throws CachePersistenceException;

    /**
     * Verify if a group is currently stored in the persistent cache.
     *
     * @param groupName The name of the group to check.
     */
    public boolean isGroupStored(String groupName) throws CachePersistenceException;

    /**
     * Clear the entire persistent cache (including the root)
     */
    public void clear() throws CachePersistenceException;

    /**
     * Allow the persistence code to initialize itself based on the supplied
     * cache configuration.
     */
    public PersistenceListener configure(Config config);

    /**
     * Removes an object from the persistent cache
     */
    public void remove(String key) throws CachePersistenceException;

    /**
     * Removes a group from the persistent cache.
     *
     * @param groupName The name of the cache group to remove.
     */
    public void removeGroup(String groupName) throws CachePersistenceException;

    /**
     * Retrieves an object from the persistent cache.
     *
     * @param key The unique cache key that maps to the object
     * being retrieved.
     * @return The object, or <code>null</code> if no object was found
     * matching the supplied key.
     */
    public Object retrieve(String key) throws CachePersistenceException;

    /**
     * Stores an object in the persistent cache.
     *
     * @param key The key to uniquely identify this object.
     * @param obj The object to persist. Most implementations
     * of this interface will require this object implements
     * <code>Serializable</code>.
     */
    public void store(String key, Object obj) throws CachePersistenceException;

    /**
     * Stores a group in the persistent cache.
     *
     * @param groupName The name of the group to persist.
     * @param group A set containing the keys of all the <code>CacheEntry</code>
     * objects that belong to this group.
     */
    public void storeGroup(String groupName, Set group) throws CachePersistenceException;

    /**
     * Retrieves a group from the persistent cache.
     *
     * @param groupName The name of the group to retrieve.
     * @return The returned set should contain the keys
     * of all the <code>CacheEntry</code> objects that belong
     * to this group.
     */
    Set retrieveGroup(String groupName) throws CachePersistenceException;
}
