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

package org.apache.shardingsphere.underlying.merge.result.impl.memory;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.executor.sql.QueryResult;
import org.apache.shardingsphere.underlying.merge.result.MergedResult;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Memory merged result.
 *
 * @param <T> type of rule
 */
@RequiredArgsConstructor
public abstract class MemoryMergedResult<T extends BaseRule> implements MergedResult {
    
    private final Iterator<MemoryQueryResultRow> memoryResultSetRows;
    
    private MemoryQueryResultRow currentResultSetRow;
    
    private boolean wasNull;
    
    protected MemoryMergedResult(final T rule, final SchemaMetaData schemaMetaData, final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        List<MemoryQueryResultRow> memoryQueryResultRowList = init(rule, schemaMetaData, sqlStatementContext, queryResults);
        memoryResultSetRows = memoryQueryResultRowList.iterator();
        if (!memoryQueryResultRowList.isEmpty()) {
            currentResultSetRow = memoryQueryResultRowList.get(0);
        }
    }
    
    protected abstract List<MemoryQueryResultRow> init(T rule, SchemaMetaData schemaMetaData, SQLStatementContext sqlStatementContext, List<QueryResult> queryResults) throws SQLException;
    
    @Override
    public final boolean next() {
        if (memoryResultSetRows.hasNext()) {
            currentResultSetRow = memoryResultSetRows.next();
            return true;
        }
        return false;
    }
    
    @Override
    public final Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        if (Blob.class == type || Clob.class == type || Reader.class == type || InputStream.class == type || SQLXML.class == type) {
            throw new SQLFeatureNotSupportedException();
        }
        Object result = currentResultSetRow.getCell(columnIndex);
        wasNull = null == result;
        return result;
    }
    
    @Override
    public final Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
        // TODO implement with calendar
        Object result = currentResultSetRow.getCell(columnIndex);
        wasNull = null == result;
        return result;
    }
    
    @Override
    public final InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final boolean wasNull() {
        return wasNull;
    }
}
