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

package com.sun.enterprise.resource.pool;

import com.sun.appserv.connectors.internal.api.ConnectorConstants.PoolType;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.appserv.connectors.internal.api.TransactedPoolManager;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.resource.ClientSecurityInfo;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.listener.PoolLifeCycle;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.jvnet.hk2.annotations.Contract;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.RetryableUnavailableException;
import jakarta.transaction.Transaction;
import java.util.Hashtable;

/**
 * PoolManager manages jdbc and connector connection pool
 */
@Contract
public interface PoolManager extends TransactedPoolManager {

    // transaction support levels
    static public final int NO_TRANSACTION = 0;
    static public final int LOCAL_TRANSACTION = 1;
    static public final int XA_TRANSACTION = 2;

    // Authentication mechanism levels
    static public final int BASIC_PASSWORD = 0;
    static public final int KERBV5 = 1;

    // Credential Interest levels
    static public final String PASSWORD_CREDENTIAL = "jakarta.resource.spi.security.PasswordCredential";
    static public final String GENERIC_CREDENTIAL = "jakarta.resource.spi.security.GenericCredential";

    /**
     * Flush Connection pool by reinitializing the connections
     * established in the pool.
     * @param poolInfo
     * @throws com.sun.appserv.connectors.internal.api.PoolingException
     */
    public boolean flushConnectionPool(PoolInfo poolInfo) throws PoolingException;

    //Get status of pool
    public PoolStatus getPoolStatus(PoolInfo poolInfo);


    public ResourceHandle getResourceFromPool(ResourceSpec spec, ResourceAllocator alloc, ClientSecurityInfo info,
                                       Transaction tran) throws PoolingException, RetryableUnavailableException;

    public void createEmptyConnectionPool(PoolInfo poolInfo, PoolType pt, Hashtable env) throws PoolingException;


    public void putbackResourceToPool(ResourceHandle h, boolean errorOccurred);

    public void putbackBadResourceToPool(ResourceHandle h);

    public void putbackDirectToPool(ResourceHandle h, PoolInfo poolInfo);


    public void resourceClosed(ResourceHandle res);

    public void badResourceClosed(ResourceHandle res);

    public void resourceErrorOccurred(ResourceHandle res);

    public void resourceAbortOccurred(ResourceHandle res);

    public void transactionCompleted(Transaction tran, int status);

    public void emptyResourcePool(ResourceSpec spec);

    public void killPool(PoolInfo poolInfo);

    public void reconfigPoolProperties(ConnectorConnectionPool ccp) throws PoolingException;

/*
    public ConcurrentHashMap getMonitoredPoolTable();
*/

    public boolean switchOnMatching(PoolInfo poolInfo);

    /**
     * Obtain a transactional resource such as JDBC connection
     *
     * @param spec  Specification for the resource
     * @param alloc Allocator for the resource
     * @param info  Client security for this request
     * @return An object that represents a connection to the resource
     * @throws PoolingException Thrown if some error occurs while
     *                          obtaining the resource
     */
    public Object getResource(ResourceSpec spec, ResourceAllocator alloc, ClientSecurityInfo info)
            throws PoolingException, RetryableUnavailableException;

    public ResourceReferenceDescriptor getResourceReference(String jndiName, String logicalName);

    public void killAllPools();

    public void killFreeConnectionsInPools();

    public ResourcePool getPool(PoolInfo poolInfo);

    public void setSelfManaged(PoolInfo poolInfo, boolean flag);

    public void lazyEnlist(ManagedConnection mc) throws ResourceException;

    public void registerPoolLifeCycleListener(PoolLifeCycle poolListener);

    public void unregisterPoolLifeCycleListener();
}

