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

import backtype.storm.Config;
import backtype.storm.metric.api.AssignableMetric;
import backtype.storm.metric.api.MeanReducer;
import backtype.storm.metric.api.ReducedMetric;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The MySql Bin Log Spout that emits events from mysql bin logs as a stream.
 */
public class MySqlBinLogSpout extends BaseRichSpout {

    /** The Logger. */
    public static final Logger  LOGGER                     = LoggerFactory.getLogger(MySqlBinLogSpout.class);

    private static final ObjectMapper MAPPER               = new ObjectMapper();
    private long                msgAckCount                = 0;
    private long                msgSidelineCount           = 0;
    private long                msgFailedCount             = 0;
    private long                currentCommittedOffsetInZk = -1;
    private String              databaseName               = null;

    private final MySqlSpoutConfig  spoutConfig;
    private String                  topologyInstanceId;
    private String                  topologyName;
    private ZkClient                zkClient;
    private MySqlClient             mySqlClient;
    private OpenReplicatorClient    openReplicatorClient;
    private SpoutOutputCollector    collector;
    private long                    zkLastUpdateMs;
    private ClientFactory           clientFactory;

    private AssignableMetric failureCountMetric;
    private AssignableMetric sidelineCountMetric;
    private AssignableMetric successCountMetric;
    private ReducedMetric    txEventProcessTime;

    BinLogPosition  lastEmittedBeginTxPosition = null;
    SortedMap<Long, Long>                   failureMessages           = new TreeMap<Long, Long>();
    LinkedBlockingQueue<TransactionEvent>   txQueue                   = new LinkedBlockingQueue<TransactionEvent>();
    SortedMap<Long, RetryTransactionEvent>  pendingMessagesToBeAcked  = new TreeMap<Long, RetryTransactionEvent>();


    /**
     * Initialize the MySql Spout.
     *
     * @param spoutConfig MySql + Zookeeper Configuration
     */
    public MySqlBinLogSpout(MySqlSpoutConfig spoutConfig) {
        this (spoutConfig, new ClientFactory());
    }

    /**
     * Initialize the MySql Spout, with provided client factories.
     * This will mostly be used in unit testing.
     *
     * @param spoutConfig MySql + Zookeeper Configuration
     * @param clientFactory Client Factory
     */
    public MySqlBinLogSpout(MySqlSpoutConfig spoutConfig, ClientFactory clientFactory) {
        this.spoutConfig = spoutConfig;
        this.clientFactory = clientFactory;
    }

    @Override
    public void open(Map conf, final TopologyContext context, final SpoutOutputCollector spoutOutputCollector) {

        Preconditions.checkNotNull(this.spoutConfig.getZkBinLogStateConfig(),
                "Zookeeper Config cannot be null");

        Preconditions.checkNotNull(this.spoutConfig.getMysqlConfig(),
                "Mysql Config cannot be null");

        LOGGER.info("Initiating MySql Spout with config {}", this.spoutConfig.toString());

        this.collector          = spoutOutputCollector;
        this.topologyInstanceId = context.getStormId();
        this.topologyName       = conf.get(Config.TOPOLOGY_NAME).toString();
        this.databaseName       = this.spoutConfig.getMysqlConfig().getDatabase();

        initializeAndRegisterAllMetrics(context);

        zkClient = this.clientFactory.getZkClient(conf, this.spoutConfig.getZkBinLogStateConfig());
        mySqlClient = this.clientFactory.getMySqlClient(this.spoutConfig.getMysqlConfig());
        openReplicatorClient = this.clientFactory.getReplicatorClient(mySqlClient, zkClient);

        begin();
    }

    /**
     * Start all clients.
     */
    public void begin() {
        this.zkClient.start();
        this.lastEmittedBeginTxPosition = openReplicatorClient.initialize(this.spoutConfig.getMysqlConfig(),
                                                                       this.spoutConfig.getZkBinLogStateConfig(),
                                                                       this.txQueue);
        openReplicatorClient.start();
    }

    @Override
    public void close() {
        zkClient.close();
        mySqlClient.close();
        openReplicatorClient.close();
    }

    @Override
    public void nextTuple() {
        RetryTransactionEvent txRetrEvent = null;
        if (this.failureMessages.isEmpty()) {

            TransactionEvent txEvent = this.txQueue.poll();
            if (txEvent != null) {
                txRetrEvent = new RetryTransactionEvent(txEvent, 1);
            }
        } else {

            long failedScn = this.failureMessages.firstKey();
            txRetrEvent = this.pendingMessagesToBeAcked.get(failedScn);
            if (txRetrEvent != null) {
                if (txRetrEvent.getNumRetries() >= this.spoutConfig.getFailureConfig().getNumMaxRetries()) {
                        this.spoutConfig.getFailureConfig().getSidelineStrategy().sideline(txRetrEvent.getTxEvent());
                        this.failureMessages.remove(failedScn);
                        this.pendingMessagesToBeAcked.remove(failedScn);
                        this.msgSidelineCount++;
                        this.sidelineCountMetric.setValue(this.msgSidelineCount);
                        LOGGER.info("Sidelining message id .... {}", failedScn);
                        txRetrEvent = null;
                } else {
                    txRetrEvent = new RetryTransactionEvent(txRetrEvent.getTxEvent(), txRetrEvent.getNumRetries() + 1);
                }
            } else {
                //Nothing was pending it seems... Remove from failure
                this.failureMessages.remove(failedScn);
            }
        }

        try {
            if (txRetrEvent != null) {
                LOGGER.debug("Received Tx Event in Spout...{}", txRetrEvent);
                TransactionEvent txEvent = txRetrEvent.getTxEvent();
                this.txEventProcessTime.update(txEvent.getEndTimeInNanos() - txEvent.getStartTimeInNanos());
                    String txJson = MAPPER.writeValueAsString(txEvent);
                    BinLogPosition binLogPosition = new BinLogPosition(txEvent.getBinLogPosition(),
                                                                       txEvent.getBinLogFileName());
                long scn = binLogPosition.getSCN();
                this.pendingMessagesToBeAcked.put(scn, txRetrEvent);
                this.lastEmittedBeginTxPosition = binLogPosition;
                collector.emit(new Values(txEvent.getDatabaseName(), txJson), scn);
            }

            long diffWithNow = System.currentTimeMillis() - zkLastUpdateMs;
            if (diffWithNow > this.spoutConfig.getZkBinLogStateConfig().getZkScnUpdateRateInMs() || diffWithNow < 0) {
                    commit();
            }
        } catch (ZkException ex) {
            LOGGER.error("Error occurred in Zookeeper..{}", ex);
        } catch (Exception ex) {
            LOGGER.error("Error occurred in processing event {}, exception {}", txRetrEvent, ex.getStackTrace());
        }

    }

    @Override
    public void ack(Object msgId) {
        LOGGER.trace("Acking For... {}", msgId);
        long scn = (Long) msgId;
        this.pendingMessagesToBeAcked.remove(scn);
        this.failureMessages.remove(scn);
        this.msgAckCount++;
        this.successCountMetric.setValue(this.msgAckCount);
    }

    @Override
    public void fail(Object msgId) {
        LOGGER.trace("Failing For... {}", msgId);
        int numFailures = this.failureMessages.size();
        if (numFailures >= this.spoutConfig.getFailureConfig().getNumMaxTotalFailAllowed()) {
            throw new RuntimeException("Failure count greater than configured allowed failures...Stopping");
        }
        long scn = (Long) msgId;
        this.failureMessages.put(scn, System.currentTimeMillis());
        this.msgFailedCount++;
        this.failureCountMetric.setValue(this.msgFailedCount);
    }

    @Override
    public void deactivate() {
        commit();
    }

    /**
     * Fields emitted by the spout.
     *
     * @param declarer the fields declarer
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("databaseName", "txEvent"));
    }

    /**
     * Updates zookeeper with the bin log position/offset.
     */
    public void commit() {
        long offset = (pendingMessagesToBeAcked.isEmpty()) ? this.lastEmittedBeginTxPosition.getSCN() :
                                                             pendingMessagesToBeAcked.firstKey();
        if (currentCommittedOffsetInZk != offset) {
            LOGGER.trace("Updating ZK with offset {} for topology: {} with Id: {}",
                                                    offset, this.topologyName, this.topologyInstanceId);
            OffsetInfo offsetInfo = null;
            if (pendingMessagesToBeAcked.isEmpty()) {
                offsetInfo = new OffsetInfo(offset,
                                            this.topologyName,
                                            this.topologyInstanceId,
                                            this.databaseName,
                                            this.lastEmittedBeginTxPosition.getBinLogPosition(),
                                            this.lastEmittedBeginTxPosition.getBinLogFileName());
            } else {
                TransactionEvent txEvent = pendingMessagesToBeAcked.get(offset).getTxEvent();
                offsetInfo = new OffsetInfo(offset,
                                            this.topologyName,
                                            this.topologyInstanceId,
                                            txEvent.getDatabaseName(),
                                            txEvent.getBinLogPosition(),
                                            txEvent.getBinLogFileName());
            }

            zkClient.write(this.spoutConfig.getZkBinLogStateConfig().getZkScnCommitPath(), offsetInfo);
            zkLastUpdateMs = System.currentTimeMillis();
            currentCommittedOffsetInZk = offset;
            LOGGER.debug("Update Complete in ZK at node {} with offset {} for topology: {} with Id: {}",
                                                            this.spoutConfig.getZkBinLogStateConfig().getZkScnCommitPath(),
                                                            offset, topologyName, topologyInstanceId);
        } else {
            LOGGER.trace("No update in ZK for offset {}", offset);
        }
    }

    private void initializeAndRegisterAllMetrics(TopologyContext context) {
        this.failureCountMetric     = new AssignableMetric(this.msgFailedCount);
        this.successCountMetric     = new AssignableMetric(this.msgAckCount);
        this.sidelineCountMetric    = new AssignableMetric(this.msgSidelineCount);
        this.txEventProcessTime     = new ReducedMetric(new MeanReducer());

        context.registerMetric(SpoutConstants.METRIC_FAILURECOUNT, this.failureCountMetric,
                               SpoutConstants.DEFAULT_TIMEBUCKETSIZEINSECS);
        context.registerMetric(SpoutConstants.METRIC_SUCCESSCOUNT, this.successCountMetric,
                               SpoutConstants.DEFAULT_TIMEBUCKETSIZEINSECS);
        context.registerMetric(SpoutConstants.METRIC_SIDELINECOUNT, this.sidelineCountMetric,
                               SpoutConstants.DEFAULT_TIMEBUCKETSIZEINSECS);
        context.registerMetric(SpoutConstants.METRIC_TXPROCESSTIME, this.txEventProcessTime,
                               SpoutConstants.DEFAULT_TIMEBUCKETSIZEINSECS);
    }
}









