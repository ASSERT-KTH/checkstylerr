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

package com.tencent.angel.ml.matrix.psf.aggr;

import com.tencent.angel.ml.matrix.psf.aggr.enhance.ScalarAggrResult;
import com.tencent.angel.ml.matrix.psf.aggr.enhance.ScalarPartitionAggrResult;
import com.tencent.angel.ml.matrix.psf.aggr.enhance.UnaryAggrFunc;
import com.tencent.angel.ml.matrix.psf.get.base.GetResult;
import com.tencent.angel.ml.matrix.psf.get.base.PartitionGetResult;
import com.tencent.angel.ps.impl.matrix.ServerDenseDoubleRow;
import com.tencent.angel.ps.impl.matrix.ServerSparseDoubleLongKeyRow;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import java.nio.DoubleBuffer;
import java.util.List;
import java.util.Map;

/**
 * `Sum` will return sum of the `rowId` row in `matrixId` matrix.
 * Row is a Array of double, and `Sum` is \sum { row(i) }
 */
public final class Sum extends UnaryAggrFunc {

  public Sum(int matrixId, int rowId) {
    super(matrixId, rowId);
  }

  public Sum() {
    super();
  }

  @Override
  protected double doProcessRow(ServerDenseDoubleRow row) {
    double sum = 0.0;
    DoubleBuffer data = row.getData();
    int size = row.size();
    for (int i = 0; i < size; i++) {
      sum += data.get(i);
    }
    return sum;
  }

  @Override
  protected double doProcessRow(ServerSparseDoubleLongKeyRow row) {
    long entireSize = row.getEndCol() - row.getStartCol();

    Long2DoubleOpenHashMap data = row.getData();
    double asum = 0.0;
    for (Map.Entry<Long, Double> entry: data.long2DoubleEntrySet()) {
      asum += entry.getValue();
    }
    // TODO: Have to deal with default values
    assert (data.defaultReturnValue() == 0.0);
    //asum += data.defaultReturnValue() * (entireSize - data.size());
    return asum;
  }

  @Override
  public GetResult merge(List<PartitionGetResult> partResults) {
    double sum = 0.0;
    for (PartitionGetResult partResult : partResults) {
      if (partResult != null) {
        sum += ((ScalarPartitionAggrResult) partResult).result;
      }
    }

    return new ScalarAggrResult(sum);
  }

}
