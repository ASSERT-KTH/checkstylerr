/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web.tag;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.web.ServletCacheAdministrator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * FlushTag flushes caches created with &lt;cache&gt;.
 *
 * This tag provides programmatic control over when caches are flushed,
 * and can flush all caches at once.<p>
 *
 * Usage Examples:
 * <pre><code>
 * &lt;%@ taglib uri="oscache" prefix="cache" %&gt;
 * &lt;cache:flush scope="application" /&gt;
 * &lt;cache:flush scope="session" key="foobar" /&gt;
 * </code></pre>
 *
 * Note: If no scope is provided (or scope is  null), it will flush
 * all caches globally - use with care!<p>
 * <p>
 * Flushing is done by setting an appropriate application level time,
 * which &lt;cache&gt; always looks at before retrieving the cache.
 * If this 'flush time' is &gt; that cache's last update, it will refresh
 * the cache.
 * <p>
 * As such caches are not all 'flushed', they are all marked
 * to be refreshed at their next access. That is the only way that
 * the content can still be available if the refresh fails.
 *
 * @author <a href="mailto:mike@atlassian.com">Mike Cannon-Brookes</a>
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 * @version $Revision: 254 $
 */
public class FlushTag extends TagSupport {
    ServletCacheAdministrator admin = null;

    /**
     * A cache group.
     * If specified, all content in that group will be flushed
     */
    String group = null;

    /**
     * Tag key.
     */
    String key = null;

    /**
     * if pattern value is specified, all keys that contain the pattern are flushed.
     */
    String pattern = null;
    String scope = null;
    int cacheScope = -1;

    /**
     *  The ISO-639 language code to distinguish different pages in application scope.
     */
    private String language = null;

    /**
     * The group to be flushed.
     * If specified, all cached content in the group will be flushed.
     *
     * @param group The name of the group to flush.
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * The key to be flushed.
     * If specified, only one cache entry will be flushed.
     *
     * @param value The key of the specific entry to flush.
     */
    public void setKey(String value) {
        this.key = value;
    }

    /**
     * Set the ISO-639 language code to distinguish different pages in application scope.
     *
     * @param value The language code for this cache entry.
     */
    public void setLanguage(String value) {
        this.language = value;
    }

    /**
     *  The key pattern to be flushed.
     * If specified, all entries that contain the pattern will be flushed.
     *  @param value The key of the specific entry to flush.
     */
    public void setPattern(String value) {
        this.pattern = value;
    }

    /**
     * Set the scope of this flush.
     *
     * @param value The scope - either "application" (default) or "session".
     */
    public void setScope(String value) {
        if (value != null) {
            if (value.equalsIgnoreCase(ServletCacheAdministrator.SESSION_SCOPE_NAME)) {
                cacheScope = PageContext.SESSION_SCOPE;
            } else if (value.equalsIgnoreCase(ServletCacheAdministrator.APPLICATION_SCOPE_NAME)) {
                cacheScope = PageContext.APPLICATION_SCOPE;
            }
        }
    }

    /**
     * Process the start of the tag.
     *
     * @throws JspTagException The standard tag exception thrown.
     * @return The standard Tag return.
     */
    public int doStartTag() throws JspTagException {
        if (admin == null) {
            admin = ServletCacheAdministrator.getInstance(pageContext.getServletContext());
        }

        if (group != null) // We're flushing a group
         {
            if (cacheScope >= 0) {
                Cache cache = admin.getCache((HttpServletRequest) pageContext.getRequest(), cacheScope);
                cache.flushGroup(group);
            } else {
                throw new JspTagException("A cache group was specified for flushing, but the scope wasn't supplied or was invalid");
            }
        } else if (pattern != null) // We're flushing keys which contain the pattern
         {
            if (cacheScope >= 0) {
                Cache cache = admin.getCache((HttpServletRequest) pageContext.getRequest(), cacheScope);
                cache.flushPattern(pattern);
            } else {
                throw new JspTagException("A pattern was specified for flushing, but the scope wasn't supplied or was invalid");
            }
        } else if (key == null) // we're flushing a whole scope
         {
            if (cacheScope >= 0) {
                admin.setFlushTime(cacheScope);
            } else {
                admin.flushAll();
            }
        } else // we're flushing just one key
         {
            if (cacheScope >= 0) {
                String actualKey = admin.generateEntryKey(key, (HttpServletRequest) pageContext.getRequest(), cacheScope, language);

                Cache cache = admin.getCache((HttpServletRequest) pageContext.getRequest(), cacheScope);
                cache.flushEntry(actualKey);
            } else {
                throw new JspTagException("A cache key was specified for flushing, but the scope wasn't supplied or was invalid");
            }
        }

        return SKIP_BODY;
    }
}
