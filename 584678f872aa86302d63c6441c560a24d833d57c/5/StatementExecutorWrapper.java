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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.wrapper;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shadow.rewrite.judgement.ShadowJudgementEngine;
import org.apache.shardingsphere.shadow.rewrite.judgement.impl.SimpleJudgementEngine;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.EncryptSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.MasterSlaveSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.ShadowSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.sql.parser.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.underlying.common.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContextBuilder;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.executor.context.SQLUnit;
import org.apache.shardingsphere.underlying.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.underlying.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.underlying.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.underlying.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.underlying.route.DataNodeRouter;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.apache.shardingsphere.underlying.route.context.RouteResult;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Executor wrapper for statement.
 */
@RequiredArgsConstructor
public final class StatementExecutorWrapper implements JDBCExecutorWrapper {
    
    private static final ShardingProxyContext SHARDING_PROXY_CONTEXT = ShardingProxyContext.getInstance();
    
    private final LogicSchema logicSchema;
    
    @Override
    public ExecutionContext route(final String sql) {
        if (logicSchema instanceof ShardingSchema) {
            return doShardingRoute(sql);
        }
        if (logicSchema instanceof MasterSlaveSchema) {
            return doMasterSlaveRoute(sql);
        }
        if (logicSchema instanceof EncryptSchema) {
            return doEncryptRoute(sql);
        }
        if (logicSchema instanceof ShadowSchema) {
            return doShadowRoute(sql);
        }
        return doTransparentRoute(sql);
    }
    
    private ExecutionContext doShardingRoute(final String sql) {
        Collection<BaseRule> rules = logicSchema.getShardingRule().toRules();
        SQLStatement sqlStatement = logicSchema.getSqlParserEngine().parse(sql, false);
        RouteContext routeContext = new DataNodeRouter(logicSchema.getMetaData(), SHARDING_PROXY_CONTEXT.getProperties(), rules).route(sqlStatement, sql, Collections.emptyList());
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(logicSchema.getMetaData().getSchema().getConfiguredSchemaMetaData(),
                SHARDING_PROXY_CONTEXT.getProperties(), rules).rewrite(sql, Collections.emptyList(), routeContext);
        return new ExecutionContext(routeContext.getSqlStatementContext(), ExecutionContextBuilder.build(logicSchema.getMetaData(), sqlRewriteResult));
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionContext doMasterSlaveRoute(final String sql) {
        Collection<BaseRule> rules = Collections.singletonList(((MasterSlaveSchema) logicSchema).getMasterSlaveRule());
        SQLStatement sqlStatement = logicSchema.getSqlParserEngine().parse(sql, false);
        RouteContext routeContext = new DataNodeRouter(logicSchema.getMetaData(), SHARDING_PROXY_CONTEXT.getProperties(), rules).route(sqlStatement, sql, Collections.emptyList());
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(logicSchema.getMetaData().getSchema().getConfiguredSchemaMetaData(),
                SHARDING_PROXY_CONTEXT.getProperties(), rules).rewrite(sql, Collections.emptyList(), routeContext);
        return new ExecutionContext(routeContext.getSqlStatementContext(), ExecutionContextBuilder.build(logicSchema.getMetaData(), sqlRewriteResult));
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionContext doEncryptRoute(final String sql) {
        Collection<BaseRule> rules = Collections.singletonList(((EncryptSchema) logicSchema).getEncryptRule());
        SQLStatement sqlStatement = logicSchema.getSqlParserEngine().parse(sql, false);
        RouteContext routeContext = new DataNodeRouter(logicSchema.getMetaData(), SHARDING_PROXY_CONTEXT.getProperties(), rules).route(sqlStatement, sql, Collections.emptyList());
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(logicSchema.getMetaData().getSchema().getConfiguredSchemaMetaData(),
                SHARDING_PROXY_CONTEXT.getProperties(), rules).rewrite(sql, Collections.emptyList(), routeContext);
        return new ExecutionContext(routeContext.getSqlStatementContext(), ExecutionContextBuilder.build(logicSchema.getMetaData(), sqlRewriteResult));
    }
    
    private ExecutionContext doShadowRoute(final String sql) {
        ShadowSchema shadowSchema = (ShadowSchema) logicSchema;
        SQLStatement sqlStatement = shadowSchema.getSqlParserEngine().parse(sql, true);
        RuleSchemaMetaData ruleSchemaMetaData = logicSchema.getMetaData().getSchema();
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(ruleSchemaMetaData.getConfiguredSchemaMetaData(), sql, new LinkedList<>(), sqlStatement);
        ShadowJudgementEngine shadowJudgementEngine = new SimpleJudgementEngine(shadowSchema.getShadowRule(), sqlStatementContext);
        String dataSourceName = shadowJudgementEngine.isShadowSQL()
                ? shadowSchema.getShadowRule().getRuleConfiguration().getShadowMappings().get(logicSchema.getDataSources().keySet().iterator().next())
                : logicSchema.getDataSources().keySet().iterator().next();
        SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(
                logicSchema.getMetaData().getSchema().getConfiguredSchemaMetaData(), ShardingProxyContext.getInstance().getProperties(), Collections.singletonList(shadowSchema.getShadowRule()));
        SQLRewriteUnit sqlRewriteResult = ((GenericSQLRewriteResult) sqlRewriteEntry.rewrite(sql, Collections.emptyList(), 
                new RouteContext(sqlStatementContext, Collections.emptyList(), new RouteResult()))).getSqlRewriteUnit();
        return new ExecutionContext(sqlStatementContext, new ExecutionUnit(dataSourceName, new SQLUnit(sqlRewriteResult.getSql(), sqlRewriteResult.getParameters())));
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionContext doTransparentRoute(final String sql) {
        SQLStatement sqlStatement = logicSchema.getSqlParserEngine().parse(sql, false);
        return new ExecutionContext(
                new CommonSQLStatementContext(sqlStatement), new ExecutionUnit(logicSchema.getDataSources().keySet().iterator().next(), new SQLUnit(sql, Collections.emptyList())));
    }
    
    @Override
    public Statement createStatement(final Connection connection, final SQLUnit sqlUnit, final boolean isReturnGeneratedKeys) throws SQLException {
        return connection.createStatement();
    }
    
    @Override
    public boolean executeSQL(final Statement statement, final String sql, final boolean isReturnGeneratedKeys) throws SQLException {
        return statement.execute(sql, isReturnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
    }
}
