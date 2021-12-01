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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.apache.drill.exec.expr.holders.NullableVarCharHolder;
import org.apache.drill.exec.memory.BufferAllocator;
import org.apache.drill.exec.record.BatchSchema;
import org.apache.drill.exec.record.VectorAccessible;
import org.apache.drill.exec.store.EventBasedRecordWriter.FieldConverter;
import org.apache.drill.exec.store.StringOutputRecordWriter.NullableVarCharStringFieldConverter;
import org.apache.drill.exec.store.StringOutputRecordWriter.VarCharStringFieldConverter;
import org.apache.drill.exec.store.AbstractRecordWriter;
import org.apache.drill.exec.store.RecordWriter;
import org.apache.drill.exec.store.StringOutputRecordWriter;
import org.apache.drill.exec.vector.complex.reader.FieldReader;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class DrillModelWriter extends AbstractRecordWriter implements RecordWriter {
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DrillModelWriter.class);

  private String location;
  private String prefix;
//  private final BufferAllocator allocator;

//  private String fieldDelimiter;
  private String extension;

//  private static String eol = System.getProperty("line.separator");
  private int index;
  private BufferedOutputStream stream = null;
//  private PrintStream stream = null;
  private FileSystem fs = null;

  // Record write status
  private boolean fRecordStarted = false; // true once the startRecord() is called until endRecord() is called
  private byte[] currentRecord; // contains the current record separated //by field delimiter

  public DrillModelWriter(/*BufferAllocator allocator*/) {
    //super(allocator);
//	  this.allocator = allocator;
  }

  @Override
  public void init(Map<String, String> writerOptions) throws IOException {
    this.location = writerOptions.get("location");
    this.prefix = writerOptions.get("prefix");
//    this.fieldDelimiter = writerOptions.get("separator");
    this.extension = writerOptions.get("extension");

    Configuration conf = new Configuration();
    conf.set(FileSystem.FS_DEFAULT_NAME_KEY, writerOptions.get(FileSystem.FS_DEFAULT_NAME_KEY));
    this.fs = FileSystem.get(conf);

//    this.currentRecord = new Byte;//new StringBuilder();
    this.index = 0;
  }

  
  public void startNewSchema() throws IOException {
    // wrap up the current file
    cleanup();

    // open a new file for writing data with new schema
    Path fileName = new Path(location, prefix + "_" + index + "." + extension);
    try {
      DataOutputStream fos = fs.create(fileName);
      stream = new BufferedOutputStream(fos); //PrintStream
//      stream = new PrintStream(fos); 
      logger.info("Created file: {}", fileName);
    } catch (IOException ex) {
      logger.error("Unable to create file: " + fileName, ex);
      throw ex;
    }
    index++;

    //stream.println(Joiner.on(fieldDelimiter).join(columnNames));
  }

  public void addField(/*int fieldId,*/ byte[] value) throws IOException {
	  currentRecord = new byte[value.length];
	  currentRecord = value;
	  //currentRecord.append(value);// + fieldDelimiter);
  }

  @Override
  public void startRecord() throws IOException {
    if (fRecordStarted) {
      throw new IOException("Previous record is not written completely");
    }

    fRecordStarted = true;
  }

  @Override
  public void endRecord() throws IOException {
    if (!fRecordStarted) {
      throw new IOException("No record is in writing");
    }

    // remove the extra delimiter at the end
//    currentRecord.deleteCharAt(currentRecord.length()-fieldDelimiter.length());

//    ObjectOutputStream oos = new ObjectOutputStream(stream);
//    
//    Object object = null;
//    try {
//     ObjectInputStream objectInputStream = new ObjectInputStream( new ByteArrayInputStream(currentRecord.toString().getBytes(com.google.common.base.Charsets.UTF_8)) );
//     object = objectInputStream.readObject();
//    } catch (IOException e) {
//     e.printStackTrace();
//    } catch (ClassNotFoundException e) {
//     e.printStackTrace();
//    } catch (ClassCastException e) {
//     e.printStackTrace();
//    }
// 
//    oos.writeObject(object);
//    oos.flush();
//    oos.close();
    stream.write(currentRecord);
//    stream.println(currentRecord.toString());

    // reset current record status
//    currentRecord.delete(0, currentRecord.length());
    currentRecord = null;
    fRecordStarted = false;
  }
 

  @Override
  public FieldConverter getNewVarCharConverter(int fieldId, String fieldName, FieldReader reader) {
    return new VarCharStringFieldConverter(fieldId, fieldName, reader);
  }
  public class VarCharStringFieldConverter extends FieldConverter {
	    private NullableVarCharHolder holder = new NullableVarCharHolder();

	    public VarCharStringFieldConverter(int fieldId, String fieldName, FieldReader reader) {
	      super(fieldId, fieldName, reader);
	    }

	    @Override
	    public void writeField() throws IOException {

	    	
	    	
	    reader.read(holder);
	    
	    byte[] outbuff = new byte[holder.end];
		holder.buffer.getBytes(0, outbuff);
		
		
//	    addField(fieldId, reader.readObject().toString());
		
//		logger.info("Shadi: reader.readObject().toString() : "+reader.readObject().toString());
//		logger.info("Shadi: outbuff:"+outbuff);
	    addField(outbuff);
	    }
	  }
  
  
//  @Override
//  public FieldConverter getNewMapConverter(int fieldId, String fieldName, FieldReader reader) {
//    return new ComplexStringFieldConverter(fieldId, fieldName, reader);
//  }
//
//  @Override
//  public FieldConverter getNewRepeatedMapConverter(int fieldId, String fieldName, FieldReader reader) {
//    return new ComplexStringFieldConverter(fieldId, fieldName, reader);
//  }
//
//  @Override
//  public FieldConverter getNewRepeatedListConverter(int fieldId, String fieldName, FieldReader reader) {
//    return new ComplexStringFieldConverter(fieldId, fieldName, reader);
//  }
//
//  public class ComplexStringFieldConverter extends FieldConverter {
//
//    public ComplexStringFieldConverter(int fieldId, String fieldName, FieldReader reader) {
//      super(fieldId, fieldName, reader);
//    }
//
//    @Override
//    public void writeField() throws IOException {
//      addField(reader.readObject());
//    }
//  }

  @Override
  public void cleanup() throws IOException {
//    super.cleanup();
    if (stream != null) {
      stream.close();
      stream = null;
      logger.debug("closing file");
    }
  }

  @Override
  public void abort() throws IOException {
    cleanup();
    try {
      fs.delete(new Path(location), true);
    } catch (IOException ex) {
      logger.error("Abort failed. There could be leftover output files");
      throw ex;
    }
  }

	@Override
	public void updateSchema(VectorAccessible batch) throws IOException {
	//    List<String> columnNames = Lists.newArrayList();
	//    for (int i=0; i < schema.getFieldCount(); i++) {
	//      columnNames.add(schema.getColumn(i).getLastName());
	//    }
		startNewSchema();
		
	}


}
