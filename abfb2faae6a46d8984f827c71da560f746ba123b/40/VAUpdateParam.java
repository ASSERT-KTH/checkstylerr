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

package com.tencent.angel.ml.matrix.psf.update.enhance;

import com.tencent.angel.PartitionKey;
import com.tencent.angel.ml.matrix.psf.common.Utils;
import com.tencent.angel.psagent.PSAgentContext;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * `VAUpdateParam` is Parameter class for `VAUpdateFunc`
 */
public class VAUpdateParam extends UpdateParam {

  public static class VAPartitionUpdateParam extends PartitionUpdateParam {
    private int rowId;
    private double[] array;

    public VAPartitionUpdateParam(
        int matrixId, PartitionKey partKey, int rowId, double[] array) {
      super(matrixId, partKey, false);
      this.rowId = rowId;
      this.array = array;
    }

    public VAPartitionUpdateParam() {
      super();
    }

    @Override
    public void serialize(ByteBuf buf) {
      super.serialize(buf);
      buf.writeInt(rowId);
      buf.writeInt(array.length);
      for (int i = 0; i < array.length; i++) {
        buf.writeDouble(array[i]);
      }
    }

    @Override
    public void deserialize(ByteBuf buf) {
      super.deserialize(buf);
      rowId = buf.readInt();
      int length = buf.readInt();
      array = new double[length];
      for (int i = 0; i < length; i++) {
        array[i] = buf.readDouble();
      }
    }

    @Override
    public int bufferLen() {
      return super.bufferLen() + 8 + array.length * 8;
    }

    public int getRowId() {
      return rowId;
    }

    public double[] getArray() {
      return array;
    }

    @Override
    public String toString() {
      return "VAPartitionUpdateParam [rowId=" + rowId + ", toString()="
          + super.toString() + "]";
    }
  }

  private final int rowId;
  private final double[] array;

  public VAUpdateParam(int matrixId, int rowId, double[] array) {
    super(matrixId, false);
    this.rowId = rowId;
    this.array = array;
  }

  @Override
  public List<PartitionUpdateParam> split() {
    List<PartitionKey> partList = PSAgentContext.get()
        .getMatrixPartitionRouter()
        .getPartitionKeyList(matrixId);

    int size = partList.size();
    List<PartitionUpdateParam> partParams = new ArrayList<PartitionUpdateParam>(size);
    for (PartitionKey part : partList) {
      long colNum = part.getEndCol() - part.getStartCol();

      double[] sliceArray = new double[(int)colNum];
      for (int i = 0; i < colNum; i++) {
        sliceArray[i] = array[(int)(part.getStartCol() + i)];
      }

      partParams.add(new VAPartitionUpdateParam(matrixId, part, rowId, sliceArray));
    }

    return partParams;
  }

}
