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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.SortedMap;

/**
 * A configuration representing a memcached bucket.
 */
@JsonDeserialize(as = DefaultMemcachedBucketConfig.class)
public interface MemcachedBucketConfig extends BucketConfig {

    SortedMap<Long, NodeInfo> ketamaNodes();

    /**
     * Returns the target node address for the given document id.
     *
     * @param id the id for the document.
     * @return the node address for the given document id.
     */
    String nodeForId(final byte[] id);

}
