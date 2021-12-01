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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.text;

import lombok.Getter;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandlerFactory;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.postgresql.PostgreSQLErrPacketFactory;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Command query executor for PostgreSQL.
 */
public final class PostgreSQLComQueryExecutor implements QueryCommandExecutor {
    
    private final TextProtocolBackendHandler textProtocolBackendHandler;
    
    @Getter
    private volatile ResponseType responseType;
    
    public PostgreSQLComQueryExecutor(final PostgreSQLComQueryPacket comQueryPacket, final BackendConnection backendConnection) {
        textProtocolBackendHandler = TextProtocolBackendHandlerFactory.newInstance(DatabaseTypes.getActualDatabaseType("PostgreSQL"), comQueryPacket.getSql(), backendConnection);
    }
    
    @Override
    public Collection<DatabasePacket<?>> execute() {
        if (ProxyContext.getInstance().getSchemaContexts().isCircuitBreak()) {
            return Collections.singletonList(new PostgreSQLErrorResponsePacket());
        }
        BackendResponse backendResponse = getBackendResponse();
        if (backendResponse instanceof QueryResponse) {
            Optional<PostgreSQLRowDescriptionPacket> result = createQueryPacket((QueryResponse) backendResponse);
            return result.<List<DatabasePacket<?>>>map(Collections::singletonList).orElseGet(Collections::emptyList);
        }
        if (backendResponse instanceof UpdateResponse) {
            responseType = ResponseType.UPDATE;
            return Collections.singletonList(createUpdatePacket((UpdateResponse) backendResponse));
        }
        responseType = ResponseType.ERROR;
        return Collections.singletonList(createErrorPacket((ErrorResponse) backendResponse));
    }
    
    private BackendResponse getBackendResponse() {
        BackendResponse result;
        try {
            result = textProtocolBackendHandler.execute();
        // CHECKSTYLE:OFF
        } catch (final Exception ex) {
        // CHECKSTYLE:OFF
            result = new ErrorResponse(ex);
        }
        return result;
    }
    
    private Optional<PostgreSQLRowDescriptionPacket> createQueryPacket(final QueryResponse queryResponse) {
        List<PostgreSQLColumnDescription> columnDescriptions = getPostgreSQLColumnDescriptions(queryResponse);
        if (columnDescriptions.isEmpty()) {
            responseType = ResponseType.QUERY;
        }
        if (columnDescriptions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new PostgreSQLRowDescriptionPacket(columnDescriptions.size(), columnDescriptions));
    }
    
    private List<PostgreSQLColumnDescription> getPostgreSQLColumnDescriptions(final QueryResponse queryResponse) {
        List<PostgreSQLColumnDescription> result = new LinkedList<>();
        List<QueryResult> queryResults = queryResponse.getQueryResults();
        ResultSetMetaData resultSetMetaData = queryResults.isEmpty() ? null : queryResults.get(0).getResultSetMetaData();
        int columnIndex = 0;
        for (QueryHeader each : queryResponse.getQueryHeaders()) {
            result.add(new PostgreSQLColumnDescription(each.getColumnName(), ++columnIndex, each.getColumnType(), each.getColumnLength(), resultSetMetaData));
        }
        return result;
    }
    
    private PostgreSQLCommandCompletePacket createUpdatePacket(final UpdateResponse updateResponse) {
        return new PostgreSQLCommandCompletePacket(updateResponse.getType(), updateResponse.getUpdateCount());
    }
    
    private PostgreSQLErrorResponsePacket createErrorPacket(final ErrorResponse errorResponse) {
        return PostgreSQLErrPacketFactory.newInstance(errorResponse.getCause());
    }
    
    @Override
    public boolean next() throws SQLException {
        return textProtocolBackendHandler.next();
    }
    
    @Override
    public PostgreSQLPacket getQueryData() throws SQLException {
        return new PostgreSQLDataRowPacket(textProtocolBackendHandler.getQueryData().getData());
    }
}
