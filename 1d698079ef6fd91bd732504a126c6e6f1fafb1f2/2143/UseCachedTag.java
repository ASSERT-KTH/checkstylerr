/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web.tag;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * UseCachedTag is a tag that tells a &lt;cache&gt; tag to reuse the cached body.<p>
 *
 * Usage Example:
 * <pre><code>
 *       &lt;%@ taglib uri="oscache" prefix="cache" %&gt;
 *       &lt;cache:cache key="mycache" scope="application"&gt;
 *                if (reuse cached)
 *                        &lt;cache:usecached /&gt;
 *                else
 *                        some other logic
 *       &lt;/cache:cache&gt;
 * </code></pre>
 *
 * Note this is very useful with try / catch blocks
 * so that you can still produce old cached data if an
 * exception occurs, eg your database goes down.<p>
 *
 * @author        <a href="mailto:mike@atlassian.com">Mike Cannon-Brookes</a>
 * @version        $Revision: 254 $
 */
public class UseCachedTag extends TagSupport {
    boolean use = true;

    /**
     * Set the decision to use the body content of the ancestor &lt;cache&gt; or not.
     *
     * @param value Whether or not to use the body content. Default is true.
     */
    public void setUse(boolean value) {
        this.use = value;
    }

    /**
     * The start tag.
     *
     * @throws JspTagException The standard tag exception thrown.
     * @return The standard Tag return.
     */
    public int doStartTag() throws JspTagException {
        CacheTag cacheTag = (CacheTag) TagSupport.findAncestorWithClass(this, CacheTag.class);

        if (cacheTag == null) {
            throw new JspTagException("A UseCached tag must be nested within a Cache tag");
        }

        cacheTag.setUseBody(!use);

        return SKIP_BODY;
    }
}
