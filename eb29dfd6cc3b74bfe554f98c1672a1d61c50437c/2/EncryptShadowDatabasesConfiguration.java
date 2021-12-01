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
import org.apache.shardingsphere.encrypt.api.config.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptorRuleConfiguration;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class EncryptShadowDatabasesConfiguration implements ExampleConfiguration {
    
    @Override
    public DataSource getDataSource() throws SQLException {
        ShadowRuleConfiguration shadowRuleConfiguration = new ShadowRuleConfiguration("shadow", Collections.singletonMap("ds", "ds_0"));
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds", DataSourceUtil.createDataSource("demo_ds"));
        dataSourceMap.put("ds_0", DataSourceUtil.createDataSource("shadow_demo_ds"));
        EncryptRuleConfiguration encryptRuleConfiguration = new EncryptRuleConfiguration(getEncryptorRuleConfiguration(), getEncryptTableRuleConfiguration());
        Properties properties = new Properties();
        properties.setProperty("sql.show", "true");
        properties.setProperty("query.with.cipher.column", "true");
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Arrays.asList(shadowRuleConfiguration, encryptRuleConfiguration), properties);
    }
    
    private Map<String, EncryptorRuleConfiguration> getEncryptorRuleConfiguration() {
        Map<String, EncryptorRuleConfiguration> result = new HashMap<>();
        Properties properties = new Properties();
        properties.setProperty("aes.key.value", "123456");
        EncryptorRuleConfiguration nameEncryptorRuleConfiguration = new EncryptorRuleConfiguration("aes", properties);
        EncryptorRuleConfiguration pwdEncryptorRuleConfiguration = new EncryptorRuleConfiguration("assistedTest", null);
        result.put("name_encryptror", nameEncryptorRuleConfiguration);
        result.put("pwd_encryptror", pwdEncryptorRuleConfiguration);
        return result;
    }
    
    private Map<String, EncryptTableRuleConfiguration> getEncryptTableRuleConfiguration() {
        Map<String, EncryptTableRuleConfiguration> result = new HashMap<>();
        Map<String, EncryptColumnRuleConfiguration> columns = new HashMap<>();
        columns.put("user_name", new EncryptColumnRuleConfiguration("user_name_plain", "user_name", "", "name_encryptror"));
        columns.put("pwd", new EncryptColumnRuleConfiguration("", "pwd", "assisted_query_pwd", "pwd_encryptror"));
        result.put("t_user",new EncryptTableRuleConfiguration(columns));
        return result;
    }
}
