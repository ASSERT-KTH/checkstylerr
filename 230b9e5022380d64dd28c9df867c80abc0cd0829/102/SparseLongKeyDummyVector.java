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
 *
 */

package com.tencent.angel.ml.math.vector;

import com.tencent.angel.ml.math.TAbstractVector;
import com.tencent.angel.ml.math.TVector;
import com.tencent.angel.ml.matrix.RowType;

/**
 * Sparse double vector with long key, it only contains the element indexes as the values are always 1.
 */
public class SparseLongKeyDummyVector extends TLongDoubleVector {
  /**
   * The size of alloc more
   */
  private final static int ALLOC_MORE_SIZE = 256;

  /**
   * The init size of index array
   */
  private final static int INIT_ALLOC_SIZE = 128;

  /**
   * The index array
   */
  long[] indices = null;

  /**
   * The capacity of the vector
   */
  int capacity = -1;

  /**
   * Nonzeor element number
   */
  int nonzero = -1;

  /**
   * Init the empty vector
   */
  public SparseLongKeyDummyVector() {
    this(-1);
  }

  /**
   * Init the vector with the vector dimension
   *
   * @param dim vector dimension
   */
  public SparseLongKeyDummyVector(int dim) {
    this(dim, INIT_ALLOC_SIZE);
  }

  /**
   * Init the vector with the vector dimension and index array capacity
   *
   * @param dim vector dimension
   * @param capacity index array capacity
   */
  public SparseLongKeyDummyVector(long dim, int capacity) {
    super(dim);
    this.capacity = capacity;
    this.nonzero = 0;
    this.indices = new long[capacity];
  }

  /**
   * Alloc more space for vector when the size is out of capacity
   */
  private void allocMore() {
    int allocSize = capacity + ALLOC_MORE_SIZE;
    long[] allocIndexes = new long[allocSize];
    System.arraycopy(indices, 0, allocIndexes, 0, nonzero);
    capacity = allocSize;
    indices = allocIndexes;
  }

  /**
   * clone the vector
   *
   * @return
   */
  @Override public SparseLongKeyDummyVector clone() {
    throw new UnsupportedOperationException("Unsupport operation");
  }

  @Override public double sum() {
    return nonzero * 1.0;
  }

  @Override public TLongDoubleVector elemUpdate(LongDoubleElemUpdater updater, ElemUpdateParam param) {
    throw new UnsupportedOperationException("Unsupport operation");
  }

  /**
   * get all of the index
   *
   * @return
   */
  public long[] getIndices() {
    return indices;
  }

  /**
   * get the count of nonzero element
   *
   * @return
   */
  public int getNonzero() {
    return nonzero;
  }

  /**
   * get the type
   *
   * @return
   */
  @Override public RowType getType() {
    return RowType.T_DOUBLE_SPARSE;
  }

  /**
   * get the size
   *
   * @return
   */
  @Override public int size() {
    return nonzero;
  }

  /**
   * get the sparsity
   *
   * @return
   */
  @Override public double sparsity() {
    return ((double) nonzero) / dim;
  }

  @Override public TVector plusBy(long index, double x) {
    throw new UnsupportedOperationException("Unsupport operation");
  }

  /**
   * set the value by index
   *
   * @param index the index
   * @param value the value
   */
  public void set(long index, double value) {
    if (nonzero >= indices.length) {
      allocMore();
    }
    indices[nonzero++] = index;
  }

  @Override public double get(long index) {
    throw new UnsupportedOperationException("Unsupport operation");
  }

  @Override public long[] getIndexes() {
    return indices;
  }

  @Override public double[] getValues() {
    throw new UnsupportedOperationException("Unsupport operation");
  }

  @Override public TVector plusBy(TAbstractVector other) {
    throw new UnsupportedOperationException("Unsupport operation");
  }

  @Override public TVector plusBy(TAbstractVector other, double x) {
    throw new UnsupportedOperationException("Unsupport operation");
  }

  @Override public TVector plus(TAbstractVector other) {
    throw new UnsupportedOperationException("Unsupport operation");
  }

  @Override public TVector plus(TAbstractVector other, double x) {
    throw new UnsupportedOperationException("Unsupport operation");
  }

  @Override public double dot(TAbstractVector other) {
    throw new UnsupportedOperationException("Unsupport operation");
  }

  @Override public TVector times(double x) {
    throw new UnsupportedOperationException("Unsupport operation");
  }

  @Override public TVector timesBy(double x) {
    throw new UnsupportedOperationException("Unsupport operation");
  }

  @Override public TVector filter(double x) {
    throw new UnsupportedOperationException("Unsupport operation");
  }

  @Override public void clear() {
    throw new UnsupportedOperationException("Unsupport operation");
  }

  @Override public long nonZeroNumber() {
    return nonzero;
  }

  @Override public double squaredNorm() {
    throw new UnsupportedOperationException("Unsupport operation");
  }

  @Override public double norm() {
    throw new UnsupportedOperationException("Unsupport operation");
  }
}
