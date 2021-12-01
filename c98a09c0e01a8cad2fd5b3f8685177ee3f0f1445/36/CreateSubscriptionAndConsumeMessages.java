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

package com.google.cloud.examples.pubsub.snippets;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;

/**
 * A snippet for Google Cloud Pub/Sub showing how to create a Pub/Sub pull subscription and
 * asynchronously pull messages from it.
 */
public class CreateSubscriptionAndConsumeMessages {

  public static void main(String... args) throws Exception {
    TopicName topic = TopicName.create("my-project-id", "my-topic-id");
    SubscriptionName subscription = SubscriptionName.create("my-project-id", "my-topic-id");

    try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
      subscriptionAdminClient.createSubscription(subscription, topic, PushConfig.getDefaultInstance(), 0);
    }

    MessageReceiver receiver =
        new MessageReceiver() {
          @Override
          public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
            System.out.println("Received message: " + message.getData().toStringUtf8());
            consumer.ack();
          }
        };
    Subscriber subscriber = null;
    try {
      subscriber = Subscriber.newBuilder(subscription, receiver).build();
      subscriber.addListener(
          new Subscriber.Listener() {
            @Override
            public void failed(Subscriber.State from, Throwable failure) {
              // Handle failure. This is called when the Subscriber encountered a fatal error and is shutting down.
              System.err.println(failure);
            }
          },
          MoreExecutors.directExecutor());
      subscriber.startAsync().awaitRunning();

      Thread.sleep(60000);
    } finally {
      if (subscriber != null) {
        subscriber.stopAsync();
      }
    }
  }
}
