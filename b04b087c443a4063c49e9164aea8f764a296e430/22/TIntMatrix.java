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

package com.tencent.angel.ml.math.matrix;

import com.tencent.angel.ml.math.vector.TIntVector;

/**
 * The int base matrix.
 */
public abstract class TIntMatrix extends RowbaseMatrix {
  /**
   * Create a new int matrix.
   *
   * @param row the row number
   * @param col the column number
   */
  public TIntMatrix(int row, int col) {
    super(row, col);
  }

  /**
   * Plus specified element by value.
   *
   * @param rowIndex the row index
   * @param colIndex the column index
   * @param value the value increment value
   */
  public TIntMatrix plusBy(int rowIndex, int colIndex, int value){
    if (vectors[rowIndex] == null) {
      vectors[rowIndex] = initVector(rowIndex);
    }
    ((TIntVector)vectors[rowIndex]).plusBy(colIndex, value);
    return this;
  }

  /**
   * Plus specified elements by a value list.
   * @param rowIndexes row indexes
   * @param colIndexes column indexes
   * @param values values
   */
  public TIntMatrix plusBy(int[] rowIndexes, int[] colIndexes, int[] values){
    assert (rowIndexes.length == colIndexes.length && rowIndexes.length == values.length);
    for(int i = 0; i < rowIndexes.length; i++) {
      if(vectors[rowIndexes[i]] == null) {
        vectors[rowIndexes[i]] = initVector(rowIndexes[i]);
      }

      ((TIntVector)vectors[rowIndexes[i]]).plusBy(colIndexes[i], values[i]);
    }
    return this;
  }

  /**
   * Plus specified elements in a row by a value list.
   * @param rowIndex row index
   * @param colIndexes column indexes
   * @param values values
   */
  public TIntMatrix plusBy(int rowIndex, int[] colIndexes, int[] values) {
    assert (colIndexes.length == values.length);
    if(vectors[rowIndex] == null) {
      vectors[rowIndex] = initVector(rowIndex);
    }

    for(int i = 0; i < colIndexes.length; i++) {
      ((TIntVector)vectors[rowIndex]).plusBy(colIndexes[i], values[i]);
    }
    return this;
  }

  /**
   * Get value.
   *
   * @param rowIndex the row index
   * @param colIndex the column index
   * @return the value
   */
  public int get(int rowIndex, int colIndex) {
    if (vectors[rowIndex] == null)
      return 0;
    return ((TIntVector)vectors[rowIndex]).get(colIndex);
  }
}
