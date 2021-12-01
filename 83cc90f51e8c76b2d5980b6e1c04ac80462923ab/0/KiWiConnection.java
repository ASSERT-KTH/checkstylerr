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
package org.apache.marmotta.kiwi.persistence;

import org.apache.marmotta.commons.sesame.model.LiteralCommons;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.commons.util.DateUtils;
import com.google.common.base.Preconditions;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.ExceptionConvertingIteration;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.marmotta.kiwi.caching.KiWiCacheManager;
import org.apache.marmotta.kiwi.model.rdf.*;
import org.apache.marmotta.kiwi.persistence.util.ResultSetIteration;
import org.apache.marmotta.kiwi.persistence.util.ResultTransformerFunction;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A KiWiConnection offers methods for storing and retrieving KiWiTriples, KiWiNodes, and KiWiNamespaces in the
 * database. It wraps a JDBC connection which will be committed on commit(), rolled back on rollback() and
 * closed on close();
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiConnection {

    private static Logger log = LoggerFactory.getLogger(KiWiConnection.class);


    protected KiWiDialect dialect;

    protected Connection connection;

    protected KiWiPersistence  persistence;

    protected KiWiCacheManager cacheManager;


    /**
     * Cache nodes by database ID
     */
    private Cache nodeCache;

    /**
     * Cache triples by database ID
     */
    private Cache tripleCache;


    /**
     * Cache URI resources by uri
     */
    private Cache uriCache;


    /**
     * Cache BNodes by BNode ID
     */
    private Cache bnodeCache;

    /**
     * Cache literals by literal cache key (LiteralCommons#createCacheKey(String,Locale,URI))
     */
    private Cache literalCache;


    /**
     * Look up namespaces by URI
     */
    private Cache namespaceUriCache;

    /**
     * Look up namespaces by prefix
     */
    private Cache namespacePrefixCache;

    /**
     * Cache instances of locales for language tags
     */
    private static Map<String,Locale> localeMap = new HashMap<String, Locale>();


    private Map<String,PreparedStatement> statementCache;

    private boolean autoCommit = false;


    public KiWiConnection(KiWiPersistence persistence, KiWiDialect dialect, KiWiCacheManager cacheManager) throws SQLException {
        this.cacheManager = cacheManager;
        this.dialect      = dialect;
        this.persistence  = persistence;

        initCachePool();
        initStatementCache();
    }

    private void initCachePool() {
        nodeCache    = cacheManager.getNodeCache();
        tripleCache  = cacheManager.getTripleCache();
        uriCache     = cacheManager.getUriCache();
        bnodeCache   = cacheManager.getBNodeCache();
        literalCache = cacheManager.getLiteralCache();

        namespacePrefixCache = cacheManager.getNamespacePrefixCache();
        namespaceUriCache    = cacheManager.getNamespaceUriCache();
    }

    /**
     * Load all prepared statements of the dialect into the statement cache
     * @throws SQLException
     */
    private void initStatementCache() throws SQLException {
        statementCache = new HashMap<String, PreparedStatement>();

        /*
        for(String key : dialect.getStatementIdentifiers()) {
            statementCache.put(key,connection.prepareStatement(dialect.getStatement(key)));
        }
        */
    }

    /**
     * This method must be called by all methods as soon as they actually require a JDBC connection. This allows
     * more efficient implementations in case the queries can be answered directly from the cache.
     */
    protected void requireJDBCConnection() throws SQLException {
        if(connection == null) {
            connection = persistence.getJDBCConnection();
            connection.setAutoCommit(autoCommit);
        }
    }

    /**
     * Get direct access to the JDBC connection used by this KiWiConnection.
     *
     * @return
     */
    public Connection getJDBCConnection() throws SQLException {
        requireJDBCConnection();

        return connection;
    }

    /**
     * Return the cache manager used by this connection
     * @return
     */
    public KiWiCacheManager getCacheManager() {
        return cacheManager;
    }

    public KiWiDialect getDialect() {
        return dialect;
    }

    /**
     * Load a KiWiNamespace with the given prefix, or null if the namespace does not exist. The method will first
     * look in the node cache for cached nodes. If no cache entry is found, it will run a database query
     * ("load.namespace_prefix").
     *
     * @param prefix  the prefix to look for
     * @return the KiWiNamespace with this prefix or null if it does not exist
     * @throws SQLException
     */
    public KiWiNamespace loadNamespaceByPrefix(String prefix) throws SQLException {
        Element element = namespacePrefixCache.get(prefix);
        if(element != null) {
            return (KiWiNamespace)element.getObjectValue();
        }

        requireJDBCConnection();

        // prepare a query; we will only iterate once, read only, and need only one result row since the id is unique
        PreparedStatement query = getPreparedStatement("load.namespace_prefix");
        query.setString(1, prefix);
        query.setMaxRows(1);

        // run the database query and if it yields a result, construct a new node; the method call will take care of
        // caching the constructed node for future calls
        ResultSet result = query.executeQuery();
        try {
            if(result.next()) {
                return constructNamespaceFromDatabase(result);
            } else {
                return null;
            }
        } finally {
            result.close();
        }
    }

    /**
     * Load a KiWiNamespace with the given uri, or null if the namespace does not exist. The method will first
     * look in the node cache for cached nodes. If no cache entry is found, it will run a database query
     * ("load.namespace_prefix").
     *
     * @param uri  the uri to look for
     * @return the KiWiNamespace with this uri or null if it does not exist
     * @throws SQLException
     */
    public KiWiNamespace loadNamespaceByUri(String uri) throws SQLException {
        Element element = namespaceUriCache.get(uri);
        if(element != null) {
            return (KiWiNamespace)element.getObjectValue();
        }

        requireJDBCConnection();

        // prepare a query; we will only iterate once, read only, and need only one result row since the id is unique
        PreparedStatement query = getPreparedStatement("load.namespace_uri");
        query.setString(1, uri);
        query.setMaxRows(1);

        // run the database query and if it yields a result, construct a new node; the method call will take care of
        // caching the constructed node for future calls
        ResultSet result = query.executeQuery();
        try {
            if(result.next()) {
                return constructNamespaceFromDatabase(result);
            } else {
                return null;
            }
        } finally {
            result.close();
        }
    }

    /**
     * Store the namespace passed as argument in the database. The database might enfore unique constraints and
     * thus throw an exception in case the prefix or URI is already used.
     *
     * @param namespace the namespace to store
     * @throws SQLException the prefix or URI is already used, or a database error occurred
     */
    public void storeNamespace(KiWiNamespace namespace) throws SQLException {
        // TODO: add unique constraints to table
        if(namespace.getId() != null) {
            log.warn("trying to store namespace which is already persisted: {}",namespace);
            return;
        }

        requireJDBCConnection();

        namespace.setId(getNextSequence("seq.namespaces"));

        PreparedStatement insertNamespace = getPreparedStatement("store.namespace");
        insertNamespace.setLong(1,namespace.getId());
        insertNamespace.setString(2,namespace.getPrefix());
        insertNamespace.setString(3,namespace.getUri());
        insertNamespace.setTimestamp(4,new Timestamp(namespace.getCreated().getTime()));

        insertNamespace.executeUpdate();

        namespacePrefixCache.put(new Element(namespace.getPrefix(),namespace));
        namespaceUriCache.put(new Element(namespace.getUri(),namespace));
    }

    /**
     * Delete the namespace passed as argument from the database and from the caches.
     * @param namespace the namespace to delete
     * @throws SQLException in case a database error occurred
     */
    public void deleteNamespace(KiWiNamespace namespace) throws SQLException {
        if(namespace.getId() == null) {
            log.warn("trying to remove namespace which is not persisted: {}",namespace);
            return;
        }

        requireJDBCConnection();

        PreparedStatement deleteNamespace = getPreparedStatement("delete.namespace");
        deleteNamespace.setLong(1, namespace.getId());
        deleteNamespace.executeUpdate();

        namespacePrefixCache.remove(namespace.getPrefix());
        namespaceUriCache.remove(namespace.getUri());
    }

    /**
     * Count all non-deleted triples in the triple store
     * @return
     * @throws SQLException
     */
    public long getSize() throws SQLException {
        requireJDBCConnection();

        PreparedStatement querySize = getPreparedStatement("query.size");
        ResultSet result = querySize.executeQuery();
        try {
            if(result.next()) {
                return result.getLong(1);
            } else {
                return 0;
            }
        } finally {
            result.close();
        }
    }

    /**
     * Count all non-deleted triples in the triple store
     * @return
     * @throws SQLException
     */
    public long getSize(KiWiResource context) throws SQLException {
        if(context.getId() == null) {
            return 0;
        };

        requireJDBCConnection();

        PreparedStatement querySize = getPreparedStatement("query.size_ctx");
        querySize.setLong(1,context.getId());

        ResultSet result = querySize.executeQuery();
        try {
            if(result.next()) {
                return result.getLong(1);
            } else {
                return 0;
            }
        } finally {
            result.close();
        }
    }

    /**
     * Load a KiWiNode by database ID. The method will first look in the node cache for cached nodes. If
     * no cache entry is found, it will run a database query ('load.node_by_id') on the NODES table and
     * construct an appropriate subclass instance of KiWiNode with the obtained values. The result will be
     * constructed based on the value of the NTYPE column as follows:
     * <ul>
     *     <li>'uri' - KiWiUriResource using the id and svalue (as URI) columns</li>
     *     <li>'bnode' - KiWiAnonResource using the id and svalue (as AnonId) columns</li>
     *     <li>'string' - KiWiStringLiteral using the id, svalue (literal value), lang (literal
     *         language) and ltype (literal type) columns</li>
     *     <li>'int' - KiWiIntLiteral using the id, svalue (string value), ivalue (integer value)
     *         and ltype (literal type) columns</li>
     *     <li>'double' - KiWiDoubleLiteral using the id, svalue (string value), dvalue (double
     *         value) and ltype (literal type) columns</li>
     *     <li>'boolean' - KiWiBooleanLiteral using the id, svalue (string value), bvalue (boolean
     *         value) and ltype (literal type) columns</li>
     *     <li>'date' - KiWiDateLiteral using the id, svalue (string value), tvalue (time value)
     *         and ltype (literal type) columns</li>
     * </ul>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param id the database id of the node to load
     * @return an instance of a KiWiNode subclass representing the node with the given database id;
     *     type depends on value of the ntype column
     */
    public KiWiNode loadNodeById(Long id) throws SQLException {

        // look in cache
        Element element = nodeCache.get(id);
        if(element != null) {
            return (KiWiNode)element.getObjectValue();
        }

        requireJDBCConnection();

        // prepare a query; we will only iterate once, read only, and need only one result row since the id is unique
        PreparedStatement query = getPreparedStatement("load.node_by_id");
        query.setLong(1,id);
        query.setMaxRows(1);

        // run the database query and if it yields a result, construct a new node; the method call will take care of
        // caching the constructed node for future calls
        ResultSet result = query.executeQuery();
        try {
            if(result.next()) {
                return constructNodeFromDatabase(result);
            } else {
                return null;
            }
        } finally {
            result.close();
        }

    }

    public KiWiTriple loadTripleById(Long id) throws SQLException {

        // look in cache
        Element element = tripleCache.get(id);
        if(element != null) {
            return (KiWiTriple)element.getObjectValue();
        }

        requireJDBCConnection();

        // prepare a query; we will only iterate once, read only, and need only one result row since the id is unique
        PreparedStatement query = getPreparedStatement("load.triple_by_id");
        query.setLong(1,id);
        query.setMaxRows(1);

        // run the database query and if it yields a result, construct a new node; the method call will take care of
        // caching the constructed node for future calls
        ResultSet result = query.executeQuery();
        try {
            if(result.next()) {
                return constructTripleFromDatabase(result);
            } else {
                return null;
            }
        } finally {
            result.close();
        }

    }

    /**
     * Load a KiWiUriResource by URI. The method will first look in the node cache for cached nodes. If
     * no cache entry is found, it will run a database query ('load.uri_by_uri') on the NODES table and
     * construct a new KiWiUriResource using the values of the id and svalue columns.
     * <p/>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param uri the URI of the resource to load
     * @return the KiWiUriResource identified by the given URI  or null if it does not exist
     */
    public KiWiUriResource loadUriResource(String uri) throws SQLException {
        // look in cache
        Element element = uriCache.get(uri);
        if(element != null) {
            return (KiWiUriResource)element.getObjectValue();
        }

        requireJDBCConnection();

        // prepare a query; we will only iterate once, read only, and need only one result row since the id is unique
        PreparedStatement query = getPreparedStatement("load.uri_by_uri");
        query.setString(1, uri);
        query.setMaxRows(1);

        // run the database query and if it yields a result, construct a new node; the method call will take care of
        // caching the constructed node for future calls
        ResultSet result = query.executeQuery();
        try {
            if(result.next()) {
                return (KiWiUriResource)constructNodeFromDatabase(result);
            } else {
                return null;
            }
        } finally {
            result.close();
        }
    }


    /**
     * Load a KiWiAnonResource by anonymous ID. The method will first look in the node cache for
     * cached nodes. If no cache entry is found, it will run a database query ('load.bnode_by_anonid')
     * on the NODES table and construct a new KiWiAnonResource using the values of the id and
     * svalue columns.
     * <p/>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param id the anonymous ID of the resource to load
     * @return the KiWiAnonResource identified by the given internal ID or null if it does not exist
     */
    public KiWiAnonResource loadAnonResource(String id) throws SQLException {
        // look in cache
        Element element = bnodeCache.get(id);
        if(element != null) {
            return (KiWiAnonResource)element.getObjectValue();
        }

        requireJDBCConnection();

        // prepare a query; we will only iterate once, read only, and need only one result row since the id is unique
        PreparedStatement query = getPreparedStatement("load.bnode_by_anonid");
        query.setString(1,id);
        query.setMaxRows(1);

        // run the database query and if it yields a result, construct a new node; the method call will take care of
        // caching the constructed node for future calls
        ResultSet result = query.executeQuery();
        try {
            if(result.next()) {
                return (KiWiAnonResource)constructNodeFromDatabase(result);
            } else {
                return null;
            }
        } finally {
            result.close();
        }
    }

    /**
     * Load a literal based on the value, language and type passed as argument. The method will first look in the node cache for
     * cached nodes. If no cache entry is found, it will run a database query ("load.literal_by_v")
     * on the NODES table and construct a new KiWiLiteral using the values of the literal columns (svalue, ivalue, ...). The
     * type of literal returned depends on the value of the ntype column.
     * <p/>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param value string value of the literal to load
     * @param lang  language of the literal to load (optional, 2-letter language code with optional country)
     * @param ltype the type of the literal to load (optional)
     * @return the literal matching the given arguments or null if it does not exist
     * @throws SQLException
     */
    public KiWiLiteral loadLiteral(String value, String lang, KiWiUriResource ltype) throws SQLException {
        // look in cache
        final Element element = literalCache.get(LiteralCommons.createCacheKey(value,getLocale(lang), ltype));
        if(element != null) {
            return (KiWiLiteral)element.getObjectValue();
        }

        requireJDBCConnection();

        // ltype not persisted
        if(ltype != null && ltype.getId() == null) {
            return null;
        }

        // otherwise prepare a query, depending on the parameters given
        final PreparedStatement query;
        if(lang == null && ltype == null) {
            query = getPreparedStatement("load.literal_by_v");
            query.setString(1,value);
        } else if(lang != null) {
            query = getPreparedStatement("load.literal_by_vl");
            query.setString(1,value);
            query.setString(2, lang);
        } else if(ltype != null) {
            query = getPreparedStatement("load.literal_by_vt");
            query.setString(1,value);
            query.setLong(2,ltype.getId());
        } else {
            // This cannot happen...
            throw new IllegalArgumentException("Impossible combination of lang/type in loadLiteral!");
        }

        // run the database query and if it yields a result, construct a new node; the method call will take care of
        // caching the constructed node for future calls
        ResultSet result = query.executeQuery();
        try {
            if(result.next()) {
                return (KiWiLiteral)constructNodeFromDatabase(result);
            } else {
                return null;
            }
        } finally {
            result.close();
        }
    }

    /**
     * Load a literal with the date value given as argument if it exists. The method will first look in
     * the node cache for cached nodes. If no cache entry is found, it will run a database query ("load.literal_by_tv")
     * on the NODES table and construct a new KiWiLiteral using the values of the literal columns
     * (svalue, ivalue, ...). The type of literal returned depends on the value of the ntype column.
     * <p/>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param date the date of the date literal to load
     * @return a KiWiDateLiteral with the correct date, or null if it does not exist
     * @throws SQLException
     */
    public KiWiDateLiteral loadLiteral(Date date) throws SQLException {
        // look in cache
        Element element = literalCache.get(LiteralCommons.createCacheKey(DateUtils.getDateWithoutFraction(date),Namespaces.NS_XSD + "dateTime"));
        if(element != null) {
            return (KiWiDateLiteral)element.getObjectValue();
        }

        requireJDBCConnection();

        KiWiUriResource ltype = loadUriResource(Namespaces.NS_XSD + "dateTime");

        if(ltype == null || ltype.getId() == null) {
            return null;
        }

        // otherwise prepare a query, depending on the parameters given
        PreparedStatement query = getPreparedStatement("load.literal_by_tv");
        query.setTimestamp(1, new Timestamp(DateUtils.getDateWithoutFraction(date).getTime()));
        query.setLong(2,ltype.getId());

        // run the database query and if it yields a result, construct a new node; the method call will take care of
        // caching the constructed node for future calls
        ResultSet result = query.executeQuery();
        try {
            if(result.next()) {
                return (KiWiDateLiteral)constructNodeFromDatabase(result);
            } else {
                return null;
            }
        } finally {
            result.close();
        }
    }


    /**
     * Load a integer literal with the long value given as argument if it exists. The method will first look in
     * the node cache for cached nodes. If no cache entry is found, it will run a database query ("load.literal_by_iv")
     * on the NODES table and construct a new KiWiLiteral using the values of the literal columns
     * (svalue, ivalue, ...). The type of literal returned depends on the value of the ntype column.
     * <p/>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param value the value of the integer literal to load
     * @return a KiWiIntLiteral with the correct value, or null if it does not exist
     * @throws SQLException
     */
    public KiWiIntLiteral loadLiteral(long value) throws SQLException {
        // look in cache
        Element element = literalCache.get(LiteralCommons.createCacheKey(Long.toString(value),null,Namespaces.NS_XSD + "integer"));
        if(element != null) {
            return (KiWiIntLiteral)element.getObjectValue();
        }

        requireJDBCConnection();

        KiWiUriResource ltype = loadUriResource(Namespaces.NS_XSD + "integer");

        // ltype not persisted
        if(ltype == null || ltype.getId() == null) {
            return null;
        }

        // otherwise prepare a query, depending on the parameters given
        PreparedStatement query = getPreparedStatement("load.literal_by_iv");
        query.setLong(1,value);
        query.setLong(2,ltype.getId());

        // run the database query and if it yields a result, construct a new node; the method call will take care of
        // caching the constructed node for future calls
        ResultSet result = query.executeQuery();
        try {
            if(result.next()) {
                return (KiWiIntLiteral)constructNodeFromDatabase(result);
            } else {
                return null;
            }
        } finally {
            result.close();
        }
    }

    /**
     * Load a double literal with the double value given as argument if it exists. The method will first look in
     * the node cache for cached nodes. If no cache entry is found, it will run a database query ("load.literal_by_dv")
     * on the NODES table and construct a new KiWiLiteral using the values of the literal columns
     * (svalue, ivalue, ...). The type of literal returned depends on the value of the ntype column.
     * <p/>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param value the value of the integer literal to load
     * @return a KiWiDoubleLiteral with the correct value, or null if it does not exist
     * @throws SQLException
     */
    public KiWiDoubleLiteral loadLiteral(double value) throws SQLException {
        // look in cache
        Element element = literalCache.get(LiteralCommons.createCacheKey(Double.toString(value),null,Namespaces.NS_XSD + "double"));
        if(element != null) {
            return (KiWiDoubleLiteral)element.getObjectValue();
        }

        requireJDBCConnection();

        KiWiUriResource ltype = loadUriResource(Namespaces.NS_XSD + "double");

        // ltype not persisted
        if(ltype == null || ltype.getId() == null) {
            return null;
        }

        // otherwise prepare a query, depending on the parameters given
        PreparedStatement query = getPreparedStatement("load.literal_by_dv");
        query.setDouble(1, value);
        query.setLong(2,ltype.getId());

        // run the database query and if it yields a result, construct a new node; the method call will take care of
        // caching the constructed node for future calls
        ResultSet result = query.executeQuery();
        try {
            if(result.next()) {
                return (KiWiDoubleLiteral)constructNodeFromDatabase(result);
            } else {
                return null;
            }
        } finally {
            result.close();
        }
    }

    /**
     * Load a boolean literal with the boolean value given as argument if it exists. The method will first look in
     * the node cache for cached nodes. If no cache entry is found, it will run a database query ("load.literal_by_bv")
     * on the NODES table and construct a new KiWiLiteral using the values of the literal columns
     * (svalue, ivalue, ...). The type of literal returned depends on the value of the ntype column.
     * <p/>
     * When a node is loaded from the database, it will be added to the different caches to speed up
     * subsequent requests.
     *
     * @param value the value of the integer literal to load
     * @return a KiWiBooleanLiteral with the correct value, or null if it does not exist
     * @throws SQLException
     */
    public KiWiBooleanLiteral loadLiteral(boolean value) throws SQLException {
        // look in cache
        Element element = literalCache.get(LiteralCommons.createCacheKey(Boolean.toString(value),null,Namespaces.NS_XSD + "boolean"));
        if(element != null) {
            return (KiWiBooleanLiteral)element.getObjectValue();
        }


        requireJDBCConnection();

        KiWiUriResource ltype = loadUriResource(Namespaces.NS_XSD + "boolean");

        // ltype not persisted
        if(ltype == null || ltype.getId() == null) {
            return null;
        }

        // otherwise prepare a query, depending on the parameters given
        PreparedStatement query = getPreparedStatement("load.literal_by_bv");
        query.setBoolean(1, value);
        query.setLong(2,ltype.getId());

        // run the database query and if it yields a result, construct a new node; the method call will take care of
        // caching the constructed node for future calls
        ResultSet result = query.executeQuery();
        try {
            if(result.next()) {
                return (KiWiBooleanLiteral)constructNodeFromDatabase(result);
            } else {
                return null;
            }
        } finally {
            result.close();
        }
    }

//    public KiWiTriple loadTripleById(Long id) {
//        // TODO: transactional caching
//        return null;
//    }


    /**
     * Store a new node in the database. The method will retrieve a new database id for the node and update the
     * passed object. Afterwards, the node data will be inserted into the database using appropriate INSERT
     * statements. The caller must make sure the connection is committed and closed properly.
     * <p/>
     * If the node already has an ID, the method will do nothing (assuming that it is already persistent)
     *
     * @param node
     * @throws SQLException
     */
    public synchronized void storeNode(KiWiNode node) throws SQLException {
        // if the node already has an ID, storeNode should not be called, since it is already persisted
        if(node.getId() != null) {
            log.warn("node {} already had a node ID, not persisting", node);
            return;
        }

        // ensure the data type of a literal is persisted first
        if(node instanceof KiWiLiteral) {
            KiWiLiteral literal = (KiWiLiteral)node;
            if(literal.getType() != null && literal.getType().getId() == null) {
                storeNode(literal.getType());
            }
        }

        requireJDBCConnection();

        // retrieve a new node id and set it in the node object
        node.setId(getNextSequence("seq.nodes"));

        // distinguish the different node types and run the appropriate updates
        if(node instanceof KiWiUriResource) {
            KiWiUriResource uriResource = (KiWiUriResource)node;

            PreparedStatement insertNode = getPreparedStatement("store.uri");
            insertNode.setLong(1,node.getId());
            insertNode.setString(2,uriResource.stringValue());
            insertNode.setTimestamp(3, new Timestamp(uriResource.getCreated().getTime()));
            insertNode.executeUpdate();

        } else if(node instanceof KiWiAnonResource) {
            KiWiAnonResource anonResource = (KiWiAnonResource)node;

            PreparedStatement insertNode = getPreparedStatement("store.bnode");
            insertNode.setLong(1,node.getId());
            insertNode.setString(2,anonResource.stringValue());
            insertNode.setTimestamp(3, new Timestamp(anonResource.getCreated().getTime()));
            insertNode.executeUpdate();
        } else if(node instanceof KiWiDateLiteral) {
            KiWiDateLiteral dateLiteral = (KiWiDateLiteral)node;

            PreparedStatement insertNode = getPreparedStatement("store.tliteral");
            insertNode.setLong(1,node.getId());
            insertNode.setString(2, dateLiteral.stringValue());
            insertNode.setTimestamp(3, new Timestamp(dateLiteral.getDateContent().getTime()));
            if(dateLiteral.getType() != null)
                insertNode.setLong(4,dateLiteral.getType().getId());
            else
                throw new IllegalStateException("a date literal must have a datatype");
            insertNode.setTimestamp(5, new Timestamp(dateLiteral.getCreated().getTime()));

            insertNode.executeUpdate();
        } else if(node instanceof KiWiIntLiteral) {
            KiWiIntLiteral intLiteral = (KiWiIntLiteral)node;

            PreparedStatement insertNode = getPreparedStatement("store.iliteral");
            insertNode.setLong(1,node.getId());
            insertNode.setString(2, intLiteral.getContent());
            insertNode.setDouble(3, intLiteral.getDoubleContent());
            insertNode.setLong(4, intLiteral.getIntContent());
            if(intLiteral.getType() != null)
                insertNode.setLong(5,intLiteral.getType().getId());
            else
                throw new IllegalStateException("an integer literal must have a datatype");
            insertNode.setTimestamp(6, new Timestamp(intLiteral.getCreated().getTime()));

            insertNode.executeUpdate();
        } else if(node instanceof KiWiDoubleLiteral) {
            KiWiDoubleLiteral doubleLiteral = (KiWiDoubleLiteral)node;

            PreparedStatement insertNode = getPreparedStatement("store.dliteral");
            insertNode.setLong(1, node.getId());
            insertNode.setString(2, doubleLiteral.getContent());
            insertNode.setDouble(3, doubleLiteral.getDoubleContent());
            if(doubleLiteral.getType() != null)
                insertNode.setLong(4,doubleLiteral.getType().getId());
            else
                throw new IllegalStateException("a double literal must have a datatype");
            insertNode.setTimestamp(5, new Timestamp(doubleLiteral.getCreated().getTime()));

            insertNode.executeUpdate();
        } else if(node instanceof KiWiBooleanLiteral) {
            KiWiBooleanLiteral booleanLiteral = (KiWiBooleanLiteral)node;

            PreparedStatement insertNode = getPreparedStatement("store.bliteral");
            insertNode.setLong(1,node.getId());
            insertNode.setString(2, booleanLiteral.getContent());
            insertNode.setBoolean(3, booleanLiteral.booleanValue());
            if(booleanLiteral.getType() != null)
                insertNode.setLong(4,booleanLiteral.getType().getId());
            else
                throw new IllegalStateException("a boolean literal must have a datatype");
            insertNode.setTimestamp(5, new Timestamp(booleanLiteral.getCreated().getTime()));

            insertNode.executeUpdate();
        } else if(node instanceof KiWiStringLiteral) {
            KiWiStringLiteral stringLiteral = (KiWiStringLiteral)node;

            PreparedStatement insertNode = getPreparedStatement("store.sliteral");
            insertNode.setLong(1,node.getId());
            insertNode.setString(2, stringLiteral.getContent());
            if(stringLiteral.getLocale() != null) {
                insertNode.setString(3,stringLiteral.getLocale().getLanguage());
            } else {
                insertNode.setObject(3, null);
            }
            if(stringLiteral.getType() != null) {
                insertNode.setLong(4,stringLiteral.getType().getId());
            } else {
                insertNode.setObject(4, null);
            }
            insertNode.setTimestamp(5, new Timestamp(stringLiteral.getCreated().getTime()));

            insertNode.executeUpdate();
        } else {
            log.warn("unrecognized node type: {}", node.getClass().getCanonicalName());
        }

        cacheNode(node);
    }

    /**
     * Store a triple in the database. This method assumes that all nodes used by the triple are already persisted.
     *
     * @param triple     the triple to store
     * @throws SQLException
     * @throws NullPointerException in case the subject, predicate, object or context have not been persisted
     * @return true in case the update added a new triple to the database, false in case the triple already existed
     */
    public synchronized boolean storeTriple(KiWiTriple triple) throws SQLException {

        Preconditions.checkNotNull(triple.getSubject().getId());
        Preconditions.checkNotNull(triple.getPredicate().getId());
        Preconditions.checkNotNull(triple.getObject().getId());
        Preconditions.checkNotNull(triple.getContext().getId());


        requireJDBCConnection();

        try {
            // retrieve a new triple ID and set it in the object
            if(triple.getId() == null) {
                triple.setId(getNextSequence("seq.triples"));
            }

            PreparedStatement insertTriple = getPreparedStatement("store.triple");
            insertTriple.setLong(1,triple.getId());
            insertTriple.setLong(2,triple.getSubject().getId());
            insertTriple.setLong(3,triple.getPredicate().getId());
            insertTriple.setLong(4,triple.getObject().getId());
            insertTriple.setLong(5,triple.getContext().getId());
            insertTriple.setBoolean(6,triple.isInferred());
            insertTriple.setTimestamp(7, new Timestamp(triple.getCreated().getTime()));
            int count = insertTriple.executeUpdate();

            cacheTriple(triple);

            return count > 0;
        } catch(SQLException ex) {
            // this is an ugly hack to catch duplicate key errors in some databases (H2)
            // better option could be http://stackoverflow.com/questions/6736518/h2-java-insert-ignore-allow-exception
            return false;
        }
    }


    /**
     * Mark the triple passed as argument as deleted, setting the "deleted" flag to true and
     * updating the timestamp value of "deletedAt".
     * <p/>
     * The triple remains in the database, because other entities might still reference it (e.g. a version).
     * Use the method cleanupTriples() to fully remove all deleted triples without references.
     *
     * @param triple
     */
    public void deleteTriple(KiWiTriple triple) throws SQLException {
        if(triple.getId() == null) {
            log.warn("attempting to remove non-persistent triple: {}",triple);
            return;
        }

        requireJDBCConnection();

        PreparedStatement deleteTriple = getPreparedStatement("delete.triple");
        deleteTriple.setLong(1,triple.getId());
        deleteTriple.executeUpdate();

        removeCachedTriple(triple);

        // make sure the triple is marked as deleted in case some service still holds a reference
        triple.setDeleted(true);
        triple.setDeletedAt(new Date());
    }

    /**
     * Mark the triple passed as argument as not deleted, setting the "deleted" flag to false and
     * clearing the timestamp value of "deletedAt".
     * <p/>
     * Note that this operation should only be called if the triple was deleted before in the same
     * transaction!
     *
     * @param triple
     */
    public void undeleteTriple(KiWiTriple triple) throws SQLException {
        if(triple.getId() == null) {
            log.warn("attempting to undelete non-persistent triple: {}",triple);
            return;
        }

        requireJDBCConnection();

        synchronized (triple) {
            if(!triple.isDeleted()) {
                log.warn("attemting to undelete triple that was not deleted: {}",triple);
                return;
            }

            PreparedStatement undeleteTriple = getPreparedStatement("undelete.triple");
            undeleteTriple.setLong(1, triple.getId());
            undeleteTriple.executeUpdate();

            // make sure the triple is marked as deleted in case some service still holds a reference
            triple.setDeleted(false);
            triple.setDeletedAt(null);

            cacheTriple(triple);
        }

    }


    /**
     * Remove from the database all triples that have been marked as deleted and are not referenced by any other
     * entity.
     */
    public void cleanupTriples() {
        throw new UnsupportedOperationException("garbage collection of triples is not yet supported!");
    }


    /**
     * List all contexts used in this triple store. See query.contexts .
     * @return
     * @throws SQLException
     */
    public CloseableIteration<KiWiResource, SQLException> listContexts() throws SQLException {
        requireJDBCConnection();

        PreparedStatement queryContexts = getPreparedStatement("query.contexts");

        final ResultSet result = queryContexts.executeQuery();

        return new ResultSetIteration<KiWiResource>(result, new ResultTransformerFunction<KiWiResource>() {
            @Override
            public KiWiResource apply(ResultSet row) throws SQLException {
                return (KiWiResource)loadNodeById(result.getLong("context"));
            }
        });

    }


    public CloseableIteration<KiWiNamespace, SQLException> listNamespaces() throws SQLException {
        requireJDBCConnection();

        PreparedStatement queryContexts = getPreparedStatement("query.namespaces");

        final ResultSet result = queryContexts.executeQuery();

        return new ResultSetIteration<KiWiNamespace>(result, new ResultTransformerFunction<KiWiNamespace>() {
            @Override
            public KiWiNamespace apply(ResultSet input) throws SQLException {
                return constructNamespaceFromDatabase(result);
            }
        });
    }


    /**
     * Return a Sesame RepositoryResult of statements according to the query pattern given in the arguments. Each of
     * the parameters subject, predicate, object and context may be null, indicating a wildcard query. If the boolean
     * parameter "inferred" is set to true, the result will also include inferred triples, if it is set to false only
     * base triples.
     * <p/>
     * The RepositoryResult holds a direct connection to the database and needs to be closed properly, or otherwise
     * the system might run out of resources. The returned RepositoryResult will try its best to clean up when the
     * iteration has completed or the garbage collector calls the finalize() method, but this can take longer than
     * necessary.
     *
     *
     * @param subject    the subject to query for, or null for a wildcard query
     * @param predicate  the predicate to query for, or null for a wildcard query
     * @param object     the object to query for, or null for a wildcard query
     * @param context    the context to query for, or null for a wildcard query
     * @param inferred   if true, the result will also contain triples inferred by the reasoner, if false not
     * @return a new RepositoryResult with a direct connection to the database; the result should be properly closed
     *         by the caller
     */
    public RepositoryResult<Statement> listTriples(KiWiResource subject, KiWiUriResource predicate, KiWiNode object, KiWiResource context, boolean inferred) throws SQLException {

        return new RepositoryResult<Statement>(
                new ExceptionConvertingIteration<Statement, RepositoryException>(listTriplesInternal(subject,predicate,object,context,inferred)) {
                    @Override
                    protected RepositoryException convert(Exception e) {
                        return new RepositoryException("database error while iterating over result set",e);
                    }
                }

        );
    }

    /**
     * Internal implementation for actually carrying out the query. Returns a closable iteration that can be used
     * in a repository result. The iteration is forward-only and does not allow removing result rows.
     *
     * @param subject    the subject to query for, or null for a wildcard query
     * @param predicate  the predicate to query for, or null for a wildcard query
     * @param object     the object to query for, or null for a wildcard query
     * @param context    the context to query for, or null for a wildcard query
     * @param inferred   if true, the result will also contain triples inferred by the reasoner, if false not
     * @return a ClosableIteration that wraps the database ResultSet; needs to be closed explicitly by the caller
     * @throws SQLException
     */
    private CloseableIteration<Statement, SQLException> listTriplesInternal(KiWiResource subject, KiWiUriResource predicate, KiWiNode object, KiWiResource context, boolean inferred) throws SQLException {
        // if one of the database ids is null, there will not be any database results, so we can return an empty result
        if(subject != null && subject.getId() == null) {
            return new EmptyIteration<Statement, SQLException>();
        }
        if(predicate != null && predicate.getId() == null) {
            return new EmptyIteration<Statement, SQLException>();
        }
        if(object != null && object.getId() == null) {
            return new EmptyIteration<Statement, SQLException>();
        }
        if(context != null && context.getId() == null) {
            return new EmptyIteration<Statement, SQLException>();
        }

        requireJDBCConnection();

        // otherwise we need to create an appropriate SQL query and execute it, the repository result will be read-only
        // and only allow forward iteration, so we can limit the query using the respective flags
        PreparedStatement query = connection.prepareStatement(
                constructTripleQuery(subject,predicate,object,context,inferred),
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY
        );
        query.clearParameters();

        // set query parameters
        int position = 1;
        if(subject != null) {
            query.setLong(position++, subject.getId());
        }
        if(predicate != null) {
            query.setLong(position++, predicate.getId());
        }
        if(object != null) {
            query.setLong(position++, object.getId());
        }
        if(context != null) {
            query.setLong(position++, context.getId());
        }

        final ResultSet result = query.executeQuery();


        return new ResultSetIteration<Statement>(result, true, new ResultTransformerFunction<Statement>() {
            @Override
            public Statement apply(ResultSet row) throws SQLException {
                return constructTripleFromDatabase(result);
            }
        });
    }

    /**
     * Construct the SQL query string from the query pattern passed as arguments
     *
     * @param subject    the subject to query for, or null for a wildcard query
     * @param predicate  the predicate to query for, or null for a wildcard query
     * @param object     the object to query for, or null for a wildcard query
     * @param context    the context to query for, or null for a wildcard query
     * @param inferred   if true, the result will also contain triples inferred by the reasoner, if false not
     * @return an SQL query string representing the triple pattern
     */
    protected String constructTripleQuery(KiWiResource subject, KiWiUriResource predicate, KiWiNode object, KiWiResource context, boolean inferred) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT id,subject,predicate,object,context,deleted,inferred,creator,createdAt,deletedAt FROM triples WHERE deleted = false");
        if(subject != null) {
            builder.append(" AND subject = ?");
        }
        if(predicate != null) {
            builder.append(" AND predicate = ?");
        }
        if(object != null) {
            builder.append(" AND object = ?");
        }
        if(context != null) {
            builder.append(" AND context = ?");
        }
        if(!inferred) {
            builder.append(" AND inferred = false");
        }
        return builder.toString();

    }

    protected KiWiNamespace constructNamespaceFromDatabase(ResultSet row) throws SQLException {
        KiWiNamespace result = new KiWiNamespace(row.getString("prefix"),row.getString("uri"));
        result.setId(row.getLong("id"));
        result.setCreated(new Date(row.getTimestamp("createdAt").getTime()));

        namespacePrefixCache.put(new Element(result.getPrefix(),result));
        namespaceUriCache.put(new Element(result.getUri(),result));

        return result;
    }

    /**
     * Construct an appropriate KiWiNode from the result of an SQL query. The method will not change the
     * ResultSet iterator, only read its values, so it needs to be executed for each row separately.
     * @param row
     * @return
     */
    protected KiWiNode constructNodeFromDatabase(ResultSet row) throws SQLException {

        Long id = row.getLong("id");

        Element cached = nodeCache.get(id);

        // lookup element in cache first, so we can avoid reconstructing it if it is already there
        if(cached != null) {
            return (KiWiNode)cached.getObjectValue();
        }

        String ntype = row.getString("ntype");
        if("uri".equals(ntype)) {
            KiWiUriResource result = new KiWiUriResource(row.getString("svalue"));
            result.setId(id);
            result.setCreated(new Date(row.getTimestamp("createdAt").getTime()));

            cacheNode(result);
            return result;
        } else if("bnode".equals(ntype)) {
            KiWiAnonResource result = new KiWiAnonResource(row.getString("svalue"));
            result.setId(row.getLong("id"));
            result.setCreated(new Date(row.getTimestamp("createdAt").getTime()));

            cacheNode(result);
            return result;
        } else if("string".equals(ntype)) {
            final KiWiStringLiteral result = new KiWiStringLiteral(row.getString("svalue"));
            result.setId(row.getLong("id"));
            result.setCreated(new Date(row.getTimestamp("createdAt").getTime()));

            if(row.getString("lang") != null) {
                result.setLocale(getLocale(row.getString("lang")));
            }
            if(row.getLong("ltype") != 0) {
                result.setType((KiWiUriResource) loadNodeById(row.getLong("ltype")));
            } else {
                log.warn("Loaded literal without type: '{}' (id:{}).", result.getContent(), result.getId());
            }

            cacheNode(result);
            return result;
        } else if("int".equals(ntype)) {
            KiWiIntLiteral result = new KiWiIntLiteral();
            result.setId(row.getLong("id"));
            result.setCreated(new Date(row.getTimestamp("createdAt").getTime()));
            result.setIntContent(row.getLong("ivalue"));
            if(row.getLong("ltype") != 0) {
                result.setType((KiWiUriResource) loadNodeById(row.getLong("ltype")));
            }

            cacheNode(result);
            return result;
        } else if("double".equals(ntype)) {
            KiWiDoubleLiteral result = new KiWiDoubleLiteral();
            result.setId(row.getLong("id"));
            result.setCreated(new Date(row.getTimestamp("createdAt").getTime()));
            result.setDoubleContent(row.getDouble("dvalue"));
            if(row.getLong("ltype") != 0) {
                result.setType((KiWiUriResource) loadNodeById(row.getLong("ltype")));
            }

            cacheNode(result);
            return result;
        } else if("boolean".equals(ntype)) {
            KiWiBooleanLiteral result = new KiWiBooleanLiteral();
            result.setId(row.getLong("id"));
            result.setCreated(new Date(row.getTimestamp("createdAt").getTime()));
            result.setValue(row.getBoolean("bvalue"));

            if(row.getLong("ltype") != 0) {
                result.setType((KiWiUriResource) loadNodeById(row.getLong("ltype")));
            }

            cacheNode(result);
            return result;
        } else if("date".equals(ntype)) {
            KiWiDateLiteral result = new KiWiDateLiteral();
            result.setId(row.getLong("id"));
            result.setCreated(new Date(row.getTimestamp("createdAt").getTime()));
            result.setDateContent(new Date(row.getTimestamp("tvalue").getTime()));

            if(row.getLong("ltype") != 0) {
                result.setType((KiWiUriResource) loadNodeById(row.getLong("ltype")));
            }

            cacheNode(result);
            return result;
        } else {
            throw new IllegalArgumentException("unknown node type in database result: "+ntype);
        }
    }

    /**
     * Construct a KiWiTriple from the result of an SQL query. The query result is expected to contain the
     * following columns:
     * <ul>
     *     <li>id: the database id of the triple (long value)</li>
     *     <li>subject: the database id of the subject (long value); the node will be loaded using the loadNodeById method</li>
     *     <li>predicate: the database id of the predicate (long value); the node will be loaded using the loadNodeById method</li>
     *     <li>object: the database id of the object (long value); the node will be loaded using the loadNodeById method</li>
     *     <li>context: the database id of the context (long value); the node will be loaded using the loadNodeById method</li>
     *     <li>creator: the database id of the creator (long value); the node will be loaded using the loadNodeById method; may be null</li>
     *     <li>deleted: a flag (boolean) indicating whether this triple has been deleted</li>
     *     <li>inferred: a flag (boolean) indicating whether this triple has been inferred by the KiWi reasoner</li>
     *     <li>createdAt: a timestamp representing the creation date of the triple</li>
     *     <li>createdAt: a timestamp representing the deletion date of the triple (null in case triple is not deleted)</li>
     * </ul>
     * The method will not change the ResultSet iterator, only read its values, so it needs to be executed for each row separately.
     *
     * @param row a database result containing the columns described above
     * @return a KiWiTriple representation of the database result
     */
    protected KiWiTriple constructTripleFromDatabase(ResultSet row) throws SQLException {
        Long id = row.getLong("id");

        Element cached = tripleCache.get(id);

        // lookup element in cache first, so we can avoid reconstructing it if it is already there
        if(cached != null) {
            return (KiWiTriple)cached.getObjectValue();
        }

        KiWiTriple result = new KiWiTriple();
        result.setId(id);
        result.setSubject((KiWiResource)loadNodeById(row.getLong("subject")));
        result.setPredicate((KiWiUriResource) loadNodeById(row.getLong("predicate")));
        result.setObject(loadNodeById(row.getLong("object")));
        result.setContext((KiWiResource) loadNodeById(row.getLong("context")));
        if(row.getLong("creator") != 0) {
            result.setCreator((KiWiResource)loadNodeById(row.getLong("creator")));
        }
        result.setDeleted(row.getBoolean("deleted"));
        result.setInferred(row.getBoolean("deleted"));
        result.setCreated(new Date(row.getTimestamp("createdAt").getTime()));
        try {
            if(row.getDate("deletedAt") != null) {
                result.setDeletedAt(new Date(row.getTimestamp("deletedAt").getTime()));
            }
        } catch (SQLException ex) {
            // work around a MySQL problem with null dates
            // (see http://stackoverflow.com/questions/782823/handling-datetime-values-0000-00-00-000000-in-jdbc)
        }

        tripleCache.put(new Element(id,result));

        return result;
    }


    protected static Locale getLocale(String language) {
        Locale locale = localeMap.get(language);
        if(locale == null && language != null) {
            locale = LocaleUtils.toLocale(language.replace("-","_"));
            localeMap.put(language,locale);

        }
        return locale;
    }

    /**
     * Return the prepared statement with the given identifier; first looks in the statement cache and if it does
     * not exist there create a new statement.
     *
     * @param key the id of the statement in statements.properties
     * @return
     * @throws SQLException
     */
    public PreparedStatement getPreparedStatement(String key) throws SQLException {
        requireJDBCConnection();

        PreparedStatement statement = statementCache.get(key);
        if(statement == null) {
            statement = connection.prepareStatement(dialect.getStatement(key));
            statementCache.put(key,statement);
        }
        statement.clearParameters();
        return statement;
    }

    /**
     * Get next number in a sequence; for databases without sequence support (e.g. MySQL), this method will first update a
     * sequence table and then return the value.
     *
     * @param sequenceName the identifier in statements.properties for querying the sequence
     * @return a new sequence ID
     * @throws SQLException
     */
    public long getNextSequence(String sequenceName) throws SQLException {
        requireJDBCConnection();

        // retrieve a new node id and set it in the node object

        // if there is a preparation needed to update the transaction, run it first
        if(dialect.hasStatement(sequenceName+".prep")) {
            PreparedStatement prepNodeId = getPreparedStatement(sequenceName+".prep");
            prepNodeId.executeUpdate();
        }

        PreparedStatement queryNodeId = getPreparedStatement(sequenceName);
        ResultSet resultNodeId = queryNodeId.executeQuery();
        try {
            if(resultNodeId.next()) {
                return resultNodeId.getLong(1);
            } else {
                throw new SQLException("the sequence did not return a new value");
            }
        } finally {
            resultNodeId.close();
        }

    }


    private void cacheNode(KiWiNode node) {
        if(node.getId() != null) {
            nodeCache.put(new Element(node.getId(),node));
        }
        if(node instanceof KiWiUriResource) {
            uriCache.put(new Element(node.stringValue(), node));
        } else if(node instanceof KiWiAnonResource) {
            bnodeCache.put(new Element(node.stringValue(),node));
        } else if(node instanceof KiWiLiteral) {
            literalCache.put(new Element(LiteralCommons.createCacheKey((Literal) node),node));
        }
    }

    private void cacheTriple(KiWiTriple triple) {
        if(triple.getId() != null) {
            tripleCache.put(new Element(triple.getId(),triple));
        }
    }

    private void removeCachedTriple(KiWiTriple triple) {
        if(triple.getId() != null) {
            tripleCache.remove(triple.getId());
        }
    }

    /**
     * Return a collection of database tables contained in the database. This query is used for checking whether
     * the database needs to be created when initialising the system.
     *
     *
     *
     * @return
     * @throws SQLException
     */
    public Set<String> getDatabaseTables() throws SQLException {
        requireJDBCConnection();

        PreparedStatement statement = getPreparedStatement("meta.tables");
        ResultSet result = statement.executeQuery();
        try {
            Set<String> tables = new HashSet<String>();
            while(result.next()) {
                tables.add(result.getString(1).toLowerCase());
            }
            return tables;
        } finally {
            result.close();
        }
    }


    /**
     * Return the KiWi version of the database this connection is operating on. This query is necessary for
     * checking proper state of a database when initialising the system.
     *
     * @return
     */
    public int getDatabaseVersion() throws SQLException {
        requireJDBCConnection();

        PreparedStatement statement = getPreparedStatement("meta.version");
        ResultSet result = statement.executeQuery();
        try {
            if(result.next()) {
                return Integer.parseInt(result.getString(1));
            } else {
                throw new SQLException("no version information available");
            }
        } finally {
            result.close();
        }
    }


    /**
     * Sets this connection's auto-commit mode to the given state.
     * If a connection is in auto-commit mode, then all its SQL
     * statements will be executed and committed as individual
     * transactions.  Otherwise, its SQL statements are grouped into
     * transactions that are terminated by a call to either
     * the method <code>commit</code> or the method <code>rollback</code>.
     * By default, new connections are in auto-commit
     * mode.
     * <P>
     * The commit occurs when the statement completes. The time when the statement
     * completes depends on the type of SQL Statement:
     * <ul>
     * <li>For DML statements, such as Insert, Update or Delete, and DDL statements,
     * the statement is complete as soon as it has finished executing.
     * <li>For Select statements, the statement is complete when the associated result
     * set is closed.
     * <li>For <code>CallableStatement</code> objects or for statements that return
     * multiple results, the statement is complete
     * when all of the associated result sets have been closed, and all update
     * counts and output parameters have been retrieved.
     *</ul>
     * <P>
     * <B>NOTE:</B>  If this method is called during a transaction and the
     * auto-commit mode is changed, the transaction is committed.  If
     * <code>setAutoCommit</code> is called and the auto-commit mode is
     * not changed, the call is a no-op.
     *
     * @param autoCommit <code>true</code> to enable auto-commit mode;
     *         <code>false</code> to disable it
     * @exception java.sql.SQLException if a database access error occurs,
     *  setAutoCommit(true) is called while participating in a distributed transaction,
     * or this method is called on a closed connection
     * @see #getAutoCommit
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
        if(connection != null) {
            connection.setAutoCommit(autoCommit);
        }
    }


    /**
     * Retrieves the current auto-commit mode for this <code>Connection</code>
     * object.
     *
     * @return the current state of this <code>Connection</code> object's
     *         auto-commit mode
     * @exception java.sql.SQLException if a database access error occurs
     * or this method is called on a closed connection
     * @see #setAutoCommit
     */
    public boolean getAutoCommit() throws SQLException {
        return autoCommit;
    }

    /**
     * Makes all changes made since the previous
     * commit/rollback permanent and releases any database locks
     * currently held by this <code>Connection</code> object.
     * This method should be
     * used only when auto-commit mode has been disabled.
     *
     * @exception java.sql.SQLException if a database access error occurs,
     * this method is called while participating in a distributed transaction,
     * if this method is called on a closed conection or this
     *            <code>Connection</code> object is in auto-commit mode
     * @see #setAutoCommit
     */
    public void commit() throws SQLException {
        if(connection != null) {
            connection.commit();
        }
    }

    /**
     * Undoes all changes made in the current transaction
     * and releases any database locks currently held
     * by this <code>Connection</code> object. This method should be
     * used only when auto-commit mode has been disabled.
     *
     * @exception java.sql.SQLException if a database access error occurs,
     * this method is called while participating in a distributed transaction,
     * this method is called on a closed connection or this
     *            <code>Connection</code> object is in auto-commit mode
     * @see #setAutoCommit
     */
    public void rollback() throws SQLException {
        if(connection != null && !connection.isClosed()) {
            connection.rollback();
        }
    }

    /**
     * Retrieves whether this <code>Connection</code> object has been
     * closed.  A connection is closed if the method <code>close</code>
     * has been called on it or if certain fatal errors have occurred.
     * This method is guaranteed to return <code>true</code> only when
     * it is called after the method <code>Connection.close</code> has
     * been called.
     * <P>
     * This method generally cannot be called to determine whether a
     * connection to a database is valid or invalid.  A typical client
     * can determine that a connection is invalid by catching any
     * exceptions that might be thrown when an operation is attempted.
     *
     * @return <code>true</code> if this <code>Connection</code> object
     *         is closed; <code>false</code> if it is still open
     * @exception java.sql.SQLException if a database access error occurs
     */
    public boolean isClosed() throws SQLException {
        if(connection != null) {
            return connection.isClosed();
        } else {
            return false;
        }
    }


    /**
     * Releases this <code>Connection</code> object's database and JDBC resources
     * immediately instead of waiting for them to be automatically released.
     * <P>
     * Calling the method <code>close</code> on a <code>Connection</code>
     * object that is already closed is a no-op.
     * <P>
     * It is <b>strongly recommended</b> that an application explicitly
     * commits or rolls back an active transaction prior to calling the
     * <code>close</code> method.  If the <code>close</code> method is called
     * and there is an active transaction, the results are implementation-defined.
     * <P>
     *
     * @exception java.sql.SQLException SQLException if a database access error occurs
     */
    public void close() throws SQLException {
        if(connection != null) {
            // close all prepared statements
            try {
                for(Map.Entry<String,PreparedStatement> entry : statementCache.entrySet()) {
                    try {
                        entry.getValue().close();
                    } catch (SQLException ex) {}
                }
            } catch(AbstractMethodError ex) {
                log.debug("database system does not allow closing statements");
            }

            connection.close();
        }
    }
}
