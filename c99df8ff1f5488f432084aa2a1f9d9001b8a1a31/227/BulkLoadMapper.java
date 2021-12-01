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
package org.apache.metron.dataloads.hbase.mr;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.metron.dataloads.extractor.Extractor;
import org.apache.metron.dataloads.extractor.ExtractorHandler;
import org.apache.metron.enrichment.converter.HbaseConverter;
import org.apache.metron.enrichment.lookup.LookupKV;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class BulkLoadMapper extends Mapper<Object, Text, ImmutableBytesWritable, Put>
{
    public static final String CONFIG_KEY="bl_extractor_config";
    public static final String COLUMN_FAMILY_KEY = "bl_column_family";
    public static final String LAST_SEEN_KEY = "bl_last_seen";
    public static final String CONVERTER_KEY = "bl_converter";
    Extractor extractor = null;
    String columnFamily = null;
    HbaseConverter converter;
    @Override
    public void setup(Context context) throws IOException,
            InterruptedException {
        initialize(context.getConfiguration());
    }

    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        for(LookupKV results : extractor.extract(value.toString())) {
            if (results != null) {
                Put put = converter.toPut(columnFamily, results.getKey(), results.getValue());
                write(new ImmutableBytesWritable(results.getKey().toBytes()), put, context);
            }
        }
    }

    protected void initialize(Configuration configuration) throws IOException{
        String configStr = configuration.get(CONFIG_KEY);
        extractor = ExtractorHandler.load(configStr).getExtractor();
        columnFamily = configuration.get(COLUMN_FAMILY_KEY);
        try {
            converter = (HbaseConverter) Class.forName(configuration.get(CONVERTER_KEY)).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to create converter object: " + configuration.get(CONVERTER_KEY), e);
        }
    }

    protected void write(ImmutableBytesWritable key, Put value, Context context) throws IOException, InterruptedException {
        context.write(key, value);
    }
}
