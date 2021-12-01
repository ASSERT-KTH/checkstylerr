/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.crunch.types.avro;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.mapred.AvroJob;
import org.apache.avro.mapred.AvroWrapper;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

/** An {@link org.apache.hadoop.mapreduce.InputFormat} for Avro data files. */
public class AvroInputFormat<T> extends FileInputFormat<AvroWrapper<T>, NullWritable> {
  @Override
  public RecordReader<AvroWrapper<T>, NullWritable> createRecordReader(InputSplit split, TaskAttemptContext context)
      throws IOException, InterruptedException {
    context.setStatus(split.toString());
    String jsonSchema = context.getConfiguration().get(AvroJob.INPUT_SCHEMA);
    Schema schema = new Schema.Parser().parse(jsonSchema);
    return new AvroRecordReader<T>(schema);
  }
}
