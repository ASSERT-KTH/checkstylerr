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
package org.apache.marmotta.kiwi.config;

import org.apache.marmotta.kiwi.generator.IDGeneratorType;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;

/**
 * An object to hold a KiWi persistence configuration. The configuration consists of:
 * <ul>
 *     <li>a name to identify the persistence configuration throughout the system</li>
 *     <li>a JDBC URL for connecting to the respective relational database</li>
 *     <li>a database user to access the database</li>
 *     <li>a database password for that user</li>
 *     <li>a KiWi dialect for representing the different SQL dialects</li>
 * </ul>
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiConfiguration {

    /**
     * A unique name for identifying this instance of KiWiPersistence. Can be used in case there are several
     * instances running in the same environment.
     */
    private String name;
    private String jdbcUrl;
    private String dbUser;
    private String dbPassword;

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


    /**
     * The SQL dialect to use
     */
    private KiWiDialect dialect;

    /**
     * A flag indicating if the query logging (Tomcat JDBC SlowQueryReport) is enabled or not.
     */
    private boolean queryLoggingEnabled = false;

    /**
     * Enable batched commit for triples (if supported by the database dialect). If this is enabled,
     * the KiWiConnection will use an in-memory buffer for stored triples that are committed in a batch
     * once the limit is reached or the connection committed. Enabling this can significantly improve the
     * performance, and is usually quite safe for triples.
     */
    private boolean tripleBatchCommit;


    /**
     * Enable batched commit for nodes (if supported by the database dialect). If this is enabled,
     * the KiWiConnection will use an in-memory buffer for stored triples that are committed in a batch
     * once the limit is reached or the connection committed. Enabling this can significantly improve the
     * performance. For nodes this is not safe in a clustered environment where multiple servers access the
     * same database.
     */
    private boolean nodeBatchCommit;


    private int tripleBatchSize = 10000;

    private int nodeBatchSize = 1000;

    /**
     * Size of the database cursor for pre-fetching rows on database supporting this feature. If the size is set to 0,
     * no cursor is used and all rows are retrieved in one batch.
     *
     * @see java.sql.PreparedStatement#setFetchSize(int)
     */
    private int cursorSize = 1000;

    private boolean fulltextEnabled     = false;
    private String[] fulltextLanguages;

    /**
     * The method to use for generating row IDs. Starting with Marmotta 3.2, the default is to use the Twitter Snowflake
     * algorithm.
     */
    private IDGeneratorType idGeneratorType = IDGeneratorType.SNOWFLAKE;


    public KiWiConfiguration(String name, String jdbcUrl, String dbUser, String dbPassword, KiWiDialect dialect) {
        this(name, jdbcUrl, dbUser, dbPassword, dialect, null, null);
    }

    public KiWiConfiguration(String name, String jdbcUrl, String dbUser, String dbPassword, KiWiDialect dialect, String defaultContext, String inferredContext) {
        this.dbPassword = dbPassword;
        this.dbUser = dbUser;
        this.dialect = dialect;
        this.jdbcUrl = jdbcUrl;
        this.name = name;
        this.defaultContext = defaultContext;
        this.inferredContext = inferredContext;

        tripleBatchCommit = dialect.isBatchSupported();
        nodeBatchCommit   = dialect.isBatchSupported();
    }


    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbUser() {
        return dbUser;
    }

    public KiWiDialect getDialect() {
        return dialect;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getName() {
        return name;
    }

    public boolean isQueryLoggingEnabled() {
        return queryLoggingEnabled;
    }

    public void setQueryLoggingEnabled(boolean queryLoggingEnabled) {
        this.queryLoggingEnabled = queryLoggingEnabled;
    }

    public String getDefaultContext() {
        return defaultContext;
    }

    public void setDefaultContext(String defaultContext) {
        this.defaultContext = defaultContext;
    }

    public String getInferredContext() {
        return inferredContext;
    }

    public void setInferredContext(String inferredContext) {
        this.inferredContext = inferredContext;
    }

    /**
     * Enable batched commit for triples (if supported by the database dialect). If this is enabled,
     * the KiWiConnection will use an in-memory buffer for stored triples that are committed in a batch
     * once the limit is reached or the connection committed. Enabling this can significantly improve the
     * performance, and is usually quite safe for triples.
     */
    public boolean isTripleBatchCommit() {
        return tripleBatchCommit;
    }

    /**
     * Enable batched commit for triples (if supported by the database dialect). If this is enabled,
     * the KiWiConnection will use an in-memory buffer for stored triples that are committed in a batch
     * once the limit is reached or the connection committed. Enabling this can significantly improve the
     * performance, and is usually quite safe for triples.
     */
    public void setTripleBatchCommit(boolean tripleBatchCommit) {
        if(dialect.isBatchSupported()) {
            this.tripleBatchCommit = tripleBatchCommit;
        }
    }

    /**
     * Enable batched commit for nodes (if supported by the database dialect). If this is enabled,
     * the KiWiConnection will use an in-memory buffer for stored triples that are committed in a batch
     * once the limit is reached or the connection committed. Enabling this can significantly improve the
     * performance. For nodes this is not safe in a clustered environment where multiple servers access the
     * same database.
     */
    public boolean isNodeBatchCommit() {
        return nodeBatchCommit;
    }

    /**
     * Enable batched commit for nodes (if supported by the database dialect). If this is enabled,
     * the KiWiConnection will use an in-memory buffer for stored triples that are committed in a batch
     * once the limit is reached or the connection committed. Enabling this can significantly improve the
     * performance. For nodes this is not safe in a clustered environment where multiple servers access the
     * same database.
     */
    public void setNodeBatchCommit(boolean nodeBatchCommit) {
        this.nodeBatchCommit = nodeBatchCommit;
    }

    public int getTripleBatchSize() {
        return tripleBatchSize;
    }

    public void setTripleBatchSize(int tripleBatchSize) {
        this.tripleBatchSize = tripleBatchSize;
    }

    public int getNodeBatchSize() {
        return nodeBatchSize;
    }

    public void setNodeBatchSize(int nodeBatchSize) {
        this.nodeBatchSize = nodeBatchSize;
    }

    /**
     * Size of the database cursor for pre-fetching rows on database supporting this feature. If the size is set to 0,
     * no cursor is used and all rows are retrieved in one batch.
     *
     * @see java.sql.PreparedStatement#setFetchSize(int)
     */
    public int getCursorSize() {
        return cursorSize;
    }

    /**
     * Size of the database cursor for pre-fetching rows on database supporting this feature. If the size is set to 0,
     * no cursor is used and all rows are retrieved in one batch.
     *
     * @see java.sql.PreparedStatement#setFetchSize(int)
     */
    public void setCursorSize(int cursorSize) {
        this.cursorSize = cursorSize;
    }

    /**
     * The ID generator used for generating row IDs in the database. See documentation in {@link IDGeneratorType}
     *
     * @return the type defined for this configuration
     */
    public IDGeneratorType getIdGeneratorType() {
        return idGeneratorType;
    }

    public void setIdGeneratorType(IDGeneratorType idGeneratorType) {
        this.idGeneratorType = idGeneratorType;
    }

    /**
     * Return true in case fulltext support is enabled in this configuration. If this is the case, the SPARQL module
     * will prepare the database with appropriate fulltext index support. Since this adds additional overhead, it is
     * not enabled by default.
     *
     * @return
     */
    public boolean isFulltextEnabled() {
        return fulltextEnabled;
    }

    /**
     * Set to true in case fulltext support is enabled in this configuration. If this is the case, the SPARQL module
     * will prepare the database with appropriate fulltext index support. Since this adds additional overhead, it is
     * not enabled by default.
     */
    public void setFulltextEnabled(boolean fulltextEnabled) {
        this.fulltextEnabled = fulltextEnabled;
    }

    /**
     * Return the languages (ISO codes) for which to add specific fulltext search support. The SPARQL module will add a
     * separate fulltext index for each supported language, adding additional overhead. In case you only want generic
     * fulltext support, use the empty array.
     */
    public String[] getFulltextLanguages() {
        return fulltextLanguages;
    }

    /**
     * Set the languages (ISO codes) for which to add specific fulltext search support. The SPARQL module will add a
     * separate fulltext index for each supported language, adding additional overhead. In case you only want generic
     * fulltext support, use the empty array.
     */
    public void setFulltextLanguages(String[] fulltextLanguages) {
        this.fulltextLanguages = fulltextLanguages;
    }

}
