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

package org.apache.shardingsphere.orchestration.core.registrycenter.instance;

import lombok.Getter;
import java.lang.management.ManagementFactory;
import java.util.UUID;
import org.apache.shardingsphere.orchestration.core.common.utils.IpUtils;

/**
 * Orchestration instance.
 */
@Getter
public final class OrchestrationInstance {
    
    private static final String DELIMITER = "@";

    private static OrchestrationInstance instance;
    
    private final String instanceId;

    public OrchestrationInstance(final int identifier) {
        instanceId = IpUtils.getIp() + DELIMITER + identifier + DELIMITER + UUID.randomUUID().toString();
    }

    /**
     * Get instance.
     *
     * @param   identifier  identifier on which the instance is running
     * @return  instance    the orchestration instance
     */
    public static OrchestrationInstance getInstance(final int identifier) {
        if (null == instance) {
            synchronized (OrchestrationInstance.class) {
                if (null == instance) {
                    instance = new OrchestrationInstance(identifier);
                }
            }
        }
        return instance;
    }

}
