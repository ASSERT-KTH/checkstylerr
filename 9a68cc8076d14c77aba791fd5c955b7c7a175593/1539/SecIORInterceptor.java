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

package com.sun.enterprise.iiop.security;

import org.glassfish.orb.admin.config.IiopListener;
import java.util.logging.*;
import java.util.List;

import org.omg.IOP.Codec;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;

import com.sun.logging.*;
import com.sun.enterprise.deployment.EjbDescriptor;
//import com.sun.enterprise.util.ORBManager;
//import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.glassfish.enterprise.iiop.util.IIOPUtils;
import org.glassfish.gms.bootstrap.GMSAdapter;
import org.glassfish.gms.bootstrap.GMSAdapterService;
import org.omg.CORBA.ORB;

public class SecIORInterceptor extends org.omg.CORBA.LocalObject implements org.omg.PortableInterceptor.IORInterceptor {

    private static Logger _logger = null;

    static {
        _logger = LogDomains.getLogger(SecIORInterceptor.class, LogDomains.SECURITY_LOGGER);
    }

    private Codec codec;
    private GMSAdapterService gmsAdapterService;
    private GMSAdapter gmsAdapter;

    // private GlassFishORBHelper helper = null;
    private ORB orb;

    public SecIORInterceptor(Codec c, ORB orb) {
        codec = c;
        this.orb = orb;
        this.gmsAdapterService = Lookups.getGMSAdapterService();
        if (this.gmsAdapterService == null) {
            this.gmsAdapter = null;
        } else {
            this.gmsAdapter = gmsAdapterService.getGMSAdapter();
        }
    }

    public void destroy() {
    }

    public String name() {
        return "SecIORInterceptor";
    }

    // Note: this is called for all remote refs created from this ORB,
    // including EJBs and COSNaming objects.
    public void establish_components(IORInfo iorInfo) {
        try {
            _logger.log(Level.FINE, "SecIORInterceptor.establish_components->:");
            // EjbDescriptor desc = CSIV2TaggedComponentInfo.getEjbDescriptor( iorInfo ) ;
            addCSIv2Components(iorInfo);
        } catch (Exception e) {
            _logger.log(Level.WARNING, "Exception in establish_components", e);
        } finally {
            _logger.log(Level.FINE, "SecIORInterceptor.establish_components<-:");
        }
    }

    private void addCSIv2Components(IORInfo iorInfo) {
        EjbDescriptor desc = null;
        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, ".addCSIv2Components->: " + " " + iorInfo);
            }

            if (gmsAdapter != null) {

                // If this app server instance is part of a dynamic cluster (that is,
                // one that supports RMI-IIOP failover and load balancing, DO NOT
                // create the CSIv2 components here. Instead, handle this in the
                // ORB's ServerGroupManager, in conjunctions with the
                // CSIv2SSLTaggedComponentHandler.
                return;
            }

            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, ".addCSIv2Components ");
            }

            // ORB orb = helper.getORB();
            int sslMutualAuthPort = getServerPort("SSL_MUTUALAUTH");
//    try {
//        sslMutualAuthPort = ((com.sun.corba.ee.spi.legacy.interceptor.IORInfoExt)iorInfo).
//                                getServerPort("SSL_MUTUALAUTH");
//    } catch (com.sun.corba.ee.spi.legacy.interceptor.UnknownType ute) {
//            _logger.log(Level.FINE,"UnknownType exception", ute);
//    }

            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, ".addCSIv2Components: sslMutualAuthPort: " + sslMutualAuthPort);
            }

            CSIV2TaggedComponentInfo ctc = new CSIV2TaggedComponentInfo(orb, sslMutualAuthPort);
            desc = ctc.getEjbDescriptor(iorInfo);

            // Create CSIv2 tagged component
            int sslport = getServerPort("SSL");
//    try {
//        sslport = ((com.sun.corba.ee.spi.legacy.interceptor.IORInfoExt)iorInfo).
//                                getServerPort("SSL");
//    } catch (com.sun.corba.ee.spi.legacy.interceptor.UnknownType ute) {
//            _logger.log(Level.FINE,"UnknownType exception", ute);
//    }
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, ".addCSIv2Components: sslport: " + sslport);
            }

            TaggedComponent csiv2Comp = null;
            if (desc != null) {
                csiv2Comp = ctc.createSecurityTaggedComponent(sslport, desc);
            } else {
                // this is not an EJB object, must be a non-EJB CORBA object
                csiv2Comp = ctc.createSecurityTaggedComponent(sslport);
            }

            iorInfo.add_ior_component(csiv2Comp);

        } finally {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, ".addCSIv2Components<-: " + " " + iorInfo + " " + desc);
            }
        }
    }

    private int getServerPort(String mech) {

        List<IiopListener> listenersList = IIOPUtils.getInstance().getIiopService().getIiopListener();
        IiopListener[] iiopListenerBeans = listenersList.toArray(new IiopListener[listenersList.size()]);

        for (IiopListener ilisten : iiopListenerBeans) {
            if (mech.equalsIgnoreCase("SSL")) {
                if (ilisten.getSecurityEnabled().equalsIgnoreCase("true") && ilisten.getSsl() != null
                        && !ilisten.getSsl().getClientAuthEnabled().equalsIgnoreCase("true")) {
                    return Integer.parseInt(ilisten.getPort());
                }
            } else if (mech.equalsIgnoreCase("SSL_MUTUALAUTH")) {
                if (ilisten.getSecurityEnabled().equalsIgnoreCase("true") && ilisten.getSsl() != null
                        && ilisten.getSsl().getClientAuthEnabled().equalsIgnoreCase("true")) {
                    return Integer.parseInt(ilisten.getPort());
                }
            } else if (!ilisten.getSecurityEnabled().equalsIgnoreCase("true")) {
                return Integer.parseInt(ilisten.getPort());
            }
        }
        return -1;
    }
}

// End of file.
