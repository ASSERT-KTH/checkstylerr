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
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.decorator.RouteDecorator;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.InsertClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngineFactory;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidatorFactory;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.strategy.route.hint.HintShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.sharding.strategy.route.value.RouteValue;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DMLStatement;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Sharding route decorator.
 */
public final class ShardingRouteDecorator implements RouteDecorator<ShardingRule> {
    
    @SuppressWarnings("unchecked")
    @Override
    public RouteContext decorate(final RouteContext routeContext, final ShardingSphereMetaData metaData, final ShardingRule shardingRule, final ConfigurationProperties properties) {
        SQLStatementContext sqlStatementContext = routeContext.getSqlStatementContext();
        List<Object> parameters = routeContext.getParameters();
        ShardingStatementValidatorFactory.newInstance(
                sqlStatementContext.getSqlStatement()).ifPresent(validator -> validator.validate(shardingRule, sqlStatementContext.getSqlStatement(), parameters));
        ShardingConditions shardingConditions = getShardingConditions(parameters, sqlStatementContext, metaData.getSchema().getConfiguredSchemaMetaData(), shardingRule);
        boolean needMergeShardingValues = isNeedMergeShardingValues(sqlStatementContext, shardingRule);
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement && needMergeShardingValues) {
            checkSubqueryShardingValues(sqlStatementContext, shardingRule, shardingConditions);
            mergeShardingConditions(shardingConditions);
        }
        ShardingRouteEngine shardingRouteEngine = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, properties);
        RouteResult routeResult = shardingRouteEngine.route(shardingRule);
        if (needMergeShardingValues) {
            Preconditions.checkState(1 == routeResult.getRouteUnits().size(), "Must have one sharding with subquery.");
        }
        return new RouteContext(sqlStatementContext, parameters, routeResult);
    }
    
    private ShardingConditions getShardingConditions(final List<Object> parameters, 
                                                     final SQLStatementContext sqlStatementContext, final SchemaMetaData schemaMetaData, final ShardingRule shardingRule) {
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement) {
            if (sqlStatementContext instanceof InsertStatementContext) {
                return new ShardingConditions(new InsertClauseShardingConditionEngine(shardingRule).createShardingConditions((InsertStatementContext) sqlStatementContext, parameters));
            }
            return new ShardingConditions(new WhereClauseShardingConditionEngine(shardingRule, schemaMetaData).createShardingConditions(sqlStatementContext, parameters));
        }
        return new ShardingConditions(Collections.emptyList());
    }
    
    private boolean isNeedMergeShardingValues(final SQLStatementContext sqlStatementContext, final ShardingRule shardingRule) {
        return sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isContainsSubquery() 
                && !shardingRule.getShardingLogicTableNames(sqlStatementContext.getTablesContext().getTableNames()).isEmpty();
    }
    
    private void checkSubqueryShardingValues(final SQLStatementContext sqlStatementContext, final ShardingRule shardingRule, final ShardingConditions shardingConditions) {
        for (String each : sqlStatementContext.getTablesContext().getTableNames()) {
            Optional<TableRule> tableRule = shardingRule.findTableRule(each);
            if (tableRule.isPresent() && isRoutingByHint(shardingRule, tableRule.get())
                    && !HintManager.getDatabaseShardingValues(each).isEmpty() && !HintManager.getTableShardingValues(each).isEmpty()) {
                return;
            }
        }
        Preconditions.checkState(!shardingConditions.getConditions().isEmpty(), "Must have sharding column with subquery.");
        if (shardingConditions.getConditions().size() > 1) {
            Preconditions.checkState(isSameShardingCondition(shardingRule, shardingConditions), "Sharding value must same with subquery.");
        }
    }
    
    private boolean isRoutingByHint(final ShardingRule shardingRule, final TableRule tableRule) {
        return shardingRule.getDatabaseShardingStrategy(tableRule) instanceof HintShardingStrategy && shardingRule.getTableShardingStrategy(tableRule) instanceof HintShardingStrategy;
    }
    
    private boolean isSameShardingCondition(final ShardingRule shardingRule, final ShardingConditions shardingConditions) {
        ShardingCondition example = shardingConditions.getConditions().remove(shardingConditions.getConditions().size() - 1);
        for (ShardingCondition each : shardingConditions.getConditions()) {
            if (!isSameShardingCondition(shardingRule, example, each)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameShardingCondition(final ShardingRule shardingRule, final ShardingCondition shardingCondition1, final ShardingCondition shardingCondition2) {
        if (shardingCondition1.getRouteValues().size() != shardingCondition2.getRouteValues().size()) {
            return false;
        }
        for (int i = 0; i < shardingCondition1.getRouteValues().size(); i++) {
            RouteValue shardingValue1 = shardingCondition1.getRouteValues().get(i);
            RouteValue shardingValue2 = shardingCondition2.getRouteValues().get(i);
            if (!isSameRouteValue(shardingRule, (ListRouteValue) shardingValue1, (ListRouteValue) shardingValue2)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameRouteValue(final ShardingRule shardingRule, final ListRouteValue routeValue1, final ListRouteValue routeValue2) {
        return isSameLogicTable(shardingRule, routeValue1, routeValue2) && routeValue1.getColumnName().equals(routeValue2.getColumnName()) && routeValue1.getValues().equals(routeValue2.getValues());
    }
    
    private boolean isSameLogicTable(final ShardingRule shardingRule, final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
        return shardingValue1.getTableName().equals(shardingValue2.getTableName()) || isBindingTable(shardingRule, shardingValue1, shardingValue2);
    }
    
    private boolean isBindingTable(final ShardingRule shardingRule, final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
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
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRule> getTypeClass() {
        return ShardingRule.class;
    }
}
