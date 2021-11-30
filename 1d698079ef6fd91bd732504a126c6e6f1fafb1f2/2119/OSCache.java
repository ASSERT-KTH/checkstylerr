package com.opensymphony.oscache.hibernate;

import java.util.Map;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.Timestamper;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

/**
 * Cache plugin for Hibernate 3.2 and OpenSymphony OSCache 2.4.
 * <p/>
 * The OSCache implementation assumes that identifiers have well-behaved <tt>toString()</tt> methods.
 * This implementation <b>must</b> be threadsafe.
 * 
 * @version $Revision:$
 */
public class OSCache implements Cache {
    
    /** The OSCache 2.4 cache administrator. */
    private GeneralCacheAdministrator cache;
    private final int refreshPeriod;
    private final String cron;
    private final String regionName;
    private final String[] regionGroups;
    
    public OSCache(GeneralCacheAdministrator cache, int refreshPeriod, String cron, String region) {
        this.cache = cache;
        this.refreshPeriod = refreshPeriod;
        this.cron = cron;
        this.regionName = region;
        this.regionGroups = new String[] {region};
    }

    /**
     * @see org.hibernate.cache.Cache#get(java.lang.Object)
     */
    public Object get(Object key) throws CacheException {
        try {
            return cache.getFromCache( toString(key), refreshPeriod, cron );
        }
        catch (NeedsRefreshException e) {
            cache.cancelUpdate( toString(key) );
            return null;
        }
    }

    /**
     * @see org.hibernate.cache.Cache#put(java.lang.Object, java.lang.Object)
     */
    public void put(Object key, Object value) throws CacheException {
        cache.putInCache( toString(key), value, regionGroups );
    }

    /**
     * @see org.hibernate.cache.Cache#remove(java.lang.Object)
     */
    public void remove(Object key) throws CacheException {
        cache.flushEntry( toString(key) );
    }

    /**
     * @see org.hibernate.cache.Cache#clear()
     */
    public void clear() throws CacheException {
        cache.flushGroup(regionName);
    }

    /**
     * @see org.hibernate.cache.Cache#destroy()
     */
    public void destroy() throws CacheException {
        synchronized (cache) {
            cache.destroy();
        }
    }

    /**
     * @see org.hibernate.cache.Cache#lock(java.lang.Object)
     */
    public void lock(Object key) throws CacheException {
        // local cache, so we use synchronization
    }

    /**
     * @see org.hibernate.cache.Cache#unlock(java.lang.Object)
     */
    public void unlock(Object key) throws CacheException {
        // local cache, so we use synchronization
    }

    /**
     * @see org.hibernate.cache.Cache#nextTimestamp()
     */
    public long nextTimestamp() {
        return Timestamper.next();
    }

    /**
     * @see org.hibernate.cache.Cache#getTimeout()
     */
    public int getTimeout() {
        return Timestamper.ONE_MS * 60000; //ie. 60 seconds
    }

    /**
     * @see org.hibernate.cache.Cache#toMap()
     */
    public Map toMap() {
        throw new UnsupportedOperationException();
    }    

    /**
     * @see org.hibernate.cache.Cache#getElementCountOnDisk()
     */
    public long getElementCountOnDisk() {
        return -1;
    }

    /**
     * @see org.hibernate.cache.Cache#getElementCountInMemory()
     */
    public long getElementCountInMemory() {
        return -1;
    }
    
    /**
     * @see org.hibernate.cache.Cache#getSizeInMemory()
     */
    public long getSizeInMemory() {
        return -1;
    }

    /**
     * @see org.hibernate.cache.Cache#getRegionName()
     */
    public String getRegionName() {
        return regionName;
    }

    /**
     * @see org.hibernate.cache.Cache#update(java.lang.Object, java.lang.Object)
     */
    public void update(Object key, Object value) throws CacheException {
        put(key, value);
    }    

    /**
     * @see org.hibernate.cache.Cache#read(java.lang.Object)
     */
    public Object read(Object key) throws CacheException {
        return get(key);
    }
    
    private String toString(Object key) {
        return String.valueOf(key) + "." + regionName;
    }

}
