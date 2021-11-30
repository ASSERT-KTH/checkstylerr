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

import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.resource.ClientSecurityInfo;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.listener.ConnectionEventListener;
import com.sun.enterprise.resource.pool.PoolManager;
import com.sun.appserv.connectors.internal.api.PoolingException;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import java.util.logging.Level;

/**
 * @author Tony Ng
 */
public class NoTxConnectorAllocator extends AbstractConnectorAllocator {

    class ConnectionListenerImpl extends ConnectionEventListener {
        private ResourceHandle resource;

        public ConnectionListenerImpl(ResourceHandle resource) {
            this.resource = resource;
        }

        public void connectionClosed(ConnectionEvent evt) {
            poolMgr.putbackResourceToPool(resource, false);
        }

        /**
         * Resource adapters will signal that the connection being closed is bad.
         *
         * @param evt ConnectionEvent
         */
        public void badConnectionClosed(ConnectionEvent evt) {
            ManagedConnection mc = (ManagedConnection) evt.getSource();
            mc.removeConnectionEventListener(this);
            poolMgr.badResourceClosed(resource);
        }

        public void connectionErrorOccurred(ConnectionEvent evt) {
            ManagedConnection mc = (ManagedConnection) evt.getSource();
            mc.removeConnectionEventListener(this);
            poolMgr.putbackResourceToPool(resource, true);
/*
            try {
                mc.destroy();
            } catch (Exception ex) {
                // ignore exception
            }
*/
            //GJCINT
            resource.setConnectionErrorOccurred();
        }

        public void localTransactionStarted(ConnectionEvent evt) {
            throw new IllegalStateException("local transaction not supported");
        }

        public void localTransactionCommitted(ConnectionEvent evt) {
            throw new IllegalStateException("local transaction not supported");
        }

        public void localTransactionRolledback(ConnectionEvent evt) {
            throw new IllegalStateException("local transaction not supported");
        }
    }

    public NoTxConnectorAllocator(PoolManager poolMgr,
                                  ManagedConnectionFactory mcf,
                                  ResourceSpec spec,
                                  Subject subject,
                                  ConnectionRequestInfo reqInfo,
                                  ClientSecurityInfo info,
                                  ConnectorDescriptor desc) {
        super(poolMgr, mcf, spec, subject, reqInfo, info, desc);
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
            resource.fillInResourceObjects(con, null);
        } catch (ResourceException ex) {
            throw new PoolingException(ex);
        }
    }

    public void destroyResource(ResourceHandle resource)
            throws PoolingException {

        try {
            ManagedConnection mc = (ManagedConnection) resource.getResource();
            mc.destroy();
        } catch (Exception ex) {
            throw new PoolingException(ex);
        }
    }

    public boolean isTransactional() {
        return false;
    }
}
