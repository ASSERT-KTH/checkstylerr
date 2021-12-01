/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.google.cloud.examples.logging.snippets;

import com.google.api.gax.paging.Page;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.logging.Sink;
import com.google.cloud.logging.SinkInfo;
import com.google.cloud.logging.SinkInfo.Destination.DatasetDestination;


/**
 * A snippet for Stackdriver Logging showing how to create a sink to backs log entries to BigQuery.
 * The snippet also shows how to list all sinks.
 *
 * @see <a href="https://cloud.google.com/logging/docs/api/#sinks">Sinks</a>
 */
public class CreateAndListSinks {

  public static void main(String... args) throws Exception {
    // Create a service object
    // Credentials are inferred from the environment
    try(Logging logging = LoggingOptions.getDefaultInstance().getService()) {

      // Create a sink to back log entries to a BigQuery dataset
      SinkInfo sinkInfo = SinkInfo.newBuilder("test-sink", DatasetDestination.of("test-dataset"))
          .setFilter("severity >= ERROR")
          .build();
      logging.create(sinkInfo);

      // List sinks
      Page<Sink> sinks = logging.listSinks();
      for (Sink sink : sinks.iterateAll()) {
        System.out.println(sink);
      }
    }
  }
}
