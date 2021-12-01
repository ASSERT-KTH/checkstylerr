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

package org.apache.shardingsphere.shardingjdbc.orchestration.spring.boot;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlCenterRepositoryConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.swapper.CenterRepositoryConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.strategy.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationShardingSphereDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.boot.common.SpringBootRootConfigurationProperties;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.boot.rule.LocalRulesCondition;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.boot.rule.OrchestrationSpringBootRulesConfigurationProperties;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.boot.rule.OrchestrationSpringBootRulesConfigurationYamlSwapper;
import org.apache.shardingsphere.spring.boot.datasource.DataSourcePropertiesSetterHolder;
import org.apache.shardingsphere.spring.boot.util.DataSourceUtil;
import org.apache.shardingsphere.spring.boot.util.PropertyUtil;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Orchestration spring boot configuration.
 */
@Configuration
@ComponentScan("org.apache.shardingsphere.spring.boot.converter")
@EnableConfigurationProperties({OrchestrationSpringBootRulesConfigurationProperties.class, SpringBootRootConfigurationProperties.class})
@ConditionalOnProperty(prefix = "spring.shardingsphere", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
public class OrchestrationSpringBootConfiguration implements EnvironmentAware {
    
    private final Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
    
    private final OrchestrationSpringBootRulesConfigurationProperties rules;
    
    private final SpringBootRootConfigurationProperties root;
    
    private final CenterRepositoryConfigurationYamlSwapper centerRepositorySwapper = new CenterRepositoryConfigurationYamlSwapper();
    
    private final OrchestrationSpringBootRulesConfigurationYamlSwapper configurationSwapper = new OrchestrationSpringBootRulesConfigurationYamlSwapper();
    
    /**
     * Get orchestration configuration.
     *
     * @return orchestration configuration
     */
    @Bean
    public OrchestrationConfiguration orchestrationConfiguration() {
        Preconditions.checkState(isValidOrchestrationConfiguration(), "The orchestration configuration is invalid, please configure orchestration");
        Map<String, CenterConfiguration> instanceConfigurationMap = new HashMap<>(root.getOrchestration().size(), 1);
        for (Entry<String, YamlCenterRepositoryConfiguration> entry : root.getOrchestration().entrySet()) {
            instanceConfigurationMap.put(entry.getKey(), centerRepositorySwapper.swap(entry.getValue()));
        }
        return new OrchestrationConfiguration(instanceConfigurationMap);
    }
    
    private boolean isValidOrchestrationConfiguration() {
        return !CollectionUtils.isEmpty(root.getOrchestration());
    }
    
    /**
     * Get orchestration ShardingSphere data source bean by local configuration.
     *
     * @param orchestrationConfiguration orchestration configuration
     * @return orchestration sharding data source bean
     * @throws SQLException SQL exception
     */
    @Bean
    @Conditional(LocalRulesCondition.class)
    public DataSource localShardingSphereDataSource(final OrchestrationConfiguration orchestrationConfiguration) throws SQLException {
        return new OrchestrationShardingSphereDataSource(new ShardingSphereDataSource(dataSourceMap, configurationSwapper.swap(rules), root.getProps()), orchestrationConfiguration);
    }
    
    /**
     * Get data source bean from registry center.
     *
     * @param orchestrationConfiguration orchestration configuration
     * @return data source bean
     * @throws SQLException SQL Exception
     */
    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource(final OrchestrationConfiguration orchestrationConfiguration) throws SQLException {
        return new OrchestrationShardingSphereDataSource(orchestrationConfiguration);
    }
    
    @Override
    public final void setEnvironment(final Environment environment) {
        String prefix = "spring.shardingsphere.datasource.";
        for (String each : getDataSourceNames(environment, prefix)) {
            try {
                dataSourceMap.put(each, getDataSource(environment, prefix, each));
            } catch (final ReflectiveOperationException ex) {
                throw new ShardingSphereException("Can't find data source type.", ex);
            }
        }
    }
    
    private List<String> getDataSourceNames(final Environment environment, final String prefix) {
        StandardEnvironment standardEnv = (StandardEnvironment) environment;
        standardEnv.setIgnoreUnresolvableNestedPlaceholders(true);
        String dataSources = standardEnv.getProperty(prefix + "name");
        if (StringUtils.isEmpty(dataSources)) {
            dataSources = standardEnv.getProperty(prefix + "names");
        }
        if (StringUtils.isEmpty(dataSources)) {
            return Collections.emptyList();
        }
        return new InlineExpressionParser(dataSources).splitAndEvaluate();
    }
    
    @SuppressWarnings("unchecked")
    private DataSource getDataSource(final Environment environment, final String prefix, final String dataSourceName) throws ReflectiveOperationException {
        Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + dataSourceName, Map.class);
        Preconditions.checkState(!dataSourceProps.isEmpty(), String.format("Wrong datasource [%s] properties!", dataSourceName));
        DataSource result = DataSourceUtil.getDataSource(dataSourceProps.get("type").toString(), dataSourceProps);
        DataSourcePropertiesSetterHolder.getDataSourcePropertiesSetterByType(dataSourceProps.get("type").toString()).ifPresent(
            dataSourcePropertiesSetter -> dataSourcePropertiesSetter.propertiesSet(environment, prefix, dataSourceName, result));
        return result;
    }
}
