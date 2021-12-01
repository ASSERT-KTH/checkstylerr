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

package org.apache.shardingsphere.sharding.route.engine.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingDataSourceGroupBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingDatabaseBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingInstanceBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingTableBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.complex.ShardingComplexRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.ignore.ShardingIgnoreRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.unconfigured.ShardingUnconfiguredTablesRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.unicast.ShardingUnicastRoutingEngine;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.UseStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.postgresql.ResetParameterStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.TCLStatement;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

import java.util.Collection;
import java.util.Map;

/**
 * Sharding routing engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingRouteEngineFactory {
    
    /**
     * Create new instance of routing engine.
     * 
     * @param shardingRule sharding rule
     * @param metaData meta data of ShardingSphere
     * @param sqlStatementContext SQL statement context
     * @param shardingConditions shardingConditions
     * @param properties sharding sphere properties
     * @return new instance of routing engine
     */
    public static ShardingRouteEngine newInstance(final ShardingRule shardingRule, final ShardingSphereMetaData metaData, 
                                                  final SQLStatementContext sqlStatementContext, final ShardingConditions shardingConditions, final ConfigurationProperties properties) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        if (sqlStatement instanceof TCLStatement) {
            return new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (sqlStatement instanceof DDLStatement) {
            return new ShardingTableBroadcastRoutingEngine(metaData.getSchema().getConfiguredSchemaMetaData(), sqlStatementContext);
        }
        if (sqlStatement instanceof DALStatement) {
            return getDALRoutingEngine(shardingRule, metaData.getSchema().getUnconfiguredSchemaMetaDataMap(), sqlStatement, tableNames);
        }
        if (sqlStatement instanceof DCLStatement) {
            return getDCLRoutingEngine(sqlStatementContext, metaData);
        }
        if (shardingRule.isAllBroadcastTables(tableNames)) {
            return sqlStatement instanceof SelectStatement ? new ShardingUnicastRoutingEngine(tableNames) : new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement && shardingConditions.isAlwaysFalse() || tableNames.isEmpty()) {
            return new ShardingUnicastRoutingEngine(tableNames);
        }
        if (!shardingRule.tableRuleExists(tableNames)) {
            return new ShardingUnconfiguredTablesRoutingEngine(tableNames, metaData.getSchema().getUnconfiguredSchemaMetaDataMap());
        }
        return getShardingRoutingEngine(shardingRule, sqlStatementContext, shardingConditions, tableNames, properties);
    }
    
    private static ShardingRouteEngine getDALRoutingEngine(final ShardingRule shardingRule, 
                                                           final Map<String, SchemaMetaData> unconfiguredSchemaMetaDataMap, final SQLStatement sqlStatement, final Collection<String> tableNames) {
        if (sqlStatement instanceof UseStatement) {
            return new ShardingIgnoreRoutingEngine();
        }
        if (sqlStatement instanceof SetStatement || sqlStatement instanceof ResetParameterStatement || sqlStatement instanceof ShowDatabasesStatement) {
            return new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (!tableNames.isEmpty() && !shardingRule.tableRuleExists(tableNames)) {
            return new ShardingUnconfiguredTablesRoutingEngine(tableNames, unconfiguredSchemaMetaDataMap);
        }
        if (!tableNames.isEmpty()) {
            return new ShardingUnicastRoutingEngine(tableNames);
        }
        return new ShardingDataSourceGroupBroadcastRoutingEngine();
    }
    
    private static ShardingRouteEngine getDCLRoutingEngine(final SQLStatementContext sqlStatementContext, final ShardingSphereMetaData metaData) {
        return isDCLForSingleTable(sqlStatementContext) 
                ? new ShardingTableBroadcastRoutingEngine(metaData.getSchema().getConfiguredSchemaMetaData(), sqlStatementContext)
                : new ShardingInstanceBroadcastRoutingEngine(metaData.getDataSources());
    }
    
    private static boolean isDCLForSingleTable(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof TableAvailable) {
            TableAvailable tableSegmentsAvailable = (TableAvailable) sqlStatementContext;
            return 1 == tableSegmentsAvailable.getAllTables().size() && !"*".equals(tableSegmentsAvailable.getAllTables().iterator().next().getTableName().getIdentifier().getValue());
        }
        return false;
    }
    
    private static ShardingRouteEngine getShardingRoutingEngine(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext,
                                                                final ShardingConditions shardingConditions, final Collection<String> tableNames, final ConfigurationProperties properties) {
        Collection<String> shardingTableNames = shardingRule.getShardingLogicTableNames(tableNames);
        if (1 == shardingTableNames.size() || shardingRule.isAllBindingTables(shardingTableNames)) {
            return new ShardingStandardRoutingEngine(shardingTableNames.iterator().next(), sqlStatementContext, shardingConditions, properties);
        }
        // TODO config for cartesian set
        return new ShardingComplexRoutingEngine(tableNames, sqlStatementContext, shardingConditions, properties);
    }
}
