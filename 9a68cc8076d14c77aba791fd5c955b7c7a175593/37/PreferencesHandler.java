/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.common.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import java.util.List;

/**
 *  <p>This class contains handlers for managing preferences.</p>
 *
 * @author jasonlee
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class PreferencesHandler {

    /**
     * <p> This handler should be used whenever you want to add a Tag to a
     *     page.  If the exact same Tag is added twice, it will be
     *     ignored.  If "user" is not specified the current principal user
     *     will be used for this value.</p>
     */
    @Handler(id = "gf.addTag",
        input = {
            @HandlerInput(name="tagName", type=String.class, required=true),
            @HandlerInput(name="tagViewId", type=String.class, required=true),
            @HandlerInput(name="displayName", type=String.class),
            @HandlerInput(name="user", type=String.class)
        }
    )
    public static void saveTagInformation(HandlerContext handlerCtx) {
        String user = (String) handlerCtx.getInputValue("user");
        if (user == null) {
            user = handlerCtx.getFacesContext().getExternalContext().
                    getUserPrincipal().getName();
        }
        TagSupport.addTag(
            (String) handlerCtx.getInputValue("tagName"),
            (String) handlerCtx.getInputValue("tagViewId"),
            (String) handlerCtx.getInputValue("displayName"),
            user);
    }

    /**
     * <p> This handler provides a way to search for tags.  All 3 properties
     *     are optional.  If none are specified, all tags will be returned.
     *     If more than one are specified, tags matching all specified
     *     criteria will be returned.</p>
     */
    @Handler(id="gf.queryTags",
        input = {
            @HandlerInput(name="tagName", type=String.class),
            @HandlerInput(name="tagViewId", type=String.class),
            @HandlerInput(name="user", type=String.class)
            },
        output = {
            @HandlerOutput(name="results", type=List.class) })
    public static void searchTags(HandlerContext handlerCtx) {
        // Perform Search
        List<Tag> results = TagSupport.queryTags(
            (String) handlerCtx.getInputValue("tagName"),
            (String) handlerCtx.getInputValue("tagViewId"),
            (String) handlerCtx.getInputValue("user"));

        // Set the results...
        handlerCtx.setOutputValue("results", results);
    }

    /**
     * <p> This handler provides a way to remove tags.  If the user is not
     *     specified, the current "principal user" will be used.</p>
     */
    @Handler(id="gf.removeTag",
        input = {
            @HandlerInput(name="tagName", type=String.class, required=true),
            @HandlerInput(name="tagViewId", type=String.class, required=true),
            @HandlerInput(name="user", type=String.class) } )
    public static void removeTag(HandlerContext handlerCtx) {
        // Make sure we have the user...
        String user = (String) handlerCtx.getInputValue("user");
        if (user == null) {
            user = handlerCtx.getFacesContext().getExternalContext().
                    getUserPrincipal().getName();
        }

        // Delete...
        TagSupport.removeTag(
            (String) handlerCtx.getInputValue("tagName"),
            (String) handlerCtx.getInputValue("tagViewId"),
            user);
    }

    /**
     * <p> This handler normalizes the given tagViewId.  This is required in
     *     order to ensure tagViewId's are compared the same way every
     *     time.</p>
     */
    @Handler(id="gf.normalizeTagViewId",
        input = {
            @HandlerInput(name="viewId", type=String.class, required=true) },
        output = {
            @HandlerOutput(name="tagViewId", type=String.class )})
    public static void normalizeTagViewId(HandlerContext handlerCtx) {
        handlerCtx.setOutputValue("tagViewId",
            TagSupport.normalizeTagViewId(
                    (String) handlerCtx.getInputValue("viewId")));
    }
}
