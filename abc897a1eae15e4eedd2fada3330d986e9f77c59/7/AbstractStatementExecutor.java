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

package org.apache.shardingsphere.driver.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.executor.kernel.InputGroup;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.StatementExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.executor.SQLExecutor;
import org.apache.shardingsphere.infra.metadata.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.refresh.MetaDataRefreshStrategyFactory;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.spi.RuleMetaDataNotifier;
import org.apache.shardingsphere.infra.rule.DataNodeRoutedRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.order.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract statement executor.
 */
@Getter
@RequiredArgsConstructor
public abstract class AbstractStatementExecutor {
    
    static {
        ShardingSphereServiceLoader.register(RuleMetaDataNotifier.class);
    }
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final SchemaContexts schemaContexts;
    
    private final SQLExecutor sqlExecutor;
    
    protected boolean isNeedAccumulate(final Collection<ShardingSphereRule> rules, final SQLStatementContext<?> sqlStatementContext) {
        return rules.stream().anyMatch(each -> ((DataNodeRoutedRule) each).isNeedAccumulate(sqlStatementContext.getTablesContext().getTableNames()));
    }
    
    protected int accumulate(final List<Integer> results) {
        int result = 0;
        for (Integer each : results) {
            result += null == each ? 0 : each;
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    protected void refreshTableMetaData(final SchemaContext schemaContext, final SQLStatementContext<?> sqlStatementContext) throws SQLException {
        if (null == sqlStatementContext) {
            return;
        }
        Optional<MetaDataRefreshStrategy> refreshStrategy = MetaDataRefreshStrategyFactory.newInstance(sqlStatementContext);
        if (refreshStrategy.isPresent()) {
            RuleSchemaMetaDataLoader metaDataLoader = new RuleSchemaMetaDataLoader(schemaContext.getSchema().getRules());
            refreshStrategy.get().refreshMetaData(schemaContext.getSchema().getMetaData(), schemaContexts.getDatabaseType(),
                    dataSourceMap, sqlStatementContext, tableName -> metaDataLoader.load(schemaContexts.getDatabaseType(),
                            dataSourceMap, tableName, schemaContexts.getProps()));
            notifyPersistRuleMetaData(DefaultSchema.LOGIC_NAME, schemaContext.getSchema().getMetaData().getRuleSchemaMetaData());
        }
    }
    
    private void notifyPersistRuleMetaData(final String schemaName, final RuleSchemaMetaData metaData) {
        OrderedSPIRegistry.getRegisteredServices(Collections.singletonList(metaData),
                RuleMetaDataNotifier.class).values().forEach(each -> each.notify(schemaName, metaData));
    }
    
    /**
     * Execute SQL.
     *
     * @param inputGroups input groups
     * @param sqlStatementContext SQL statement context
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public abstract boolean execute(Collection<InputGroup<StatementExecuteUnit>> inputGroups, SQLStatementContext<?> sqlStatementContext) throws SQLException;
    
    /**
     * Execute query.
     *
     * @param inputGroups input groups
     * @return result set list
     * @throws SQLException SQL exception
     */
    public abstract List<QueryResult> executeQuery(Collection<InputGroup<StatementExecuteUnit>> inputGroups) throws SQLException;
    
    /**
     * Execute update.
     *
     * @param inputGroups input groups
     * @param sqlStatementContext SQL statement context
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public abstract int executeUpdate(Collection<InputGroup<StatementExecuteUnit>> inputGroups, SQLStatementContext<?> sqlStatementContext) throws SQLException;
}
