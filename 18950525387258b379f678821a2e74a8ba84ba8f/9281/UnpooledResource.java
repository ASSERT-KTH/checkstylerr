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

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.ResourceState;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import org.glassfish.resourcebase.resources.api.PoolInfo;

import jakarta.transaction.Transaction;
import java.util.Hashtable;

/**
 * This resource pool is created when connection pooling is switched off
 * Hence no pooling happens in this resource pool
 *
 * @author Kshitiz Saxena
 * @since 9.1
 */
public class UnpooledResource extends ConnectionPool{

    private int poolSize;

    /** Creates a new instance of UnpooledResourcePool */
    public UnpooledResource(PoolInfo poolInfo, Hashtable env) throws PoolingException {
        super(poolInfo, env);

        //No pool is being maintained, hence no pool cleanup is needed
        //in case of failure
        failAllConnections = false;
    }

    @Override
    protected synchronized void initPool(ResourceAllocator allocator)
            throws PoolingException{

        if (poolInitialized) {
            return;
        }

        //nothing needs to be done as pooling is disabled
        poolSize = 0;

        poolInitialized = true;
    }

    @Override
    protected ResourceHandle prefetch(ResourceSpec spec, ResourceAllocator alloc,
            Transaction tran) {
        return null;
    }

    @Override
    protected void reconfigureSteadyPoolSize(int oldSteadyPoolSize,
                                           int newSteadyPoolSize) throws PoolingException {
        //No-op as the steady pool size should not be reconfigured when connection
        //pooling is switched off
    }

    @Override
    protected ResourceHandle getUnenlistedResource(ResourceSpec spec, ResourceAllocator alloc,
            Transaction tran) throws PoolingException {
        ResourceHandle handle = null;

        if(incrementPoolSize()){
            try{
                handle = createSingleResource(alloc);
            }catch (PoolingException ex){
                decrementPoolSize();
                throw ex;
            }
            ResourceState state = new ResourceState();
            handle.setResourceState(state);
            state.setEnlisted(false);
            setResourceStateToBusy(handle);
            return handle;
        }
        String msg = localStrings.getStringWithDefault(
                "poolmgr.max.pool.size.reached",
                "In-use connections equal max-pool-size therefore cannot allocate any more connections.");
        throw new PoolingException(msg);
    }

    @Override
    public void resourceErrorOccurred(ResourceHandle resourceHandle) throws IllegalStateException {
        freeResource(resourceHandle);
    }

    @Override
    protected void freeResource(ResourceHandle resourceHandle){
        decrementPoolSize();
        deleteResource(resourceHandle);
    }

    private synchronized boolean incrementPoolSize(){
        if(poolSize >= maxPoolSize){
            _logger.info("Fail as poolSize : " + poolSize);
            return false;
        }
        poolSize++;
        return true;
    }

    private synchronized void decrementPoolSize(){
        poolSize--;
    }
}
