/*
 * Copyright 2016 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * EDITING INSTRUCTIONS
 * This file is referenced in READMEs and javadoc. Any change to this file should be reflected in
 * the project's READMEs and package-info.java.
 */

package com.google.cloud.examples.bigquery.snippets;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import java.util.HashMap;
import java.util.Map;

/**
 * A snippet for Google Cloud BigQuery showing how to create a BigQuery dataset and table. Once
 * created, the snippet streams data into the table and then queries it.
 */
public class InsertDataAndQueryTable {

  public static void main(String... args) throws InterruptedException {
    // Create a service instance
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

    // Create a dataset
    String datasetId = "my_dataset_id";
    bigquery.create(DatasetInfo.newBuilder(datasetId).build());

    TableId tableId = TableId.of(datasetId, "my_table_id");
    // Table field definition
    Field stringField = Field.of("StringField", LegacySQLTypeName.STRING);
    // Table schema definition
    Schema schema = Schema.of(stringField);
    // Create a table
    StandardTableDefinition tableDefinition = StandardTableDefinition.of(schema);
    bigquery.create(TableInfo.of(tableId, tableDefinition));

    // Define rows to insert
    Map<String, Object> firstRow = new HashMap<>();
    Map<String, Object> secondRow = new HashMap<>();
    firstRow.put("StringField", "value1");
    secondRow.put("StringField", "value2");
    // Create an insert request
    InsertAllRequest insertRequest =
        InsertAllRequest.newBuilder(tableId).addRow(firstRow).addRow(secondRow).build();
    // Insert rows
    InsertAllResponse insertResponse = bigquery.insertAll(insertRequest);
    // Check if errors occurred
    if (insertResponse.hasErrors()) {
      System.out.println("Errors occurred while inserting rows");
    }

    // Create a query request
    QueryJobConfiguration queryConfig =
        QueryJobConfiguration.newBuilder("SELECT * FROM my_dataset_id.my_table_id").build();
    // Read rows
    System.out.println("Table rows:");
    for (FieldValueList row : bigquery.query(queryConfig).iterateAll()) {
      System.out.println(row);
    }
  }
}
