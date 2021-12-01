/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.kiwi.sail;

import com.google.common.collect.MapMaker;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.model.caching.IntArray;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailBase;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An implementation of a KiWi triple store without extended transaction support. The KiWiStore holds a reference to
 * a KiWiPersistence object to access the database. Each SailConnection will be directly associated with a database
 * connection and a database transaction.
 * <p/>
 * Note that extended KiWi functionality like the reasoner or versioning require the extended transaction support.
 * In these cases, please see KiWiTransactionalStore.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiStore extends NotifyingSailBase {


    /**
     * The repository-wide value factory, using the valueFactoryConnection above. Will be initialised when
     * getValueFactory() is called for the first time.
     */
    private KiWiValueFactory repositoryValueFactory;


    private KiWiPersistence persistence;

    /**
     * The default context to use when no explicit context is given in createStatement. The KiWi triple store
     * does not support null values for the context of a triple, so this URL must be set to an appropriate value
     */
    private String defaultContext;

    /**
     * The context to use for storing all inferred triples. The value set here will override all contexts
     * given to addInferredTriple, because KiWi always stores all inferred triples in the same context.
     */
    private String inferredContext;


    private boolean initialized = false;

    /**
     * For some operations (e.g. looking up nodes and triples) we hold a store-wide lock to avoid clashes between
     * threads. This could probably be relaxed a bit or even dropped altogether, but this approach is safer.
     */
    protected ReentrantLock nodeLock;

    protected ReentrantLock tripleLock;

    /**
     * This is a hash map for storing references to resources that have not yet been persisted. It is used e.g. when
     * one or more transactions are currently active and request the creation of same resource several times
     * (via createResource()).
     * <p/>
     * The map is implemented as a hash map with weak references, i.e. the entries are volatile and
     * will be removed by the garbage collector once they are not referred anymore somewhere else (e.g. in a
     * transaction).
     * <p/>
     * The registry is not a proper cache, entries will be removed when they are no longer referred. Also, the
     * registry should not be used to check for existence of a resource via getResource(), it is purely meant
     * to ensure that a resource is not created multiple times.
     */
    protected ConcurrentMap<IntArray,Statement> tripleRegistry;

    public KiWiStore(KiWiPersistence persistence, String defaultContext, String inferredContext) {
    	this.persistence    = persistence;
    	this.defaultContext = defaultContext;
        this.nodeLock       = new ReentrantLock();
        this.tripleLock     = new ReentrantLock();
        this.inferredContext = inferredContext;
    	
    	tripleRegistry  = new MapMaker().weakValues().makeMap();

    }

    @Deprecated
    public KiWiStore(String name, String jdbcUrl, String db_user, String db_password, KiWiDialect dialect, String defaultContext, String inferredContext) {
    	this(new KiWiConfiguration(name,jdbcUrl,db_user,db_password,dialect, defaultContext, inferredContext));
    }

    public KiWiStore(KiWiConfiguration configuration) {
        this(new KiWiPersistence(configuration), configuration.getDefaultContext(), configuration.getInferredContext());
    }


    /**
     * Do store-specific operations to initialize the store. The default
     * implementation of this method does nothing.
     */
    @Override
    protected void initializeInternal() throws SailException {
        try {
            persistence.initDatabase();
            persistence.initialise();

            initialized = true;
        } catch (SQLException e) {
            throw new SailException("database error while initialising database",e);
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * The default context to use when no explicit context is given in createStatement. The KiWi triple store
     * does not support null values for the context of a triple, so this URL must be set to an appropriate value
     */
    public String getDefaultContext() {
        return defaultContext;
    }


    /**
     * The context to use for storing all inferred triples. The value set here will override all contexts
     * given to addInferredTriple, because KiWi always stores all inferred triples in the same context.
     */
    public String getInferredContext() {
        return inferredContext;
    }

    /**
     * Return a reference to the persistence object used by this KiWiStore.
     * @return
     */
    public KiWiPersistence getPersistence() {
        return persistence;
    }

    /**
     * Returns a store-specific SailConnection object.
     *
     * @return A connection to the store.
     */
    @Override
    protected KiWiSailConnection getConnectionInternal() throws SailException {
        return new KiWiSailConnection(this);
    }

    /**
     * Do store-specific operations to ensure proper shutdown of the store.
     */
    @Override
    protected void shutDownInternal() throws SailException {
        closeValueFactory();
        persistence.shutdown();
    }

    /**
     * In case there is a value factory managed by this repository directly, close it (and the underlying database
     * connection)
     */
    public void closeValueFactory() {
        if(repositoryValueFactory != null) {
            repositoryValueFactory.close();
            repositoryValueFactory = null;
            persistence.setValueFactory(null);
        }

    }

    /**
     * Checks whether this Sail object is writable, i.e. if the data contained in
     * this Sail object can be changed.
     */
    @Override
    public boolean isWritable() throws SailException {
        return true;
    }

    /**
     * Gets a ValueFactory object that can be used to create URI-, blank node-,
     * literal- and statement objects.
     *
     * @return a ValueFactory object for this Sail object.
     */
    @Override
    public synchronized ValueFactory getValueFactory() {
        if(repositoryValueFactory == null) {
            repositoryValueFactory = new KiWiValueFactory(this,  defaultContext);
            persistence.setValueFactory(repositoryValueFactory);
        }
        return repositoryValueFactory;
    }

}
