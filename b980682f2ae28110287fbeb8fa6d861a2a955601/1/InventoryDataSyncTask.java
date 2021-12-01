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

package org.apache.shardingsphere.shardingscaling.core.synctask.inventory;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.ScalingContext;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.controller.task.ReportCallback;
import org.apache.shardingsphere.shardingscaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.shardingscaling.core.exception.SyncTaskExecuteException;
import org.apache.shardingsphere.shardingscaling.core.execute.engine.ExecuteCallback;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.MemoryChannel;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.dumper.Dumper;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.dumper.DumperFactory;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.importer.Importer;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.importer.ImporterFactory;
import org.apache.shardingsphere.shardingscaling.core.synctask.SyncTask;
import org.apache.shardingsphere.underlying.common.database.metadata.DataSourceMetaData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Table slice execute task.
 */
@Slf4j
public final class InventoryDataSyncTask implements SyncTask {
    
    private final SyncConfiguration syncConfiguration;
    
    private final DataSourceManager dataSourceManager;
    
    private final String syncTaskId;
    
    private long estimatedRows;
    
    private AtomicLong syncedRows = new AtomicLong();
    
    private Dumper dumper;
    
    public InventoryDataSyncTask(final SyncConfiguration syncConfiguration) {
        this(syncConfiguration, new DataSourceManager());
    }
    
    public InventoryDataSyncTask(final SyncConfiguration syncConfiguration, final DataSourceManager dataSourceManager) {
        this.syncConfiguration = syncConfiguration;
        this.dataSourceManager = dataSourceManager;
        syncTaskId = generateSyncTaskId(syncConfiguration.getDumperConfiguration());
    }
    
    private String generateSyncTaskId(final RdbmsConfiguration dumperConfiguration) {
        DataSourceMetaData dataSourceMetaData = dumperConfiguration.getDataSourceConfiguration().getDataSourceMetaData();
        String result = String.format("inventory-%s-%s", Optional.ofNullable(dataSourceMetaData.getCatalog()).orElse(dataSourceMetaData.getSchema()), dumperConfiguration.getTableName());
        return null == dumperConfiguration.getWhereCondition() ? result : result + "#" + dumperConfiguration.getSpiltNum();
    }
    
    @Override
    public void start(final ReportCallback callback) {
        getEstimatedRows();
        instanceDumper();
        Importer importer = ImporterFactory.newInstance(syncConfiguration.getImporterConfiguration(), dataSourceManager);
        instanceChannel(importer);
        Future future = ScalingContext.getInstance().getImporterExecuteEngine().submit(importer, new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
            }
    
            @Override
            public void onFailure(final Throwable throwable) {
                dumper.stop();
            }
        });
        dumper.start();
        checkResult(future);
    }
    
    private void getEstimatedRows() {
        DataSource dataSource = dataSourceManager.getDataSource(syncConfiguration.getDumperConfiguration().getDataSourceConfiguration());
        try (Connection connection = dataSource.getConnection()) {
            ResultSet resultSet = connection.prepareStatement(String.format("SELECT COUNT(*) FROM %s %s",
                    syncConfiguration.getDumperConfiguration().getTableName(),
                    syncConfiguration.getDumperConfiguration().getWhereCondition()))
                    .executeQuery();
            resultSet.next();
            estimatedRows = resultSet.getInt(1);
        } catch (SQLException e) {
            throw new SyncTaskExecuteException("get estimated rows error.", e);
        }
    }
    
    private void instanceDumper() {
        syncConfiguration.getDumperConfiguration().setTableNameMap(syncConfiguration.getTableNameMap());
        dumper = DumperFactory.newInstanceJdbcDumper(syncConfiguration.getDumperConfiguration(), dataSourceManager);
    }
    
    private void instanceChannel(final Importer importer) {
        MemoryChannel channel = new MemoryChannel(records -> {
            int count = 0;
            for (Record record : records) {
                if (DataRecord.class.equals(record.getClass())) {
                    count++;
                }
            }
            syncedRows.addAndGet(count);
        });
        dumper.setChannel(channel);
        importer.setChannel(channel);
    }
    
    private void checkResult(final Future future) {
        try {
            future.get();
        } catch (InterruptedException ignored) {
        } catch (ExecutionException e) {
            throw new SyncTaskExecuteException(String.format("Task %s execute failed ", syncTaskId), e.getCause());
        }
    }
    
    @Override
    public void stop() {
        if (null != dumper) {
            dumper.stop();
            dumper = null;
        }
    }
    
    @Override
    public SyncProgress getProgress() {
        return new InventoryDataSyncTaskProgress(syncTaskId, estimatedRows, syncedRows.get());
    }
}
