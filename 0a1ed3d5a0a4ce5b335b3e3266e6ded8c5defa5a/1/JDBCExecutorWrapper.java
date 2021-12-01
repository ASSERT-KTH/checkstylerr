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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.wrapper;

import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.group.ExecuteGroupEngine;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.group.StatementOption;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC executor wrapper.
 */
public interface JDBCExecutorWrapper {
    
    /**
     * Execute SQL.
     * 
     * @param sql SQL to be routed
     * @return execution context
     * @throws SQLException SQL exception
     */
    ExecutionContext execute(String sql) throws SQLException;
    
    /**
     * Get execute group engine.
     * 
     * @param backendConnection backend connection
     * @param option statement option
     * @return execute group engine
     */
    ExecuteGroupEngine getExecuteGroupEngine(BackendConnection backendConnection, StatementOption option);
    
    /**
     * Execute SQL.
     * 
     * @param statement statement
     * @param sql SQL to be executed
     * @param isReturnGeneratedKeys is return generated keys
     * @return {@code true} is for query, {@code false} is for update
     * @throws SQLException SQL exception
     */
    boolean execute(Statement statement, String sql, boolean isReturnGeneratedKeys) throws SQLException;
}
