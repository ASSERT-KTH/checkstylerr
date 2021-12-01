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

package com.google.cloud.pubsub.spi.v1;

import com.google.api.gax.core.AbstractApiService;
import com.google.api.gax.core.ApiClock;
import com.google.api.gax.core.ApiService;
import com.google.api.gax.core.CurrentMillisClock;
import com.google.api.gax.core.FlowControlSettings;
import com.google.api.gax.core.FlowController;
import com.google.api.gax.grpc.ExecutorProvider;
import com.google.api.gax.grpc.InstantiatingExecutorProvider;
import com.google.api.stats.Distribution;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.google.pubsub.v1.SubscriptionName;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.Duration;

/**
 * A Cloud Pub/Sub <a href="https://cloud.google.com/pubsub/docs/subscriber">subscriber</a> that is
 * associated with a specific subscription at creation.
 *
 * <p>A {@link Subscriber} allows you to provide an implementation of a {@link MessageReceiver
 * receiver} to which messages are going to be delivered as soon as they are received by the
 * subscriber. The delivered messages then can be {@link AckReply#ACK acked} or {@link AckReply#NACK
 * nacked} at will as they get processed by the receiver. Nacking a messages implies a later
 * redelivery of such message.
 *
 * <p>The subscriber handles the ack management, by automatically extending the ack deadline while
 * the message is being processed, to then issue the ack or nack of such message when the processing
 * is done. <strong>Note:</strong> message redelivery is still possible.
 *
 * <p>It also provides customizable options that control:
 *
 * <ul>
 *   <li>Ack deadline extension: such as the amount of time ahead to trigger the extension of
 *       message acknowledgement expiration.
 *   <li>Flow control: such as the maximum outstanding messages or maximum outstanding bytes to keep
 *       in memory before the receiver either ack or nack them.
 * </ul>
 *
 * <p>If no credentials are provided, the {@link Subscriber} will use application default
 * credentials through {@link GoogleCredentials#getApplicationDefault}.
 */
public class Subscriber extends AbstractApiService {
  private static final int THREADS_PER_CHANNEL = 5;
  @VisibleForTesting static final int CHANNELS_PER_CORE = 10;
  private static final int MAX_INBOUND_MESSAGE_SIZE =
      20 * 1024 * 1024; // 20MB API maximum message size.
  private static final int INITIAL_ACK_DEADLINE_SECONDS = 10;
  private static final int MAX_ACK_DEADLINE_SECONDS = 600;
  static final int MIN_ACK_DEADLINE_SECONDS = 10;
  private static final Duration ACK_DEADLINE_UPDATE_PERIOD = Duration.standardMinutes(1);
  private static final double PERCENTILE_FOR_ACK_DEADLINE_UPDATES = 99.9;

  private static final Logger logger = Logger.getLogger(Subscriber.class.getName());

  private final SubscriptionName subscriptionName;
  private final String cachedSubscriptionNameString;
  private final FlowControlSettings flowControlSettings;
  private final Duration ackExpirationPadding;
  private final Duration maxAckExtensionPeriod;
  private final ScheduledExecutorService executor;
  private final Distribution ackLatencyDistribution =
      new Distribution(MAX_ACK_DEADLINE_SECONDS + 1);
  private final int numChannels;
  private final FlowController flowController;
  private final ManagedChannelBuilder<? extends ManagedChannelBuilder<?>> channelBuilder;
  private final Credentials credentials;
  private final MessageReceiver receiver;
  private final List<StreamingSubscriberConnection> streamingSubscriberConnections;
  private final List<PollingSubscriberConnection> pollingSubscriberConnections;
  private final ApiClock clock;
  private final List<AutoCloseable> closeables = new ArrayList<>();
  private ScheduledFuture<?> ackDeadlineUpdater;
  private int streamAckDeadlineSeconds;

  private Subscriber(Builder builder) throws IOException {
    receiver = builder.receiver;
    flowControlSettings = builder.flowControlSettings;
    subscriptionName = builder.subscriptionName;
    cachedSubscriptionNameString = subscriptionName.toString();
    ackExpirationPadding = builder.ackExpirationPadding;
    maxAckExtensionPeriod = builder.maxAckExtensionPeriod;
    streamAckDeadlineSeconds =
        Math.max(
            INITIAL_ACK_DEADLINE_SECONDS,
            Ints.saturatedCast(ackExpirationPadding.getStandardSeconds()));
    clock = builder.clock.isPresent() ? builder.clock.get() : CurrentMillisClock.getDefaultClock();

    flowController = new FlowController(builder.flowControlSettings);

    executor = builder.executorProvider.getExecutor();
    if (builder.executorProvider.shouldAutoClose()) {
      closeables.add(
          new AutoCloseable() {
            @Override
            public void close() throws IOException {
              executor.shutdown();
            }
          });
    }

    // TODO(pongad): remove this when we move to ManagedChannelBuilder
    String defaultEndpoint = SubscriptionAdminSettings.getDefaultEndpoint();
    int colonPos = defaultEndpoint.indexOf(':');

    channelBuilder =
        builder.channelBuilder.isPresent()
            ? builder.channelBuilder.get()
            : NettyChannelBuilder.forAddress(
                    defaultEndpoint.substring(0, colonPos),
                    Integer.parseInt(defaultEndpoint.substring(colonPos+1)))
                .maxMessageSize(MAX_INBOUND_MESSAGE_SIZE)
                .flowControlWindow(5000000) // 2.5 MB
                .negotiationType(NegotiationType.TLS)
                .sslContext(GrpcSslContexts.forClient().ciphers(null).build())
                .executor(executor);

    credentials =
        builder.credentials.isPresent()
            ? builder.credentials.get()
            : GoogleCredentials.getApplicationDefault()
                .createScoped(SubscriptionAdminSettings.getDefaultServiceScopes());

    numChannels = Math.max(1, Runtime.getRuntime().availableProcessors()) * CHANNELS_PER_CORE;
    streamingSubscriberConnections = new ArrayList<StreamingSubscriberConnection>(numChannels);
    pollingSubscriberConnections = new ArrayList<PollingSubscriberConnection>(numChannels);
  }

  /**
   * Constructs a new {@link Builder}.
   *
   * <p>Once {@link Builder#build} is called a gRPC stub will be created for use of the {@link
   * Subscriber}.
   *
   * @param subscription Cloud Pub/Sub subscription to bind the subscriber to
   * @param receiver an implementation of {@link MessageReceiver} used to process the received
   *     messages
   */
  public static Builder defaultBuilder(SubscriptionName subscription, MessageReceiver receiver) {
    return new Builder(subscription, receiver);
  }

  /** Subscription which the subscriber is subscribed to. */
  public SubscriptionName getSubscriptionName() {
    return subscriptionName;
  }

  /** Acknowledgement expiration padding. See {@link Builder#setAckExpirationPadding}. */
  public Duration getAckExpirationPadding() {
    return ackExpirationPadding;
  }

  /** The flow control settings the Subscriber is configured with. */
  public FlowControlSettings getFlowControlSettings() {
    return flowControlSettings;
  }

  /**
   * Initiates service startup and returns immediately.
   *
   * <p>Example of receiving a specific number of messages.
   * <pre> {@code
   * Subscriber subscriber = Subscriber.defaultBuilder(subscription, receiver).build();
   * subscriber.addListener(new Subscriber.Listener() {
   *   public void failed(Subscriber.State from, Throwable failure) {
   *     // Handle error.
   *   }
   * }, executor);
   * subscriber.startAsync();
   *
   * // Wait for a stop signal.
   * done.get();
   * subscriber.stopAsync().awaitTerminated();
   * }</pre>
   *
   */
  @Override
  public ApiService startAsync() {
    // Override only for the docs.
    return super.startAsync();
  }

  @Override
  protected void doStart() {
    logger.log(Level.FINE, "Starting subscriber group.");
    // Streaming pull is not enabled on the service yet.
    // startStreamingConnections();
    startPollingConnections();
    notifyStarted();
  }

  @Override
  protected void doStop() {
    stopAllStreamingConnections();
    stopAllPollingConnections();
    try {
      for (AutoCloseable closeable : closeables) {
        closeable.close();
      }
      notifyStopped();
    } catch (Exception e) {
      notifyFailed(e);
    }
  }

  private void startStreamingConnections() {
    synchronized (streamingSubscriberConnections) {
      for (int i = 0; i < numChannels; i++) {
        streamingSubscriberConnections.add(
            new StreamingSubscriberConnection(
                cachedSubscriptionNameString,
                credentials,
                receiver,
                ackExpirationPadding,
                maxAckExtensionPeriod,
                streamAckDeadlineSeconds,
                ackLatencyDistribution,
                channelBuilder.build(),
                flowController,
                executor,
                clock));
      }
      startConnections(
          streamingSubscriberConnections,
          new Listener() {
            @Override
            public void failed(State from, Throwable failure) {
              // If a connection failed is because of a fatal error, we should fail the
              // whole subscriber.
              stopAllStreamingConnections();
              if (failure instanceof StatusRuntimeException
                  && ((StatusRuntimeException) failure).getStatus().getCode()
                      == Status.Code.UNIMPLEMENTED) {
                logger.info("Unable to open streaming connections, falling back to polling.");
                startPollingConnections();
                return;
              }
              notifyFailed(failure);
            }
          });
    }

    ackDeadlineUpdater =
        executor.scheduleAtFixedRate(
            new Runnable() {
              @Override
              public void run() {
                // It is guaranteed this will be <= MAX_ACK_DEADLINE_SECONDS, the max of the API.
                long ackLatency =
                    ackLatencyDistribution.getNthPercentile(PERCENTILE_FOR_ACK_DEADLINE_UPDATES);
                if (ackLatency > 0) {
                  int possibleStreamAckDeadlineSeconds =
                      Math.max(
                          MIN_ACK_DEADLINE_SECONDS,
                          Ints.saturatedCast(
                              Math.max(ackLatency, ackExpirationPadding.getStandardSeconds())));
                  if (streamAckDeadlineSeconds != possibleStreamAckDeadlineSeconds) {
                    streamAckDeadlineSeconds = possibleStreamAckDeadlineSeconds;
                    logger.log(
                        Level.FINER,
                        "Updating stream deadline to {0} seconds.",
                        streamAckDeadlineSeconds);
                    for (StreamingSubscriberConnection subscriberConnection :
                        streamingSubscriberConnections) {
                      subscriberConnection.updateStreamAckDeadline(streamAckDeadlineSeconds);
                    }
                  }
                }
              }
            },
            ACK_DEADLINE_UPDATE_PERIOD.getMillis(),
            ACK_DEADLINE_UPDATE_PERIOD.getMillis(),
            TimeUnit.MILLISECONDS);
  }

  private void stopAllStreamingConnections() {
    stopConnections(streamingSubscriberConnections);
    if (ackDeadlineUpdater != null) {
      ackDeadlineUpdater.cancel(true);
    }
  }

  private void startPollingConnections() {
    synchronized (pollingSubscriberConnections) {
      for (int i = 0; i < numChannels; i++) {
        pollingSubscriberConnections.add(
            new PollingSubscriberConnection(
                cachedSubscriptionNameString,
                credentials,
                receiver,
                ackExpirationPadding,
                maxAckExtensionPeriod,
                ackLatencyDistribution,
                channelBuilder.build(),
                flowController,
                executor,
                clock));
      }
      startConnections(
          pollingSubscriberConnections,
          new Listener() {
            @Override
            public void failed(State from, Throwable failure) {
              // If a connection failed is because of a fatal error, we should fail the
              // whole subscriber.
              stopAllPollingConnections();
              try {
                notifyFailed(failure);
              } catch (IllegalStateException e) {
                if (isRunning()) {
                  throw e;
                }
                // It could happen that we are shutting down while some channels fail.
              }
            }
          });
    }
  }

  private void stopAllPollingConnections() {
    stopConnections(pollingSubscriberConnections);
  }

  private void startConnections(
      List<? extends ApiService> connections, final ApiService.Listener connectionsListener) {
    final CountDownLatch subscribersStarting = new CountDownLatch(numChannels);
    for (final ApiService subscriber : connections) {
      executor.submit(
          new Runnable() {
            @Override
            public void run() {
              subscriber.addListener(connectionsListener, executor);
              try {
                subscriber.startAsync().awaitRunning();
              } finally {
                subscribersStarting.countDown();
              }
            }
          });
    }
    try {
      subscribersStarting.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void stopConnections(List<? extends ApiService> connections) {
    ArrayList<ApiService> liveConnections;
    synchronized (connections) {
      liveConnections = new ArrayList<ApiService>(connections);
      connections.clear();
    }
    final CountDownLatch connectionsStopping = new CountDownLatch(liveConnections.size());
    for (final ApiService subscriberConnection : liveConnections) {
      executor.submit(
          new Runnable() {
            @Override
            public void run() {
              try {
                subscriberConnection.stopAsync().awaitTerminated();
              } catch (IllegalStateException ignored) {
                // It is expected for some connections to be already in state failed so stop will
                // throw this expection.
              }
              connectionsStopping.countDown();
            }
          });
    }
    try {
      connectionsStopping.await();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  /** Builder of {@link Subscriber Subscribers}. */
  public static final class Builder {
    private static final Duration MIN_ACK_EXPIRATION_PADDING = Duration.millis(100);
    private static final Duration DEFAULT_ACK_EXPIRATION_PADDING = Duration.millis(500);
    private static final Duration DEFAULT_MAX_ACK_EXTENSION_PERIOD = Duration.standardMinutes(60);

    static final ExecutorProvider DEFAULT_EXECUTOR_PROVIDER =
        InstantiatingExecutorProvider.newBuilder()
            .setExecutorThreadCount(
                THREADS_PER_CHANNEL
                    * CHANNELS_PER_CORE
                    * Runtime.getRuntime().availableProcessors())
            .build();

    SubscriptionName subscriptionName;
    Optional<Credentials> credentials = Optional.absent();
    MessageReceiver receiver;

    Duration ackExpirationPadding = DEFAULT_ACK_EXPIRATION_PADDING;
    Duration maxAckExtensionPeriod = DEFAULT_MAX_ACK_EXTENSION_PERIOD;

    FlowControlSettings flowControlSettings = FlowControlSettings.getDefaultInstance();

    ExecutorProvider executorProvider = DEFAULT_EXECUTOR_PROVIDER;
    Optional<ManagedChannelBuilder<? extends ManagedChannelBuilder<?>>> channelBuilder =
        Optional.absent();
    Optional<ApiClock> clock = Optional.absent();

    Builder(SubscriptionName subscriptionName, MessageReceiver receiver) {
      this.subscriptionName = subscriptionName;
      this.receiver = receiver;
    }

    /**
     * Credentials to authenticate with.
     *
     * <p>Must be properly scoped for accessing Cloud Pub/Sub APIs.
     */
    public Builder setCredentials(Credentials credentials) {
      this.credentials = Optional.of(Preconditions.checkNotNull(credentials));
      return this;
    }

    /**
     * ManagedChannelBuilder to use to create Channels.
     *
     * <p>Must point at Cloud Pub/Sub endpoint.
     */
    public Builder setChannelBuilder(
        ManagedChannelBuilder<? extends ManagedChannelBuilder<?>> channelBuilder) {
      this.channelBuilder =
          Optional.<ManagedChannelBuilder<? extends ManagedChannelBuilder<?>>>of(
              Preconditions.checkNotNull(channelBuilder));
      return this;
    }

    /** Sets the flow control settings. */
    public Builder setFlowControlSettings(FlowControlSettings flowControlSettings) {
      this.flowControlSettings = Preconditions.checkNotNull(flowControlSettings);
      return this;
    }

    /**
     * Set acknowledgement expiration padding.
     *
     * <p>This is the time accounted before a message expiration is to happen, so the {@link
     * Subscriber} is able to send an ack extension beforehand.
     *
     * <p>This padding duration is configurable so you can account for network latency. A reasonable
     * number must be provided so messages don't expire because of network latency between when the
     * ack extension is required and when it reaches the Pub/Sub service.
     *
     * @param ackExpirationPadding must be greater or equal to {@link #MIN_ACK_EXPIRATION_PADDING}
     */
    public Builder setAckExpirationPadding(Duration ackExpirationPadding) {
      Preconditions.checkArgument(ackExpirationPadding.compareTo(MIN_ACK_EXPIRATION_PADDING) >= 0);
      this.ackExpirationPadding = ackExpirationPadding;
      return this;
    }

    /**
     * Set the maximum period a message ack deadline will be extended.
     *
     * <p>It is recommended to set this value to a reasonable upper bound of the subscriber time to
     * process any message. This maximum period avoids messages to be <i>locked</i> by a subscriber
     * in cases when the {@link AckReply} is lost.
     *
     * <p>A zero duration effectively disables auto deadline extensions.
     */
    public Builder setMaxAckExtensionPeriod(Duration maxAckExtensionPeriod) {
      Preconditions.checkArgument(maxAckExtensionPeriod.getMillis() >= 0);
      this.maxAckExtensionPeriod = maxAckExtensionPeriod;
      return this;
    }

    /** Gives the ability to set a custom executor. */
    public Builder setExecutorProvider(ExecutorProvider executorProvider) {
      this.executorProvider = Preconditions.checkNotNull(executorProvider);
      return this;
    }

    /** Gives the ability to set a custom clock. */
    Builder setClock(ApiClock clock) {
      this.clock = Optional.of(clock);
      return this;
    }

    public Subscriber build() throws IOException {
      return new Subscriber(this);
    }
  }
}

