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
import org.apache.shardingsphere.shadow.rewrite.context.ShadowSQLRewriteContextDecorator;
import org.apache.shardingsphere.shadow.rewrite.judgement.ShadowJudgementEngine;
import org.apache.shardingsphere.shadow.rewrite.judgement.impl.PreparedJudgementEngine;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.EncryptSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.MasterSlaveSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.ShadowSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.sql.parser.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.executor.context.SQLUnit;
import org.apache.shardingsphere.underlying.pluggble.PreparedQueryPrepareEngine;
import org.apache.shardingsphere.underlying.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.underlying.rewrite.engine.SQLRewriteEngine;
import org.apache.shardingsphere.underlying.rewrite.engine.SQLRewriteResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/**
 * Executor wrapper for prepared statement.
 */
@RequiredArgsConstructor
public final class PreparedStatementExecutorWrapper implements JDBCExecutorWrapper {
    
    private static final ShardingProxyContext SHARDING_PROXY_CONTEXT = ShardingProxyContext.getInstance();
    
    private final LogicSchema logicSchema;
    
    private final List<Object> parameters;
    
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
        PreparedQueryPrepareEngine prepareEngine = new PreparedQueryPrepareEngine(
                logicSchema.getShardingRule().toRules(), ShardingProxyContext.getInstance().getProperties(), logicSchema.getMetaData(), logicSchema.getSqlParserEngine());
        return prepareEngine.prepare(sql, parameters);
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionContext doMasterSlaveRoute(final String sql) {
        PreparedQueryPrepareEngine prepareEngine = new PreparedQueryPrepareEngine(Collections.singletonList(((MasterSlaveSchema) logicSchema).getMasterSlaveRule()), 
                SHARDING_PROXY_CONTEXT.getProperties(), logicSchema.getMetaData(), logicSchema.getSqlParserEngine());
        return prepareEngine.prepare(sql, parameters);
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionContext doEncryptRoute(final String sql) {
        PreparedQueryPrepareEngine prepareEngine = new PreparedQueryPrepareEngine(Collections.singletonList(((EncryptSchema) logicSchema).getEncryptRule()),
                SHARDING_PROXY_CONTEXT.getProperties(), logicSchema.getMetaData(), logicSchema.getSqlParserEngine());
        return prepareEngine.prepare(sql, parameters);
    }
    
    private ExecutionContext doShadowRoute(final String sql) {
        ShadowSchema shadowSchema = (ShadowSchema) logicSchema;
        SQLStatement sqlStatement = shadowSchema.getSqlParserEngine().parse(sql, true);
        SchemaMetaData schemaMetaData = logicSchema.getMetaData().getSchema();
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(schemaMetaData, sql, parameters, sqlStatement);
        ShadowJudgementEngine shadowJudgementEngine = new PreparedJudgementEngine(shadowSchema.getShadowRule(), sqlStatementContext, parameters);
        SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(logicSchema.getMetaData().getSchema(), ShardingProxyContext.getInstance().getProperties());
        sqlRewriteEntry.registerDecorator(shadowSchema.getShadowRule(), new ShadowSQLRewriteContextDecorator());
        SQLRewriteContext sqlRewriteContext = sqlRewriteEntry.createSQLRewriteContext(sql, parameters, sqlStatementContext, null);
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEngine().rewrite(sqlRewriteContext);
        ExecutionContext result = new ExecutionContext(sqlStatementContext);
        String dataSourceName = shadowJudgementEngine.isShadowSQL()
                ? shadowSchema.getShadowRule().getRuleConfiguration().getShadowMappings().get(logicSchema.getDataSources().keySet().iterator().next())
                : logicSchema.getDataSources().keySet().iterator().next();
        result.getExecutionUnits().add(
                new ExecutionUnit(dataSourceName, new SQLUnit(sqlRewriteResult.getSql(), sqlRewriteResult.getParameters())));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionContext doTransparentRoute(final String sql) {
        SQLStatement sqlStatement = logicSchema.getSqlParserEngine().parse(sql, false);
        ExecutionContext result = new ExecutionContext(new CommonSQLStatementContext(sqlStatement));
        result.getExecutionUnits().add(new ExecutionUnit(logicSchema.getDataSources().keySet().iterator().next(), new SQLUnit(sql, Collections.emptyList())));
        return result;
    }
    
    @Override
    public Statement createStatement(final Connection connection, final SQLUnit sqlUnit, final boolean isReturnGeneratedKeys) throws SQLException {
        PreparedStatement result = isReturnGeneratedKeys ? connection.prepareStatement(sqlUnit.getSql(), Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(sqlUnit.getSql());
        for (int i = 0; i < sqlUnit.getParameters().size(); i++) {
            result.setObject(i + 1, sqlUnit.getParameters().get(i));
        }
        return result;
    }
    
    @Override
    public boolean executeSQL(final Statement statement, final String sql, final boolean isReturnGeneratedKeys) throws SQLException {
        return ((PreparedStatement) statement).execute();
    }
}
