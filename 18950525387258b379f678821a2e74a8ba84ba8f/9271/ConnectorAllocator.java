/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.allocator;


import java.util.logging.Level;

import javax.transaction.xa.XAResource;
import jakarta.resource.spi.*;
import jakarta.resource.ResourceException;
import javax.security.auth.Subject;

import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.resource.pool.PoolManager;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.ClientSecurityInfo;

import com.sun.appserv.connectors.internal.api.PoolingException;


/**
 * @author Tony Ng
 */
public class ConnectorAllocator extends AbstractConnectorAllocator {

    private boolean shareable;


    class ConnectionListenerImpl extends com.sun.enterprise.resource.listener.ConnectionEventListener {
        private ResourceHandle resource;

        public ConnectionListenerImpl(ResourceHandle resource) {
            this.resource = resource;
        }

        public void connectionClosed(ConnectionEvent evt) {
            if (resource.hasConnectionErrorOccurred()) {
                return;
            }
            resource.decrementCount();
            if (resource.getShareCount() == 0) {
                poolMgr.resourceClosed(resource);
            }
        }

        /**
         * Resource adapters will signal that the connection being closed is bad.
         *
         * @param evt ConnectionEvent
         */
        public void badConnectionClosed(ConnectionEvent evt) {

            if (resource.hasConnectionErrorOccurred()) {
                return;
            }
            resource.decrementCount();
            if (resource.getShareCount() == 0) {
                ManagedConnection mc = (ManagedConnection) evt.getSource();
                mc.removeConnectionEventListener(this);
                poolMgr.badResourceClosed(resource);
            }
        }

        /**
         * Resource adapters will signal that the connection is being aborted.
         *
         * @param evt ConnectionEvent
         */
        public void connectionAbortOccurred(ConnectionEvent evt) {
            resource.setConnectionErrorOccurred();

            ManagedConnection mc = (ManagedConnection) evt.getSource();
            mc.removeConnectionEventListener(this);
            poolMgr.resourceAbortOccurred(resource);
        }

        public void connectionErrorOccurred(ConnectionEvent evt) {
            resource.setConnectionErrorOccurred();

            ManagedConnection mc = (ManagedConnection) evt.getSource();
            mc.removeConnectionEventListener(this);
            poolMgr.resourceErrorOccurred(resource);
/*
            try {
                mc.destroy();
            } catch (Exception ex) {
                // ignore exception
            }
*/
        }

        public void localTransactionStarted(ConnectionEvent evt) {
            // no-op
        }

        public void localTransactionCommitted(ConnectionEvent evt) {
            // no-op
        }

        public void localTransactionRolledback(ConnectionEvent evt) {
            // no-op
        }
    }

    public ConnectorAllocator(PoolManager poolMgr,
                              ManagedConnectionFactory mcf,
                              ResourceSpec spec,
                              Subject subject,
                              ConnectionRequestInfo reqInfo,
                              ClientSecurityInfo info,
                              ConnectorDescriptor desc,
                              boolean shareable) {
        super(poolMgr, mcf, spec, subject, reqInfo, info, desc);
        this.shareable = shareable;
    }


    public ResourceHandle createResource()
            throws PoolingException {
        try {
            ManagedConnection mc =
                    mcf.createManagedConnection(subject, reqInfo);

            ResourceHandle resource =
                    createResourceHandle(mc, spec, this, info);
            ConnectionEventListener l =
                    new ConnectionListenerImpl(resource);
            mc.addConnectionEventListener(l);
            return resource;
        } catch (ResourceException ex) {
            Object[] params = new Object[]{spec.getPoolInfo(), ex.toString()};
            _logger.log(Level.WARNING,"poolmgr.create_resource_error",params);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,"Resource Exception while creating resource",ex);
            }

            if (ex.getLinkedException() != null) {
                _logger.log(Level.WARNING,
                        "poolmgr.create_resource_linked_error", ex
                                .getLinkedException().toString());
            }
            throw new PoolingException(ex);
        }
    }

    public void fillInResourceObjects(ResourceHandle resource)
            throws PoolingException {
        try {
            ManagedConnection mc = (ManagedConnection) resource.getResource();
            Object con = mc.getConnection(subject, reqInfo);
            resource.incrementCount();
            XAResource xares = mc.getXAResource();
            resource.fillInResourceObjects(con, xares);
        } catch (ResourceException ex) {
            throw new PoolingException(ex);
        }
    }

    public void destroyResource(ResourceHandle resource)
            throws PoolingException {

        try {
            closeUserConnection(resource);
        } catch (Exception ex) {
            // ignore error
        }

        try {
            ManagedConnection mc = (ManagedConnection) resource.getResource();
            mc.destroy();
        } catch (Exception ex) {
            throw new PoolingException(ex);
        }

    }

    public boolean shareableWithinComponent() {
        return shareable;
    }
}
