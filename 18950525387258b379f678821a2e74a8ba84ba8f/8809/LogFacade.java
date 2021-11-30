/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.naming.util;

import org.glassfish.logging.annotation.LoggerInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;

import java.util.logging.Logger;

public class LogFacade {
    @LoggerInfo(subsystem = "glassfish-naming", description = "logger for GlassFish appserver naming", publish = true)
    public static final String NAMING_LOGGER_NAME = "org.glassfish.naming";

    @LogMessagesResourceBundle
    public static final String NAMING_LOGGER_RB = NAMING_LOGGER_NAME + ".LogMessages";

    public static final Logger logger = Logger.getLogger(NAMING_LOGGER_NAME, NAMING_LOGGER_RB);

    private LogFacade() {}

    public static Logger getLogger() {
        return logger;
    }
}
