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
import java.util.Collections;
import java.util.List;

/**
 * Zookeeper Configuration for the spout.
 */
public final class ZkBinLogStateConfig implements Serializable {

    private final List<String>  zkServers;
    private final Integer       zkPort;
    private final String        zkRoot;
    private final String        zkSpoutId;
    private final String        zkScnCommitPath;
    private final int           zkScnUpdateRateInMs;
    private final Integer       zkSessionTimeoutInMs;
    private final Integer       zkConnectionTimeoutInMs;
    private final Integer       zkRetryTimes;
    private final Integer       zkSleepMsBetweenRetries;
    private final boolean       zkIgnoreBinLogPosition;

    /**
     * The Builder class for Zookeeper configuration.
     */
    public static class Builder {

        private final String innerZkSpoutId;

        private List<String>    innerZkServers                  = null;
        private Integer         innerZkPort                     = null;
        private String          innerZkRoot                     = SpoutConstants.DEFAULT_ZKROOT;
        private int             innerZkScnUpdateRateInMs        = SpoutConstants.DEFAULT_ZK_UPDATE_RATE_MS;
        private Integer         innerZkSessionTimeoutInMs       = null;
        private Integer         innerZkConnectionTimeoutInMs    = null;
        private Integer         innerZkRetryTimes               = null;
        private Integer         innerZkSleepMsBetweenRetries    = null;
        private boolean         innerZkIgnoreBinLogPosition     = false;

        /**
         * Set mandatory spout id.
         *
         * @param zkSpoutId the spout id to be used to store bin log offsets in
         */
        public Builder(String zkSpoutId) {
            this.innerZkSpoutId = zkSpoutId;
        }

        /**
         * Set zookeeper servers to be used.
         *
         * @param zkServers the servers that need to be used.
         * @return the builder object to continue building
         */
        public Builder servers(List<String> zkServers) {
            this.innerZkServers = zkServers;
            return this;
        }

        /**
         * Set the zookeeper port.
         *
         * @param port the port that needs to be used.
         * @return the builder object to continue building
         */
        public Builder port(int port) {
            this.innerZkPort = port;
            return this;
        }

        /**
         * The root zk path to be used by the spout.
         *
         * @param zkRoot the root zk node path
         * @return the builder object to continue building
         */
        public Builder root(String zkRoot) {
            this.innerZkRoot = zkRoot;
            return this;
        }

        /**
         * The update rate(ms) after which zookeeper would be updated with the offsets.
         *
         * @param updateRate the time in ms
         * @return the builder object to continue building
         */
        public Builder updateRateInMs(int updateRate) {
            this.innerZkScnUpdateRateInMs = updateRate;
            return this;
        }

        /**
         * The session timeout.
         *
         * @param timeOut the zk session timeout in ms
         * @return the builder object to continue building
         */
        public Builder sessionTimeOutInMs(int timeOut) {
            this.innerZkSessionTimeoutInMs = timeOut;
            return this;
        }

        /**
         * The connection timeout.
         *
         * @param timeOut the zk connection timeout.
         * @return the builder object to continue building
         */
        public Builder connectionTimeOutInMs(int timeOut) {
            this.innerZkConnectionTimeoutInMs = timeOut;
            return this;
        }

        /**
         * The number of times to retry.
         *
         * @param retryTimes number of times to retry
         * @return the builder object to continue building
         */
        public Builder retryTimes(int retryTimes) {
            this.innerZkRetryTimes = retryTimes;
            return this;
        }

        /**
         * The time to sleep between each retry to zookeeper.
         *
         * @param sleepMs time in ms to sleep
         * @return the builder object to continue building
         */
        public Builder sleepMsBetweenRetries(int sleepMs) {
            this.innerZkSleepMsBetweenRetries = sleepMs;
            return this;
        }

        /**
         * Choose to ignore the bin log offsets saved in ZK if any.
         * Can be used in scenarios where rewinding is needed.
         *
         * @param ignore to ignore zk for offsets or not
         * @return the builder object to continue building
         */
        public Builder ignoreZkBinLogPosition(boolean ignore) {
            this.innerZkIgnoreBinLogPosition = ignore;
            return this;
        }

        /**
         * Build the complete object with properties that were set.
         * @return the zk bin log config object
         */
        public ZkBinLogStateConfig build() {
            ZkBinLogStateConfig zkBinLogStateConfig =  new ZkBinLogStateConfig(this);
            return zkBinLogStateConfig;
        }

    }

    private ZkBinLogStateConfig(Builder builder) {
        this.zkServers                  = builder.innerZkServers;
        this.zkPort                     = builder.innerZkPort;
        this.zkRoot                     = builder.innerZkRoot;
        this.zkSpoutId                  = builder.innerZkSpoutId;
        this.zkScnCommitPath            = SpoutConstants.ZK_SEPARATOR
                                            + this.zkRoot
                                            + SpoutConstants.ZK_SEPARATOR
                                            + this.zkSpoutId;
        this.zkScnUpdateRateInMs        = builder.innerZkScnUpdateRateInMs;
        this.zkSessionTimeoutInMs       = builder.innerZkSessionTimeoutInMs;
        this.zkConnectionTimeoutInMs    = builder.innerZkConnectionTimeoutInMs;
        this.zkRetryTimes               = builder.innerZkRetryTimes;
        this.zkSleepMsBetweenRetries    = builder.innerZkSleepMsBetweenRetries;
        this.zkIgnoreBinLogPosition     = builder.innerZkIgnoreBinLogPosition;
    }

    public List<String> getZkServers() {
        return zkServers;
    }

    public Integer getZkPort() {
        return zkPort;
    }

    public String getZkRoot() {
        return zkRoot;
    }

    public String getZkSpoutId() {
        return zkSpoutId;
    }

    public String getZkScnCommitPath() {
        return zkScnCommitPath;
    }

    public int getZkScnUpdateRateInMs() {
        return zkScnUpdateRateInMs;
    }

    public Integer getZkSessionTimeoutInMs() {
        return zkSessionTimeoutInMs;
    }

    public Integer getZkConnectionTimeoutInMs() {
        return zkConnectionTimeoutInMs;
    }

    public Integer getZkRetryTimes() {
        return zkRetryTimes;
    }

    public Integer getZkSleepMsBetweenRetries() {
        return zkSleepMsBetweenRetries;
    }

    public boolean isZkIgnoreBinLogPosition() {
        return zkIgnoreBinLogPosition;
    }
}
