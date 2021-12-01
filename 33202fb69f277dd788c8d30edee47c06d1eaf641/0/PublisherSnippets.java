/*
 * Copyright 2017 Google LLC
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
 * This file is referenced in Publisher's javadoc. Any change to this file should be reflected in
 * Publisher's javadoc.
 */
package com.google.cloud.examples.pubsub.snippets;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.batching.BatchingSettings;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.api.gax.retrying.RetrySettings;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import java.io.FileInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.threeten.bp.Duration;

/** This class contains snippets for the {@link Publisher} interface. */
public class PublisherSnippets {
  private final Publisher publisher;

  public PublisherSnippets(Publisher publisher) {
    this.publisher = publisher;
  }

  /** Example of publishing a message. */
  // [TARGET publish(PubsubMessage)]
  // [VARIABLE "my_message"]
  public ApiFuture<String> publish(String message) {
    ByteString data = ByteString.copyFromUtf8(message);
    PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
    ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
    ApiFutures.addCallback(
        messageIdFuture,
        new ApiFutureCallback<String>() {
          public void onSuccess(String messageId) {
            System.out.println("published with message id: " + messageId);
          }

          public void onFailure(Throwable t) {
            System.out.println("failed to publish: " + t);
          }
        },
        Executors.newSingleThreadExecutor());
    return messageIdFuture;
  }

  /** Example of creating a {@code Publisher}. */
  // [TARGET newBuilder(ProjectTopicName)]
  // [VARIABLE "my_project"]
  // [VARIABLE "my_topic"]
  public static void newBuilder(String projectId, String topicId) throws Exception {
    ProjectTopicName topic = ProjectTopicName.of(projectId, topicId);
    Publisher publisher = Publisher.newBuilder(topic).build();
    try {
      // ...
    } finally {
      // When finished with the publisher, make sure to shutdown to free up resources.
      publisher.shutdown();
      publisher.awaitTermination(1, TimeUnit.MINUTES);
    }
  }

  public Publisher getPublisherWithCustomBatchSettings(ProjectTopicName topicName)
      throws Exception {
    // [START pubsub_publisher_batch_settings]
    // Batch settings control how the publisher batches messages
    long requestBytesThreshold = 5000L; // default : 1 byte
    long messageCountBatchSize = 10L; // default : 1 message

    Duration publishDelayThreshold = Duration.ofMillis(100); // default : 1 ms

    // Publish request get triggered based on request size, messages count & time since last publish
    BatchingSettings batchingSettings =
        BatchingSettings.newBuilder()
            .setElementCountThreshold(messageCountBatchSize)
            .setRequestByteThreshold(requestBytesThreshold)
            .setDelayThreshold(publishDelayThreshold)
            .build();

    Publisher publisher =
        Publisher.newBuilder(topicName).setBatchingSettings(batchingSettings).build();
    // [END pubsub_publisher_batch_settings]
    return publisher;
  }

  public Publisher getPublisherWithCustomRetrySettings(ProjectTopicName topicName)
      throws Exception {
    // [START pubsub_publisher_retry_settings]
    // Retry settings control how the publisher handles retryable failures
    Duration retryDelay = Duration.ofMillis(5); // default: 5 ms
    double retryDelayMultiplier = 2.0; // back off for repeated failures, default: 2.0
    Duration maxRetryDelay = Duration.ofSeconds(600); // default : Long.MAX_VALUE
    Duration totalTimeout = Duration.ofSeconds(10); // default: 10 seconds
    Duration initialRpcTimeout = Duration.ofSeconds(10); // default: 10 seconds
    Duration maxRpcTimeout = Duration.ofSeconds(10); // default: 10 seconds

    RetrySettings retrySettings =
        RetrySettings.newBuilder()
            .setInitialRetryDelay(retryDelay)
            .setRetryDelayMultiplier(retryDelayMultiplier)
            .setMaxRetryDelay(maxRetryDelay)
            .setTotalTimeout(totalTimeout)
            .setInitialRpcTimeout(initialRpcTimeout)
            .setMaxRpcTimeout(maxRpcTimeout)
            .build();

    Publisher publisher = Publisher.newBuilder(topicName).setRetrySettings(retrySettings).build();
    // [END pubsub_publisher_retry_settings]
    return publisher;
  }

  public Publisher getSingleThreadedPublisher(ProjectTopicName topicName) throws Exception {
    // [START pubsub_publisher_concurrency_control]
    // create a publisher with a single threaded executor
    ExecutorProvider executorProvider =
        InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(1).build();
    Publisher publisher =
        Publisher.newBuilder(topicName).setExecutorProvider(executorProvider).build();
    // [END pubsub_publisher_concurrency_control]
    return publisher;
  }

  private Publisher createPublisherWithCustomCredentials(ProjectTopicName topicName)
      throws Exception {
    // [START pubsub_publisher_custom_credentials]
    // read service account credentials from file
    CredentialsProvider credentialsProvider =
        FixedCredentialsProvider.create(
            ServiceAccountCredentials.fromStream(new FileInputStream("credentials.json")));

    Publisher publisher =
        Publisher.newBuilder(topicName).setCredentialsProvider(credentialsProvider).build();
    // [END pubsub_publisher_custom_credentials]
    return publisher;
  }
}
