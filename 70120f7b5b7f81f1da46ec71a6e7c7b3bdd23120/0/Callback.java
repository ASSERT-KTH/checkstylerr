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

package org.apache.shardingsphere.infra.callback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Meta data call back enum.
 */
public final class Callback<T> {

    private final List<BiConsumer<String, T>> consumers = new ArrayList<>();

    /**
     * Register rule schema metaData consumer.
     *
     * @param consumer consumer
     */
    public void register(final BiConsumer<String, T> consumer) {
        consumers.add(consumer);
    }

    /**
     * Run rule schema metaData persist to metaData center.
     *
     * @param schemaName schemaName
     * @param ruleSchemaMetaData rule schema metaData
     */
    public void run(final String schemaName, final T ruleSchemaMetaData) {
        for (BiConsumer<String, T> each : consumers) {
            each.accept(schemaName, ruleSchemaMetaData);
        }
    }
}