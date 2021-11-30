/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.impl.authorization;

import org.glassfish.security.services.api.authorization.AzAction;

public final class AzActionImpl extends AzAttributesImpl implements AzAction {

    private final String action;


    /**
     * Constructor
     *
     * @param action The represented action, null or "*" to represent all actions
     */
    public AzActionImpl( String action )  {
        super( NAME );

        if ( "*".equals( action ) ) {
            action = null;
        }
        this.action = action;
    }


    /**
     * Determines the action represented as a string.
     * @return The represented action, null represents all actions
     * @see org.glassfish.security.services.api.authorization.AzAction#getAction()
     */
    @Override
    public String getAction() {
        return action;
    }


    /**
     * Determines the represented action, "*" represents all actions
     * @return The represented action.
     */
    @Override
    public String toString() {
        return action == null ? "*" : action;
    }
}
