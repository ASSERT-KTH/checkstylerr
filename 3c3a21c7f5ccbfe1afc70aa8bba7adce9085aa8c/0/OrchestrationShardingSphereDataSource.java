/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.driver.orchestration.internal.datasource;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationDataSource;
import org.apache.shardingsphere.driver.orchestration.internal.circuit.datasource.CircuitBreakerDataSource;
import org.apache.shardingsphere.driver.orchestration.internal.schema.JDBCOrchestrationSchemaContexts;
import org.apache.shardingsphere.driver.orchestration.internal.util.DataSourceConverter;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.kernel.context.SchemaContextsBuilder;
import org.apache.shardingsphere.orchestration.core.config.ConfigCenter;
import org.apache.shardingsphere.orchestration.core.facade.OrchestrationFacade;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Orchestration ShardingSphere data source.
 */
@Getter
public final class OrchestrationShardingSphereDataSource extends AbstractUnsupportedOperationDataSource implements AutoCloseable {
    
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    @Setter
    private PrintWriter logWriter = new PrintWriter(System.out);
    
    private final SchemaContexts schemaContexts;
    
    public OrchestrationShardingSphereDataSource(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        OrchestrationFacade orchestrationFacade = createOrchestrationFacade(orchestrationConfig);
        schemaContexts = new JDBCOrchestrationSchemaContexts(createSchemaContexts(orchestrationFacade), orchestrationFacade);
    }
    
    public OrchestrationShardingSphereDataSource(final Map<String, DataSource> dataSourceMap, final Collection<RuleConfiguration> ruleConfigurations,
                                                 final Properties props, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        OrchestrationFacade orchestrationFacade = createOrchestrationFacade(orchestrationConfig);
        schemaContexts = new JDBCOrchestrationSchemaContexts(createSchemaContexts(dataSourceMap, ruleConfigurations, props), orchestrationFacade);
        uploadLocalConfiguration(orchestrationFacade);
    }
    
    private OrchestrationFacade createOrchestrationFacade(final OrchestrationConfiguration config) {
        OrchestrationFacade result = OrchestrationFacade.getInstance();
        result.init(config, Collections.singletonList(DefaultSchema.LOGIC_NAME));
        result.onlineInstance();
        return result;
    }
    
    private SchemaContexts createSchemaContexts(final OrchestrationFacade orchestrationFacade) throws SQLException {
        ConfigCenter configCenter = orchestrationFacade.getConfigCenter();
        Map<String, DataSourceConfiguration> dataSourceConfigs = configCenter.loadDataSourceConfigurations(DefaultSchema.LOGIC_NAME);
        Collection<RuleConfiguration> ruleConfigurations = configCenter.loadRuleConfigurations(DefaultSchema.LOGIC_NAME);
        Map<String, DataSource> dataSourceMap = DataSourceConverter.getDataSourceMap(dataSourceConfigs);
        SchemaContextsBuilder schemaContextsBuilder = new SchemaContextsBuilder(createDatabaseType(dataSourceMap), 
                Collections.singletonMap(DefaultSchema.LOGIC_NAME, dataSourceMap),
                Collections.singletonMap(DefaultSchema.LOGIC_NAME, ruleConfigurations), 
                new Authentication(), configCenter.loadProperties());
        return schemaContextsBuilder.build();
    }
    
    private SchemaContexts createSchemaContexts(final Map<String, DataSource> dataSourceMap, final Collection<RuleConfiguration> ruleConfigurations,
                                                final Properties props) throws SQLException {
        SchemaContextsBuilder schemaContextsBuilder = new SchemaContextsBuilder(createDatabaseType(dataSourceMap),
                Collections.singletonMap(DefaultSchema.LOGIC_NAME, dataSourceMap),
                Collections.singletonMap(DefaultSchema.LOGIC_NAME, ruleConfigurations),
                new Authentication(), props);
        return schemaContextsBuilder.build();
    }
    
    private DatabaseType createDatabaseType(final Map<String, DataSource> dataSourceMap) throws SQLException {
        DatabaseType result = null;
        for (DataSource each : dataSourceMap.values()) {
            DatabaseType databaseType = createDatabaseType(each);
            Preconditions.checkState(null == result || result == databaseType, String.format("Database type inconsistent with '%s' and '%s'", result, databaseType));
            result = databaseType;
        }
        return result;
    }
    
    private DatabaseType createDatabaseType(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseTypes.getDatabaseTypeByURL(connection.getMetaData().getURL());
        }
    }
    
    private void uploadLocalConfiguration(final OrchestrationFacade orchestrationFacade) {
        Map<String, DataSourceConfiguration> dataSourceConfigs = DataSourceConverter.getDataSourceConfigurationMap(schemaContexts.getDefaultSchemaContext().getSchema().getDataSources());
        Collection<RuleConfiguration> ruleConfigurations = schemaContexts.getDefaultSchemaContext().getSchema().getConfigurations();
        Properties props = schemaContexts.getProps().getProps();
        orchestrationFacade.onlineInstance(
                Collections.singletonMap(DefaultSchema.LOGIC_NAME, dataSourceConfigs), 
                Collections.singletonMap(DefaultSchema.LOGIC_NAME, ruleConfigurations), null, props);
    }
    
    @Override
    public Connection getConnection() {
        return schemaContexts.isCircuitBreak() ? new CircuitBreakerDataSource().getConnection()
                : new ShardingSphereConnection(getDataSourceMap(), schemaContexts, TransactionTypeHolder.get());
    }
    
    @Override
    public Connection getConnection(final String username, final String password) {
        return getConnection();
    }
    
    @Override
    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
    
    @Override
    public void close() throws Exception {
        getDataSourceMap().forEach((key, value) -> close(value));
        schemaContexts.close();
    }
    
    private void close(final DataSource dataSource) {
        try {
            Method method = dataSource.getClass().getDeclaredMethod("close");
            method.setAccessible(true);
            method.invoke(dataSource);
        } catch (final ReflectiveOperationException ignored) {
        }
    }
    
    private Map<String, DataSource> getDataSourceMap() {
        return schemaContexts.getSchemaContexts().get(DefaultSchema.LOGIC_NAME).getSchema().getDataSources();
    }
}
