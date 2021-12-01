/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.protocol.record.value;

import io.zeebe.protocol.record.RecordValueWithVariables;
import io.zeebe.protocol.record.intent.MessageIntent;

/**
 * Represents a message event or command.
 *
 * <p>See {@link MessageIntent} for intents.
 */
public interface MessageRecordValue extends RecordValueWithVariables {
  /** @return the name of the message */
  String getName();

  /** @return the correlation key of the message */
  String getCorrelationKey();

  /**
   * The ID of a message is an optional field which is used to make messages unique and prevent
   * publishing the same message twice during its lifetime.
   *
   * @return the id of the message
   */
  String getMessageId();

  /** @return the time to live of the message */
  long getTimeToLive();

  /**
   * @return the unix timestamp in milliseconds until when the message can be correlated. If the
   *     deadline is exceeded then the message expires and will be removed. If this property is not
   *     set, it returns -1 instead.
   */
  long getDeadline();
}
