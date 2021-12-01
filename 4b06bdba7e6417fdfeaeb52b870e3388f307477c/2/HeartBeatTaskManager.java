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

package org.apache.shardingsphere.cluster.heartbeat.task;

import com.google.common.base.Preconditions;

import java.util.Timer;

/**
 * Heart beat task manager.
 */
public final class HeartBeatTaskManager {
    
    private static final String TIMER_NAME = "ShardingSphere-Cluster-HeartBeat";
    
    private Integer interval;
    
    private final Timer timer;
    
    public HeartBeatTaskManager(final Integer interval) {
        this.interval = interval;
        timer = new Timer(TIMER_NAME);
    }
    
    /**
     * Start heart beat task.
     *
     * @param heartBeatTask heart beat task
     */
    public void start(final HeartBeatTask heartBeatTask) {
        Preconditions.checkNotNull(heartBeatTask, "task can not be null");
        timer.schedule(heartBeatTask, interval*1000, interval*1000);
    }
}
