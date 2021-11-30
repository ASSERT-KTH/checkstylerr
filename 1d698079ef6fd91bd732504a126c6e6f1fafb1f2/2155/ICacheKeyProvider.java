/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web.filter;

import javax.servlet.http.HttpServletRequest;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.web.ServletCacheAdministrator;

/**
 * Provider interface for cache key creation. A developer can implement a method which provides
 * cache keys based on the request, the servlet cache administrator and cache.
 * 
 * JIRA issue: http://jira.opensymphony.com/browse/CACHE-179
 *
 * @author <a href="mailto:ltorunski@t-online.de">Lars Torunski</a>
 * @version $Revision: 276 $
 */
public interface ICacheKeyProvider {

    /**
     * Creates the cache key for the CacheFilter.
     *
     * @param httpRequest the http request.
     * @param scAdmin the ServletCacheAdministrator of the cache
     * @param cache the cache of the ServletCacheAdministrator
     * @return the cache key
     */
    public String createCacheKey(HttpServletRequest httpRequest, ServletCacheAdministrator scAdmin, Cache cache);

}