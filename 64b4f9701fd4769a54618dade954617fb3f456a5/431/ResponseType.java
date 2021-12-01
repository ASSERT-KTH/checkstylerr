/*
 * Tencent is pleased to support the open source community by making Angel available.
 * 
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * 
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * https://opensource.org/licenses/BSD-3-Clause
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.tencent.angel.ml.matrix.transport;

import java.util.HashMap;
import java.util.Map;

public enum ResponseType {
  SUCCESS(1), FAILED(2), FATAL(3), NOTREADY(4);

  public static Map<Integer, ResponseType> typeIdToTypeMap;
  static {
    typeIdToTypeMap = new HashMap<Integer, ResponseType>();
    typeIdToTypeMap.put(SUCCESS.typeId, SUCCESS);
    typeIdToTypeMap.put(FAILED.typeId, FAILED);
    typeIdToTypeMap.put(FATAL.typeId, FATAL);
    typeIdToTypeMap.put(NOTREADY.typeId, NOTREADY);
  }


  public static ResponseType valueOf(int id) {
    return typeIdToTypeMap.get(id);
  }

  private final int typeId;

  ResponseType(int methodId) {
    this.typeId = methodId;
  }

  public int getTypeId() {
    return typeId;
  }
}
