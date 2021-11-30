/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web.tag;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.util.StringUtil;
import com.opensymphony.oscache.web.ServletCacheAdministrator;
import com.opensymphony.oscache.web.WebEntryRefreshPolicy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TryCatchFinally;

/**
 * CacheTag is a tag that allows for server-side caching of post-processed JSP content.<p>
 *
 * It also gives great programatic control over refreshing, flushing and updating the cache.<p>
 *
 * Usage Example:
 * <pre><code>
 *     &lt;%@ taglib uri="oscache" prefix="cache" %&gt;
 *     &lt;cache:cache key="mycache"
 *                 scope="application"
 *                 refresh="false"
 *                 time="30">
 *              jsp content here... refreshed every 30 seconds
 *     &lt;/cache:cache&gt;
 * </code></pre>
 *
 * @author <a href="mailto:mike@atlassian.com">Mike Cannon-Brookes</a>
 * @author <a href="mailto:tgochenour@peregrine.com">Todd Gochenour</a>
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 * @version        $Revision: 331 $
 */
public class CacheTag extends BodyTagSupport implements TryCatchFinally {
    /**
    * Constants for time computation
    */
    private final static int SECOND = 1;
    private final static int MINUTE = 60 * SECOND;
    private final static int HOUR = 60 * MINUTE;
    private final static int DAY = 24 * HOUR;
    private final static int WEEK = 7 * DAY;
    private final static int MONTH = 30 * DAY;
    private final static int YEAR = 365 * DAY;

    /**
    * The key under which the tag counter will be stored in the request
    */
    private final static String CACHE_TAG_COUNTER_KEY = "__oscache_tag_counter";

    /**
    * Constants for refresh time
    */
    final static private int ONE_MINUTE = 60;
    final static private int ONE_HOUR = 60 * ONE_MINUTE;
    final static private int DEFAULT_TIMEOUT = ONE_HOUR;
    private static transient Log log = LogFactory.getLog(CacheTag.class);

    /**
    * Cache modes
    */
    final static private int SILENT_MODE = 1;

    /**
    * A flag to indicate whether a NeedsRefreshException was thrown and
    * the update needs to be cancelled
    */
    boolean cancelUpdateRequired = false;
    private Cache cache = null;

    /**
    * If no groups are specified, the cached content does not get put into any groups
    */
    private List groups = null;
    private ServletCacheAdministrator admin = null;

    /**
    * The actual key to use. This is generated based on the supplied key, scope etc.
    */
    private String actualKey = null;

    /**
    * The content that was retrieved from cache
    */
    private String content = null;

    /**
    * The cron expression that is used to expire cache entries at specific dates and/or times.
    */
    private String cron = null;

    /**
    * if cache key is null, the request URI is used
    */
    private String key = null;

    /**
    *  The ISO-639 language code to distinguish different pages in application scope
    */
    private String language = null;

    /**
    * Class used to handle the refresh policy logic
    */
    private String refreshPolicyClass = null;

    /**
    * Parameters that will be passed to the init method of the
    * refresh policy instance.
    */
    private String refreshPolicyParam = null;

    /**
    * Whether the cache should be refreshed instantly
    */
    private boolean refresh = false;

    /**
    * used for subtags to tell this tag that we should use the cached version
    */
    private boolean useBody = true;

    /**
    * The cache mode. Valid values are SILENT_MODE
    */
    private int mode = 0;

    /**
    * The cache scope to use
    */
    private int scope = PageContext.APPLICATION_SCOPE;

    /**
    * time (in seconds) before cache should be refreshed
    */
    private int time = DEFAULT_TIMEOUT;

    /**
    * Set the time this cache entry will be cached for. A date and/or time in
    * either ISO-8601 format or a simple format can be specified. The acceptable
    * syntax for the simple format can be any one of the following:
    *
    * <ul>
    * <li>0 (seconds)
    * <li>0s (seconds)
    * <li>0m (minutes)
    * <li>0h (hours)
    * <li>0d (days)
    * <li>0w (weeks)
    * </ul>
    *
    * @param duration The duration to cache this content (using either the simple
    * or the ISO-8601 format). Passing in a duration of zero will turn off the
    * caching, while a negative value will result in the cached content never
    * expiring (ie, the cached content will always be served as long as it is
    * present).
    */
    public void setDuration(String duration) {
        try {
            // Try Simple Date Format Duration first because it's faster
            this.time = parseDuration(duration);
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("Failed parsing simple duration format '" + duration + "' (" + ex.getMessage() + "). Trying ISO-8601 format...");
            }

            try {
                // Try ISO-8601 Duration
                this.time = parseISO_8601_Duration(duration);
            } catch (Exception ex1) {
                // An invalid duration entered, not much impact.
                // The default timeout will be used
                log.warn("The requested cache duration '" + duration + "' is invalid (" + ex1.getMessage() + "). Reverting to the default timeout");
                this.time = DEFAULT_TIMEOUT;
            }
        }
    }

    /**
    * Sets the cron expression that should be used to expire content at specific
    * dates and/or times.
    */
    public void setCron(String cron) {
        this.cron = cron;
    }

    /**
     * Sets the groups for this cache entry. Any existing groups will
     * be replaced.
     *
     * @param groups A comma-delimited list of groups that the cache entry belongs to.
     */
    public void setGroups(String groups) {
    	// FIXME: ArrayList doesn't avoid duplicates
        this.groups = StringUtil.split(groups, ',');
    }

    /**
     * Adds to the groups for this cache entry.
     *
     * @param group A group to which the cache entry should belong.
     */
    void addGroup(String group) {
        if (groups == null) {
        	// FIXME: ArrayList doesn't avoid duplicates
            groups = new ArrayList();
        }

        groups.add(group);
    }

    /**
     * Adds comma-delimited list of groups that the cache entry belongs to.
     *
     * @param groups A comma-delimited list of groups that the cache entry belongs to also.
     */
    void addGroups(String groupsString) {
        if (groups == null) {
        	// FIXME: ArrayList doesn't avoid duplicates
            groups = new ArrayList();
        }

        groups.addAll(StringUtil.split(groupsString, ','));
    }

    /**
    * Set the key for this cache entry.
    *
    * @param key The key for this cache entry.
    */
    public void setKey(String key) {
        this.key = key;
    }

    /**
    * Set the ISO-639 language code to distinguish different pages in application scope
    *
    * @param language The language code for this cache entry.
    */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
    * This method allows the user to programatically decide whether the cached
    * content should be refreshed immediately.
    *
    * @param refresh Whether or not to refresh this cache entry immediately.
    */
    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    /**
    * Setting this to <code>true</code> prevents the cache from writing any output
    * to the response, however the JSP content is still cached as normal.
    * @param mode The cache mode to use.
    */
    public void setMode(String mode) {
        if ("silent".equalsIgnoreCase(mode)) {
            this.mode = SILENT_MODE;
        } else {
            this.mode = 0;
        }
    }

    /**
    * Class used to handle the refresh policy logic
    */
    public void setRefreshpolicyclass(String refreshPolicyClass) {
        this.refreshPolicyClass = refreshPolicyClass;
    }

    /**
    * Parameters that will be passed to the init method of the
    * refresh policy instance.
    */
    public void setRefreshpolicyparam(String refreshPolicyParam) {
        this.refreshPolicyParam = refreshPolicyParam;
    }

    // ----------- setMethods ------------------------------------------------------

    /**
    * Set the scope of this cache.
    * <p>
    * @param scope The scope of this cache. Either "application" (default) or "session".
    */
    public void setScope(String scope) {
        if (scope.equalsIgnoreCase(ServletCacheAdministrator.SESSION_SCOPE_NAME)) {
            this.scope = PageContext.SESSION_SCOPE;
        } else {
            this.scope = PageContext.APPLICATION_SCOPE;
        }
    }

    /**
    * Set the time this cache entry will be cached for (in seconds)
    *
    * @param time The time to cache this content (in seconds). Passing in
    * a time of zero will turn off the caching. A negative value for the
    * time will result in the cached content never expiring (ie, the cached
    * content will always be served if it is present)
    */
    public void setTime(int time) {
        this.time = time;
    }

    /**
    * This controls whether or not the body of the tag is evaluated or used.<p>
    *
    * It is most often called by the &lt;UseCached /&gt; tag to tell this tag to
    * use the cached content.
    *
    * @see UseCachedTag
    * @param useBody Whether or not to use the cached content.
    */
    public void setUseBody(boolean useBody) {
        if (log.isDebugEnabled()) {
            log.debug("<cache>: Set useBody to " + useBody);
        }

        this.useBody = useBody;
    }

    /**
    * After the cache body, either update the cache, serve new cached content or
    *  indicate an error.
    *
    * @throws JspTagException The standard exception thrown.
    * @return The standard BodyTag return.
    */
    public int doAfterBody() throws JspTagException {
        String body = null;

        try {
            // if we have a body, and we have not been told to use the cached version
            if ((bodyContent != null) && (useBody || (time == 0)) && ((body = bodyContent.getString()) != null)) {
                if ((time != 0) || (refreshPolicyClass != null)) {
                    // Instantiate custom refresh policy if needed
                    WebEntryRefreshPolicy policy = null;

                    if (refreshPolicyClass != null) {
                        try {
                            policy = (WebEntryRefreshPolicy) Class.forName(refreshPolicyClass).newInstance();
                            policy.init(actualKey, refreshPolicyParam);
                        } catch (Exception e) {
                            if (log.isInfoEnabled()) {
                                log.info("<cache>: Problem instantiating or initializing refresh policy : " + refreshPolicyClass);
                            }
                        }
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("<cache>: Updating cache entry with new content : " + actualKey);
                    }

                    cancelUpdateRequired = false;

                    if ((groups == null) || groups.isEmpty()) {
                        cache.putInCache(actualKey, body, policy);
                    } else {
                        String[] groupArray = new String[groups.size()];
                        groups.toArray(groupArray);
                        cache.putInCache(actualKey, body, groupArray, policy, null);
                    }
                }
            }
            // otherwise if we have been told to use the cached content and we have cached content
            else {
                if (!useBody && (content != null)) {
                    if (log.isInfoEnabled()) {
                        log.info("<cache>: Using cached version as instructed, useBody = false : " + actualKey);
                    }

                    body = content;
                }
                // either the cached entry is blank and a subtag has said don't useBody, or body is null
                else {
                    if (log.isInfoEnabled()) {
                        log.info("<cache>: Missing cached content : " + actualKey);
                    }

                    body = "Missing cached content";
                }
            }

            // Only display anything if we're not running in silent mode
            if (mode != SILENT_MODE) {
                bodyContent.clearBody();
                bodyContent.write(body);
                bodyContent.writeOut(bodyContent.getEnclosingWriter());
            }
        } catch (java.io.IOException e) {
            throw new JspTagException("IO Error: " + e.getMessage());
        }

        return SKIP_BODY;
    }

    public void doCatch(Throwable throwable) throws Throwable {
        throw throwable;
    }

    /**
    * The end tag - clean up variables used.
    *
    * @throws JspTagException The standard exception thrown.
    * @return The standard BodyTag return.
    */
    public int doEndTag() throws JspTagException {
        return EVAL_PAGE;
    }

    public void doFinally() {
        if (cancelUpdateRequired && (actualKey != null)) {
            cache.cancelUpdate(actualKey);
        }
        
        // reset all states, CACHE-144
        groups = null;
        scope = PageContext.APPLICATION_SCOPE;
        cron = null;
        key = null;
        language = null;
        refreshPolicyClass = null;
        refreshPolicyParam = null;
        time = DEFAULT_TIMEOUT;
        refresh = false;
        mode = 0;
    }

    /**
    * The start of the tag.
    * <p>
    * Grabs the administrator, the cache, the specific cache entry, then decides
    * whether to refresh.
    * <p>
    * If no refresh is needed, this serves the cached content directly.
    *
    * @throws JspTagException The standard exception thrown.
    * @return The standard doStartTag() return.
    */
    public int doStartTag() throws JspTagException {
        cancelUpdateRequired = false;
        useBody = true;
        content = null;

        // We can only skip the body if the cache has the data
        int returnCode = EVAL_BODY_BUFFERED;

        if (admin == null) {
            admin = ServletCacheAdministrator.getInstance(pageContext.getServletContext());
        }

        // Retrieve the cache
        if (scope == PageContext.SESSION_SCOPE) {
            cache = admin.getSessionScopeCache(((HttpServletRequest) pageContext.getRequest()).getSession(true));
        } else {
            cache = admin.getAppScopeCache(pageContext.getServletContext());
        }

        // This allows to have multiple cache tags on a single page without
        // having to specify keys. However, nested cache tags are not supported.
        // In that case you would have to supply a key.
        String suffix = null;

        if (key == null) {
            synchronized (pageContext.getRequest()) {
                Object o = pageContext.getRequest().getAttribute(CACHE_TAG_COUNTER_KEY);

                if (o == null) {
                    suffix = "1";
                } else {
                    suffix = Integer.toString(Integer.parseInt((String) o) + 1);
                }
            }

            pageContext.getRequest().setAttribute(CACHE_TAG_COUNTER_KEY, suffix);
        }

        // Generate the actual cache key
        actualKey = admin.generateEntryKey(key, (HttpServletRequest) pageContext.getRequest(), scope, language, suffix);

        /*
        if
        - refresh is not set,
        - the cacheEntry itself does not need to be refreshed before 'time' and
        - the administrator has not had the cache entry's scope flushed

        send out the cached version!
        */
        try {
            if (refresh) {
                // Force a refresh
                content = (String) cache.getFromCache(actualKey, 0, cron);
            } else {
                // Use the specified refresh period
                content = (String) cache.getFromCache(actualKey, time, cron);
            }

            try {
                if (log.isDebugEnabled()) {
                    log.debug("<cache>: Using cached entry : " + actualKey);
                }

                // Ensure that the cache returns the data correctly. Else re-evaluate the body
                if ((content != null)) {
                    if (mode != SILENT_MODE) {
                        pageContext.getOut().write(content);
                    }

                    returnCode = SKIP_BODY;
                }
            } catch (IOException e) {
                throw new JspTagException("IO Exception: " + e.getMessage());
            }
        } catch (NeedsRefreshException nre) {
            cancelUpdateRequired = true;
            content = (String) nre.getCacheContent();
        }

        if (returnCode == EVAL_BODY_BUFFERED) {
            if (log.isDebugEnabled()) {
                log.debug("<cache>: Cached content not used: New cache entry, cache stale or scope flushed : " + actualKey);
            }
        }

        return returnCode;
    }

    /**
    * Convert a SimpleDateFormat string to seconds
    * Acceptable format are :
    * <ul>
    * <li>0s (seconds)
    * <li>0m (minute)
    * <li>0h (hour)
    * <li>0d (day)
    * <li>0w (week)
    * </ul>
    * @param   duration The simple date time to parse
    * @return  The value in seconds
    */
    private int parseDuration(String duration) {
        int time = 0;

        //Detect if the factor is specified
        try {
            time = Integer.parseInt(duration);
        } catch (Exception ex) {
            //Extract number and ajust this number with the time factor
            for (int i = 0; i < duration.length(); i++) {
                if (!Character.isDigit(duration.charAt(i))) {
                    time = Integer.parseInt(duration.substring(0, i));

                    switch ((int) duration.charAt(i)) {
                        case (int) 's':
                            time *= SECOND;
                            break;
                        case (int) 'm':
                            time *= MINUTE;
                            break;
                        case (int) 'h':
                            time *= HOUR;
                            break;
                        case (int) 'd':
                            time *= DAY;
                            break;
                        case (int) 'w':
                            time *= WEEK;
                            break;
                        default:
                        //no defined use as is
                    }

                    break;
                }

                // if
            }

            // for
        }

        // catch
        return time;
    }

    /**
    * Parse an ISO-8601 format date and return it's value in seconds
    *
    * @param duration The ISO-8601 date
    * @return The equivalent number of seconds
    * @throws Exception
    */
    private int parseISO_8601_Duration(String duration) throws Exception {
        int years = 0;
        int months = 0;
        int days = 0;
        int hours = 0;
        int mins = 0;
        int secs = 0;

        // If there is a negative sign, it must be first
        // If it is present, we will ignore it
        int index = duration.indexOf("-");

        if (index > 0) {
            throw new Exception("Invalid duration (- must be at the beginning)");
        }

        // First caracter must be P
        String workValue = duration.substring(index + 1);

        if (workValue.charAt(0) != 'P') {
            throw new Exception("Invalid duration (P must be at the beginning)");
        }

        // Must contain a value
        workValue = workValue.substring(1);

        if (workValue.length() == 0) {
            throw new Exception("Invalid duration (nothing specified)");
        }

        // Check if there is a T
        index = workValue.indexOf('T');

        String timeString = "";

        if (index > 0) {
            timeString = workValue.substring(index + 1);

            // Time cannot be empty
            if (timeString.equals("")) {
                throw new Exception("Invalid duration (T with no time)");
            }

            workValue = workValue.substring(0, index);
        } else if (index == 0) {
            timeString = workValue.substring(1);
            workValue = "";
        }

        if (!workValue.equals("")) {
            validateDateFormat(workValue);

            int yearIndex = workValue.indexOf('Y');
            int monthIndex = workValue.indexOf('M');
            int dayIndex = workValue.indexOf('D');

            if ((yearIndex != -1) && (monthIndex != -1) && (yearIndex > monthIndex)) {
                throw new Exception("Invalid duration (Date part not properly specified)");
            }

            if ((yearIndex != -1) && (dayIndex != -1) && (yearIndex > dayIndex)) {
                throw new Exception("Invalid duration (Date part not properly specified)");
            }

            if ((dayIndex != -1) && (monthIndex != -1) && (monthIndex > dayIndex)) {
                throw new Exception("Invalid duration (Date part not properly specified)");
            }

            if (yearIndex >= 0) {
                years = (new Integer(workValue.substring(0, yearIndex))).intValue();
            }

            if (monthIndex >= 0) {
                months = (new Integer(workValue.substring(yearIndex + 1, monthIndex))).intValue();
            }

            if (dayIndex >= 0) {
                if (monthIndex >= 0) {
                    days = (new Integer(workValue.substring(monthIndex + 1, dayIndex))).intValue();
                } else {
                    if (yearIndex >= 0) {
                        days = (new Integer(workValue.substring(yearIndex + 1, dayIndex))).intValue();
                    } else {
                        days = (new Integer(workValue.substring(0, dayIndex))).intValue();
                    }
                }
            }
        }

        if (!timeString.equals("")) {
            validateHourFormat(timeString);

            int hourIndex = timeString.indexOf('H');
            int minuteIndex = timeString.indexOf('M');
            int secondIndex = timeString.indexOf('S');

            if ((hourIndex != -1) && (minuteIndex != -1) && (hourIndex > minuteIndex)) {
                throw new Exception("Invalid duration (Time part not properly specified)");
            }

            if ((hourIndex != -1) && (secondIndex != -1) && (hourIndex > secondIndex)) {
                throw new Exception("Invalid duration (Time part not properly specified)");
            }

            if ((secondIndex != -1) && (minuteIndex != -1) && (minuteIndex > secondIndex)) {
                throw new Exception("Invalid duration (Time part not properly specified)");
            }

            if (hourIndex >= 0) {
                hours = (new Integer(timeString.substring(0, hourIndex))).intValue();
            }

            if (minuteIndex >= 0) {
                mins = (new Integer(timeString.substring(hourIndex + 1, minuteIndex))).intValue();
            }

            if (secondIndex >= 0) {
                if (timeString.length() != (secondIndex + 1)) {
                    throw new Exception("Invalid duration (Time part not properly specified)");
                }

                if (minuteIndex >= 0) {
                    timeString = timeString.substring(minuteIndex + 1, timeString.length() - 1);
                } else {
                    if (hourIndex >= 0) {
                        timeString = timeString.substring(hourIndex + 1, timeString.length() - 1);
                    } else {
                        timeString = timeString.substring(0, timeString.length() - 1);
                    }
                }

                if (timeString.indexOf('.') == (timeString.length() - 1)) {
                    throw new Exception("Invalid duration (Time part not properly specified)");
                }

                secs = (new Double(timeString)).intValue();
            }
        }

        // Compute Value
        return secs + (mins * MINUTE) + (hours * HOUR) + (days * DAY) + (months * MONTH) + (years * YEAR);
    }

    /**
    * Validate the basic date format
    *
    * @param basicDate The string to validate
    * @throws Exception
    */
    private void validateDateFormat(String basicDate) throws Exception {
        int yearCounter = 0;
        int monthCounter = 0;
        int dayCounter = 0;

        for (int counter = 0; counter < basicDate.length(); counter++) {
            // Check if there's any other caracters than Y, M, D and numbers
            if (!Character.isDigit(basicDate.charAt(counter)) && (basicDate.charAt(counter) != 'Y') && (basicDate.charAt(counter) != 'M') && (basicDate.charAt(counter) != 'D')) {
                throw new Exception("Invalid duration (Date part not properly specified)");
            }

            // Check if the allowed caracters are present more than 1 time
            if (basicDate.charAt(counter) == 'Y') {
                yearCounter++;
            }

            if (basicDate.charAt(counter) == 'M') {
                monthCounter++;
            }

            if (basicDate.charAt(counter) == 'D') {
                dayCounter++;
            }
        }

        if ((yearCounter > 1) || (monthCounter > 1) || (dayCounter > 1)) {
            throw new Exception("Invalid duration (Date part not properly specified)");
        }
    }

    /**
    * Validate the basic hour format
    *
    * @param basicHour The string to validate
    * @throws Exception
    */
    private void validateHourFormat(String basicHour) throws Exception {
        int minuteCounter = 0;
        int secondCounter = 0;
        int hourCounter = 0;

        for (int counter = 0; counter < basicHour.length(); counter++) {
            if (!Character.isDigit(basicHour.charAt(counter)) && (basicHour.charAt(counter) != 'H') && (basicHour.charAt(counter) != 'M') && (basicHour.charAt(counter) != 'S') && (basicHour.charAt(counter) != '.')) {
                throw new Exception("Invalid duration (Time part not properly specified)");
            }

            if (basicHour.charAt(counter) == 'H') {
                hourCounter++;
            }

            if (basicHour.charAt(counter) == 'M') {
                minuteCounter++;
            }

            if (basicHour.charAt(counter) == 'S') {
                secondCounter++;
            }
        }

        if ((hourCounter > 1) || (minuteCounter > 1) || (secondCounter > 1)) {
            throw new Exception("Invalid duration (Time part not properly specified)");
        }
    }
}
