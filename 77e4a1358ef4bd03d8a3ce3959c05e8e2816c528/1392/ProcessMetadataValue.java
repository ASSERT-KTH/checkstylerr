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
package io.zeebe.protocol.record.value.deployment;

import io.zeebe.protocol.record.RecordValue;

/** Represents deployed process meta data, so all important properties of an deployed process. */
public interface ProcessMetadataValue extends RecordValue {
  /** @return the bpmn process ID of this process */
  String getBpmnProcessId();

  /** @return the version of this process */
  int getVersion();

  /** @return the key of this process */
  long getProcessDefinitionKey();

  /** @return the name of the resource through which this process was deployed */
  String getResourceName();

  /** @return the checksum of the process (MD5) */
  byte[] getChecksum();
}
