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

package org.glassfish.internal.api;

import org.omg.CORBA.*;
import org.jvnet.hk2.annotations.*;

/**
 * Contract for ORB provider.
 *
 * @author Jerome Dochez
 */
@Contract
public interface ORBLocator {
    public static final String JNDI_CORBA_ORB_PROPERTY =
        "java.naming.corba.orb";
    public static final String JNDI_PROVIDER_URL_PROPERTY =
        "java.naming.provider.url";
    public static final String OMG_ORB_INIT_HOST_PROPERTY =
        "org.omg.CORBA.ORBInitialHost";
    public static final String OMG_ORB_INIT_PORT_PROPERTY =
        "org.omg.CORBA.ORBInitialPort";

    // Same as ORBConstants.FOLB_CLIENT_GROUP_INFO_SERVICE,
    // but we can't reference ORBConstants from the naming bundle!
    public static final String FOLB_CLIENT_GROUP_INFO_SERVICE =
        "FolbClientGroupInfoService" ;

    public static final String DEFAULT_ORB_INIT_HOST = "localhost";
    public static final String DEFAULT_ORB_INIT_PORT = "3700";


    // This property is true if SSL is required to be used by
    // non-EJB CORBA objects in the server.
    public static final String ORB_SSL_SERVER_REQUIRED =
            "com.sun.CSIV2.ssl.server.required";
    //
    // This property is true if client authentication is required by
    // non-EJB CORBA objects in the server.
    public static final String ORB_CLIENT_AUTH_REQUIRED =
            "com.sun.CSIV2.client.auth.required";

     // This property is true (in appclient Main)
    // if SSL is required to be used by clients.
    public static final String ORB_SSL_CLIENT_REQUIRED =
            "com.sun.CSIV2.ssl.client.required";

    /**
     * Get or create the default orb.  This can be called for any process type.  However,
     * protocol manager and CosNaming initialization only take place for the Server.
     * @return an initialized ORB instance
     */
    public ORB getORB();

    public int getORBPort(ORB orb);

    public String getORBHost(ORB orb);
}
