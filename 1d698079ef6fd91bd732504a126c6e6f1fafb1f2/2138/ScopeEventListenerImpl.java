/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.extra;

import com.opensymphony.oscache.base.events.ScopeEvent;
import com.opensymphony.oscache.base.events.ScopeEventListener;
import com.opensymphony.oscache.base.events.ScopeEventType;

/**
 * Implementation of a ScopeEventListener that keeps track of the scope flush events.
 * We are not using any synchronized so that this does not become a bottleneck.
 * The consequence is that on retrieving values, the operations that are
 * currently being done won't be counted.
 *
 * @version        $Revision: 254 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public class ScopeEventListenerImpl implements ScopeEventListener {
    /**
     * Scope names
     */
    public static final String[] SCOPE_NAMES = {
        null, "page", "request", "session", "application"
    };

    /**
     * Number of known scopes
     */
    public static final int NB_SCOPES = SCOPE_NAMES.length - 1;

    /**
     * Page scope number
     */
    public static final int PAGE_SCOPE = 1;

    /**
     * Request scope number
     */
    public static final int REQUEST_SCOPE = 2;

    /**
     * Session scope number
     */
    public static final int SESSION_SCOPE = 3;

    /**
     * Application scope number
     */
    public static final int APPLICATION_SCOPE = 4;

    /**
     * Flush counter for all scopes.
     * Add one to the number of scope because the array is being used
     * from position 1 instead of 0 for convenience
     */
    private int[] scopeFlushCount = new int[NB_SCOPES + 1];

    public ScopeEventListenerImpl() {
    }

    /**
     * Gets the flush count for scope {@link ScopeEventListenerImpl#APPLICATION_SCOPE}.
     * <p>
     * @return The total number of application flush
     */
    public int getApplicationScopeFlushCount() {
        return scopeFlushCount[APPLICATION_SCOPE];
    }

    /**
     * Gets the flush count for scope {@link ScopeEventListenerImpl#PAGE_SCOPE}.
     * @return The total number of page flush
     */
    public int getPageScopeFlushCount() {
        return scopeFlushCount[PAGE_SCOPE];
    }

    /**
     * Gets the flush count for scope {@link ScopeEventListenerImpl#REQUEST_SCOPE}.
     * @return The total number of request flush
     */
    public int getRequestScopeFlushCount() {
        return scopeFlushCount[REQUEST_SCOPE];
    }

    /**
     * Gets the flush count for scope {@link ScopeEventListenerImpl#SESSION_SCOPE}.
     * @return The total number of session flush
     */
    public int getSessionScopeFlushCount() {
        return scopeFlushCount[SESSION_SCOPE];
    }

    /**
     * Returns the total flush count.
     * @return The total number of scope flush
     */
    public int getTotalScopeFlushCount() {
        int total = 0;

        for (int count = 1; count <= NB_SCOPES; count++) {
            total += scopeFlushCount[count];
        }

        return total;
    }

    /**
     * Handles all the scope flush events.
     * @param event The scope event
     */
    public void scopeFlushed(ScopeEvent event) {
        // Get the event type and process it
        ScopeEventType eventType = event.getEventType();

        if (eventType == ScopeEventType.ALL_SCOPES_FLUSHED) {
            // All 4 scopes were flushed, increment the counters
            for (int count = 1; count <= NB_SCOPES; count++) {
                scopeFlushCount[count]++;
            }
        } else if (eventType == ScopeEventType.SCOPE_FLUSHED) {
            // Get back the scope from the event and increment the flush count
            scopeFlushCount[event.getScope()]++;
        } else {
            // Unknown event!
            throw new IllegalArgumentException("Unknown Scope Event type received");
        }
    }

    /**
     * Returns all the flush counter in a string form.
     */
    public String toString() {
        StringBuffer returnString = new StringBuffer("Flush count for ");

        for (int count = 1; count <= NB_SCOPES; count++) {
            returnString.append("scope " + SCOPE_NAMES[count] + " = " + scopeFlushCount[count] + ", ");
        }

        // Remove the last 2 chars, which are ", "
        returnString.setLength(returnString.length() - 2);

        return returnString.toString();
    }
}
