/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.couchbase.client.deps.io.netty.handler.codec.memcache;

import io.netty.handler.codec.DecoderResult;

import static com.couchbase.client.core.lang.backport.java.util.Objects.requireNonNull;

/**
 * The default {@link MemcacheObject} implementation.
 */
public abstract class AbstractMemcacheObject implements MemcacheObject {

    private DecoderResult decoderResult = DecoderResult.SUCCESS;

    protected AbstractMemcacheObject() {
        // Disallow direct instantiation
    }

    @Override
    public DecoderResult getDecoderResult() {
        return decoderResult;
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        decoderResult = requireNonNull(result, "DecoderResult should not be null.");
    }

}
