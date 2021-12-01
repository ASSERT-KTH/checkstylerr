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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.wrapper;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContextBuilder;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.group.StatementExecuteGroupEngine;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.group.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.group.ExecuteGroupEngine;
import org.apache.shardingsphere.infra.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.route.DataNodeRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.metrics.MetricsUtils;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;

/**
 * Executor wrapper for statement.
 */
@RequiredArgsConstructor
public final class StatementExecutorWrapper implements JDBCExecutorWrapper {
    
    private static final ProxySchemaContexts PROXY_SCHEMA_CONTEXTS = ProxySchemaContexts.getInstance();
    
    private final SchemaContext schema;
    
    private final SQLStatement sqlStatement;
    
    @SuppressWarnings("unchecked")
    @Override
    public ExecutionContext execute(final String sql) {
        Collection<ShardingSphereRule> rules = schema.getSchema().getRules();
        if (rules.isEmpty()) {
            return createExecutionContext(sql);
        }
        RouteContext routeContext = 
                new DataNodeRouter(schema.getSchema().getMetaData(), PROXY_SCHEMA_CONTEXTS.getSchemaContexts().getProps(), rules).route(sqlStatement, sql, Collections.emptyList());
        routeMetricsCollect(routeContext, rules);
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(schema.getSchema().getMetaData().getSchema().getConfiguredSchemaMetaData(),
                PROXY_SCHEMA_CONTEXTS.getSchemaContexts().getProps(), rules).rewrite(sql, Collections.emptyList(), routeContext);
        return new ExecutionContext(routeContext.getSqlStatementContext(), ExecutionContextBuilder.build(schema.getSchema().getMetaData(), sqlRewriteResult));
    }
    
    private ExecutionContext createExecutionContext(final String sql) {
        String dataSource = schema.getSchema().getDataSources().isEmpty() ? "" : schema.getSchema().getDataSources().keySet().iterator().next();
        return new ExecutionContext(
                new CommonSQLStatementContext(sqlStatement), new ExecutionUnit(dataSource, new SQLUnit(sql, Collections.emptyList())));
    }
    
    @Override
    public boolean execute(final Statement statement, final String sql, final boolean isReturnGeneratedKeys) throws SQLException {
        return statement.execute(sql, isReturnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
    }
    
    @Override
    public ExecuteGroupEngine getExecuteGroupEngine(final BackendConnection backendConnection, final StatementOption option) {
        int maxConnectionsSizePerQuery = PROXY_SCHEMA_CONTEXTS.getSchemaContexts().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new StatementExecuteGroupEngine(maxConnectionsSizePerQuery, backendConnection, option, schema.getSchema().getRules());
    }
    
    private void routeMetricsCollect(final RouteContext routeContext, final Collection<ShardingSphereRule> rules) {
        MetricsUtils.buriedShardingMetrics(routeContext.getRouteResult().getRouteUnits());
        MetricsUtils.buriedShardingRuleMetrics(routeContext, rules);
    }
}
