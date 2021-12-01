/*
 * Tencent is pleased to support the open source community by making Angel available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/Apache-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */


package com.tencent.angel.localcluster;

import org.apache.hadoop.yarn.api.records.ApplicationId;

/**
 * Local resource manager event.
 */
public class LocalRMEvent {
  /**
   * application id
   */
  private final ApplicationId appId;

  /**
   * event type
   */
  private final LocalRMEventType type;

  /**
   * Create a LocalRMEvent
   *
   * @param appId application id
   * @param type  event type
   */
  public LocalRMEvent(ApplicationId appId, LocalRMEventType type) {
    this.appId = appId;
    this.type = type;
  }

  /**
   * Get application id
   *
   * @return application id
   */
  public ApplicationId getAppId() {
    return appId;
  }

  /**
   * Get event type
   *
   * @return event type
   */
  public LocalRMEventType getType() {
    return type;
  }

  @Override public String toString() {
    return "LocalRMEvent [appId=" + appId + ", type=" + type + "]";
  }
}
