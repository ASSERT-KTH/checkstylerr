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

package org.apache.shardingsphere.example.shadow.table.raw.jdbc.config;

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.TableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.strategy.algorithm.sharding.inline.InlineShardingAlgorithm;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class ShardingShadowDatabasesConfiguration implements ExampleConfiguration {
    
    @Override
    public DataSource getDataSource() throws SQLException {
        ShadowRuleConfiguration shadowRuleConfiguration = new ShadowRuleConfiguration("shadow", new HashMap<String, String>(){{
            put("ds_0","shadow_ds_0");
            put("ds_1","shadow_ds_1");
        }});
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds_0", DataSourceUtil.createDataSource("demo_ds_0"));
        dataSourceMap.put("ds_1", DataSourceUtil.createDataSource("demo_ds_1"));
        dataSourceMap.put("shadow_ds_0", DataSourceUtil.createDataSource("shadow_demo_ds_0"));
        dataSourceMap.put("shadow_ds_1", DataSourceUtil.createDataSource("shadow_demo_ds_1"));
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getTableRuleConfigs().add(getUserTableConfiguration());
        Properties properties = new Properties();
        properties.setProperty("sql.show","true");
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Arrays.asList(shadowRuleConfiguration, shardingRuleConfiguration), properties);
    }
    
    private TableRuleConfiguration getUserTableConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration("t_user", "ds_${0..1}.t_user");
        result.setTableShardingStrategyConfig(getTableStandardShardingStrategyConfiguration());
        result.setDatabaseShardingStrategyConfig(getDatabaseStandardShardingStrategyConfiguration());
        return result;
    }
    
    private StandardShardingStrategyConfiguration getTableStandardShardingStrategyConfiguration() {
        InlineShardingAlgorithm inlineShardingAlgorithm = new InlineShardingAlgorithm();
        inlineShardingAlgorithm.getProperties().setProperty("algorithm.expression", "t_user");
        return new StandardShardingStrategyConfiguration("user_id", inlineShardingAlgorithm);
    }
    
    private StandardShardingStrategyConfiguration getDatabaseStandardShardingStrategyConfiguration() {
        InlineShardingAlgorithm inlineShardingAlgorithm = new InlineShardingAlgorithm();
        inlineShardingAlgorithm.getProperties().setProperty("algorithm.expression", "ds_${user_id % 2}");
        return new StandardShardingStrategyConfiguration("user_id", inlineShardingAlgorithm);
    }
}
