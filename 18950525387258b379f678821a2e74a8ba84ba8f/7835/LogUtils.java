/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.webservices.transport.tcp;

import java.util.logging.Logger;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 *
 * @author Lukas Jungmann
 */
public final class LogUtils {

    private static final String LOGMSG_PREFIX = "AS-WSSOAPTCP";

    @LogMessagesResourceBundle
    public static final String LOG_MESSAGES = "org.glassfish.webservices.transport.tcp.LogMessages";

    @LoggerInfo(subsystem = "WEBSERVICES", description = "SOAP/TCP Transport Logger", publish = true)
    public static final String LOG_DOMAIN = "jakarta.enterprise.webservices.transport.tcp";

    private static final Logger LOGGER = Logger.getLogger(LOG_DOMAIN, LOG_MESSAGES);

    public static Logger getLogger() {
        return LOGGER;
    }

    @LogMessageInfo(
            message = "Initialize SOAP/TCP protocol for port: {0}",
            comment = "{0} - port number",
            level = "INFO")
    public static final String SOAPTCP_PROTOCOL_INITIALIZED = LOGMSG_PREFIX + "-00001";

    @LogMessageInfo(
            message = "Can not convert SOAP/TCP protocol id to byte array.",
            level = "WARNING")
    public static final String CANNOT_CONVERT_PROTOCOL_ID = LOGMSG_PREFIX + "-00002";

    @LogMessageInfo(
            message = "SOAP/TCP endpoint removed: {0}",
            level = "FINE")
    public static final String SOAPTCP_ENDPOINT_REMOVED = LOGMSG_PREFIX + "-00003";

    @LogMessageInfo(
            message = "SOAP/TCP endpoint added: {0}",
            level = "FINE")
    public static final String SOAPTCP_ENDPOINT_ADDED = LOGMSG_PREFIX + "-00004";

    @LogMessageInfo(
            message = "Following exception was thrown",
            level = "INFO")
    public static final String EXCEPTION_THROWN = LOGMSG_PREFIX + "-00050";

    @LogMessageInfo(
            message = "WSTCPAdapterRegistryImpl. Register adapter. Path: {0}",
            level = "FINE")
    public static final String ADAPTER_REGISTERED = LOGMSG_PREFIX + "-00006";

    @LogMessageInfo(
            message = "WSTCPAdapterRegistryImpl. DeRegister adapter for {0}",
            level = "FINE")
    public static final String ADAPTER_DEREGISTERED = LOGMSG_PREFIX + "-00007";
}
