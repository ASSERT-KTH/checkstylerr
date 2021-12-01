/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.store.model;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.drill.common.exceptions.DrillRuntimeException;
import org.apache.drill.common.exceptions.ExecutionSetupException;
import org.apache.drill.common.expression.FieldReference;
import org.apache.drill.common.expression.SchemaPath;
import org.apache.drill.common.types.TypeProtos;
import org.apache.drill.common.types.Types;
import org.apache.drill.exec.expr.holders.VarCharHolder;
import org.apache.drill.exec.memory.OutOfMemoryException;
import org.apache.drill.exec.ops.FragmentContext;
import org.apache.drill.exec.ops.OperatorContext;
import org.apache.drill.exec.physical.impl.OutputMutator;
import org.apache.drill.exec.record.MaterializedField;
import org.apache.drill.exec.store.AbstractRecordReader;
import org.apache.drill.exec.store.dfs.DrillFileSystem;
import org.apache.drill.exec.vector.RepeatedVarCharVector;
import org.apache.drill.exec.vector.complex.impl.VectorContainerWriter;
import org.apache.drill.exec.vector.complex.writer.VarCharWriter;
import org.apache.hadoop.fs.Path;

public class DrillModelReader extends AbstractRecordReader {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DrillModelReader.class);
	static final String COL_NAME = "columns"; //TODO: Shadi this can be changed to "model"
	
	private FieldReference ref = new FieldReference(COL_NAME);
	private OutputMutator mutator;
	//  private VectorContainerWriter writer;
	private VarCharWriter writer;
	private RepeatedVarCharVector vector;
	private Path hadoopPath;
	private String inputPath;
	private BufferedInputStream stream;
	private DrillFileSystem fileSystem;
	//  private JsonReader jsonReader;
	private int recordCount;
	private FragmentContext fragmentContext;
	private OperatorContext operatorContext;
	private List<SchemaPath> columns;
	//  private boolean enableAllTextMode;
	


	public DrillModelReader(FragmentContext fragmentContext, String inputPath, DrillFileSystem fileSystem,
			List<SchemaPath> columns) throws OutOfMemoryException {
		this.inputPath = inputPath;
		this.hadoopPath = new Path(inputPath);
		this.fileSystem = fileSystem;
		this.fragmentContext = fragmentContext;
		this.columns = columns;
		//    this.enableAllTextMode = fragmentContext.getOptions().getOption(ExecConstants.JSON_ALL_TEXT_MODE).bool_val;
	}

	@Override
	public void setup(OperatorContext context, OutputMutator output) throws ExecutionSetupException {
		try{
			//      CompressionCodecFactory factory = new CompressionCodecFactory(new Configuration());
			//      CompressionCodec codec = factory.getCodec(hadoopPath); // infers from file ext.
			//      if (codec != null) {
			//        this.stream = codec.createInputStream(fileSystem.open(hadoopPath));
			//      } else {
			InputStream fis = fileSystem.open(hadoopPath);
			this.stream = new BufferedInputStream(fis); 
//			logger.info("shadi: hadooppath: "+hadoopPath);
			//      }

			this.writer = new VectorContainerWriter(output);
			this.mutator = output;
			 
			 MaterializedField field = MaterializedField.create(ref, Types.repeated(TypeProtos.MinorType.VARCHAR));
		    try {
		      vector = output.addField(field, RepeatedVarCharVector.class);
		    } catch (Exception e) {
		      throw new DrillRuntimeException("Failure in setting up reader", e);
		    }
			 
			//      this.jsonReader = new JsonReader(fragmentContext.getManagedBuffer(), columns, enableAllTextMode);
			//      this.jsonReader.setSource(stream);
		}catch(Exception e){
			throw new DrillRuntimeException("Failure reading Model file.", e);
		}
	}
	

	public OperatorContext getOperatorContext() {
		return operatorContext;
	}

	public void setOperatorContext(OperatorContext operatorContext) {
		this.operatorContext = operatorContext;
	}

	@Override
	public int next() {

		try {
			if(stream.available()==0)
				return 0;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		VarCharHolder holder = new VarCharHolder();
		try {
			byte[] outbuff = new byte[stream.available()];

			stream.read(outbuff, 0, outbuff.length);


				vector.allocateNewSafe();
				
//				vector.getMutator().addSafe(recordCount, outbuff);
				
				vector.getMutator().addSafe(recordCount, this.inputPath.getBytes());

				vector.getMutator().setValueCount(recordCount);
				
				recordCount++;

			if (recordCount == 0) {
				throw new IOException("Record was too large to copy into vector.");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return recordCount;
		
	}

	@Override
	public void close() {
		try {
			stream.close();
		} catch (IOException e) {
			logger.warn("Failure while closing stream. {}", e);
		}
	}


}
















///**
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.apache.drill.exec.store.model;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//
//import javax.annotation.Nullable;
//
//import com.google.common.base.Predicate;
//import com.google.common.collect.Iterables;
//
//import org.apache.drill.common.exceptions.DrillRuntimeException;
//import org.apache.drill.common.exceptions.ExecutionSetupException;
//import org.apache.drill.common.expression.FieldReference;
//import org.apache.drill.common.expression.SchemaPath;
//import org.apache.drill.common.types.TypeProtos;
//import org.apache.drill.common.types.Types;
//import org.apache.drill.exec.ExecConstants;
//import org.apache.drill.exec.exception.SchemaChangeException;
//import org.apache.drill.exec.ops.FragmentContext;
//import org.apache.drill.exec.ops.OperatorContext;
//import org.apache.drill.exec.physical.impl.OutputMutator;
//import org.apache.drill.exec.record.MaterializedField;
//import org.apache.drill.exec.store.AbstractRecordReader;
//import org.apache.drill.exec.vector.RepeatedVarCharVector;
//import org.apache.drill.exec.vector.ValueVector;
//import org.apache.hadoop.io.LongWritable;
//import org.apache.hadoop.io.Text;
//import org.apache.hadoop.mapred.FileSplit;
//import org.apache.hadoop.mapred.JobConf;
//import org.apache.hadoop.mapred.Reporter;
//import org.apache.hadoop.mapred.TextInputFormat;
//
//import com.google.common.base.Preconditions;
//import com.google.common.collect.Lists;
//
//public class DrillModelReader extends AbstractRecordReader {
//  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DrillModelReader.class);
//
//  static final String COL_NAME = "columns"; //TODO: Shadi this can be changed to "model"
//
//  private org.apache.hadoop.mapred.RecordReader<LongWritable, Text> reader;
//  private List<ValueVector> vectors = Lists.newArrayList();
////  private byte delimiter;
////  private int targetRecordCount;
//  private FieldReference ref = new FieldReference(COL_NAME);
////  private FragmentContext fragmentContext;
//  private OperatorContext operatorContext;
//  private RepeatedVarCharVector vector;
//  private List<Integer> columnIds = Lists.newArrayList();
//  private LongWritable key;
//  private Text value;
//  private int numCols = 0;
//
////  public DrillModelReader(FileSplit split, FragmentContext context, char delimiter, List<SchemaPath> columns) {
//	public DrillModelReader(FileSplit split, FragmentContext context, List<SchemaPath> columns) {
////    this.fragmentContext = context;
////    this.delimiter = (byte) delimiter;
//    setColumns(columns);
//
//    if (!isStarQuery()) {
//      String pathStr;
//      for (SchemaPath path : columns) {
//        assert path.getRootSegment().isNamed();
//        pathStr = path.getRootSegment().getPath();
//        Preconditions.checkArgument(pathStr.equals(COL_NAME) || (pathStr.equals("*") && path.getRootSegment().getChild() == null),
//            "Selected column(s) must have name 'columns' or must be plain '*'");
//
//        if (path.getRootSegment().getChild() != null) {
//          Preconditions.checkArgument(path.getRootSegment().getChild().isArray(), "Selected column must be an array index");
//          int index = path.getRootSegment().getChild().getArraySegment().getIndex();
//          columnIds.add(index);
//        }
//      }
//      Collections.sort(columnIds);
//      numCols = columnIds.size();
//    }
////    targetRecordCount = context.getConfig().getInt(ExecConstants.TEXT_LINE_READER_BATCH_SIZE);
//
//    TextInputFormat inputFormat = new TextInputFormat();
//    JobConf job = new JobConf();
//    job.setInt("io.file.buffer.size", context.getConfig().getInt(ExecConstants.TEXT_LINE_READER_BUFFER_SIZE));
//    job.setInputFormat(inputFormat.getClass());
//    try {
//      reader = inputFormat.getRecordReader(split, job, Reporter.NULL);
//      key = reader.createKey();
//      value = reader.createValue();
//      logger.info("shadi: key/value"+key+"/"+value);
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    }
//  }
//
//  @Override
//  public boolean isStarQuery() {
//    return super.isStarQuery() || Iterables.tryFind(getColumns(), new Predicate<SchemaPath>() {
//      private final SchemaPath COLUMNS = SchemaPath.getSimplePath("columns");
//      @Override
//      public boolean apply(@Nullable SchemaPath path) {
//        return path.equals(COLUMNS);
//      }
//    }).isPresent();
//  }
//
//  public OperatorContext getOperatorContext() {
//    return operatorContext;
//  }
//
//  public void setOperatorContext(OperatorContext operatorContext) {
//    this.operatorContext = operatorContext;
//  }
//
//  @Override
//  public void setup(OutputMutator output) throws ExecutionSetupException {
//    MaterializedField field = MaterializedField.create(ref, Types.repeated(TypeProtos.MinorType.VARCHAR));
//    try {
//      vector = output.addField(field, RepeatedVarCharVector.class);
////      logger.info("shadi: field:"+field.toString());
////      byte[] tmp= new byte[vector.getData().capacity()];
////      vector.getData().getBytes(0, tmp, 0,tmp.length);
////      
////      logger.info("shadi: vector:"+new String(tmp, com.google.common.base.Charsets.UTF_8));
////      logger.info("shadi: output:"+output.toString());
//    } catch (SchemaChangeException e) {
//      throw new ExecutionSetupException(e);
//    }
//  }
//
//  @Override
//  public int next() {
////    logger.debug("vector value capacity {}", vector.getValueCapacity());
////    logger.debug("vector byte capacity {}", vector.getByteCapacity());
//    int batchSize = 0;
//    try {
//      int recordCount = 0;
//      vector.getMutator().startNewGroup(0);
//      byte[] finalValue = new byte[0];
//      while (recordCount < Character.MAX_VALUE && batchSize < 200*1000 && reader.next(key, value)) {
//    	  
//    	  byte[] tmp;
//    	  logger.info("shadi: in-loop: value "+new String(value.getBytes(), com.google.common.base.Charsets.UTF_8));
//    	  if(finalValue.length>0){
//	    	  ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
//	          outputStream.write( finalValue );
//	          outputStream.write( value.getBytes() );
//	          tmp = outputStream.toByteArray( );       
//    	  } else {
//    		  tmp = value.getBytes();         
//    	  }
//    	  finalValue = new byte[tmp.length];
//          finalValue = tmp;
//          logger.info("shadi: in-loop: "+new String(finalValue, com.google.common.base.Charsets.UTF_8));
//          
//          vector.getMutator().addSafe(recordCount, value.getBytes(), start + 1, end - start - 1);
//          
////          vector.getMutator().addSafe(recordCount, value.getBytes(), start + 1, end - start - 1);
////          batchSize += end - start;
//          
////        int start;
////        int end = value.getLength();//-1;
////
////        // index of the scanned field
////        int p = 0;
////        int i = 0;
//////        vector.getMutator().startNewGroup(recordCount);
////        // Process each field in this line
////        while (end < value.getLength() - 1) {
////          if(numCols > 0 && p >= numCols) {
////            break;
////          }
////          start = end;
//////          if (delimiter == '\n') {
//////            end = value.getLength();
//////          } else {
//////            end = find(value, delimiter, start + 1);
//////            if (end == -1) {
//////              end = value.getLength();
//////            }
//////          }
////          if (numCols > 0 && i++ < columnIds.get(p)) {
////            vector.getMutator().addSafe(recordCount, value.getBytes(), start + 1, 0);
////            continue;
////          }
////          p++;
//////          byte[] tmp = finalValue;
//////          byte[] newLine = value.getBytes();
////          
//////          finalValue = new byte[tmp.length+ end - start - 1];
//////          System.arraycopy(tmp, 0, finalValue, 0, tmp.length);
//////          System.arraycopy(newLine, 0, finalValue, tmp.length, newLine.length);
////          
////          ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
////          outputStream.write( finalValue );
////          outputStream.write( value.getBytes() );
////
////          finalValue = outputStream.toByteArray( );
////          
//////          vector.getMutator().addSafe(recordCount, value.getBytes(), start + 1, end - start - 1);
////          batchSize += end - start;
////        }
//          batchSize = finalValue.length;
//        recordCount++;
//      }
//      
//      recordCount++;
//      
//      vector.getMutator().addSafe(0, finalValue, 0, finalValue.length);
//      
//      for (ValueVector v : vectors) {
//        v.getMutator().setValueCount(recordCount);
//      }
//      vector.getMutator().setValueCount(recordCount);
//      
//      logger.debug("text scan batch size {}", batchSize);
//      return recordCount;
//    } catch (IOException e) {
//      cleanup();
//      throw new DrillRuntimeException(e);
//    }
//  }
//
//  /**
//   * Returns the index within the text of the first occurrence of delimiter, starting the search at the specified index.
//   *
//   * @param  text  the text being searched
//   * @param  delimiter the delimiter
//   * @param  start the index to start searching
//   * @return      the first occurrence of delimiter, starting the search at the specified index
//   */
////  public int find(Text text, byte delimiter, int start) {
////    int len = text.getLength();
////    int p = start;
////    byte[] bytes = text.getBytes();
////    boolean inQuotes = false;
////    while (p < len) {
////      if ('\"' == bytes[p]) {
////        inQuotes = !inQuotes;
////      }
////      if (!inQuotes && bytes[p] == delimiter) {
////        return p;
////      }
////      p++;
////    }
////    return -1;
////  }
//
//  @Override
//  public void cleanup() {
//    try {
//      reader.close();
//    } catch (IOException e) {
//      logger.warn("Exception closing reader: {}", e);
//    }
//  }
//}
