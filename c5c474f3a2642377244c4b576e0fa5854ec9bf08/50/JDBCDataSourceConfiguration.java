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

package org.apache.shardingsphere.shardingscaling.core.config;

import org.apache.shardingsphere.underlying.common.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseType;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseTypes;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * JDBC data source configuration.
 */
@Getter
@Setter
@EqualsAndHashCode(exclude = {"databaseType"})
public final class JDBCDataSourceConfiguration implements DataSourceConfiguration {
    
    private String jdbcUrl;
    
    private String username;
    
    private String password;
    
    private DatabaseType databaseType;
    
    public JDBCDataSourceConfiguration(final String jdbcUrl, final String username, final String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        databaseType = DatabaseTypes.getDatabaseTypeByURL(jdbcUrl);
    }
    
    @Override
    public DataSourceMetaData getDataSourceMetaData() {
        return databaseType.getDataSourceMetaData(jdbcUrl, username);
    }
}
