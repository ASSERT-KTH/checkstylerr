/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util.logging;

import java.util.logging.Level;


/**
 * Class IASLevel
 */
public class IASLevel extends Level
{

    /**
     * Constructor IASLevel
     *
     *
     * @param name
     * @param value
     */
    protected IASLevel(String name, int value) {
        super(name, value);
    }

    /**
     * Field
     */
    public static final IASLevel ALERT = new IASLevel("ALERT", 1100);

    /**
     * Field
     */
    public static final IASLevel FATAL = new IASLevel("FATAL", 1200);
}
