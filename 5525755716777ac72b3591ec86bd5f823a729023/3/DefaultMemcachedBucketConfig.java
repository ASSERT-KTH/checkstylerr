/*
 * Copyright (c) 2016-2017 Couchbase, Inc.
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

import com.couchbase.client.core.env.ConfigParserEnvironment;
import com.couchbase.client.core.service.ServiceType;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.util.CharsetUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultMemcachedBucketConfig extends AbstractBucketConfig implements MemcachedBucketConfig {

    private final long rev;
    private final TreeMap<Long, NodeInfo> ketamaNodes;
    private final ConfigParserEnvironment env;

    /**
     * Creates a new {@link MemcachedBucketConfig}.
     *
     * @param env the bootstrap part of environment object.
     * @param rev the revision of the config.
     * @param name the name of the bucket.
     * @param uri the URI for this bucket.
     * @param streamingUri the streaming URI for this bucket.
     * @param nodeInfos related node information.
     * @param portInfos port info for the nodes, including services.
     */
    @JsonCreator
    public DefaultMemcachedBucketConfig(
            @JacksonInject("env")ConfigParserEnvironment env,
            @JsonProperty("rev") long rev,
            @JsonProperty("uuid") String uuid,
            @JsonProperty("name") String name,
            @JsonProperty("uri") String uri,
            @JsonProperty("streamingUri") String streamingUri,
            @JsonProperty("nodes") List<NodeInfo> nodeInfos,
            @JsonProperty("nodesExt") List<PortInfo> portInfos,
            @JsonProperty("bucketCapabilities") List<BucketCapabilities> bucketCapabilities,
            @JacksonInject("origin") String origin) {
        super(uuid, name, BucketNodeLocator.KETAMA, uri, streamingUri, nodeInfos, portInfos, bucketCapabilities, origin);
        this.env = env;
        this.rev = rev;
        this.ketamaNodes = new TreeMap<>();
        populateKetamaNodes();
    }

    @Override
    public boolean tainted() {
        return false;
    }

    @Override
    public long rev() {
        return rev;
    }

    @Override
    public BucketType type() {
        return BucketType.MEMCACHED;
    }

    @Override
    public SortedMap<Long, NodeInfo> ketamaNodes() {
        return ketamaNodes;
    }

    private void populateKetamaNodes() {
        for (NodeInfo node : nodes()) {
            if (!node.services().containsKey(ServiceType.BINARY)) {
                continue;
            }

            for (int i = 0; i < 40; i++) {
                MessageDigest md5;
                try {
                    md5 = MessageDigest.getInstance("MD5");
                    md5.update(env.memcachedHashingStrategy().hash(node, i).getBytes(CharsetUtil.UTF_8));
                    byte[] digest = md5.digest();
                    for (int j = 0; j < 4; j++) {
                        Long key = ((long) (digest[3 + j * 4] & 0xFF) << 24)
                            | ((long) (digest[2 + j * 4] & 0xFF) << 16)
                            | ((long) (digest[1 + j * 4] & 0xFF) << 8)
                            | (digest[j * 4] & 0xFF);
                        ketamaNodes.put(key, node);
                    }
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalStateException("Could not populate ketama nodes.", e);
                }
            }
        }
    }

    @Override
    public String nodeForId(final byte[] id) {
        long hash = calculateKetamaHash(id);

        if (!ketamaNodes.containsKey(hash)) {
            SortedMap<Long, NodeInfo> tailMap = ketamaNodes.tailMap(hash);
            if (tailMap.isEmpty()) {
                hash = ketamaNodes.firstKey();
            } else {
                hash = tailMap.firstKey();
            }
        }

        return ketamaNodes.get(hash).hostname();
    }

    @Override
    public boolean hasFastForwardMap() {
        return false;
    }

    /**
     * Calculates the ketama hash for the given key.
     *
     * @param key the key to calculate.
     * @return the calculated hash.
     */
    private static long calculateKetamaHash(final byte[] key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(key);
            byte[] digest = md5.digest();
            long rv = ((long) (digest[3] & 0xFF) << 24)
                    | ((long) (digest[2] & 0xFF) << 16)
                    | ((long) (digest[1] & 0xFF) << 8)
                    | (digest[0] & 0xFF);
            return rv & 0xffffffffL;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not encode ketama hash.", e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultMemcachedBucketConfig{");
        sb.append("rev=").append(rev);
        sb.append(", ketamaNodes=").append(ketamaNodes);
        sb.append('}');
        return sb.toString();
    }
}
