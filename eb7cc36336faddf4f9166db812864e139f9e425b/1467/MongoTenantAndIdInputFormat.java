/*
 * Copyright 2012-2013 inBloom, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.slc.sli.aggregation.mapreduce.io;

import com.mongodb.hadoop.MongoInputFormat;
import com.mongodb.hadoop.input.MongoInputSplit;

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * MongoTenantAndIdInputFormat
 */
public class MongoTenantAndIdInputFormat extends MongoInputFormat {
    protected MongoInputFormat privateFormat = new MongoInputFormat();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public RecordReader createRecordReader(InputSplit split, TaskAttemptContext context) {
        if (!(split instanceof MongoInputSplit)) {
            throw new IllegalStateException("Creation of a new MongoTenantAndIdInputFormat requires a MongoInputSplit instance.");
        }

        final MongoInputSplit mis = (MongoInputSplit) split;
        return new MongoTenantAndIdRecordReader(mis);
    }

}
