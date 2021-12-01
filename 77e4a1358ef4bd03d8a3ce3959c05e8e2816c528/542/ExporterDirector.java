/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.zeebe.broker.exporter.stream;

import io.zeebe.broker.Loggers;
import io.zeebe.db.ZeebeDb;
import io.zeebe.engine.processing.streamprocessor.EventFilter;
import io.zeebe.engine.processing.streamprocessor.RecordValues;
import io.zeebe.engine.processing.streamprocessor.TypedEventImpl;
import io.zeebe.exporter.api.context.Context;
import io.zeebe.logstreams.log.LogStream;
import io.zeebe.logstreams.log.LogStreamReader;
import io.zeebe.logstreams.log.LoggedEvent;
import io.zeebe.protocol.impl.record.RecordMetadata;
import io.zeebe.protocol.impl.record.UnifiedRecordValue;
import io.zeebe.protocol.record.RecordType;
import io.zeebe.protocol.record.ValueType;
import io.zeebe.util.exception.UnrecoverableException;
import io.zeebe.util.health.FailureListener;
import io.zeebe.util.health.HealthMonitorable;
import io.zeebe.util.health.HealthStatus;
import io.zeebe.util.retry.BackOffRetryStrategy;
import io.zeebe.util.retry.EndlessRetryStrategy;
import io.zeebe.util.retry.RetryStrategy;
import io.zeebe.util.sched.Actor;
import io.zeebe.util.sched.ActorCondition;
import io.zeebe.util.sched.ActorScheduler;
import io.zeebe.util.sched.SchedulingHints;
import io.zeebe.util.sched.future.ActorFuture;
import io.zeebe.util.sched.future.CompletableActorFuture;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.agrona.LangUtil;
import org.slf4j.Logger;

public final class ExporterDirector extends Actor implements HealthMonitorable {

  private static final String ERROR_MESSAGE_EXPORTING_ABORTED =
      "Expected to export record '{}' successfully, but exception was thrown.";
  private static final String ERROR_MESSAGE_RECOVER_FROM_SNAPSHOT_FAILED =
      "Expected to find event with the snapshot position %s in log stream, but nothing was found. Failed to recover '%s'.";

  private static final Logger LOG = Loggers.EXPORTER_LOGGER;
  private final AtomicBoolean isOpened = new AtomicBoolean(false);
  private final List<ExporterContainer> containers;
  private final LogStream logStream;
  private final RecordExporter recordExporter;
  private final ZeebeDb zeebeDb;
  private final ExporterMetrics metrics;
  private final String name;
  private final RetryStrategy exportingRetryStrategy;
  private final RetryStrategy recordWrapStrategy;
  private final List<FailureListener> listeners = new ArrayList<>();
  private LogStreamReader logStreamReader;
  private EventFilter eventFilter;
  private ExportersState state;
  private volatile HealthStatus healthStatus = HealthStatus.HEALTHY;

  private ActorCondition onCommitPositionUpdatedCondition;
  private boolean inExportingPhase;
  private boolean isPaused;
  private ExporterPhase exporterPhase;

  public ExporterDirector(final ExporterDirectorContext context, final boolean shouldPauseOnStart) {
    name = context.getName();
    containers =
        context.getDescriptors().stream().map(ExporterContainer::new).collect(Collectors.toList());

    logStream = Objects.requireNonNull(context.getLogStream());
    final int partitionId = logStream.getPartitionId();
    metrics = new ExporterMetrics(partitionId);
    recordExporter = new RecordExporter(metrics, containers, partitionId);
    exportingRetryStrategy = new BackOffRetryStrategy(actor, Duration.ofSeconds(10));
    recordWrapStrategy = new EndlessRetryStrategy(actor);
    zeebeDb = context.getZeebeDb();
    isPaused = shouldPauseOnStart;
  }

  public ActorFuture<Void> startAsync(final ActorScheduler actorScheduler) {
    return actorScheduler.submitActor(this, SchedulingHints.ioBound());
  }

  public ActorFuture<Void> stopAsync() {
    return actor.close();
  }

  public ActorFuture<Void> pauseExporting() {
    return actor.call(
        () -> {
          isPaused = true;
          exporterPhase = ExporterPhase.PAUSED;
        });
  }

  public ActorFuture<Void> resumeExporting() {
    return actor.call(
        () -> {
          isPaused = false;
          exporterPhase = ExporterPhase.EXPORTING;
          actor.submit(this::readNextEvent);
        });
  }

  public ActorFuture<ExporterPhase> getPhase() {
    if (actor.isClosed()) {
      return CompletableActorFuture.completed(ExporterPhase.CLOSED);
    }
    return actor.call(() -> exporterPhase);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  protected void onActorStarting() {
    final ActorFuture<LogStreamReader> newReaderFuture = logStream.newLogStreamReader();
    actor.runOnCompletionBlockingCurrentPhase(
        newReaderFuture,
        (reader, errorOnReceivingReader) -> {
          if (errorOnReceivingReader == null) {
            logStreamReader = reader;
          } else {
            // TODO https://github.com/zeebe-io/zeebe/issues/3499
            // ideally we could fail the actor start future such that we are able to propagate the
            // error
            LOG.error(
                "Unexpected error on retrieving reader from log {}",
                logStream.getLogName(),
                errorOnReceivingReader);
            actor.close();
          }
        });
  }

  @Override
  protected void onActorStarted() {
    try {
      LOG.debug("Recovering exporter from snapshot");
      recoverFromSnapshot();

      for (final ExporterContainer container : containers) {
        container.initContainer(actor, metrics, state);
        container.configureExporter();
      }

      eventFilter = createEventFilter(containers);
      LOG.debug("Set event filter for exporters: {}", eventFilter);

    } catch (final Exception e) {
      onFailure();
      LangUtil.rethrowUnchecked(e);
    }

    isOpened.set(true);
    onSnapshotRecovered();
  }

  @Override
  protected void onActorClosing() {
    logStreamReader.close();
    if (onCommitPositionUpdatedCondition != null) {
      logStream.removeOnCommitPositionUpdatedCondition(onCommitPositionUpdatedCondition);
      onCommitPositionUpdatedCondition = null;
    }
  }

  @Override
  protected void onActorClosed() {
    LOG.debug("Closed exporter director '{}'.", getName());
    exporterPhase = ExporterPhase.CLOSED;
  }

  @Override
  protected void onActorCloseRequested() {
    isOpened.set(false);
    containers.forEach(ExporterContainer::close);
  }

  @Override
  protected void handleFailure(final Exception failure) {
    LOG.error("Actor '{}' failed in phase {} with: {} .", name, actor.getLifecyclePhase(), failure);
    actor.fail();

    if (failure instanceof UnrecoverableException) {
      healthStatus = HealthStatus.DEAD;

      for (final var listener : listeners) {
        listener.onUnrecoverableFailure();
      }
    } else {
      healthStatus = HealthStatus.UNHEALTHY;

      for (final var listener : listeners) {
        listener.onFailure();
      }
    }
  }

  private void recoverFromSnapshot() {
    state = new ExportersState(zeebeDb, zeebeDb.createContext());

    final long snapshotPosition = state.getLowestPosition();
    final boolean failedToRecoverReader = !logStreamReader.seekToNextEvent(snapshotPosition);
    if (failedToRecoverReader) {
      throw new IllegalStateException(
          String.format(ERROR_MESSAGE_RECOVER_FROM_SNAPSHOT_FAILED, snapshotPosition, getName()));
    }

    LOG.debug(
        "Recovered exporter '{}' from snapshot at lastExportedPosition {}",
        getName(),
        snapshotPosition);
  }

  private ExporterEventFilter createEventFilter(final List<ExporterContainer> containers) {

    final List<Context.RecordFilter> recordFilters =
        containers.stream().map(c -> c.getContext().getFilter()).collect(Collectors.toList());

    final Map<RecordType, Boolean> acceptRecordTypes =
        Arrays.stream(RecordType.values())
            .collect(
                Collectors.toMap(
                    Function.identity(),
                    type -> recordFilters.stream().anyMatch(f -> f.acceptType(type))));

    final Map<ValueType, Boolean> acceptValueTypes =
        Arrays.stream(ValueType.values())
            .collect(
                Collectors.toMap(
                    Function.identity(),
                    type -> recordFilters.stream().anyMatch(f -> f.acceptValue(type))));

    return new ExporterEventFilter(acceptRecordTypes, acceptValueTypes);
  }

  private void onFailure() {
    isOpened.set(false);
    actor.close();
  }

  private void onSnapshotRecovered() {
    onCommitPositionUpdatedCondition =
        actor.onCondition(
            getName() + "-on-commit-lastExportedPosition-updated", this::readNextEvent);
    logStream.registerOnCommitPositionUpdatedCondition(onCommitPositionUpdatedCondition);

    // start reading
    for (final ExporterContainer container : containers) {
      container.initPosition();
      container.openExporter();
    }

    clearExporterState();

    if (state.hasExporters()) {
      if (!isPaused) {
        exporterPhase = ExporterPhase.EXPORTING;
        actor.submit(this::readNextEvent);
      } else {
        exporterPhase = ExporterPhase.PAUSED;
      }

    } else {
      actor.close();
    }
  }

  private void skipRecord(final LoggedEvent currentEvent) {
    final RecordMetadata metadata = new RecordMetadata();
    final long eventPosition = currentEvent.getPosition();

    currentEvent.readMetadata(metadata);
    metrics.eventSkipped(metadata.getValueType());

    // increase position of all up to date exporters - an up to date exporter is one which has
    // acknowledged the last record we passed to it
    for (final ExporterContainer container : containers) {
      container.updatePositionOnSkipIfUpToDate(eventPosition);
    }

    actor.submit(this::readNextEvent);
  }

  private void readNextEvent() {
    if (shouldExport()) {
      final LoggedEvent currentEvent = logStreamReader.next();
      if (eventFilter == null || eventFilter.applies(currentEvent)) {
        inExportingPhase = true;
        exportEvent(currentEvent);
      } else {
        skipRecord(currentEvent);
      }
    }
  }

  private boolean shouldExport() {
    return isOpened.get() && logStreamReader.hasNext() && !inExportingPhase && !isPaused;
  }

  private void exportEvent(final LoggedEvent event) {
    final ActorFuture<Boolean> wrapRetryFuture =
        recordWrapStrategy.runWithRetry(
            () -> {
              recordExporter.wrap(event);
              return true;
            },
            this::isClosed);

    actor.runOnCompletion(
        wrapRetryFuture,
        (b, t) -> {
          assert t == null : "Throwable must be null";

          final ActorFuture<Boolean> retryFuture =
              exportingRetryStrategy.runWithRetry(recordExporter::export, this::isClosed);

          actor.runOnCompletion(
              retryFuture,
              (bool, throwable) -> {
                if (throwable != null) {
                  LOG.error(ERROR_MESSAGE_EXPORTING_ABORTED, event, throwable);
                  onFailure();
                } else {
                  metrics.eventExported(recordExporter.getTypedEvent().getValueType());
                  inExportingPhase = false;
                  actor.submit(this::readNextEvent);
                }
              });
        });
  }

  public ExportersState getState() {
    return state;
  }

  private void clearExporterState() {
    final List<String> exporterIds =
        containers.stream().map(ExporterContainer::getId).collect(Collectors.toList());

    state.visitPositions(
        (exporterId, position) -> {
          if (!exporterIds.contains(exporterId)) {
            state.removePosition(exporterId);
            LOG.info(
                "The exporter '{}' is not configured anymore. Its lastExportedPosition is removed from the state.",
                exporterId);
          }
        });
  }

  private boolean isClosed() {
    return !isOpened.get();
  }

  @Override
  public void addFailureListener(final FailureListener listener) {
    actor.run(() -> listeners.add(listener));
  }

  @Override
  public HealthStatus getHealthStatus() {
    return healthStatus;
  }

  private static class RecordExporter {

    private final RecordValues recordValues = new RecordValues();
    private final RecordMetadata rawMetadata = new RecordMetadata();
    private final List<ExporterContainer> containers;
    private final TypedEventImpl typedEvent;
    private final ExporterMetrics exporterMetrics;

    private boolean shouldExport;
    private int exporterIndex;

    RecordExporter(
        final ExporterMetrics exporterMetrics,
        final List<ExporterContainer> containers,
        final int partitionId) {
      this.containers = containers;
      typedEvent = new TypedEventImpl(partitionId);
      this.exporterMetrics = exporterMetrics;
    }

    void wrap(final LoggedEvent rawEvent) {
      rawEvent.readMetadata(rawMetadata);

      final UnifiedRecordValue recordValue =
          recordValues.readRecordValue(rawEvent, rawMetadata.getValueType());

      shouldExport = recordValue != null;
      if (shouldExport) {
        typedEvent.wrap(rawEvent, rawMetadata, recordValue);
        exporterIndex = 0;
      }
    }

    public boolean export() {
      if (!shouldExport) {
        return true;
      }

      final int exportersCount = containers.size();

      // current error handling strategy is simply to repeat forever until the record can be
      // successfully exported.
      while (exporterIndex < exportersCount) {
        final ExporterContainer container = containers.get(exporterIndex);

        if (container.exportRecord(rawMetadata, typedEvent)) {
          exporterIndex++;
          exporterMetrics.setLastExportedPosition(container.getId(), typedEvent.getPosition());
        } else {
          return false;
        }
      }

      return true;
    }

    TypedEventImpl getTypedEvent() {
      return typedEvent;
    }
  }

  private static class ExporterEventFilter implements EventFilter {

    private final RecordMetadata metadata = new RecordMetadata();
    private final Map<RecordType, Boolean> acceptRecordTypes;
    private final Map<ValueType, Boolean> acceptValueTypes;

    ExporterEventFilter(
        final Map<RecordType, Boolean> acceptRecordTypes,
        final Map<ValueType, Boolean> acceptValueTypes) {
      this.acceptRecordTypes = acceptRecordTypes;
      this.acceptValueTypes = acceptValueTypes;
    }

    @Override
    public boolean applies(final LoggedEvent event) {
      event.readMetadata(metadata);

      final RecordType recordType = metadata.getRecordType();
      final ValueType valueType = metadata.getValueType();

      return acceptRecordTypes.get(recordType) && acceptValueTypes.get(valueType);
    }

    @Override
    public String toString() {
      return "ExporterEventFilter{"
          + "acceptRecordTypes="
          + acceptRecordTypes
          + ", acceptValueTypes="
          + acceptValueTypes
          + '}';
    }
  }
}
