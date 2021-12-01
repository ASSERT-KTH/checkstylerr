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
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.angel.ml.matrix;

import com.tencent.angel.conf.MatrixConf;
import com.tencent.angel.model.output.format.ModelFilesConstent;
import com.tencent.angel.model.output.format.ModelFilesMeta;
import com.tencent.angel.ps.LongKeyPartitioner;
import com.tencent.angel.ps.PSPartitioner;
import com.tencent.angel.ps.Partitioner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * MatrixContext is used for user to set Matrix information.
 */
public class MatrixContext {
  private final static Log LOG = LogFactory.getLog(MatrixContext.class);

  /** Matrix readable name */
  private String name;

  /** Number of rows for this matrix */
  private int rowNum;

  /** Number of cols for this matrix */
  private long colNum;

  /** Number of rows for one block */
  private int maxRowNumInBlock;

  /** Number of cols for one block */
  private long maxColNumInBlock;

  /** Partitioner for this matrix  */
  private Class<? extends Partitioner> partitionerClass;

  /** Row type */
  private RowType rowType;

  /** Others key value attributes for this matrix. */
  private Map<String, String> attributes;

  /** Matrix id */
  private int matrixId;

  /**
   * Creates a new MatrixContext by default.
   */
  public MatrixContext() {
    this("", -1, -1, -1, -1, RowType.T_DOUBLE_DENSE);
  }

  /**
   * Create a new MatrixContext
   * @param name matrix name
   * @param rowNum matrix row number
   * @param colNum matrix column number
   */
  public MatrixContext(String name, int rowNum, long colNum) {
    this(name, rowNum, colNum, -1, -1, RowType.T_DOUBLE_DENSE);
  }

  /**
   * Create a new MatrixContext
   * @param name matrix name
   * @param rowNum matrix row number
   * @param colNum matrix column number
   * @param maxRowNumInBlock matrix block row number
   * @param maxColNumInBlock matrix block column number
   */
  public MatrixContext(String name, int rowNum, long colNum, int maxRowNumInBlock, long maxColNumInBlock) {
    this(name, rowNum, colNum, maxRowNumInBlock, maxColNumInBlock, RowType.T_DOUBLE_DENSE);
  }

  /**
   * Create a new MatrixContext
   * @param name matrix name
   * @param rowNum matrix row number
   * @param colNum matrix column number
   * @param maxRowNumInBlock matrix block row number
   * @param maxColNumInBlock matrix block column number
   * @param rowType matrix row type
   */
  public MatrixContext(String name, int rowNum, long colNum, int maxRowNumInBlock, long maxColNumInBlock, RowType rowType) {
    this.name = name;
    this.rowNum = rowNum;
    this.colNum = colNum;
    this.maxRowNumInBlock = maxRowNumInBlock;
    this.maxColNumInBlock = maxColNumInBlock;
    this.rowType = rowType;
    this.attributes = new HashMap<>();
    this.matrixId = -1;
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets row num.
   *
   * @return the row num
   */
  public int getRowNum() {
    return rowNum;
  }

  /**
   * Gets col num.
   *
   * @return the col num
   */
  public long getColNum() {
    return colNum;
  }

  /**
   * Gets max row num in block.
   *
   * @return the max row num in block
   */
  public int getMaxRowNumInBlock() {
    return maxRowNumInBlock;
  }

  /**
   * Gets max col num in block.
   *
   * @return the max col num in block
   */
  public long getMaxColNumInBlock() {
    return maxColNumInBlock;
  }

  /**
   * Gets partitioner.
   *
   * @return the partitioner
   */
  public Class<? extends Partitioner> getPartitionerClass() {
    return partitionerClass;
  }

  /**
   * Gets row type.
   *
   * @return the row type
   */
  public RowType getRowType() {
    return rowType;
  }

  /**
   * Gets attributes.
   *
   * @return the attributes
   */
  public Map<String, String> getAttributes() {
    return attributes;
  }

  /**
   * Set matrix id
   * @param matrixId
   */
  public void setMatrixId(int matrixId) {
    this.matrixId = matrixId;
  }

  /**
   * Sets name.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets row num.
   *
   * @param rowNum the row num
   */
  public void setRowNum(int rowNum) {
    this.rowNum = rowNum;
  }

  /**
   * Sets col num.
   *
   * @param colNum the col num
   */
  public void setColNum(long colNum) {
    this.colNum = colNum;
  }

  /**
   * Sets max row num in block.
   *
   * @param maxRowNumInBlock the max row num in block
   */
  public void setMaxRowNumInBlock(int maxRowNumInBlock) {
    this.maxRowNumInBlock = maxRowNumInBlock;
  }

  /**
   * Sets max col num in block.
   *
   * @param maxColNumInBlock the max col num in block
   */
  public void setMaxColNumInBlock(long maxColNumInBlock) {
    this.maxColNumInBlock = maxColNumInBlock;
  }

  /**
   * Sets partitioner.
   *
   * @param partitionerClass the partitioner class
   */
  public void setPartitionerClass(Class<? extends Partitioner> partitionerClass) {
    this.partitionerClass = partitionerClass;
  }

  /**
   * Sets row type.
   *
   * @param rowType the row type
   */
  public void setRowType(RowType rowType) {
    this.rowType = rowType;
  }

  /**
   * Set matrix op log type
   * @param type op log type
   */
  public void setMatrixOpLogType(MatrixOpLogType type) {
    attributes.put(MatrixConf.MATRIX_OPLOG_TYPE, type.name());
  }

  /**
   * Set matrix context.
   *
   * @param key   the key
   * @param value the value
   * @return the matrix context
   */
  public MatrixContext set(String key, String value) {
    attributes.put(key, value);
    return this;
  }

  private void initPartitioner() {
    if(partitionerClass != null) {
      return;
    }

    if(rowType == RowType.T_DOUBLE_SPARSE_LONGKEY) {
      partitionerClass = LongKeyPartitioner.class;
    } else {
      partitionerClass = PSPartitioner.class;
    }
  }

  private String checkMatrixParams() {
    StringBuilder sb = new StringBuilder();
    if(name == null || name.isEmpty()) {
      sb.append("matrix name must not be empty");
      sb.append("\n");
    }
    if(rowNum <= 0 || rowNum > Integer.MAX_VALUE) {
      sb.append("matrix row number must > 0 and <= " + Integer.MAX_VALUE + ", but is ").append(rowNum);
      sb.append("\n");
    }
    if(rowNum > 0 && maxRowNumInBlock > rowNum) {
      sb.append("matrix block row number must > 0 and < ").append(rowNum).append(", but is ").append(maxRowNumInBlock);
      sb.append("\n");
    }

    if(rowType != RowType.T_DOUBLE_SPARSE_LONGKEY)  {
      if(colNum <= 0 || colNum > Integer.MAX_VALUE) {
        sb.append("matrix column number must > 0 and <= " + Integer.MAX_VALUE + ", but is ").append(colNum);
        sb.append("\n");
      }
    }

    if(colNum > 0 && maxColNumInBlock > colNum) {
      sb.append("matrix block column number must > 0 and < ").append(colNum).append(", but is ").append(maxColNumInBlock);
    }
    return sb.toString();
  }

  /**
   * Get matrix id
   * @return matrix id
   */
  public int getMatrixId() {
    return matrixId;
  }

  /**
   * Init matrix
   * @param conf
   * @throws IOException
   */
  public void init(Configuration conf) throws IOException {
    initPartitioner();
    String loadPath = attributes.get(MatrixConf.MATRIX_LOAD_PATH);
    if(loadPath != null) {
      loadMatrixMetaFromFile(name, loadPath, conf);
    }
  }

  private void loadMatrixMetaFromFile(String name, String path, Configuration conf) throws IOException {
    Path meteFilePath = new Path(new Path(path, name), ModelFilesConstent.modelMetaFileName);
    ModelFilesMeta meta = new ModelFilesMeta();

    FileSystem fs  = meteFilePath.getFileSystem(conf);
    LOG.info("Load matrix meta for matrix " + name + " from " + path);

    if(!fs.exists(meteFilePath)) {
      throw new IOException("matrix meta file does not exist ");
    }

    FSDataInputStream input = fs.open(meteFilePath);
    meta.read(input);
    input.close();

    rowNum = meta.getRow();
    colNum = meta.getCol();
    maxRowNumInBlock = meta.getBlockRow();
    maxColNumInBlock = meta.getBlockCol();
    rowType = RowType.valueOf(meta.getRowType());
    Map<String, String> oldAttributes = meta.getOptions();
    if(oldAttributes != null && !oldAttributes.isEmpty()) {
      for(Map.Entry<String, String> kv : oldAttributes.entrySet()) {
        attributes.putIfAbsent(kv.getKey(), kv.getValue());
      }
    }
  }

  @Override public String toString() {
    return "MatrixContext{" + "name='" + name + '\'' + ", rowNum=" + rowNum + ", colNum=" + colNum
      + ", maxRowNumInBlock=" + maxRowNumInBlock + ", maxColNumInBlock=" + maxColNumInBlock
      + ", partitionerClass=" + partitionerClass + ", rowType=" + rowType + ", attributes="
      + attributes + ", matrixId=" + matrixId + '}';
  }
}
