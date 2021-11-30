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

package com.sun.web.security;

import java.util.logging.Logger;

import org.apache.catalina.Realm;
import org.apache.catalina.core.ContainerBase;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.ComponentInvocation.ComponentInvocationType;
import org.glassfish.api.invocation.ComponentInvocationHandler;
import org.glassfish.api.invocation.InvocationException;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.invocation.RegisteredComponentInvocationHandler;
import org.jvnet.hk2.annotations.Service;

import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Service(name = "webSecurityCIH")
@Singleton
public class WebSecurityComponentInvocationHandler implements RegisteredComponentInvocationHandler {

    private static Logger _logger = null;

    @Inject
    private InvocationManager invManager;

    static {
        _logger = LogDomains.getLogger(WebSecurityComponentInvocationHandler.class, LogDomains.EJB_LOGGER);
    }

    private ComponentInvocationHandler webSecurityCompInvHandler = new ComponentInvocationHandler() {

        @Override
        public void beforePreInvoke(ComponentInvocationType invType, ComponentInvocation prevInv, ComponentInvocation newInv)
                throws InvocationException {
            if (invType == ComponentInvocationType.SERVLET_INVOCATION) {
                Object cont = newInv.getContainer();
                if (cont instanceof ContainerBase) {
                    Realm realm = ((ContainerBase) cont).getRealm();
                    if (realm instanceof RealmAdapter) {
                        ((RealmAdapter) realm).preSetRunAsIdentity(newInv);
                    }
                }
            }
        }

        @Override
        public void afterPreInvoke(ComponentInvocationType invType, ComponentInvocation prevInv, ComponentInvocation curInv)
                throws InvocationException {
        }

        @Override
        public void beforePostInvoke(ComponentInvocationType invType, ComponentInvocation prevInv, ComponentInvocation curInv)
                throws InvocationException {
        }

        @Override
        public void afterPostInvoke(ComponentInvocationType invType, ComponentInvocation prevInv, ComponentInvocation curInv)
                throws InvocationException {
            if (invType == ComponentInvocationType.SERVLET_INVOCATION) {
                Object cont = curInv.getContainer();
                if (cont instanceof ContainerBase) {
                    Realm realm = ((ContainerBase) cont).getRealm();
                    if (realm instanceof RealmAdapter) {
                        ((RealmAdapter) realm).postSetRunAsIdentity(curInv);
                    }
                }
            }
        }
    };

    @Override
    public ComponentInvocationHandler getComponentInvocationHandler() {
        return webSecurityCompInvHandler;
    }

    @Override
    public void register() {
        invManager.registerComponentInvocationHandler(ComponentInvocationType.SERVLET_INVOCATION, this);
    }

}
