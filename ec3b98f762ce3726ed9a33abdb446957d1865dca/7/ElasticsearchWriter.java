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
package org.apache.metron.elasticsearch.writer;

import com.google.common.collect.Lists;
import org.apache.metron.common.Constants;
import org.apache.metron.common.configuration.writer.WriterConfiguration;
import org.apache.metron.common.field.FieldNameConverter;
import org.apache.metron.common.field.FieldNameConverters;
import org.apache.metron.common.writer.BulkMessageWriter;
import org.apache.metron.common.writer.BulkWriterResponse;
import org.apache.metron.elasticsearch.bulk.BulkDocumentWriter;
import org.apache.metron.elasticsearch.bulk.ElasticsearchBulkDocumentWriter;
import org.apache.metron.elasticsearch.bulk.WriteFailure;
import org.apache.metron.elasticsearch.bulk.WriteSuccess;
import org.apache.metron.elasticsearch.bulk.BulkDocumentWriterResults;
import org.apache.metron.elasticsearch.client.ElasticsearchClient;
import org.apache.metron.elasticsearch.client.ElasticsearchClientFactory;
import org.apache.metron.elasticsearch.utils.ElasticsearchUtils;
import org.apache.metron.stellar.common.utils.ConversionUtils;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Tuple;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.metron.stellar.common.Constants.Fields.TIMESTAMP;

/**
 * A {@link BulkMessageWriter} that writes messages to Elasticsearch.
 */
public class ElasticsearchWriter implements BulkMessageWriter<JSONObject>, Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * The Elasticsearch client.
   */
  private transient ElasticsearchClient client;

  /**
   * Responsible for writing documents.
   *
   * <p>Uses a {@link TupleBasedDocument} to maintain the relationship between
   * a {@link Tuple} and the document created from the contents of that tuple. If
   * a document cannot be written, the associated tuple needs to be failed.
   */
  private transient BulkDocumentWriter<TupleBasedDocument> documentWriter;

  /**
   * A simple data formatter used to build the appropriate Elasticsearch index name.
   */
  private SimpleDateFormat dateFormat;

  @Override
  public void init(Map stormConf, TopologyContext topologyContext, WriterConfiguration configurations) {
    Map<String, Object> globalConfiguration = configurations.getGlobalConfig();
    dateFormat = ElasticsearchUtils.getIndexFormat(globalConfiguration);

    // only create the document writer, if one does not already exist. useful for testing.
    if(documentWriter == null) {
      client = ElasticsearchClientFactory.create(globalConfiguration);
      documentWriter = new ElasticsearchBulkDocumentWriter<>(client);
    }
  }

  @Override
  public BulkWriterResponse write(String sensorType,
                                  WriterConfiguration configurations,
                                  Iterable<Tuple> tuplesIter,
                                  List<JSONObject> messages) {

    // fetch the field name converter for this sensor type
    FieldNameConverter fieldNameConverter = FieldNameConverters.create(sensorType, configurations);
    String indexPostfix = dateFormat.format(new Date());
    String indexName = ElasticsearchUtils.getIndexName(sensorType, indexPostfix, configurations);

    // the number of tuples must match the number of messages
    List<Tuple> tuples = Lists.newArrayList(tuplesIter);
    int batchSize = tuples.size();
    if(messages.size() != batchSize) {
      throw new IllegalStateException(format("Expect same number of tuples and messages; |tuples|=%d, |messages|=%d",
              tuples.size(), messages.size()));
    }

    // create a document from each message
    for(int i=0; i<tuples.size(); i++) {
      JSONObject message = messages.get(i);
      Tuple tuple = tuples.get(i);
      TupleBasedDocument document = createDocument(message, tuple, sensorType, fieldNameConverter);
      documentWriter.addDocument(document, indexName);
    }

    // write the documents
    BulkDocumentWriterResults<TupleBasedDocument> results = documentWriter.write();

    // build the response
    BulkWriterResponse response = new BulkWriterResponse();
    for(WriteSuccess<TupleBasedDocument> success: results.getSuccesses()) {
      response.addSuccess(success.getDocument().getTuple());
    }
    for(WriteFailure<TupleBasedDocument> failure: results.getFailures()) {
      response.addError(failure.getCause(), failure.getDocument().getTuple());
    }
    return response;
  }

  private TupleBasedDocument createDocument(JSONObject message,
                                            Tuple tuple,
                                            String sensorType,
                                            FieldNameConverter fieldNameConverter) {
    // transform the message fields to the source fields of the indexed document
    JSONObject source = new JSONObject();
    for(Object k : message.keySet()){
      copyField(k.toString(), message, source, fieldNameConverter);
    }

    // define the document id
    String guid = ConversionUtils.convert(source.get(Constants.GUID), String.class);
    if(guid == null) {
      LOG.warn("Missing '{}' field; document ID will be auto-generated.", Constants.GUID);
    }

    // define the document timestamp
    Long timestamp = null;
    Object value = source.get(TIMESTAMP.getName());
    if(value != null) {
      timestamp = Long.parseLong(value.toString());
    } else {
      LOG.warn("Missing '{}' field; timestamp will be set to system time.", TIMESTAMP.getName());
    }

    return new TupleBasedDocument(source, guid, sensorType, timestamp, tuple);
  }

  @Override
  public String getName() {
    return "elasticsearch";
  }

  @Override
  public void close() throws Exception {
    if(client != null) {
      client.close();
    }
  }

  /**
   * Copies the value of a field from the source message to the destination message.
   *
   * <p>A field name may also be transformed in the destination message by the {@link FieldNameConverter}.
   *
   * @param sourceFieldName The name of the field to copy from the source message
   * @param source The source message.
   * @param destination The destination message.
   * @param fieldNameConverter The field name converter that transforms the field name
   *                           between the source and destination.
   */
  //JSONObject doesn't expose map generics
  @SuppressWarnings("unchecked")
  private void copyField(
          String sourceFieldName,
          JSONObject source,
          JSONObject destination,
          FieldNameConverter fieldNameConverter) {

    // allow the field name to be transformed
    String destinationFieldName = fieldNameConverter.convert(sourceFieldName);

    // copy the field
    destination.put(destinationFieldName, source.get(sourceFieldName));
  }

  /**
   * Set the document writer.  Primarily used for testing.
   * @param documentWriter The {@link BulkDocumentWriter} to use.
   */
  public void setDocumentWriter(BulkDocumentWriter<TupleBasedDocument> documentWriter) {
    this.documentWriter = documentWriter;
  }
}

