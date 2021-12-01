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

package org.apache.shardingsphere.sharding.route.engine;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.rule.BindingTableRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.core.strategy.route.hint.HintShardingStrategy;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.InsertClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.context.ShardingRouteContext;
import org.apache.shardingsphere.sharding.route.engine.keygen.GeneratedKey;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngineFactory;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidatorFactory;
import org.apache.shardingsphere.sql.parser.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.sql.parser.binder.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.route.DateNodeRouter;
import org.apache.shardingsphere.underlying.route.context.RouteResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Sharding router.
 */
@RequiredArgsConstructor
public final class ShardingRouter implements DateNodeRouter {
    
    private final ShardingRule shardingRule;

    private final ShardingSphereProperties properties;
    
    private final ShardingSphereMetaData metaData;
    
    private final SQLParserEngine sqlParserEngine;
    
    @Override
    @SuppressWarnings("unchecked")
    public ShardingRouteContext route(final String sql, final List<Object> parameters, final boolean useCache) {
        SQLStatement sqlStatement = parse(sql, useCache);
        ShardingStatementValidatorFactory.newInstance(sqlStatement).ifPresent(shardingStatementValidator -> shardingStatementValidator.validate(shardingRule, sqlStatement, parameters));
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(metaData.getRelationMetas(), sql, parameters, sqlStatement);
        Optional<GeneratedKey> generatedKey = sqlStatement instanceof InsertStatement
                ? GeneratedKey.getGenerateKey(shardingRule, metaData.getTables(), parameters, (InsertStatement) sqlStatement) : Optional.empty();
        ShardingConditions shardingConditions = getShardingConditions(parameters, sqlStatementContext, generatedKey.orElse(null), metaData.getRelationMetas());
        boolean needMergeShardingValues = isNeedMergeShardingValues(sqlStatementContext);
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement && needMergeShardingValues) {
            checkSubqueryShardingValues(sqlStatementContext, shardingConditions);
            mergeShardingConditions(shardingConditions);
        }
        ShardingRouteEngine shardingRouteEngine = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, properties);
        RouteResult routeResult = shardingRouteEngine.route(shardingRule);
        if (needMergeShardingValues) {
            Preconditions.checkState(1 == routeResult.getRouteUnits().size(), "Must have one sharding with subquery.");
        }
        return new ShardingRouteContext(sqlStatementContext, routeResult, shardingConditions, generatedKey.orElse(null));
    }
    
    /*
     * To make sure SkyWalking will be available at the next release of ShardingSphere,
     * a new plugin should be provided to SkyWalking project if this API changed.
     *
     * @see <a href="https://github.com/apache/skywalking/blob/master/docs/en/guides/Java-Plugin-Development-Guide.md#user-content-plugin-development-guide">Plugin Development Guide</a>
     *
     */
    private SQLStatement parse(final String sql, final boolean useCache) {
        return sqlParserEngine.parse(sql, useCache);
    }
    
    private ShardingConditions getShardingConditions(final List<Object> parameters, final SQLStatementContext sqlStatementContext, final GeneratedKey generatedKey, final RelationMetas relationMetas) {
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement) {
            if (sqlStatementContext instanceof InsertStatementContext) {
                InsertStatementContext shardingInsertStatement = (InsertStatementContext) sqlStatementContext;
                return new ShardingConditions(new InsertClauseShardingConditionEngine(shardingRule).createShardingConditions(shardingInsertStatement, generatedKey, parameters));
            }
            return new ShardingConditions(new WhereClauseShardingConditionEngine(shardingRule, relationMetas).createShardingConditions(sqlStatementContext, parameters));
        }
        return new ShardingConditions(Collections.emptyList());
    }
    
    private boolean isNeedMergeShardingValues(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isContainsSubquery() 
                && !shardingRule.getShardingLogicTableNames(sqlStatementContext.getTablesContext().getTableNames()).isEmpty();
    }
    
    private void checkSubqueryShardingValues(final SQLStatementContext sqlStatementContext, final ShardingConditions shardingConditions) {
        for (String each : sqlStatementContext.getTablesContext().getTableNames()) {
            Optional<TableRule> tableRule = shardingRule.findTableRule(each);
            if (tableRule.isPresent() && isRoutingByHint(tableRule.get()) && !HintManager.getDatabaseShardingValues(each).isEmpty() && !HintManager.getTableShardingValues(each).isEmpty()) {
                return;
            }
        }
        Preconditions.checkState(!shardingConditions.getConditions().isEmpty(), "Must have sharding column with subquery.");
        if (shardingConditions.getConditions().size() > 1) {
            Preconditions.checkState(isSameShardingCondition(shardingConditions), "Sharding value must same with subquery.");
        }
    }
    
    private boolean isRoutingByHint(final TableRule tableRule) {
        return shardingRule.getDatabaseShardingStrategy(tableRule) instanceof HintShardingStrategy && shardingRule.getTableShardingStrategy(tableRule) instanceof HintShardingStrategy;
    }
    
    private boolean isSameShardingCondition(final ShardingConditions shardingConditions) {
        ShardingCondition example = shardingConditions.getConditions().remove(shardingConditions.getConditions().size() - 1);
        for (ShardingCondition each : shardingConditions.getConditions()) {
            if (!isSameShardingCondition(example, each)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameShardingCondition(final ShardingCondition shardingCondition1, final ShardingCondition shardingCondition2) {
        if (shardingCondition1.getRouteValues().size() != shardingCondition2.getRouteValues().size()) {
            return false;
        }
        for (int i = 0; i < shardingCondition1.getRouteValues().size(); i++) {
            RouteValue shardingValue1 = shardingCondition1.getRouteValues().get(i);
            RouteValue shardingValue2 = shardingCondition2.getRouteValues().get(i);
            if (!isSameRouteValue((ListRouteValue) shardingValue1, (ListRouteValue) shardingValue2)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameRouteValue(final ListRouteValue routeValue1, final ListRouteValue routeValue2) {
        return isSameLogicTable(routeValue1, routeValue2)
                && routeValue1.getColumnName().equals(routeValue2.getColumnName()) && routeValue1.getValues().equals(routeValue2.getValues());
    }
    
    private boolean isSameLogicTable(final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
        return shardingValue1.getTableName().equals(shardingValue2.getTableName()) || isBindingTable(shardingValue1, shardingValue2);
    }
    
    private boolean isBindingTable(final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
        Optional<BindingTableRule> bindingRule = shardingRule.findBindingTableRule(shardingValue1.getTableName());
        return bindingRule.isPresent() && bindingRule.get().hasLogicTable(shardingValue2.getTableName());
    }
    
    private void mergeShardingConditions(final ShardingConditions shardingConditions) {
        if (shardingConditions.getConditions().size() > 1) {
            ShardingCondition shardingCondition = shardingConditions.getConditions().remove(shardingConditions.getConditions().size() - 1);
            shardingConditions.getConditions().clear();
            shardingConditions.getConditions().add(shardingCondition);
        }
    }
}
