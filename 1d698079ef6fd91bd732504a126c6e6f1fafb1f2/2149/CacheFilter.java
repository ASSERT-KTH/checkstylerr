/*
 * Copyright (c) 2002-2007 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web.filter;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.Config;
import com.opensymphony.oscache.base.EntryRefreshPolicy;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.util.ClassLoaderUtil;
import com.opensymphony.oscache.util.StringUtil;
import com.opensymphony.oscache.web.ServletCacheAdministrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * CacheFilter is a filter that allows for server-side caching of post-processed servlet content.<p>
 *
 * It also gives great programatic control over refreshing, flushing and updating the cache.<p>
 *
 * @author <a href="mailto:sergek [ AT ] lokitech.com">Serge Knystautas</a>
 * @author <a href="mailto:mike [ AT ] atlassian.com">Mike Cannon-Brookes</a>
 * @author <a href="mailto:ltorunski [ AT ] t-online.de">Lars Torunski</a>
 * @version $Revision: 434 $
 */
public class CacheFilter implements Filter, ICacheKeyProvider, ICacheGroupsProvider {
    // Header
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_EXPIRES = "Expires";
    public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    // Fragment parameter
    public static final int FRAGMENT_AUTODETECT = -1;
    public static final int FRAGMENT_NO = 0;
    public static final int FRAGMENT_YES = 1;
    
    // No cache parameter
    public static final int NOCACHE_OFF = 0;
    public static final int NOCACHE_SESSION_ID_IN_URL = 1;
    
    // Last Modified parameter
    public static final long LAST_MODIFIED_OFF = 0;
    public static final long LAST_MODIFIED_ON = 1;
    public static final long LAST_MODIFIED_INITIAL = -1;
    
    // Expires parameter
    public static final long EXPIRES_OFF = 0;
    public static final long EXPIRES_ON = 1;
    public static final long EXPIRES_TIME = -1;
    
    // Cache Control
    public static final long MAX_AGE_NO_INIT = Long.MIN_VALUE;
    public static final long MAX_AGE_TIME = Long.MAX_VALUE;

    // request attribute to avoid reentrance
    private final static String REQUEST_FILTERED = "__oscache_filtered__";
    private String requestFiltered;

    // the policy for the expires header
    private EntryRefreshPolicy expiresRefreshPolicy;
    
    // the logger
    private final Log log = LogFactory.getLog(this.getClass());

    // filter variables
    private FilterConfig config;
    private ServletCacheAdministrator admin = null;
    private int cacheScope = PageContext.APPLICATION_SCOPE; // filter scope - default is APPLICATION
    private int fragment = FRAGMENT_AUTODETECT; // defines if this filter handles fragments of a page - default is auto detect
    private int time = 60 * 60; // time before cache should be refreshed - default one hour (in seconds)
    private String cron = null; // A cron expression that determines when this cached content will expire - default is null
    private int nocache = NOCACHE_OFF; // defines special no cache option for the requests - default is off
    private long lastModified = LAST_MODIFIED_INITIAL; // defines if the last-modified-header will be sent - default is intial setting
    private long expires = EXPIRES_ON; // defines if the expires-header will be sent - default is on
    private long cacheControlMaxAge = -60; // defines which max-age in Cache-Control to be set - default is 60 seconds for max-age
    private ICacheKeyProvider cacheKeyProvider = this; // the provider of the cache key - default is the CacheFilter itselfs
    private ICacheGroupsProvider cacheGroupsProvider = this; // the provider of the cache groups - default is the CacheFilter itselfs
    private List disableCacheOnMethods = null; // caching can be disabled by defining the http methods - default is off

    /**
     * Filter clean-up
     */
    public void destroy() {
        //Not much to do...
    }

    /**
     * The doFilter call caches the response by wrapping the <code>HttpServletResponse</code>
     * object so that the output stream can be caught. This works by splitting off the output
     * stream into two with the {@link SplitServletOutputStream} class. One stream gets written
     * out to the response as normal, the other is fed into a byte array inside a {@link ResponseContent}
     * object.
     *
     * @param request The servlet request
     * @param response The servlet response
     * @param chain The filter chain
     * @throws ServletException IOException
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (log.isInfoEnabled()) {
            log.info("OSCache: filter in scope " + cacheScope);
        }

        // avoid reentrance (CACHE-128) and check if request is cacheable
        if (isFilteredBefore(request) || !isCacheableInternal(request)) {
            chain.doFilter(request, response);
            return;
        }
        request.setAttribute(requestFiltered, Boolean.TRUE);

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // checks if the response will be a fragment of a page
        boolean fragmentRequest = isFragment(httpRequest);

        // avoid useless session creation for application scope pages (CACHE-129)
        Cache cache;
        if (cacheScope == PageContext.SESSION_SCOPE) {
            cache = admin.getSessionScopeCache(httpRequest.getSession(true));
        } else {
            cache = admin.getAppScopeCache(config.getServletContext());
        }

        // generate the cache entry key
        String key = cacheKeyProvider.createCacheKey(httpRequest, admin, cache);

        try {
            ResponseContent respContent = (ResponseContent) cache.getFromCache(key, time, cron);

            if (log.isInfoEnabled()) {
                log.info("OSCache: Using cached entry for " + key);
            }

            boolean acceptsGZip = false;
            if ((!fragmentRequest) && (lastModified != LAST_MODIFIED_OFF)) {
                long clientLastModified = httpRequest.getDateHeader(HEADER_IF_MODIFIED_SINCE); // will return -1 if no header...

                // only reply with SC_NOT_MODIFIED
                // if the client has already the newest page and the response isn't a fragment in a page 
                if ((clientLastModified != -1) && (clientLastModified >= respContent.getLastModified())) {
                    ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
                
                acceptsGZip = respContent.isContentGZiped() && acceptsGZipEncoding(httpRequest); 
            }

            respContent.writeTo(response, fragmentRequest, acceptsGZip);
            // acceptsGZip is used for performance reasons above; use the following line for CACHE-49
            // respContent.writeTo(response, fragmentRequest, acceptsGZipEncoding(httpRequest));
        } catch (NeedsRefreshException nre) {
            boolean updateSucceeded = false;

            try {
                if (log.isInfoEnabled()) {
                    log.info("OSCache: New cache entry, cache stale or cache scope flushed for " + key);
                }

                CacheHttpServletResponseWrapper cacheResponse = new CacheHttpServletResponseWrapper((HttpServletResponse) response, fragmentRequest, time * 1000L, lastModified, expires, cacheControlMaxAge);
                chain.doFilter(request, cacheResponse);
                cacheResponse.flushBuffer();

                // Only cache if the response is cacheable
                if (isCacheableInternal(cacheResponse)) {
                    // get the cache groups of the content
                    String[] groups = cacheGroupsProvider.createCacheGroups(httpRequest, admin, cache);
                    // Store as the cache content the result of the response
                    cache.putInCache(key, cacheResponse.getContent(), groups, expiresRefreshPolicy, null);
                    updateSucceeded = true;
                    if (log.isInfoEnabled()) {
                        log.info("OSCache: New entry added to the cache with key " + key);
                    }
                }
            } finally {
                if (!updateSucceeded) {
                    cache.cancelUpdate(key);
                }
            }
        }
    }

    /**
     * Initialize the filter. This retrieves a {@link ServletCacheAdministrator}
     * instance and configures the filter based on any initialization parameters.<p>
     * The supported initialization parameters are:
     * <ul>
     * 
     * <li><b>oscache-properties-file</b> - the properties file that contains the OSCache configuration
     * options to be used by the Cache that this Filter should use.</li>
     * 
     * @param filterConfig The filter configuration
     */
    public void init(FilterConfig filterConfig) {
        // Get whatever settings we want...
        config = filterConfig;

        log.info("OSCache: Initializing CacheFilter with filter name " + config.getFilterName());

        // setting the request filter to avoid reentrance with the same filter
        requestFiltered = REQUEST_FILTERED + config.getFilterName();
        log.info("Request filter attribute is " + requestFiltered);

    	// filter Properties file
        Properties props = null;
        try {
            String propertiesfile = config.getInitParameter("oscache-properties-file");
            
            if (propertiesfile != null && propertiesfile.length() > 0) {
            	props = Config.loadProperties(propertiesfile, "CacheFilter with filter name '" + config.getFilterName()+ "'");
            }
        } catch (Exception e) {
            log.info("OSCache: Init parameter 'oscache-properties-file' not set, using default.");
        }
        admin = ServletCacheAdministrator.getInstance(config.getServletContext(), props);

        // filter parameter time
        String timeParam = config.getInitParameter("time");
        if (timeParam != null) {
            try {
                setTime(Integer.parseInt(timeParam));
            } catch (NumberFormatException nfe) {
                log.error("OSCache: Unexpected value for the init parameter 'time', defaulting to one hour. Message=" + nfe.getMessage());
            }
        }
        
        // filter parameter scope
        String scopeParam = config.getInitParameter("scope");
        if (scopeParam != null) {
            if ("session".equalsIgnoreCase(scopeParam)) {
                setCacheScope(PageContext.SESSION_SCOPE);
            } else if ("application".equalsIgnoreCase(scopeParam)) {
                setCacheScope(PageContext.APPLICATION_SCOPE);
            } else {
                log.error("OSCache: Wrong value '" + scopeParam + "' for init parameter 'scope', defaulting to 'application'.");
            }
            
        }

        // filter parameter cron
        setCron(config.getInitParameter("cron"));

        // filter parameter fragment
        String fragmentParam = config.getInitParameter("fragment");
        if (fragmentParam != null) {
            if ("no".equalsIgnoreCase(fragmentParam)) {
                setFragment(FRAGMENT_NO);
            } else if ("yes".equalsIgnoreCase(fragmentParam)) {
                setFragment(FRAGMENT_YES);
            } else if ("auto".equalsIgnoreCase(fragmentParam)) {
                setFragment(FRAGMENT_AUTODETECT);
            } else {
                log.error("OSCache: Wrong value '" + fragmentParam + "' for init parameter 'fragment', defaulting to 'auto detect'.");
            }
        }
        
        // filter parameter nocache
        String nocacheParam = config.getInitParameter("nocache");
        if (nocacheParam != null) {
            if ("off".equalsIgnoreCase(nocacheParam)) {
                nocache = NOCACHE_OFF;
            } else if ("sessionIdInURL".equalsIgnoreCase(nocacheParam)) {
                nocache = NOCACHE_SESSION_ID_IN_URL;
            } else {
                log.error("OSCache: Wrong value '" + nocacheParam + "' for init parameter 'nocache', defaulting to 'off'.");
            }
        }

        // filter parameter last modified
        String lastModifiedParam = config.getInitParameter("lastModified");
        if (lastModifiedParam != null) {
            if ("off".equalsIgnoreCase(lastModifiedParam)) {
                lastModified = LAST_MODIFIED_OFF;
            } else if ("on".equalsIgnoreCase(lastModifiedParam)) {
                lastModified = LAST_MODIFIED_ON;
            } else if ("initial".equalsIgnoreCase(lastModifiedParam)) {
                lastModified = LAST_MODIFIED_INITIAL;
            } else {
                log.error("OSCache: Wrong value '" + lastModifiedParam + "' for init parameter 'lastModified', defaulting to 'initial'.");
            }
        }
        
        // filter parameter expires
        String expiresParam = config.getInitParameter("expires");
        if (expiresParam != null) {
            if ("off".equalsIgnoreCase(expiresParam)) {
                setExpires(EXPIRES_OFF);
            } else if ("on".equalsIgnoreCase(expiresParam)) {
                setExpires(EXPIRES_ON);
            } else if ("time".equalsIgnoreCase(expiresParam)) {
                setExpires(EXPIRES_TIME);
            } else {
                log.error("OSCache: Wrong value '" + expiresParam + "' for init parameter 'expires', defaulting to 'on'.");
            }
        }

        // filter parameter Cache-Control
        String cacheControlMaxAgeParam = config.getInitParameter("max-age");
        if (cacheControlMaxAgeParam != null) {
            if (cacheControlMaxAgeParam.equalsIgnoreCase("no init")) {
                setCacheControlMaxAge(MAX_AGE_NO_INIT);
            } else if (cacheControlMaxAgeParam.equalsIgnoreCase("time")) {
                setCacheControlMaxAge(MAX_AGE_TIME);
            } else {
                try {
                    setCacheControlMaxAge(Long.parseLong(cacheControlMaxAgeParam));
                } catch (NumberFormatException nfe) {
                    log.error("OSCache: Unexpected value for the init parameter 'max-age', defaulting to '60'. Message=" + nfe.getMessage());
                }
            }
        }

        // filter parameter ICacheKeyProvider
        ICacheKeyProvider cacheKeyProviderParam = (ICacheKeyProvider)instantiateFromInitParam("ICacheKeyProvider", ICacheKeyProvider.class, this.getClass().getName());
        if (cacheKeyProviderParam != null) {
            setCacheKeyProvider(cacheKeyProviderParam);
        }

        // filter parameter ICacheGroupsProvider
        ICacheGroupsProvider cacheGroupsProviderParam = (ICacheGroupsProvider)instantiateFromInitParam("ICacheGroupsProvider", ICacheGroupsProvider.class, this.getClass().getName());
        if (cacheGroupsProviderParam != null) {
            setCacheGroupsProvider(cacheGroupsProviderParam);
        }
        
        // filter parameter EntryRefreshPolicy
        EntryRefreshPolicy expiresRefreshPolicyParam = (EntryRefreshPolicy)instantiateFromInitParam("EntryRefreshPolicy", EntryRefreshPolicy.class, ExpiresRefreshPolicy.class.getName());
        if (expiresRefreshPolicyParam != null) {
            setExpiresRefreshPolicy(expiresRefreshPolicyParam);
        } else {
            // setting the refresh period for this cache filter
            setExpiresRefreshPolicy(new ExpiresRefreshPolicy(time));
        }
        
        // filter parameter scope
        String disableCacheOnMethodsParam = config.getInitParameter("disableCacheOnMethods");
        if (StringUtil.hasLength(disableCacheOnMethodsParam)) {
            disableCacheOnMethods = StringUtil.split(disableCacheOnMethodsParam, ',');   
            // log.error("OSCache: Wrong value '" + disableCacheOnMethodsParam + "' for init parameter 'disableCacheOnMethods', defaulting to 'null'.");
        }

    }

    private Object instantiateFromInitParam(String classInitParam, Class interfaceClass, String defaultObjectName) {
		String className = config.getInitParameter(classInitParam);
		if (className != null) {
            try {
                Class clazz = ClassLoaderUtil.loadClass(className, this.getClass());
                if (!interfaceClass.isAssignableFrom(clazz)) {
                    log.error("OSCache: Specified class '" + className + "' does not implement" + interfaceClass.getName() + ". Using default " + defaultObjectName + ".");
                    return null;
                } else {
                    return clazz.newInstance();
                }
            } catch (ClassNotFoundException e) {
                log.error("OSCache: Class '" + className + "' not found. Defaulting to " + defaultObjectName + ".", e);
            } catch (InstantiationException e) {
                log.error("OSCache: Class '" + className + "' could not be instantiated because it is not a concrete class. Using default object " + defaultObjectName + ".", e);
            } catch (IllegalAccessException e) {
                log.error("OSCache: Class '"+ className+ "' could not be instantiated because it is not public. Using default object " + defaultObjectName + ".", e);
            }
		}
        return null;
	}
    
    /**
     * {@link ICacheKeyProvider}
     * @see com.opensymphony.oscache.web.filter.ICacheKeyProvider#createCacheKey(javax.servlet.http.HttpServletRequest, ServletCacheAdministrator, Cache)
     */
    public String createCacheKey(HttpServletRequest httpRequest, ServletCacheAdministrator scAdmin, Cache cache) {
        return scAdmin.generateEntryKey(null, httpRequest, cacheScope);
    }

    /**
     * {@link ICacheGroupsProvider}
     * @see com.opensymphony.oscache.web.filter.ICacheGroupsProvider#createCacheGroups(javax.servlet.http.HttpServletRequest, ServletCacheAdministrator, Cache)
     */
    public String[] createCacheGroups(HttpServletRequest httpRequest, ServletCacheAdministrator scAdmin, Cache cache) {
        return null;
    }

    /**
     * Checks if the request is a fragment in a page.
     *
     * According to Java Servlet API 2.2 (8.2.1 Dispatching Requests, Included
     * Request Parameters), when a servlet is being used from within an include,
     * the attribute <code>javax.servlet.include.request_uri</code> is set.
     * According to Java Servlet API 2.3 this is excepted for servlets obtained
     * by using the getNamedDispatcher method.
     *
     * @param request the to be handled request
     * @return true if the request is a fragment in a page
     */
    public boolean isFragment(HttpServletRequest request) {
        if (fragment == FRAGMENT_AUTODETECT) {
            return request.getAttribute("javax.servlet.include.request_uri") != null;
        } else {
            return (fragment == FRAGMENT_NO) ? false : true;
        }
    }

    /**
     * Checks if the request was filtered before, so
     * guarantees to be executed once per request. You
     * can override this methods to define a more specific
     * behaviour.
     *
     * @param request checks if the request was filtered before.
     * @return true if it is the first execution
     */
    public boolean isFilteredBefore(ServletRequest request) {
        return request.getAttribute(requestFiltered) != null;
    }

    /*
     * isCacheableInternal gurarantees that the log information is correct.
     * 
     * @param request The servlet request
     * @return Returns a boolean indicating if the request can be cached or not.
     */
    private final boolean isCacheableInternal(ServletRequest request) {
        final boolean cacheable = isCacheable(request);

        if (log.isDebugEnabled()) {
            log.debug("OSCache: the request " + ((cacheable) ? "is" : "is not") + " cachable.");
        }
        
        return cacheable;
    }

    /**
     * isCacheable is a method allowing a subclass to decide if a request is
     * cachable or not.
     * 
     * @param request The servlet request
     * @return Returns a boolean indicating if the request can be cached or not.
     */
    public boolean isCacheable(ServletRequest request) {
        boolean cacheable = request instanceof HttpServletRequest;

        if (cacheable) {
            HttpServletRequest requestHttp = (HttpServletRequest) request;
            // CACHE-272 don't cache special http request methods
            if ((disableCacheOnMethods != null) && (disableCacheOnMethods.contains(requestHttp.getMethod()))) {
                return false;
            }
            if (nocache == NOCACHE_SESSION_ID_IN_URL) { // don't cache requests if session id is in the URL
                cacheable = !requestHttp.isRequestedSessionIdFromURL();
            }
        }

        return cacheable;
    }
    
    /*
     * isCacheableInternal gurarantees that the log information is correct.
     * 
     * @param cacheResponse the HTTP servlet response
     * @return Returns a boolean indicating if the response can be cached or not.
     */
    private final boolean isCacheableInternal(CacheHttpServletResponseWrapper cacheResponse) {
        final boolean cacheable = isCacheable(cacheResponse);

        if (log.isDebugEnabled()) {
            log.debug("OSCache: the response " + ((cacheable) ? "is" : "is not") + " cachable.");
        }
        
        return cacheable;
    }

    /**
     * isCacheable is a method allowing subclass to decide if a response is
     * cachable or not.
     * 
     * @param cacheResponse the HTTP servlet response
     * @return Returns a boolean indicating if the response can be cached or not.
     */
    public boolean isCacheable(CacheHttpServletResponseWrapper cacheResponse) {
        // TODO implement CACHE-137 here
        // Only cache if the response was 200
        return cacheResponse.getStatus() == HttpServletResponse.SC_OK;
    }

    /**
     * Check if the client browser support gzip compression.
     * 
     * @param request the http request
     * @return true if client browser supports GZIP
     */
    public boolean acceptsGZipEncoding(HttpServletRequest request) {
        String acceptEncoding = request.getHeader(HEADER_ACCEPT_ENCODING);
        return  (acceptEncoding != null) && (acceptEncoding.indexOf("gzip") != -1);
    }

    // ---------------------------------
    // --- getter and setter methods ---
    // ---------------------------------
    
    /**
     * @return the max-age of the cache control
     * @since 2.4
     */
    public long getCacheControlMaxAge() {
        if ((cacheControlMaxAge == MAX_AGE_NO_INIT) || (cacheControlMaxAge == MAX_AGE_TIME)) {
            return cacheControlMaxAge;
        }
        return - cacheControlMaxAge;
    }

    /**
     * <b>max-age</b> - defines the cache control response header max-age. Acceptable values are
     * <code>MAX_AGE_NO_INIT</code> for don't initializing the max-age cache control, 
     * <code>MAX_AGE_TIME</code> the max-age information will be based on the time parameter and creation time of the content (expiration timestamp minus current timestamp), and
     * <code>[positive integer]</code> value constant in seconds to be set in every response, the default value is 60.
     * 
     * @param cacheControlMaxAge the cacheControlMaxAge to set
     * @since 2.4
     */
    public void setCacheControlMaxAge(long cacheControlMaxAge) {
        if ((cacheControlMaxAge == MAX_AGE_NO_INIT) || (cacheControlMaxAge == MAX_AGE_TIME)) {
            this.cacheControlMaxAge = cacheControlMaxAge;
        } else if (cacheControlMaxAge >= 0) {
            // declare the cache control as a constant
            // TODO check if this value can be stored as a positive long without changing it
            this.cacheControlMaxAge = - cacheControlMaxAge;
        } else {
            log.warn("OSCache: 'max-age' must be at least a positive integer, defaulting to '60'. ");
            this.cacheControlMaxAge = -60;
        }
    }

    /**
     * @return the cacheGroupsProvider
     * @since 2.4
     */
    public ICacheGroupsProvider getCacheGroupsProvider() {
        return cacheGroupsProvider;
    }

    /**
     * <b>ICacheGroupsProvider</b> - Class implementing the interface <code>ICacheGroupsProvider</code>.
     * A developer can implement a method which provides cache groups based on the request, 
     * the servlect cache administrator and cache. The parameter has to be not <code>null</code>.
     *
     * @param cacheGroupsProvider the cacheGroupsProvider to set
     * @since 2.4
     */
    public void setCacheGroupsProvider(ICacheGroupsProvider cacheGroupsProvider) {
        if (cacheGroupsProvider == null) throw new IllegalArgumentException("The ICacheGroupsProvider is null.");
        this.cacheGroupsProvider = cacheGroupsProvider;
    }

    /**
     * @return the cacheKeyProvider
     * @since 2.4
     */
    public ICacheKeyProvider getCacheKeyProvider() {
        return cacheKeyProvider;
    }

    /**
     * <b>ICacheKeyProvider</b> - Class implementing the interface <code>ICacheKeyProvider</code>.
     * A developer can implement a method which provides cache keys based on the request, 
     * the servlect cache administrator and cache. The parameter has to be not <code>null</code>.
     * 
     * @param cacheKeyProvider the cacheKeyProvider to set
     * @since 2.4
     */
    public void setCacheKeyProvider(ICacheKeyProvider cacheKeyProvider) {
        if (cacheKeyProvider == null) throw new IllegalArgumentException("The ICacheKeyProvider is null.");
        this.cacheKeyProvider = cacheKeyProvider;
    }

    /**
     * Returns PageContext.APPLICATION_SCOPE or PageContext.SESSION_SCOPE.
     * @return the cache scope
     * @since 2.4
     */
    public int getCacheScope() {
        return cacheScope;
    }

    /**
     * <b>scope</b> - the default scope to cache content. Acceptable values
     * are <code>PageContext.APPLICATION_SCOPE</code> (default) and <code>PageContext.SESSION_SCOPE</code>.
     * 
     * @param cacheScope the cacheScope to set
     * @since 2.4
     */
    public void setCacheScope(int cacheScope) {
        if ((cacheScope != PageContext.APPLICATION_SCOPE) && (cacheScope != PageContext.SESSION_SCOPE))
            throw new IllegalArgumentException("Acceptable values for cache scope are PageContext.APPLICATION_SCOPE or PageContext.SESSION_SCOPE");
        this.cacheScope = cacheScope;
    }

    /**
     * @return the cron
     * @since 2.4
     */
    public String getCron() {
        return cron;
    }

    /**
     * <b>cron</b> - defines an expression that determines when the page content will expire.
     * This allows content to be expired at particular dates and/or times, rather than once 
     * a cache entry reaches a certain age.
     * 
     * @param cron the cron to set
     * @since 2.4
     */
    public void setCron(String cron) {
        this.cron = cron;
    }

    /**
     * @return the expires header
     * @since 2.4
     */
    public long getExpires() {
        return expires;
    }

    /**
     * <b>expires</b> - defines if the expires header will be sent in the response. Acceptable values are
     * <code>EXPIRES_OFF</code> for don't sending the header, even it is set in the filter chain, 
     * <code>EXPIRES_ON</code> (default) for sending it if it is set in the filter chain and 
     * <code>EXPIRES_TIME</code> the expires information will be intialized based on the time parameter and creation time of the content.
     * 
     * @param expires the expires to set
     * @since 2.4
     */
    public void setExpires(long expires) {
        if ((expires < EXPIRES_TIME) || (expires > EXPIRES_ON)) throw new IllegalArgumentException("Expires value out of range.");
        this.expires = expires;
    }

    /**
     * @return the expiresRefreshPolicy
     * @since 2.4
     */
    public EntryRefreshPolicy getExpiresRefreshPolicy() {
        return expiresRefreshPolicy;
    }

    /**
     * <b>EntryRefreshPolicy</b> - Class implementing the interface <code>EntryRefreshPolicy</code>.
     * A developer can implement a class which provides a different custom cache invalidation policy for a specific cache entry.
     * If not specified, the default policy is timed entry expiry as specified with the <b>time</b> parameter described above. 
     *
     * @param expiresRefreshPolicy the expiresRefreshPolicy to set
     * @since 2.4
     */
    public void setExpiresRefreshPolicy(EntryRefreshPolicy expiresRefreshPolicy) {
        if (expiresRefreshPolicy == null) throw new IllegalArgumentException("The EntryRefreshPolicy is null.");
        this.expiresRefreshPolicy = expiresRefreshPolicy;
    }

    /**
     * @return the fragment
     * @since 2.4
     */
    public int getFragment() {
        return fragment;
    }

    /**
     * <b>fragment</b> - defines if this filter handles fragments of a page. Acceptable values
     * are <code>FRAGMENT_AUTODETECT</code> (default) for auto detect, <code>FRAGMENT_NO</code> and <code>FRAGMENT_YES</code>.
     * 
     * @param fragment the fragment to set
     * @since 2.4
     */
    public void setFragment(int fragment) {
        if ((fragment < FRAGMENT_AUTODETECT) || (fragment > FRAGMENT_YES)) throw new IllegalArgumentException("Fragment value out of range.");
        this.fragment = fragment;
    }

    /**
     * @return the lastModified
     * @since 2.4
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * <b>lastModified</b> - defines if the last modified header will be sent in the response. Acceptable values are
     * <code>LAST_MODIFIED_OFF</code> for don't sending the header, even it is set in the filter chain, 
     * <code>LAST_MODIFIED_ON</code> for sending it if it is set in the filter chain and 
     * <code>LAST_MODIFIED_INITIAL</code> (default) the last modified information will be set based on the current time and changes are allowed.
     * 
     * @param lastModified the lastModified to set
     * @since 2.4
     */
    public void setLastModified(long lastModified) {
        if ((lastModified < LAST_MODIFIED_INITIAL) || (lastModified > LAST_MODIFIED_ON)) throw new IllegalArgumentException("LastModified value out of range.");
        this.lastModified = lastModified;
    }

    /**
     * @return the nocache
     * @since 2.4
     */
    public int getNocache() {
        return nocache;
    }

    /**
     * <b>nocache</b> - defines which objects shouldn't be cached. Acceptable values
     * are <code>NOCACHE_OFF</code> (default) and <code>NOCACHE_SESSION_ID_IN_URL</code> if the session id is
     * contained in the URL.
     * 
     * @param nocache the nocache to set
     * @since 2.4
     */
    public void setNocache(int nocache) {
        if ((nocache < NOCACHE_OFF) || (nocache > NOCACHE_SESSION_ID_IN_URL)) throw new IllegalArgumentException("Nocache value out of range.");
        this.nocache = nocache;
    }

    /**
     * @return the time
     * @since 2.4
     */
    public int getTime() {
        return time;
    }

    /**
     * <b>time</b> - the default time (in seconds) to cache content for. The default
     * value is 3600 seconds (one hour). Specifying -1 (indefinite expiry) as the cache 
     * time will ensure a content does not become stale until it is either explicitly 
     * flushed or the expires refresh policy causes the entry to expire.
     * 
     * @param time the time to set
     * @since 2.4
     */
    public void setTime(int time) {
        this.time = time;
        // check if ExpiresRefreshPolicy has to be reset
        if (expiresRefreshPolicy instanceof ExpiresRefreshPolicy) {
            ((ExpiresRefreshPolicy) expiresRefreshPolicy).setRefreshPeriod(time);
        }
    }

    /**
     * @link http://java.sun.com/j2ee/sdk_1.3/techdocs/api/javax/servlet/http/HttpServletRequest.html#getMethod()
     * @return the list of http method names for which cacheing should be disabled
     * @since 2.4
     */
    public List getDisableCacheOnMethods() {
        return disableCacheOnMethods;
    }

    /**
     * <b>disableCacheOnMethods</b> - Defines the http method name for which cacheing should be disabled.
     * The default value is <code>null</code> for cacheing all requests without regarding the method name.
     * @link http://java.sun.com/j2ee/sdk_1.3/techdocs/api/javax/servlet/http/HttpServletRequest.html#getMethod()
     * @param disableCacheOnMethods the list of http method names for which cacheing should be disabled
     * @since 2.4
     */
    public void setDisableCacheOnMethods(List disableCacheOnMethods) {
        this.disableCacheOnMethods = disableCacheOnMethods;
    }
    
    // TODO: check if getter/setter for oscache-properties-file is possible
    
}
