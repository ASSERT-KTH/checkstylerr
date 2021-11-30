/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web;

import com.opensymphony.oscache.base.EntryRefreshPolicy;

/**
 * Interface to implement an entry refresh policy.
 * Specify the name of the implementing class using the refreshpolicyclass
 * attribute of the cache tag. If any additional parameters are required,
 * they should be supplied using the refreshpolicyparam attribute.<p>
 *
 * For example:
 * <pre>
 * &lt;cache:cache key="mykey"
 *              refreshpolicyclass="com.mycompany.cache.policy.MyRefreshPolicy"
 *              refreshpolicyparam="...additional data..."&gt;
       My cached content
 * &lt;/cache:cache&gt;
 * </pre>
 *
 * @version        $Revision: 254 $
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 */
public interface WebEntryRefreshPolicy extends EntryRefreshPolicy {
    /**
     * Initializes the refresh policy.
     *
     * @param key   The cache key that is being checked.
     * @param param Any optional parameters that were supplied
     */
    public void init(String key, String param);
}
