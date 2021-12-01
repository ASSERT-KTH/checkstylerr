/*
 * Copyright (c) 2016 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.couchbase.client.core.config;

import com.couchbase.client.core.utils.NetworkAddress;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of a {@link ClusterConfig}.
 *
 * @author Michael Nitschinger
 * @since 1.0
 */
public class DefaultClusterConfig implements ClusterConfig {

    /**
     * Holds all current bucket configurations.
     */
    private final Map<String, BucketConfig> bucketConfigs;

    /**
     * Creates a new {@link DefaultClusterConfig}.
     */
    public DefaultClusterConfig() {
        bucketConfigs = new ConcurrentHashMap<String, BucketConfig>();
    }

    @Override
    public BucketConfig bucketConfig(final String bucketName) {
        return bucketConfigs.get(bucketName);
    }

    @Override
    public void setBucketConfig(final String bucketName, final BucketConfig config) {
        bucketConfigs.put(bucketName, config);
    }

    @Override
    public void deleteBucketConfig(String bucketName) {
        bucketConfigs.remove(bucketName);
    }

    @Override
    public boolean hasBucket(final String bucketName) {
        return bucketConfigs.containsKey(bucketName);
    }

    @Override
    public Map<String, BucketConfig> bucketConfigs() {
        return bucketConfigs;
    }

    @Override
    public Set<NetworkAddress> allNodeAddresses() {
        Set<NetworkAddress> nodes = new HashSet<NetworkAddress>();
        for (BucketConfig bc : bucketConfigs().values()) {
            for (NodeInfo ni : bc.nodes()) {
                nodes.add(ni.hostname());
            }
        }
        return nodes;
    }

    @Override
    public String toString() {
        return "DefaultClusterConfig{" +
            "bucketConfigs=" + bucketConfigs +
            '}';
    }
}
