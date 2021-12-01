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

import org.apache.commons.lang3.LocaleUtils;
import org.apache.marmotta.commons.locking.ObjectLocks;
import org.apache.marmotta.commons.sesame.model.LiteralCommons;
import org.apache.marmotta.commons.sesame.model.LiteralKey;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.commons.util.DateUtils;
import org.apache.marmotta.kiwi.model.caching.IntArray;
import org.apache.marmotta.kiwi.model.rdf.*;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.openrdf.model.*;
import org.openrdf.model.impl.ContextStatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiValueFactory implements ValueFactory {

    private static Logger log = LoggerFactory.getLogger(KiWiValueFactory.class);

    private Random anonIdGenerator;


    /**
     * This is a hash map for storing references to resources that have not yet been persisted. It is used e.g. when
     * one or more transactions are currently active and request the creation of same resource several times
     * (via createResource()).
     * <p/°
     * The map is implemented as a hash map with weak references, i.e. the entries are volatile and
     * will be removed by the garbage collector once they are not referred anymore somewhere else (e.g. in a
     * transaction).
     * <p/>
     * The registry is not a proper cache, entries will be removed when they are no longer referred. Also, the
     * registry should not be used to check for existence of a resource via getResource(), it is purely meant
     * to ensure that a resource is not created multiple times.
     */
    private ConcurrentMap<IntArray,Statement> tripleRegistry;

    private KiWiStore store;


    private ObjectLocks resourceLocks;
    private ObjectLocks literalLocks;

    private String defaultContext;

    private boolean batchCommit;

    private int batchSize = 1000;

    // the list containing the in-memory nodes that need to be committed later
    private List<KiWiNode> nodeBatch;

    // a quick lookup allowing to lookup nodes while they are not yet in the database
    private Map<String,KiWiUriResource> batchUriLookup;
    private Map<String,KiWiAnonResource> batchBNodeLookup;
    private Map<String,KiWiLiteral> batchLiteralLookup;

    private int poolSize = 4;
    private int poolPosition = 0;

    private ArrayList<KiWiConnection> pooledConnections;


    private ReentrantReadWriteLock commitLock = new ReentrantReadWriteLock();


    public KiWiValueFactory(KiWiStore store, String defaultContext) {
        resourceLocks = new ObjectLocks();
        literalLocks  = new ObjectLocks();

        anonIdGenerator = new Random();
        tripleRegistry  = store.tripleRegistry;

        this.store          = store;
        this.defaultContext = defaultContext;

        // batch commits
        this.nodeBatch      = Collections.synchronizedList(new ArrayList<KiWiNode>(batchSize));

        this.batchCommit    = store.getPersistence().getConfiguration().isBatchCommit();
        this.batchSize      = store.getPersistence().getConfiguration().getBatchSize();

        this.batchUriLookup     = new ConcurrentHashMap<String,KiWiUriResource>();
        this.batchBNodeLookup   = new ConcurrentHashMap<String, KiWiAnonResource>();
        this.batchLiteralLookup = new ConcurrentHashMap<String,KiWiLiteral>();

        this.pooledConnections = new ArrayList<>(poolSize);
        try {
            for(int i = 0; i<poolSize ; i++) {
                pooledConnections.add(store.getPersistence().getConnection());
            }
        } catch (SQLException e) {
            log.error("error initialising value factory connection pool",e);
        }
    }

    protected KiWiConnection aqcuireConnection() {
        try {
            if(batchCommit) {
                return pooledConnections.get(poolPosition++ % poolSize);
            } else {
                return store.getPersistence().getConnection();
            }
        } catch(SQLException ex) {
            log.error("could not acquire database connection", ex);
            throw new RuntimeException(ex);
        }
    }

    protected void releaseConnection(KiWiConnection con) {
        if(!batchCommit) {
            try {
                con.commit();
                con.close();
            } catch (SQLException ex) {
                log.error("could not release database connection", ex);
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Creates a new bNode.
     *
     * @return An object representing the bNode.
     */
    @Override
    public BNode createBNode() {
        return createBNode(Long.toHexString(System.currentTimeMillis())+Integer.toHexString(anonIdGenerator.nextInt(1000)));
    }

    /**
     * Creates a new URI from the supplied string-representation.
     *
     * @param uri A string-representation of a URI.
     * @return An object representing the URI.
     */
    @Override
    public URI createURI(String uri) {
        commitLock.readLock().lock();

        resourceLocks.lock(uri);

        try {
            KiWiUriResource result = batchUriLookup.get(uri);

            if(result != null) {
                return result;
            } else {

                KiWiConnection connection = aqcuireConnection();
                try {
                    // first look in the registry for newly created resources if the resource has already been created and
                    // is still volatile
                    result = connection.loadUriResource(uri);

                    if(result == null) {
                        result = new KiWiUriResource(uri);

                        if(batchCommit) {
                            result.setId(connection.getNodeId());
                            batchUriLookup.put(uri, result);

                            nodeBatch.add(result);

                        } else {
                            connection.storeNode(result, false);
                        }

                    }
                    if(result.getId() == null) {
                        log.error("node ID is null!");
                    }

                    return result;
                } catch (SQLException e) {
                    log.error("database error, could not load URI resource",e);
                    throw new IllegalStateException("database error, could not load URI resource",e);
                } finally {
                    releaseConnection(connection);
                }
            }
        } finally {
            resourceLocks.unlock(uri);
            commitLock.readLock().unlock();

            try {
                if(nodeBatch.size() >= batchSize) {
                    flushBatch();
                }
            } catch (SQLException e) {
                log.error("database error, could not load URI resource",e);
                throw new IllegalStateException("database error, could not load URI resource",e);
            }
        }

    }

    /**
     * Creates a new URI from the supplied namespace and local name. Calling this
     * method is funtionally equivalent to calling
     * {@link #createURI(String) createURI(namespace+localName)}, but allows the
     * ValueFactory to reuse supplied namespace and local name strings whenever
     * possible. Note that the values returned by {@link org.openrdf.model.URI#getNamespace()} and
     * {@link org.openrdf.model.URI#getLocalName()} are not necessarily the same as the values that
     * are supplied to this method.
     *
     * @param namespace The URI's namespace.
     * @param localName The URI's local name.
     * @throws IllegalArgumentException If the supplied namespace and localname do not resolve to a legal
     *                                  (absolute) URI.
     */
    @Override
    public URI createURI(String namespace, String localName) {
        return createURI(namespace+localName);
    }

    /**
     * Creates a new blank node with the given node identifier.
     *
     * @param nodeID The blank node identifier.
     * @return An object representing the blank node.
     */
    @Override
    public BNode createBNode(String nodeID) {
        commitLock.readLock().lock();
        resourceLocks.lock(nodeID);

        try {
            KiWiAnonResource result = batchBNodeLookup.get(nodeID);

            if(result != null) {
                return result;
            } else {
                KiWiConnection connection = aqcuireConnection();
                try {
                    // first look in the registry for newly created resources if the resource has already been created and
                    // is still volatile
                    result = connection.loadAnonResource(nodeID);

                    if(result == null) {
                        result = new KiWiAnonResource(nodeID);

                        if(batchCommit) {
                            result.setId(connection.getNodeId());
                            nodeBatch.add(result);
                            batchBNodeLookup.put(nodeID,result);
                        } else {
                            connection.storeNode(result, false);
                        }
                    }
                    if(result.getId() == null) {
                        log.error("node ID is null!");
                    }

                    return result;
                } catch (SQLException e) {
                    log.error("database error, could not load anonymous resource",e);
                    throw new IllegalStateException("database error, could not load anonymous resource",e);
                } finally {
                    releaseConnection(connection);
                }
            }
        } finally {
            resourceLocks.unlock(nodeID);
            commitLock.readLock().unlock();

            try {
                if(nodeBatch.size() >= batchSize) {
                    flushBatch();
                }
            } catch (SQLException e) {
                log.error("database error, could not load URI resource",e);
                throw new IllegalStateException("database error, could not load URI resource",e);
            }
        }
    }

    /**
     * Creates a new literal representing the specified date that is typed using
     * the appropriate XML Schema date/time datatype.
     *
     * @since 2.7.0
     */
    @Override
    public Literal createLiteral(Date date) {
        return createLiteral(date, null, LiteralCommons.getXSDType(Date.class));
    }

    /**
     * Creates a typed {@link org.openrdf.model.Literal} out of the supplied object, mapping the
     * runtime type of the object to the appropriate XML Schema type. If no
     * mapping is available, the method returns a literal with the string
     * representation of the supplied object as the value, and
     * {@link org.openrdf.model.vocabulary.XMLSchema#STRING} as the datatype. Recognized types are
     * {@link Boolean}, {@link Byte}, {@link Double}, {@link Float},
     * {@link Integer}, {@link Long}, {@link Short}, {@link javax.xml.datatype.XMLGregorianCalendar }
     * , and {@link java.util.Date}.
     *
     * @param object an object to be converted to a typed literal.
     * @return a typed literal representation of the supplied object.
     * @since 2.7.0
     */
    public Literal createLiteral(Object object) {
        if(object instanceof XMLGregorianCalendar) {
            return createLiteral((XMLGregorianCalendar)object);
        } else {
            return createLiteral(object,null,LiteralCommons.getXSDType(object.getClass()));
        }
    }

    /**
     * Creates a new literal with the supplied label.
     *
     * @param label The literal's label.
     */
    @Override
    public Literal createLiteral(String label) {
        // FIXME: MARMOTTA-39 (no default datatype before RDF-1.1)
        // return createLiteral(label, null, LiteralCommons.getXSDType(String.class));
        return createLiteral(label, null, null);
    }

    /**
     * Creates a new literal with the supplied label and language attribute.
     *
     * @param label    The literal's label.
     * @param language The literal's language attribute, or <tt>null</tt> if the literal
     *                 doesn't have a language.
     */
    @Override
    public Literal createLiteral(String label, String language) {
        // FIXME: MARMOTTA-39 (no rdf:langString before RDF-1.1)
        // return createLiteral(label,language,LiteralCommons.getRDFLangStringType());
        return createLiteral(label, language, null);
    }

    /**
     * Creates a new literal with the supplied label and datatype.
     *
     * @param label    The literal's label.
     * @param datatype The literal's datatype, or <tt>null</tt> if the literal doesn't
     *                 have a datatype.
     */
    @Override
    public Literal createLiteral(String label, URI datatype) {
        return createLiteral(label,null,datatype.stringValue());
    }


    /**
     * Internal createLiteral method for different datatypes. This method distinguishes based on the Java class
     * type and the type argument passed as argument how to load and possibly create the new literal.
     *
     * @param value
     * @param lang
     * @param type
     * @param <T>
     * @return
     */
    private <T> KiWiLiteral createLiteral(T value, String lang, String type) {
        commitLock.readLock().lock();
        final Locale locale;
        if(lang != null) {
            locale = LocaleUtils.toLocale(lang.replace("-","_"));
        } else
            locale  = null;

        if (lang != null) {
            // FIXME: MARMOTTA-39 (no rdf:langString)
            // type = LiteralCommons.getRDFLangStringType();
        } else if (type == null) {
            // FIXME: MARMOTTA-39 (no default datatype before RDF-1.1)
            // type = LiteralCommons.getXSDType(value.getClass());
        }
        String key = LiteralCommons.createCacheKey(value.toString(),locale,type);
        LiteralKey lkey = new LiteralKey(value,type,lang);

        literalLocks.lock(lkey);

        try {
            KiWiLiteral result = batchLiteralLookup.get(key);


            if(result != null) {
                return result;
            } else {
                final KiWiUriResource rtype = type==null?null:(KiWiUriResource)createURI(type);

                final KiWiConnection connection = aqcuireConnection();
                try {


                    // differentiate between the different types of the value
                    if (type == null) {
                        // FIXME: MARMOTTA-39 (this is to avoid a NullPointerException in the following if-clauses)
                        result = connection.loadLiteral(value.toString(), lang, rtype);

                        if(result == null) {
                            result = new KiWiStringLiteral(value.toString(), locale, rtype);
                        }
                    } else if(value instanceof Date || type.equals(Namespaces.NS_XSD+"dateTime")) {
                        // parse if necessary
                        final Date dvalue;
                        if(value instanceof Date) {
                            dvalue = (Date)value;
                        } else {
                            dvalue = DateUtils.parseDate(value.toString());
                        }

                        result = connection.loadLiteral(dvalue);

                        if(result == null) {
                            result= new KiWiDateLiteral(dvalue, rtype);
                        }
                    } else if(Integer.class.equals(value.getClass()) || int.class.equals(value.getClass())  ||
                            Long.class.equals(value.getClass())    || long.class.equals(value.getClass()) ||
                            type.equals(Namespaces.NS_XSD+"integer") || type.equals(Namespaces.NS_XSD+"long")) {
                        long ivalue = 0;
                        if(Integer.class.equals(value.getClass()) || int.class.equals(value.getClass())) {
                            ivalue = (Integer)value;
                        } else if(Long.class.equals(value.getClass()) || long.class.equals(value.getClass())) {
                            ivalue = (Long)value;
                        } else {
                            ivalue = Long.parseLong(value.toString());
                        }


                        result = connection.loadLiteral(ivalue);

                        if(result == null) {
                            result= new KiWiIntLiteral(ivalue, rtype);
                        }
                    } else if(Double.class.equals(value.getClass())   || double.class.equals(value.getClass())  ||
                            Float.class.equals(value.getClass())    || float.class.equals(value.getClass()) ||
                            type.equals(Namespaces.NS_XSD+"double") || type.equals(Namespaces.NS_XSD+"float")) {
                        double dvalue = 0.0;
                        if(Float.class.equals(value.getClass()) || float.class.equals(value.getClass())) {
                            dvalue = (Float)value;
                        } else if(Double.class.equals(value.getClass()) || double.class.equals(value.getClass())) {
                            dvalue = (Double)value;
                        } else {
                            dvalue = Double.parseDouble(value.toString());
                        }


                        result = connection.loadLiteral(dvalue);

                        if(result == null) {
                            result= new KiWiDoubleLiteral(dvalue, rtype);
                        }
                    } else if(Boolean.class.equals(value.getClass())   || boolean.class.equals(value.getClass())  ||
                            type.equals(Namespaces.NS_XSD+"boolean")) {
                        boolean bvalue = false;
                        if(Boolean.class.equals(value.getClass())   || boolean.class.equals(value.getClass())) {
                            bvalue = (Boolean)value;
                        } else {
                            bvalue = Boolean.parseBoolean(value.toString());
                        }


                        result = connection.loadLiteral(bvalue);

                        if(result == null) {
                            result= new KiWiBooleanLiteral(bvalue, rtype);
                        }
                    } else {
                        result = connection.loadLiteral(value.toString(), lang, rtype);

                        if(result == null) {
                            result = new KiWiStringLiteral(value.toString(), locale, rtype);
                        }
                    }

                    if(result.getId() == null) {
                        if(batchCommit) {
                            result.setId(connection.getNodeId());
                            batchLiteralLookup.put(key, result);

                            nodeBatch.add(result);
                        } else {
                            connection.storeNode(result, false);
                        }
                    }

                    return result;

                } catch (SQLException e) {
                    log.error("database error, could not load literal",e);
                    throw new IllegalStateException("database error, could not load literal",e);
                } finally {
                    releaseConnection(connection);
                }
            }
        } finally {
            literalLocks.unlock(lkey);
            commitLock.readLock().unlock();

            try {
                if(nodeBatch.size() >= batchSize) {
                    flushBatch();
                }
            } catch (SQLException e) {
                log.error("database error, could not load URI resource",e);
                throw new IllegalStateException("database error, could not load URI resource",e);
            }
        }
    }

    /**
     * Creates a new <tt>xsd:boolean</tt>-typed literal representing the
     * specified value.
     *
     * @param value The value for the literal.
     * @return An <tt>xsd:boolean</tt>-typed literal for the specified value.
     */
    @Override
    public Literal createLiteral(boolean value) {
        return createLiteral(value,null,LiteralCommons.getXSDType(Boolean.class));
    }

    /**
     * Creates a new <tt>xsd:byte</tt>-typed literal representing the
     * specified value.
     *
     * @param value The value for the literal.
     * @return An <tt>xsd:byte</tt>-typed literal for the specified value.
     */
    @Override
    public Literal createLiteral(byte value) {
        return createLiteral((int)value,null,LiteralCommons.getXSDType(Byte.class));
    }

    /**
     * Creates a new <tt>xsd:short</tt>-typed literal representing the
     * specified value.
     *
     * @param value The value for the literal.
     * @return An <tt>xsd:short</tt>-typed literal for the specified value.
     */
    @Override
    public Literal createLiteral(short value) {
        return createLiteral((int)value,null,LiteralCommons.getXSDType(Short.class));
    }

    /**
     * Creates a new <tt>xsd:int</tt>-typed literal representing the specified
     * value.
     *
     * @param value The value for the literal.
     * @return An <tt>xsd:int</tt>-typed literal for the specified value.
     */
    @Override
    public Literal createLiteral(int value) {
        return createLiteral(value,null,LiteralCommons.getXSDType(Integer.class));
    }

    /**
     * Creates a new <tt>xsd:long</tt>-typed literal representing the
     * specified value.
     *
     * @param value The value for the literal.
     * @return An <tt>xsd:long</tt>-typed literal for the specified value.
     */
    @Override
    public Literal createLiteral(long value) {
        return createLiteral(value,null,LiteralCommons.getXSDType(Long.class));
    }

    /**
     * Creates a new <tt>xsd:float</tt>-typed literal representing the
     * specified value.
     *
     * @param value The value for the literal.
     * @return An <tt>xsd:float</tt>-typed literal for the specified value.
     */
    @Override
    public Literal createLiteral(float value) {
        return createLiteral(value,null,LiteralCommons.getXSDType(Float.class));
    }

    /**
     * Creates a new <tt>xsd:double</tt>-typed literal representing the
     * specified value.
     *
     * @param value The value for the literal.
     * @return An <tt>xsd:double</tt>-typed literal for the specified value.
     */
    @Override
    public Literal createLiteral(double value) {
        return createLiteral(value,null,LiteralCommons.getXSDType(Double.class));
    }

    /**
     * Creates a new literal representing the specified calendar that is typed
     * using the appropriate XML Schema date/time datatype.
     *
     * @param calendar The value for the literal.
     * @return An typed literal for the specified calendar.
     */
    @Override
    public Literal createLiteral(XMLGregorianCalendar calendar) {
        Date value = calendar.toGregorianCalendar().getTime();

        return createLiteral(value,null,LiteralCommons.getXSDType(Date.class));
    }

    /**
     * Creates a new statement with the supplied subject, predicate and object.
     *
     * @param subject   The statement's subject.
     * @param predicate The statement's predicate.
     * @param object    The statement's object.
     * @return The created statement.
     */
    @Override
    public Statement createStatement(Resource subject, URI predicate, Value object) {
        return createStatement(subject, predicate, object, createURI(defaultContext));
    }

    /**
     * Creates a new statement with the supplied subject, predicate and object
     * and associated context.
     *
     * @param subject   The statement's subject.
     * @param predicate The statement's predicate.
     * @param object    The statement's object.
     * @param context   The statement's context.
     * @return The created statement.
     */
    @Override
    public Statement createStatement(Resource subject, URI predicate, Value object, Resource context) {
        return new ContextStatementImpl(subject,predicate,object,context);
    }

    /**
     * Creates a new statement with the supplied subject, predicate and object and associated context. This is a
     * specialised form of createStatement that allows the existance check for a triple to run in the same connection
     * as the rest of the repository operations.
     *
     * @param subject   The statement's subject.
     * @param predicate The statement's predicate.
     * @param object    The statement's object.
     * @param context   The statement's context.
     * @return The created statement.
     */
    public Statement createStatement(Resource subject, URI predicate, Value object, Resource context, KiWiConnection connection) {
        IntArray cacheKey = IntArray.createSPOCKey(subject, predicate, object, context);
        KiWiTriple result = (KiWiTriple)tripleRegistry.get(cacheKey);
        try {
            if(result == null || result.isDeleted()) {
                KiWiResource ksubject   = convert(subject);
                KiWiUriResource kpredicate = convert(predicate);
                KiWiNode kobject    = convert(object);
                KiWiResource    kcontext   = convert(context);

                result = new KiWiTriple(ksubject,kpredicate,kobject,kcontext);
                result.setId(connection.getTripleId(ksubject,kpredicate,kobject,kcontext,true));
                if(result.getId() == null) {
                    result.setMarkedForReasoning(true);
                }

                tripleRegistry.put(cacheKey,result);
            }
            return result;
        } catch (SQLException e) {
            log.error("database error, could not load triple", e);
            throw new IllegalStateException("database error, could not load triple",e);
        }
    }

    /**
     * Remove a statement from the triple registry. Called when the statement is deleted and the transaction commits.
     * @param triple
     */
    protected void removeStatement(KiWiTriple triple) {
        IntArray cacheKey = IntArray.createSPOCKey(triple.getSubject(), triple.getPredicate(), triple.getObject(), triple.getContext());
        tripleRegistry.remove(cacheKey);
        triple.setDeleted(true);
    }


    public KiWiResource convert(Resource r) {
        return (KiWiResource)convert((Value)r);
    }

    public KiWiUriResource convert(URI r) {
        return (KiWiUriResource)convert((Value)r);
    }

    public KiWiNode convert(Value value) {
        if(value == null) {
            return null;
        } else if(value instanceof KiWiNode) {
            return (KiWiNode)value;
        } else if(value instanceof URI) {
            return (KiWiUriResource)createURI(value.stringValue());
        } else if(value instanceof BNode) {
            return (KiWiAnonResource)createBNode(value.stringValue());
        } else if(value instanceof Literal) {
            Literal l = (Literal)value;
            return createLiteral(l.getLabel(),l.getLanguage(), l.getDatatype() != null ? l.getDatatype().stringValue(): null);
        } else {
            throw new IllegalArgumentException("the value passed as argument does not have the correct type");
        }

    }

    public boolean isBatchCommit() {
        return batchCommit;
    }

    public void setBatchCommit(boolean batchCommit) {
        this.batchCommit = batchCommit;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }


    /**
     * Immediately flush the batch to the database using the value factory's connection. The method expects the
     * underlying connection to start and commit the node batch.
     */
    public void flushBatch() throws SQLException {
        KiWiConnection con = aqcuireConnection();
        try {
            flushBatch(con);
        } finally {
            releaseConnection(con);
        }

    }


    /**
     * Immediately flush the batch to the database. The method expects the underlying connection to start and commit
     * the node batch.
     */
    public void flushBatch(KiWiConnection con) throws SQLException {
        commitLock.writeLock().lock();
        try {
            if(batchCommit && nodeBatch.size() > 0) {
                List<KiWiNode> processed = this.nodeBatch;
                this.nodeBatch      = Collections.synchronizedList(new ArrayList<KiWiNode>(batchSize));

                con.startNodeBatch();

                for(KiWiNode n : processed) {
                    con.storeNode(n,true);
                }
                batchLiteralLookup.clear();
                batchUriLookup.clear();
                batchBNodeLookup.clear();

                con.commitNodeBatch();
            }
        } finally {
            commitLock.writeLock().unlock();
        }
    }

    public void close() {
        for(KiWiConnection con : pooledConnections) {
            try {
                if(!con.isClosed()) {
                    con.commit();
                    con.close();
                }
            } catch (SQLException e) {
                log.warn("could not close value factory connection: {}",e.getMessage());
            }
        }
    }


}
