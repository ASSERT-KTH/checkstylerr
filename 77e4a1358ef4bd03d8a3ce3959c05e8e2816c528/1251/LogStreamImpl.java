/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.zeebe.logstreams.impl.log;

import io.zeebe.dispatcher.Dispatcher;
import io.zeebe.dispatcher.Dispatchers;
import io.zeebe.logstreams.impl.Loggers;
import io.zeebe.logstreams.log.LogStream;
import io.zeebe.logstreams.log.LogStreamBatchWriter;
import io.zeebe.logstreams.log.LogStreamReader;
import io.zeebe.logstreams.log.LogStreamRecordWriter;
import io.zeebe.logstreams.log.LogStreamWriter;
import io.zeebe.logstreams.storage.LogStorage;
import io.zeebe.logstreams.storage.LogStorageReader;
import io.zeebe.util.exception.UnrecoverableException;
import io.zeebe.util.health.FailureListener;
import io.zeebe.util.health.HealthStatus;
import io.zeebe.util.sched.Actor;
import io.zeebe.util.sched.ActorCondition;
import io.zeebe.util.sched.ActorScheduler;
import io.zeebe.util.sched.channel.ActorConditions;
import io.zeebe.util.sched.future.ActorFuture;
import io.zeebe.util.sched.future.CompletableActorFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import org.slf4j.Logger;

public final class LogStreamImpl extends Actor implements LogStream, FailureListener {

  private static final long INVALID_ADDRESS = -1L;

  private static final Logger LOG = Loggers.LOGSTREAMS_LOGGER;
  private static final String APPENDER_SUBSCRIPTION_NAME = "appender";

  private final ActorConditions onCommitPositionUpdatedConditions;
  private final String logName;
  private final int partitionId;
  private final int maxFrameLength;
  private final ActorScheduler actorScheduler;
  private final List<LogStreamReader> readers;
  private final LogStorage logStorage;
  private final CompletableActorFuture<Void> closeFuture;
  private final int nodeId;
  private ActorFuture<LogStorageAppender> appenderFuture;
  private Dispatcher writeBuffer;
  private LogStorageAppender appender;
  private long commitPosition;
  private Throwable closeError; // set if any error occurred during closeAsync
  private final String actorName;
  private FailureListener failureListener;
  private volatile HealthStatus healthStatus = HealthStatus.HEALTHY;

  LogStreamImpl(
      final ActorScheduler actorScheduler,
      final ActorConditions onCommitPositionUpdatedConditions,
      final String logName,
      final int partitionId,
      final int nodeId,
      final int maxFrameLength,
      final LogStorage logStorage) {
    this.actorScheduler = actorScheduler;
    this.onCommitPositionUpdatedConditions = onCommitPositionUpdatedConditions;
    this.logName = logName;

    this.partitionId = partitionId;
    this.nodeId = nodeId;
    actorName = buildActorName(nodeId, "LogStream", partitionId);

    this.maxFrameLength = maxFrameLength;
    this.logStorage = logStorage;
    closeFuture = new CompletableActorFuture<>();

    commitPosition = INVALID_ADDRESS;
    readers = new ArrayList<>();

    try (final LogStorageReader storageReader = logStorage.newReader();
        final LogStreamReader reader = new LogStreamReaderImpl(storageReader)) {
      internalSetCommitPosition(reader.seekToEnd());
    }
  }

  @Override
  public int getPartitionId() {
    return partitionId;
  }

  @Override
  public String getLogName() {
    return logName;
  }

  @Override
  public ActorFuture<Long> getCommitPositionAsync() {
    return actor.call(() -> commitPosition);
  }

  @Override
  public void setCommitPosition(final long commitPosition) {
    actor.call(() -> internalSetCommitPosition(commitPosition));
  }

  @Override
  public ActorFuture<LogStreamReader> newLogStreamReader() {
    return actor.call(this::createLogStreamReader);
  }

  @Override
  public ActorFuture<LogStreamRecordWriter> newLogStreamRecordWriter() {
    // this should be replaced after refactoring the actor control
    if (actor.isClosed()) {
      return CompletableActorFuture.completedExceptionally(new RuntimeException("Actor is closed"));
    }

    final var writerFuture = new CompletableActorFuture<LogStreamRecordWriter>();
    actor.run(() -> createWriter(writerFuture, LogStreamWriterImpl::new));
    return writerFuture;
  }

  @Override
  public ActorFuture<LogStreamBatchWriter> newLogStreamBatchWriter() {
    // this should be replaced after refactoring the actor control
    if (actor.isClosed()) {
      return CompletableActorFuture.completedExceptionally(new RuntimeException("Actor is closed"));
    }

    final var writerFuture = new CompletableActorFuture<LogStreamBatchWriter>();
    actor.run(() -> createWriter(writerFuture, LogStreamBatchWriterImpl::new));
    return writerFuture;
  }

  @Override
  public void registerOnCommitPositionUpdatedCondition(final ActorCondition condition) {
    actor.call(() -> onCommitPositionUpdatedConditions.registerConsumer(condition));
  }

  @Override
  public void removeOnCommitPositionUpdatedCondition(final ActorCondition condition) {
    actor.call(() -> onCommitPositionUpdatedConditions.removeConsumer(condition));
  }

  @Override
  public String getName() {
    return actorName;
  }

  @Override
  protected void onActorClosing() {
    LOG.info("On closing logstream {} close {} readers", logName, readers.size());
    readers.forEach(LogStreamReader::close);
  }

  @Override
  protected void onActorClosed() {
    if (closeError != null) {
      closeFuture.completeExceptionally(closeError);
    } else {
      closeFuture.complete(null);
    }
  }

  @Override
  public void close() {
    closeAsync().join();
  }

  @Override
  public ActorFuture<Void> closeAsync() {
    if (actor.isClosed()) {
      return closeFuture;
    }

    actor.run(
        () ->
            closeAppender()
                .onComplete(
                    (nothing, appenderError) -> {
                      closeError = appenderError;
                      actor.close();
                    }));
    return closeFuture;
  }

  @Override
  protected void handleFailure(final Exception failure) {
    if (failure instanceof UnrecoverableException) {
      onUnrecoverableFailure();
    }

    super.handleFailure(failure);
  }

  private LogStreamReader createLogStreamReader() {
    final LogStreamReader newReader = new LogStreamReaderImpl(logStorage.newReader());
    readers.add(newReader);
    return newReader;
  }

  private void internalSetCommitPosition(final long commitPosition) {
    if (commitPosition > this.commitPosition) {
      this.commitPosition = commitPosition;
      onCommitPositionUpdatedConditions.signalConsumers();
    }
  }

  private <T extends LogStreamWriter> void createWriter(
      final CompletableActorFuture<T> writerFuture, final WriterCreator<T> creator) {
    if (appender != null) {
      writerFuture.complete(creator.create(partitionId, writeBuffer));
    } else if (appenderFuture != null) {
      appenderFuture.onComplete(onOpenAppender(writerFuture, creator));
    } else {
      openAppender().onComplete(onOpenAppender(writerFuture, creator));
    }
  }

  private <T extends LogStreamWriter> BiConsumer<LogStorageAppender, Throwable> onOpenAppender(
      final CompletableActorFuture<T> writerFuture, final WriterCreator<T> creator) {
    return (openedAppender, errorOnOpeningAppender) -> {
      if (errorOnOpeningAppender == null) {
        writerFuture.complete(creator.create(partitionId, writeBuffer));
      } else {
        writerFuture.completeExceptionally(errorOnOpeningAppender);
      }
    };
  }

  private ActorFuture<Void> closeAppender() {
    final var closeAppenderFuture = new CompletableActorFuture<Void>();
    if (appender == null) {
      closeAppenderFuture.complete(null);
      return closeAppenderFuture;
    }

    appenderFuture = null;
    LOG.info("Close appender for log stream {}", logName);
    final var toCloseAppender = appender;
    final var toCloseWriteBuffer = writeBuffer;
    appender = null;
    writeBuffer = null;
    toCloseAppender
        .closeAsync()
        .onComplete(
            (v, t) -> {
              if (t == null) {
                toCloseWriteBuffer.closeAsync().onComplete(closeAppenderFuture);
              } else {
                closeAppenderFuture.completeExceptionally(t);
              }
            });
    return closeAppenderFuture;
  }

  private ActorFuture<LogStorageAppender> openAppender() {
    if (appenderFuture != null) {
      return appenderFuture;
    }

    final var appenderOpenFuture = new CompletableActorFuture<LogStorageAppender>();

    appenderFuture = appenderOpenFuture;

    final long lastPosition;
    try {
      lastPosition = getLastPosition();
    } catch (final UnrecoverableException e) {
      onUnrecoverableFailure();
      appenderFuture.completeExceptionally(e);
      return appenderFuture;
    }

    final long initialPosition;
    if (lastPosition > 0) {
      internalSetCommitPosition(lastPosition);
      initialPosition = lastPosition + 1;
    } else {
      initialPosition = 1;
    }

    writeBuffer =
        Dispatchers.create(buildActorName(nodeId, "dispatcher", partitionId))
            .maxFragmentLength(maxFrameLength)
            .initialPosition(initialPosition)
            .name(logName + "-write-buffer")
            .actorScheduler(actorScheduler)
            .build();

    writeBuffer
        .openSubscriptionAsync(APPENDER_SUBSCRIPTION_NAME)
        .onComplete(
            (subscription, throwable) -> {
              if (throwable == null) {
                appender =
                    new LogStorageAppender(
                        buildActorName(nodeId, "LogAppender", partitionId),
                        partitionId,
                        logStorage,
                        subscription,
                        maxFrameLength,
                        this::setCommitPosition);

                actorScheduler
                    .submitActor(appender)
                    .onComplete(
                        (v, t) -> {
                          if (t != null) {
                            onOpenAppenderFailed(t);
                          } else {
                            appenderFuture.complete(appender);
                            appender.addFailureListener(this);
                          }
                        });
              } else {
                onOpenAppenderFailed(throwable);
              }
            });

    return appenderOpenFuture;
  }

  private void onOpenAppenderFailed(final Throwable error) {
    LOG.error("Unexpected error when opening appender", error);
    appenderFuture.completeExceptionally(error);
    onFailure();
  }

  private long getLastPosition() {
    try (final LogStorageReader storageReader = logStorage.newReader();
        final LogStreamReader logReader = new LogStreamReaderImpl(storageReader)) {
      return logReader.seekToEnd();
    }
  }

  @Override
  public HealthStatus getHealthStatus() {
    return healthStatus;
  }

  @Override
  public void addFailureListener(final FailureListener failureListener) {
    actor.run(() -> this.failureListener = failureListener);
  }

  @Override
  public void onFailure() {
    actor.run(
        () -> {
          healthStatus = HealthStatus.UNHEALTHY;
          if (failureListener != null) {
            failureListener.onFailure();
          }
          closeAsync();
        });
  }

  @Override
  public void onRecovered() {
    actor.run(
        () -> {
          healthStatus = HealthStatus.HEALTHY;
          if (failureListener != null) {
            failureListener.onRecovered();
          }
        });
  }

  @Override
  public void onUnrecoverableFailure() {
    actor.run(
        () -> {
          healthStatus = HealthStatus.DEAD;
          if (failureListener != null) {
            failureListener.onUnrecoverableFailure();
          }
          closeAsync();
        });
  }

  @FunctionalInterface
  private interface WriterCreator<T extends LogStreamWriter> {

    T create(int partitionId, Dispatcher dispatcher);
  }
}
