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
import org.apache.shardingsphere.shadow.rewrite.judgement.impl.SimpleJudgementEngine;
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
import org.apache.shardingsphere.underlying.pluggble.prepare.SimpleQueryPrepareEngine;
import org.apache.shardingsphere.underlying.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.underlying.rewrite.engine.SQLRewriteEngine;
import org.apache.shardingsphere.underlying.rewrite.engine.SQLRewriteResult;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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
        SimpleQueryPrepareEngine prepareEngine = new SimpleQueryPrepareEngine(
                logicSchema.getShardingRule().toRules(), ShardingProxyContext.getInstance().getProperties(), logicSchema.getMetaData(), logicSchema.getSqlParserEngine());
        return prepareEngine.prepare(sql, Collections.emptyList());
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionContext doMasterSlaveRoute(final String sql) {
        SimpleQueryPrepareEngine prepareEngine = new SimpleQueryPrepareEngine(Collections.singletonList(((MasterSlaveSchema) logicSchema).getMasterSlaveRule()),
                SHARDING_PROXY_CONTEXT.getProperties(), logicSchema.getMetaData(), logicSchema.getSqlParserEngine());
        return prepareEngine.prepare(sql, Collections.emptyList());
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionContext doEncryptRoute(final String sql) {
        SimpleQueryPrepareEngine prepareEngine = new SimpleQueryPrepareEngine(Collections.singletonList(((EncryptSchema) logicSchema).getEncryptRule()), 
                SHARDING_PROXY_CONTEXT.getProperties(), logicSchema.getMetaData(), logicSchema.getSqlParserEngine());
        return prepareEngine.prepare(sql, Collections.emptyList());
    }
    
    private ExecutionContext doShadowRoute(final String sql) {
        ShadowSchema shadowSchema = (ShadowSchema) logicSchema;
        SQLStatement sqlStatement = shadowSchema.getSqlParserEngine().parse(sql, true);
        SchemaMetaData schemaMetaData = logicSchema.getMetaData().getSchema();
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(schemaMetaData, sql, new LinkedList<>(), sqlStatement);
        ShadowJudgementEngine shadowJudgementEngine = new SimpleJudgementEngine(shadowSchema.getShadowRule(), sqlStatementContext);
        String dataSourceName = shadowJudgementEngine.isShadowSQL()
                ? shadowSchema.getShadowRule().getRuleConfiguration().getShadowMappings().get(logicSchema.getDataSources().keySet().iterator().next())
                : logicSchema.getDataSources().keySet().iterator().next();
        SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(logicSchema.getMetaData().getSchema(), ShardingProxyContext.getInstance().getProperties());
        sqlRewriteEntry.registerDecorator(shadowSchema.getShadowRule(), new ShadowSQLRewriteContextDecorator());
        SQLRewriteContext sqlRewriteContext = sqlRewriteEntry.createSQLRewriteContext(sql, Collections.emptyList(), sqlStatementContext, null);
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEngine().rewrite(sqlRewriteContext);
        ExecutionContext result = new ExecutionContext(sqlStatementContext);
        result.getExecutionUnits().add(new ExecutionUnit(dataSourceName, new SQLUnit(sqlRewriteResult.getSql(), sqlRewriteResult.getParameters())));
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
        return connection.createStatement();
    }
    
    @Override
    public boolean executeSQL(final Statement statement, final String sql, final boolean isReturnGeneratedKeys) throws SQLException {
        return statement.execute(sql, isReturnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
    }
}
