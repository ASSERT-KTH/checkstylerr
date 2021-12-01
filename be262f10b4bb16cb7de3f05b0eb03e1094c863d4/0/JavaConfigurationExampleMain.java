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

package org.apache.shardingsphere.example.orchestration.raw.jdbc;

import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.ExampleExecuteTemplate;
import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.apache.shardingsphere.example.core.jdbc.service.OrderServiceImpl;
import org.apache.shardingsphere.example.orchestration.raw.jdbc.config.RegistryCenterConfigurationUtil;
import org.apache.shardingsphere.example.orchestration.raw.jdbc.config.cloud.CloudEncryptConfiguration;
import org.apache.shardingsphere.example.orchestration.raw.jdbc.config.cloud.CloudMasterSlaveConfiguration;
import org.apache.shardingsphere.example.orchestration.raw.jdbc.config.cloud.CloudShadowConfiguration;
import org.apache.shardingsphere.example.orchestration.raw.jdbc.config.cloud.CloudShardingDatabasesAndTablesConfiguration;
import org.apache.shardingsphere.example.orchestration.raw.jdbc.config.local.LocalEncryptConfiguration;
import org.apache.shardingsphere.example.orchestration.raw.jdbc.config.local.LocalMasterSlaveConfiguration;
import org.apache.shardingsphere.example.orchestration.raw.jdbc.config.local.LocalShadowConfiguration;
import org.apache.shardingsphere.example.orchestration.raw.jdbc.config.local.LocalShardingDatabasesAndTablesConfiguration;
import org.apache.shardingsphere.example.type.RegistryCenterType;
import org.apache.shardingsphere.example.type.ShardingType;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingSphereDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/*
 * 1. Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.
 * 2. Please make sure sharding-orchestration-center-zookeeper-curator in your pom if registryCenterType = RegistryCenterType.ZOOKEEPER.
 * 3. Please make sure sharding-orchestration-center-nacos in your pom if registryCenterType = RegistryCenterType.NACOS.
 */
public class JavaConfigurationExampleMain {
    
    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES_AND_TABLES;
//    private static ShardingType shardingType = ShardingType.MASTER_SLAVE;
//    private static ShardingType shardingType = ShardingType.ENCRYPT;
//    private static ShardingType shardingType = ShardingType.SHADOW;
    
    private static boolean loadConfigFromRegCenter = false;
//    private static boolean loadConfigFromRegCenter = true;
    
    private static RegistryCenterType registryCenterType = RegistryCenterType.ZOOKEEPER;
//    private static RegistryCenterType registryCenterType = RegistryCenterType.NACOS;
    
    public static void main(final String[] args) throws Exception {
        DataSource dataSource = getDataSource(shardingType, loadConfigFromRegCenter);
        try {
            ExampleExecuteTemplate.run(getExampleService(dataSource));
        } finally {
            closeDataSource(dataSource);
        }
    }
    
    private static DataSource getDataSource(final ShardingType shardingType, final boolean loadConfigFromRegCenter) throws SQLException {
        Map<String, CenterConfiguration> centerConfigurationMap = getRegistryCenterConfiguration(registryCenterType, shardingType);
        ExampleConfiguration configuration;
        switch (shardingType) {
            case SHARDING_DATABASES_AND_TABLES:
                configuration = loadConfigFromRegCenter
                    ? new CloudShardingDatabasesAndTablesConfiguration(centerConfigurationMap) : new LocalShardingDatabasesAndTablesConfiguration(centerConfigurationMap);
                break;
            case MASTER_SLAVE:
                configuration = loadConfigFromRegCenter ? new CloudMasterSlaveConfiguration(centerConfigurationMap) : new LocalMasterSlaveConfiguration(centerConfigurationMap);
                break;
            case ENCRYPT:
                configuration = loadConfigFromRegCenter ? new CloudEncryptConfiguration(centerConfigurationMap) : new LocalEncryptConfiguration(centerConfigurationMap);
                break;
            case SHADOW:
                configuration = loadConfigFromRegCenter ? new CloudShadowConfiguration(centerConfigurationMap) : new LocalShadowConfiguration(centerConfigurationMap);
                break;
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
        return configuration.getDataSource();
    }
    
    private static Map<String, CenterConfiguration> getRegistryCenterConfiguration(final RegistryCenterType registryCenterType, final ShardingType shardingType) {
        return RegistryCenterType.ZOOKEEPER == registryCenterType ? RegistryCenterConfigurationUtil.getZooKeeperConfiguration(String.valueOf(!loadConfigFromRegCenter), shardingType)
                : RegistryCenterConfigurationUtil.getNacosConfiguration(String.valueOf(!loadConfigFromRegCenter), shardingType);
    }
    
    private static ExampleService getExampleService(final DataSource dataSource) {
        return new OrderServiceImpl(dataSource);
    }
    
    private static void closeDataSource(final DataSource dataSource) throws Exception {
        if (dataSource instanceof ShardingSphereDataSource) {
            ((ShardingSphereDataSource) dataSource).close();
        }
    }
}
