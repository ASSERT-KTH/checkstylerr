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

import java.io.Serializable;

/**
 * My Sql Spout Complete configuration.
 */
public class MySqlSpoutConfig implements Serializable {

    private final MySqlConfig           mysqlConfig;
    private final ZkBinLogStateConfig   zkBinLogStateConfig;
    private final FailureConfig         failureConfig;
    private final int                   metricsTimeBucketSizeInSecs;
    private final int                   bufferCapacity;

    /**
     * Initialize a spout configuration without sidelining.
     * @param mysqlConfig mysql configuration.
     * @param zkBinLogStateConfig zookeeper configuration.
     */
    public MySqlSpoutConfig(MySqlConfig mysqlConfig, ZkBinLogStateConfig zkBinLogStateConfig) {
        this (mysqlConfig, zkBinLogStateConfig,
              new FailureConfig(SpoutConstants.DEFAULT_NUMMAXRETRIES, SpoutConstants.DEFAULT_NUMMAXTOTFAILALLOWED),
              SpoutConstants.DEFAULT_TIMEBUCKETSIZEINSECS, SpoutConstants.DEFAULT_BUFFER_CAPACITY);
    }

    /**
     * Initialize a spout configuration with sidelining.
     * @param mysqlConfig mysql configuration.
     * @param zkBinLogStateConfig zookeeper configuration.
     * @param metricsTimeBucketSizeInSecs time in which the metrics will be sent to the consumer
     */
    public MySqlSpoutConfig(MySqlConfig mysqlConfig, ZkBinLogStateConfig zkBinLogStateConfig, int metricsTimeBucketSizeInSecs) {
        this (mysqlConfig, zkBinLogStateConfig,
                new FailureConfig(SpoutConstants.DEFAULT_NUMMAXRETRIES, SpoutConstants.DEFAULT_NUMMAXTOTFAILALLOWED),
                metricsTimeBucketSizeInSecs, SpoutConstants.DEFAULT_BUFFER_CAPACITY);
    }

    /**
     * Initialize a spout configuration with sidelining.
     * @param mysqlConfig mysql configuration.
     * @param zkBinLogStateConfig zookeeper configuration.
     * @param failureConfig failure configuration(sidelining)
     */
    public MySqlSpoutConfig(MySqlConfig mysqlConfig, ZkBinLogStateConfig zkBinLogStateConfig,
                            FailureConfig failureConfig) {
        this (mysqlConfig, zkBinLogStateConfig, failureConfig,
                SpoutConstants.DEFAULT_TIMEBUCKETSIZEINSECS, SpoutConstants.DEFAULT_BUFFER_CAPACITY);
    }

    /**
     * Initialize a spout configuration with sidelining.
     * @param mysqlConfig mysql configuration.
     * @param zkBinLogStateConfig zookeeper configuration.
     * @param failureConfig failure configuration(sidelining)
     * @param metricsTimeBucketSizeInSecs time in which the metrics will be sent to the consumer
     * @param bufferCapacity the capacity of the internal queue..
     */
    public MySqlSpoutConfig(MySqlConfig mysqlConfig, ZkBinLogStateConfig zkBinLogStateConfig,
                            FailureConfig failureConfig, int metricsTimeBucketSizeInSecs,
                            int bufferCapacity) {
        this.mysqlConfig = mysqlConfig;
        this.zkBinLogStateConfig = zkBinLogStateConfig;
        this.failureConfig = failureConfig;
        this.metricsTimeBucketSizeInSecs = metricsTimeBucketSizeInSecs;
        this.bufferCapacity = bufferCapacity;
    }

    public MySqlConfig getMysqlConfig() {
        return mysqlConfig;
    }

    public ZkBinLogStateConfig getZkBinLogStateConfig() {
        return zkBinLogStateConfig;
    }

    public FailureConfig getFailureConfig() {
        return failureConfig;
    }

    public int getMetricsTimeBucketSizeInSecs() {
        return metricsTimeBucketSizeInSecs;
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    @Override
    public String toString() {
        return "MySqlSpoutConfig{" +
                "mysqlConfig=" + mysqlConfig +
                ", zkBinLogStateConfig=" + zkBinLogStateConfig +
                ", failureConfig=" + failureConfig +
                ", metricsTimeBucketSizeInSecs=" + metricsTimeBucketSizeInSecs +
                ", bufferCapacity=" + bufferCapacity +
                '}';
    }
}
