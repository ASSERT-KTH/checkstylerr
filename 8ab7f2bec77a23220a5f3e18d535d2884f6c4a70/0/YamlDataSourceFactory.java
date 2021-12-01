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

package org.apache.shardingsphere.example.proxy.hint.factory;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.example.proxy.hint.config.DatasourceConfig;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

public class YamlDataSourceFactory {
    
    public static DataSource createDataSource(File yamlFile) throws IOException {
        DatasourceConfig datasourceConfig = YamlEngine.unmarshal(yamlFile, DatasourceConfig.class);
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(datasourceConfig.getDriverClassName());
        dataSource.setJdbcUrl(datasourceConfig.getJdbcUrl());
        dataSource.setUsername(datasourceConfig.getUsername());
        dataSource.setPassword(datasourceConfig.getPassword());
        return dataSource;
    }
}
