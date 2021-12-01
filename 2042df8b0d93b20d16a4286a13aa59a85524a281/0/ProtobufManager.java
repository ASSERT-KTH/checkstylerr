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

package org.apache.servicecomb.codec.protobuf.definition;

import org.apache.servicecomb.codec.protobuf.utils.ScopedProtobufSchemaManager;
import org.apache.servicecomb.core.definition.OperationMeta;

public final class ProtobufManager {
  public static final String EXT_ID = "protobuf";

  private static final Object LOCK = new Object();

  public static OperationProtobuf getOrCreateOperation(OperationMeta operationMeta) throws Exception {
    OperationProtobuf operationProtobuf = operationMeta.getExtData(EXT_ID);
    if (operationProtobuf == null) {
      synchronized (LOCK) {
        operationProtobuf = operationMeta.getExtData(EXT_ID);
        if (operationProtobuf == null) {
          operationProtobuf = new OperationProtobuf(ScopedProtobufSchemaManager.INSTANCE, operationMeta);
          operationMeta.putExtData(EXT_ID, operationProtobuf);
        }
      }
    }

    return operationProtobuf;
  }
}
