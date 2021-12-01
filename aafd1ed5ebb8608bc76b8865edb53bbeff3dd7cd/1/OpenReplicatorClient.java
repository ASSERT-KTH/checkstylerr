/**
 * Copyright 2016 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.storm.mysql;

import com.flipkart.storm.mysql.schema.DatabaseInfo;
import com.google.code.or.OpenReplicator;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper over open replicator. Takes care of retrieving the MySql schema as well.
 */
public class OpenReplicatorClient {

    /** The logger. */
    public static final Logger LOGGER = LoggerFactory.getLogger(OpenReplicatorClient.class);

    private OpenReplicator      openReplicator;
    private final ZkClient      zkClient;
    private final MySqlClient   mySqlClient;

    /**
     * Provide Open Replicator Client with external clients.
     *
     * @param mysqlClient the mysql client
     * @param client the zookeeper client
     */
    public OpenReplicatorClient(MySqlClient mysqlClient, ZkClient client) {
        this.zkClient = client;
        this.mySqlClient = mysqlClient;
    }

    /**
     * Initialize while providing user spout configurations and internal transaction buffer.
     *
     * @param mySqlConfig Mysql user defined configurations.
     * @param zkConfig Zookeeper configurations.
     * @param txEventQueue the internal buffer for transaction events.
     * @return the start bin log position.
     */
    public BinLogPosition initialize(MySqlConfig mySqlConfig,
                                    ZkBinLogStateConfig zkConfig,
                                    LinkedBlockingQueue<TransactionEvent> txEventQueue) {
        this.openReplicator = new OpenReplicator();
        this.openReplicator.setUser(mySqlConfig.getUser());
        this.openReplicator.setPassword(mySqlConfig.getPassword());
        this.openReplicator.setServerId(mySqlConfig.getServerId());
        this.openReplicator.setPort(mySqlConfig.getPort());
        this.openReplicator.setHost(mySqlConfig.getHost());

        BinLogPosition binLogPosition = getBinLogPositionToStartFrom(mySqlConfig, zkConfig);
        this.openReplicator.setBinlogPosition(binLogPosition.getBinLogPosition());
        this.openReplicator.setBinlogFileName(binLogPosition.getBinLogFileName());
        this.openReplicator.setBinlogEventListener(new SpoutBinLogEventListener(txEventQueue,
                                                                                getSchema(mySqlConfig),
                                                                                binLogPosition.getBinLogFileName()));
        return binLogPosition;
    }

    /**
     * Start the replication.
     */
    public void start() {
        try {
            this.openReplicator.start();
        } catch (Exception ex) {
            throw new RuntimeException("Error initializing the MySQL replicator...", ex);
        }
    }

    private BinLogPosition getBinLogPositionToStartFrom(MySqlConfig mysqlConfig, ZkBinLogStateConfig zkConfig) {
        try {
                if (zkConfig.isZkIgnoreBinLogPosition()) {
                    LOGGER.info("Ignoring Zookeeper state because ignoreZkBingLogPosition set to true...");
                    BinLogPosition binLogPosition = getBinLogPosition(mysqlConfig);
                    LOGGER.info("Starting from BinLogFile {} and BinLogPosition {}",
                            binLogPosition.getBinLogFileName(), binLogPosition.getBinLogPosition());
                    return binLogPosition;
                } else {
                    OffsetInfo offsetInfo = getDetailsFromZK(zkConfig.getZkScnCommitPath());
                    if (offsetInfo == null) {
                        LOGGER.info("No Information of offsets found in zookeeper, trying from MySQL...");
                        BinLogPosition binLogPosition = getBinLogPosition(mysqlConfig);
                        LOGGER.info("Starting from BinLogFile {} and BinLogPosition {}",
                                binLogPosition.getBinLogFileName(), binLogPosition.getBinLogPosition());
                        return binLogPosition;
                    } else {
                        LOGGER.info("Offset Information found in Zookeeper. Starting from BinLogFile {} BinLogPosition {}",
                                offsetInfo.getBinLogFileName(), offsetInfo.getBinLogPosition());
                        return new BinLogPosition(offsetInfo.getBinLogPosition(), offsetInfo.getBinLogFileName());
                    }
                }
            } catch (Exception ex) {
                    throw new RuntimeException("Could not get starting offset to read from", ex);
            }
    }

    private BinLogPosition getBinLogPosition(MySqlConfig mysqlConfig) throws SQLException {
        if (Strings.isNullOrEmpty(mysqlConfig.getBinLogFileName())) {
            return this.mySqlClient.getBinLogDetails();
        } else {

            return new BinLogPosition(mysqlConfig.getBinLogPosition(), mysqlConfig.getBinLogFileName());
        }
    }

    private DatabaseInfo getSchema(MySqlConfig mySqlConfig) {
        try {
            DatabaseInfo dbSchemaInfo = this.mySqlClient.getDatabaseSchema(mySqlConfig.getDatabase(),
                                                                           mySqlConfig.getTables());
            LOGGER.info("Table List to propagate events from {}", dbSchemaInfo.getAllTableNames());
            LOGGER.info("Complete Schema Recognized : {}", dbSchemaInfo);
            return dbSchemaInfo;
        } catch (SQLException ex) {
            throw new RuntimeException("Error reading schema information from MySQL...", ex);
        }
    }


    private OffsetInfo getDetailsFromZK(String path) {
        OffsetInfo zkOffsetInfo = null;
        try {
            zkOffsetInfo = zkClient.read(path);
            LOGGER.info("Read information from: Path {} Offset Info {}", path, zkOffsetInfo);
        } catch (Throwable e) {
            LOGGER.warn("Error reading from ZkNode: {} {}", path, e);
        }
        return zkOffsetInfo;
    }

    /**
     * Stop replication.
     */
    public void close() {
        try {
            openReplicator.stop(100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
