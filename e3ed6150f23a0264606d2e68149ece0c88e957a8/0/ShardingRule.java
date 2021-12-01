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

package org.apache.shardingsphere.sharding.rule;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.DataNodeRoutedRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.TypedSPIRegistry;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.sharding.strategy.ShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.ShardingStrategyFactory;
import org.apache.shardingsphere.sharding.strategy.none.NoneShardingStrategy;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Sharding rule.
 */
@Getter
public final class ShardingRule implements DataNodeRoutedRule {
    
    static {
        ShardingSphereServiceLoader.register(ShardingAlgorithm.class);
        ShardingSphereServiceLoader.register(KeyGenerateAlgorithm.class);
    }
    
    private final Collection<String> dataSourceNames;
    
    private final Map<String, ShardingAlgorithm> shardingAlgorithms = new LinkedHashMap<>();
    
    private final Map<String, KeyGenerateAlgorithm> keyGenerators = new LinkedHashMap<>();
    
    private final Collection<TableRule> tableRules;
    
    private final Collection<BindingTableRule> bindingTableRules;
    
    private final Collection<String> broadcastTables;
    
    private final ShardingStrategy defaultDatabaseShardingStrategy;
    
    private final ShardingStrategy defaultTableShardingStrategy;
    
    private final KeyGenerateAlgorithm defaultKeyGenerateAlgorithm;
    
    public ShardingRule(final ShardingRuleConfiguration configuration, final Collection<String> dataSourceNames) {
        Preconditions.checkArgument(null != configuration, "ShardingRuleConfig cannot be null.");
        Preconditions.checkArgument(null != dataSourceNames && !dataSourceNames.isEmpty(), "Data sources cannot be empty.");
        this.dataSourceNames = getDataSourceNames(configuration.getTables(), dataSourceNames);
        configuration.getShardingAlgorithms().forEach((key, value) -> shardingAlgorithms.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, ShardingAlgorithm.class)));
        configuration.getKeyGenerators().forEach((key, value) -> keyGenerators.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, KeyGenerateAlgorithm.class)));
        tableRules = new LinkedList<>(createTableRules(configuration.getTables(), configuration.getDefaultKeyGenerateStrategy()));
        tableRules.addAll(createAutoTableRules(configuration.getAutoTables(), configuration.getDefaultKeyGenerateStrategy()));
        broadcastTables = configuration.getBroadcastTables();
        bindingTableRules = createBindingTableRules(configuration.getBindingTableGroups());
        defaultDatabaseShardingStrategy = createDefaultShardingStrategy(configuration.getDefaultDatabaseShardingStrategy());
        defaultTableShardingStrategy = createDefaultShardingStrategy(configuration.getDefaultTableShardingStrategy());
        defaultKeyGenerateAlgorithm = null == configuration.getDefaultKeyGenerateStrategy()
                ? TypedSPIRegistry.getRegisteredService(KeyGenerateAlgorithm.class) : keyGenerators.get(configuration.getDefaultKeyGenerateStrategy().getKeyGeneratorName());
    }
    
    public ShardingRule(final AlgorithmProvidedShardingRuleConfiguration configuration, final Collection<String> dataSourceNames) {
        Preconditions.checkArgument(null != configuration, "ShardingRuleConfig cannot be null.");
        Preconditions.checkArgument(null != dataSourceNames && !dataSourceNames.isEmpty(), "Data sources cannot be empty.");
        this.dataSourceNames = getDataSourceNames(configuration.getTables(), dataSourceNames);
        shardingAlgorithms.putAll(configuration.getShardingAlgorithms());
        keyGenerators.putAll(configuration.getKeyGenerators());
        tableRules = new LinkedList<>(createTableRules(configuration.getTables(), configuration.getDefaultKeyGenerateStrategy()));
        tableRules.addAll(createAutoTableRules(configuration.getAutoTables(), configuration.getDefaultKeyGenerateStrategy()));
        broadcastTables = configuration.getBroadcastTables();
        bindingTableRules = createBindingTableRules(configuration.getBindingTableGroups());
        defaultDatabaseShardingStrategy = createDefaultShardingStrategy(configuration.getDefaultDatabaseShardingStrategy());
        defaultTableShardingStrategy = createDefaultShardingStrategy(configuration.getDefaultTableShardingStrategy());
        defaultKeyGenerateAlgorithm = null == configuration.getDefaultKeyGenerateStrategy()
                ? TypedSPIRegistry.getRegisteredService(KeyGenerateAlgorithm.class) : keyGenerators.get(configuration.getDefaultKeyGenerateStrategy().getKeyGeneratorName());
    }
    
    private Collection<String> getDataSourceNames(final Collection<ShardingTableRuleConfiguration> tableRuleConfigs, final Collection<String> dataSourceNames) {
        Collection<String> result = new LinkedHashSet<>();
        if (tableRuleConfigs.isEmpty()) {
            return dataSourceNames;
        }
        for (ShardingTableRuleConfiguration each : tableRuleConfigs) {
            if (null == each.getActualDataNodes()) {
                return dataSourceNames;
            }
            result.addAll(getDataSourceNames(each));
        }
        return result;
    }
    
    private Collection<String> getDataSourceNames(final ShardingTableRuleConfiguration shardingTableRuleConfiguration) {
        Collection<String> result = new LinkedHashSet<>();
        for (String each : new InlineExpressionParser(shardingTableRuleConfiguration.getActualDataNodes()).splitAndEvaluate()) {
            result.add(new DataNode(each).getDataSourceName());
        }
        return result;
    }
    
    private Collection<TableRule> createTableRules(
            final Collection<ShardingTableRuleConfiguration> tableRuleConfigurations, final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfiguration) {
        return tableRuleConfigurations.stream().map(
            each -> {
                ShardingAlgorithm databaseShardingAlgorithm = null == each.getDatabaseShardingStrategy()
                        ? null : shardingAlgorithms.get(each.getDatabaseShardingStrategy().getShardingAlgorithmName());
                ShardingAlgorithm tableShardingAlgorithm = null == each.getTableShardingStrategy() ? null : shardingAlgorithms.get(each.getTableShardingStrategy().getShardingAlgorithmName());
                return new TableRule(each, dataSourceNames, databaseShardingAlgorithm, tableShardingAlgorithm, getDefaultGenerateKeyColumn(defaultKeyGenerateStrategyConfiguration));
            }).collect(Collectors.toList());
    }
    
    private Collection<TableRule> createAutoTableRules(
            final Collection<ShardingAutoTableRuleConfiguration> autoTableRuleConfigurations, final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfiguration) {
        return autoTableRuleConfigurations.stream().map(
            each -> {
                ShardingAlgorithm shardingAlgorithm = null == each.getShardingStrategy() ? null : shardingAlgorithms.get(each.getShardingStrategy().getShardingAlgorithmName());
                return new TableRule(each, dataSourceNames, shardingAlgorithm, getDefaultGenerateKeyColumn(defaultKeyGenerateStrategyConfiguration));
            }).collect(Collectors.toList());
    }
    
    private String getDefaultGenerateKeyColumn(final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfiguration) {
        return Optional.ofNullable(defaultKeyGenerateStrategyConfiguration).map(KeyGenerateStrategyConfiguration::getColumn).orElse(null);
    }
    
    private Collection<BindingTableRule> createBindingTableRules(final Collection<String> bindingTableGroups) {
        return bindingTableGroups.stream().map(this::createBindingTableRule).collect(Collectors.toList());
    }
    
    private BindingTableRule createBindingTableRule(final String bindingTableGroup) {
        return new BindingTableRule(Splitter.on(",").trimResults().splitToList(bindingTableGroup).stream().map(this::getTableRule).collect(Collectors.toList()));
    }
    
    private ShardingStrategy createDefaultShardingStrategy(final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        return null == shardingStrategyConfiguration ? new NoneShardingStrategy()
                : ShardingStrategyFactory.newInstance(shardingStrategyConfiguration, shardingAlgorithms.get(shardingStrategyConfiguration.getShardingAlgorithmName()));
    }
    
    /**
     * Find table rule.
     *
     * @param logicTableName logic table name
     * @return table rule
     */
    public Optional<TableRule> findTableRule(final String logicTableName) {
        return tableRules.stream().filter(each -> each.getLogicTable().equalsIgnoreCase(logicTableName)).findFirst();
    }
    
    /**
     * Find table rule via actual table name.
     *
     * @param actualTableName actual table name
     * @return table rule
     */
    public Optional<TableRule> findTableRuleByActualTable(final String actualTableName) {
        return tableRules.stream().filter(each -> each.isExisted(actualTableName)).findFirst();
    }
    
    /**
     * Get table rule.
     *
     * @param logicTableName logic table name
     * @return table rule
     */
    public TableRule getTableRule(final String logicTableName) {
        Optional<TableRule> tableRule = findTableRule(logicTableName);
        if (tableRule.isPresent()) {
            return tableRule.get();
        }
        if (isBroadcastTable(logicTableName)) {
            return new TableRule(dataSourceNames, logicTableName);
        }
        throw new ShardingSphereConfigurationException("Cannot find table rule with logic table: '%s'", logicTableName);
    }
    
    /**
     * Get database sharding strategy.
     *
     * <p>
     * Use default database sharding strategy if not found.
     * </p>
     *
     * @param tableRule table rule
     * @return database sharding strategy
     */
    public ShardingStrategy getDatabaseShardingStrategy(final TableRule tableRule) {
        return null == tableRule.getDatabaseShardingStrategy() ? defaultDatabaseShardingStrategy : tableRule.getDatabaseShardingStrategy();
    }
    
    /**
     * Get table sharding strategy.
     *
     * <p>
     * Use default table sharding strategy if not found.
     * </p>
     *
     * @param tableRule table rule
     * @return table sharding strategy
     */
    public ShardingStrategy getTableShardingStrategy(final TableRule tableRule) {
        return null == tableRule.getTableShardingStrategy() ? defaultTableShardingStrategy : tableRule.getTableShardingStrategy();
    }
    
    /**
     * Judge logic tables is all belong to binding encryptors.
     *
     * @param logicTableNames logic table names
     * @return logic tables is all belong to binding encryptors or not
     */
    public boolean isAllBindingTables(final Collection<String> logicTableNames) {
        if (logicTableNames.isEmpty()) {
            return false;
        }
        Optional<BindingTableRule> bindingTableRule = findBindingTableRule(logicTableNames);
        if (!bindingTableRule.isPresent()) {
            return false;
        }
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(bindingTableRule.get().getAllLogicTables());
        return !result.isEmpty() && result.containsAll(logicTableNames);
    }
    
    private Optional<BindingTableRule> findBindingTableRule(final Collection<String> logicTableNames) {
        return logicTableNames.stream().map(this::findBindingTableRule).filter(Optional::isPresent).findFirst().orElse(Optional.empty());
    }
    
    /**
     * Find binding table rule via logic table name.
     *
     * @param logicTableName logic table name
     * @return binding table rule
     */
    public Optional<BindingTableRule> findBindingTableRule(final String logicTableName) {
        return bindingTableRules.stream().filter(each -> each.hasLogicTable(logicTableName)).findFirst();
    }
    
    /**
     * Judge logic tables is all belong to broadcast encryptors.
     *
     * @param logicTableNames logic table names
     * @return logic tables is all belong to broadcast encryptors or not
     */
    public boolean isAllBroadcastTables(final Collection<String> logicTableNames) {
        return !logicTableNames.isEmpty() && logicTableNames.stream().allMatch(this::isBroadcastTable);
    }
    
    /**
     * Judge logic table is belong to broadcast tables.
     *
     * @param logicTableName logic table name
     * @return logic table is belong to broadcast tables or not
     */
    public boolean isBroadcastTable(final String logicTableName) {
        return broadcastTables.stream().anyMatch(each -> each.equalsIgnoreCase(logicTableName));
    }
    
    /**
     * Judge if there is at least one table rule for logic tables.
     *
     * @param logicTableNames logic table names
     * @return whether a table rule exists for logic tables
     */
    public boolean tableRuleExists(final Collection<String> logicTableNames) {
        return logicTableNames.stream().anyMatch(each -> findTableRule(each).isPresent() || isBroadcastTable(each));
    }
    
    /**
     * Judge is sharding column or not.
     *
     * @param columnName column name
     * @param tableName table name
     * @return is sharding column or not
     */
    public boolean isShardingColumn(final String columnName, final String tableName) {
        return tableRules.stream().anyMatch(each -> each.getLogicTable().equalsIgnoreCase(tableName) && isShardingColumn(each, columnName));
    }
    
    private boolean isShardingColumn(final TableRule tableRule, final String columnName) {
        return getDatabaseShardingStrategy(tableRule).getShardingColumns().contains(columnName) || getTableShardingStrategy(tableRule).getShardingColumns().contains(columnName);
    }
    
    /**
     * Judge is generate key column or not.
     *
     * @param columnName column name
     * @param tableName table name
     * @return is generate key column or not
     */
    public boolean isGenerateKeyColumn(final String columnName, final String tableName) {
        return tableRules.stream().anyMatch(each -> each.getLogicTable().equalsIgnoreCase(tableName) && isGenerateKeyColumn(each, columnName));
    }
    
    private boolean isGenerateKeyColumn(final TableRule tableRule, final String columnName) {
        Optional<String> generateKeyColumn = tableRule.getGenerateKeyColumn();
        return generateKeyColumn.isPresent() && generateKeyColumn.get().equalsIgnoreCase(columnName);
    }
    
    /**
     * Find column name of generated key.
     *
     * @param logicTableName logic table name
     * @return column name of generated key
     */
    public Optional<String> findGenerateKeyColumnName(final String logicTableName) {
        return tableRules.stream().filter(each -> each.getLogicTable().equalsIgnoreCase(logicTableName) && each.getGenerateKeyColumn().isPresent())
                .map(TableRule::getGenerateKeyColumn).findFirst().orElse(Optional.empty());
    }
    
    /**
     * Generate key.
     *
     * @param logicTableName logic table name
     * @return generated key
     */
    public Comparable<?> generateKey(final String logicTableName) {
        Optional<TableRule> tableRule = findTableRule(logicTableName);
        if (!tableRule.isPresent()) {
            throw new ShardingSphereConfigurationException("Cannot find strategy for generate keys.");
        }
        KeyGenerateAlgorithm keyGenerator = null != tableRule.get().getKeyGeneratorName() ? keyGenerators.get(tableRule.get().getKeyGeneratorName()) : defaultKeyGenerateAlgorithm;
        return keyGenerator.generateKey();
    }
    
    /**
     * Find data node by logic table name.
     *
     * @param logicTableName logic table name
     * @return data node
     */
    public DataNode getDataNode(final String logicTableName) {
        TableRule tableRule = getTableRule(logicTableName);
        return tableRule.getActualDataNodes().get(0);
    }
    
    /**
     * Find data node by data source and logic table.
     *
     * @param dataSourceName data source name
     * @param logicTableName logic table name
     * @return data node
     */
    public DataNode getDataNode(final String dataSourceName, final String logicTableName) {
        TableRule tableRule = getTableRule(logicTableName);
        return tableRule.getActualDataNodes().stream().filter(each -> dataSourceNames.contains(each.getDataSourceName())
                && each.getDataSourceName().equals(dataSourceName)).findFirst()
                .orElseThrow(() -> new ShardingSphereConfigurationException("Cannot find actual data node for data source name: '%s' and logic table name: '%s'", dataSourceName, logicTableName));
    }
    
    /**
     * Get sharding logic table names.
     *
     * @param logicTableNames logic table names
     * @return sharding logic table names
     */
    public Collection<String> getShardingLogicTableNames(final Collection<String> logicTableNames) {
        return logicTableNames.stream().filter(each -> findTableRule(each).isPresent()).collect(Collectors.toCollection(LinkedList::new));
    }
    
    /**
     * Get logic and actual binding tables.
     *
     * @param dataSourceName data source name
     * @param logicTable logic table name
     * @param actualTable actual table name
     * @param availableLogicBindingTables available logic binding table names
     * @return logic and actual binding tables
     */
    public Map<String, String> getLogicAndActualTablesFromBindingTable(final String dataSourceName,
                                                                       final String logicTable, final String actualTable, final Collection<String> availableLogicBindingTables) {
        Map<String, String> result = new LinkedHashMap<>();
        findBindingTableRule(logicTable).ifPresent(bindingTableRule -> result.putAll(bindingTableRule.getLogicAndActualTables(dataSourceName, logicTable, actualTable, availableLogicBindingTables)));
        return result;
    }
    
    @Override
    public Map<String, Collection<DataNode>> getAllDataNodes() {
        return tableRules.stream().collect(Collectors.toMap(TableRule::getLogicTable, TableRule::getActualDataNodes));
    }
    
    @Override
    public Collection<String> getAllActualTables() {
        return tableRules.stream().flatMap(each -> each.getActualDataNodes().stream().map(DataNode::getTableName)).collect(Collectors.toSet());
    }
    
    @Override
    public Optional<String> findFirstActualTable(final String logicTable) {
        return findTableRule(logicTable).map(tableRule -> tableRule.getActualDataNodes().get(0).getTableName());
    }
    
    @Override
    public boolean isNeedAccumulate(final Collection<String> tables) {
        return !isAllBroadcastTables(tables);
    }
    
    @Override
    public Optional<String> findLogicTableByActualTable(final String actualTable) {
        return findTableRuleByActualTable(actualTable).map(TableRule::getLogicTable);
    }
}
