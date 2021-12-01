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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.aware.ShardingRuleAware;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.impl.TableToken;
import org.apache.shardingsphere.sql.parser.relation.segment.table.Table;
import org.apache.shardingsphere.sql.parser.relation.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerAvailable;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.CollectionSQLTokenGenerator;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Table token generator.
 */
@Setter
public final class TableTokenGenerator implements CollectionSQLTokenGenerator, ShardingRuleAware {
    
    private ShardingRule shardingRule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return true;
    }
    
    @Override
    public Collection<TableToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<TableToken> result = new LinkedList<>();
        for (SQLSegment each : sqlStatementContext.getSqlStatement().getAllSQLSegments()) {
            if (each instanceof ProjectionsSegment) {
                result.addAll(generateSQLTokens(sqlStatementContext, (ProjectionsSegment) each));
            } else if (each instanceof ColumnSegment) {
                Optional<TableToken> tableToken = generateSQLToken(sqlStatementContext, (ColumnSegment) each);
                if (tableToken.isPresent()) {
                    result.add(tableToken.get());
                }
            } else if (each instanceof TableSegment) {
                Optional<TableToken> tableToken = generateSQLToken(sqlStatementContext.getSqlStatement(), (TableSegment) each);
                if (tableToken.isPresent()) {
                    result.add(tableToken.get());
                }
            } else if (each instanceof WhereSegment) {
                result.addAll(generateSQLTokens(sqlStatementContext, (WhereSegment) each));
            } else if (each instanceof GroupBySegment) {
                result.addAll(generateSQLTokens(sqlStatementContext, ((GroupBySegment) each).getGroupByItems()));
            } else if (each instanceof OrderBySegment) {
                result.addAll(generateSQLTokens(sqlStatementContext, ((OrderBySegment) each).getOrderByItems()));
            }
        }
        return result;
    }
    
    private Collection<TableToken> generateSQLTokens(final SQLStatementContext sqlStatementContext, final ProjectionsSegment projectionsSegment) {
        Collection<TableToken> result = new LinkedList<>();
        for (ProjectionSegment each : projectionsSegment.getProjections()) {
            if (each instanceof ShorthandProjectionSegment) {
                Optional<TableToken> tableToken = generateSQLToken(sqlStatementContext, (ShorthandProjectionSegment) each);
                if (tableToken.isPresent()) {
                    result.add(tableToken.get());
                }
            }
            if (each instanceof ColumnProjectionSegment) {
                Optional<TableToken> tableToken = generateSQLToken(sqlStatementContext, (ColumnProjectionSegment) each);
                if (tableToken.isPresent()) {
                    result.add(tableToken.get());
                }
            }
        }
        return result;
    }
    
    private Collection<TableToken> generateSQLTokens(final SQLStatementContext sqlStatementContext, final WhereSegment where) {
        Collection<TableToken> result = new LinkedList<>();
        for (AndPredicate each : where.getAndPredicates()) {
            for (PredicateSegment predicate : each.getPredicates()) {
                result.addAll(getTableTokens(sqlStatementContext, predicate));
            }
        }
        return result;
    }
    
    private Collection<TableToken> generateSQLTokens(final SQLStatementContext sqlStatementContext, final Collection<OrderByItemSegment> orderBys) {
        Collection<TableToken> result = new LinkedList<>();
        for (OrderByItemSegment each : orderBys) {
            if (isToGenerateTableToken(sqlStatementContext.getTablesContext(), each)) {
                TableSegment segment = ((ColumnOrderByItemSegment) each).getColumn().getOwner().get();
                result.add(new TableToken(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier()));
            }
        }
        return result;
    }
    
    private Optional<TableToken> generateSQLToken(final SQLStatementContext sqlStatementContext, final OwnerAvailable<TableSegment> segment) {
        Optional<TableSegment> owner = segment.getOwner();
        return owner.isPresent() && isToGenerateTableToken(sqlStatementContext, owner.get())
                ? Optional.of(new TableToken(owner.get().getStartIndex(), owner.get().getStopIndex(), owner.get().getIdentifier())) : Optional.<TableToken>absent();
    }
    
    private Optional<TableToken> generateSQLToken(final SQLStatement sqlStatement, final TableSegment segment) {
        return isToGenerateTableToken(sqlStatement, segment) ? Optional.of(new TableToken(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier())) : Optional.<TableToken>absent();
    }
    
    private Collection<TableToken> getTableTokens(final SQLStatementContext sqlStatementContext, final PredicateSegment predicate) {
        Collection<TableToken> result = new LinkedList<>();
        if (isToGenerateTableTokenForPredicate(sqlStatementContext.getTablesContext(), predicate)) {
            TableSegment segment = predicate.getColumn().getOwner().get();
            result.add(new TableToken(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier()));
        }
        if (isToGenerateTableTokenForColumn(sqlStatementContext.getTablesContext(), predicate)) {
            TableSegment segment = ((ColumnSegment) predicate.getRightValue()).getOwner().get();
            result.add(new TableToken(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier()));
        }
        if (isToGenerateTableTokenForProjection(sqlStatementContext.getTablesContext(), predicate)) {
            TableSegment segment = ((ColumnProjectionSegment) predicate.getRightValue()).getOwner().get();
            result.add(new TableToken(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier()));
        }
        return result;
    }
    
    private boolean isToGenerateTableTokenForPredicate(final TablesContext tablesContext, final PredicateSegment predicate) {
        return predicate.getColumn().getOwner().isPresent() && isTable(predicate.getColumn().getOwner().get(), tablesContext);
    }
    
    private boolean isToGenerateTableTokenForColumn(final TablesContext tablesContext, final PredicateSegment predicate) {
        return predicate.getRightValue() instanceof ColumnSegment && predicate.getColumn().getOwner().isPresent()
                && isTable(predicate.getColumn().getOwner().get(), tablesContext);
    }
    
    private boolean isToGenerateTableTokenForProjection(final TablesContext tablesContext, final PredicateSegment predicate) {
        return predicate.getRightValue() instanceof ColumnProjectionSegment && ((ColumnProjectionSegment) predicate.getRightValue()).getOwner().isPresent()
                && isTable(((ColumnProjectionSegment) predicate.getRightValue()).getOwner().get(), tablesContext);
    }
    
    private boolean isToGenerateTableToken(final TablesContext tablesContext, final OrderByItemSegment each) {
        return each instanceof ColumnOrderByItemSegment && ((ColumnOrderByItemSegment) each).getColumn().getOwner().isPresent() 
                && isTable(((ColumnOrderByItemSegment) each).getColumn().getOwner().get(), tablesContext);
    }
    
    private boolean isToGenerateTableToken(final SQLStatementContext sqlStatementContext, final TableSegment tableSegment) {
        Optional<Table> table = sqlStatementContext.getTablesContext().find(tableSegment.getIdentifier().getValue());
        return table.isPresent() && !table.get().getAlias().isPresent() && shardingRule.findTableRule(table.get().getName()).isPresent();
    }
    
    private boolean isToGenerateTableToken(final SQLStatement sqlStatement, final TableSegment segment) {
        return shardingRule.findTableRule(segment.getIdentifier().getValue()).isPresent() || !(sqlStatement instanceof SelectStatement);
    }
    
    private boolean isTable(final TableSegment owner, final TablesContext tablesContext) {
        return !tablesContext.findTableFromAlias(owner.getIdentifier().getValue()).isPresent();
    }
    
}
