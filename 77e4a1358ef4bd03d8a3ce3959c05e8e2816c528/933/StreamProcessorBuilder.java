/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.zeebe.engine.processing.streamprocessor;

import io.zeebe.db.ZeebeDb;
import io.zeebe.engine.processing.streamprocessor.writers.CommandResponseWriter;
import io.zeebe.engine.state.EventApplier;
import io.zeebe.engine.state.mutable.MutableZeebeState;
import io.zeebe.logstreams.log.LogStream;
import io.zeebe.util.sched.ActorScheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class StreamProcessorBuilder {

  private final ProcessingContext processingContext;
  private final List<StreamProcessorLifecycleAware> lifecycleListeners = new ArrayList<>();
  private TypedRecordProcessorFactory typedRecordProcessorFactory;
  private ActorScheduler actorScheduler;
  private ZeebeDb zeebeDb;
  private Function<MutableZeebeState, EventApplier> eventApplierFactory;
  private int nodeId;

  public StreamProcessorBuilder() {
    processingContext = new ProcessingContext();
  }

  public StreamProcessorBuilder streamProcessorFactory(
      final TypedRecordProcessorFactory typedRecordProcessorFactory) {
    this.typedRecordProcessorFactory = typedRecordProcessorFactory;
    return this;
  }

  public StreamProcessorBuilder actorScheduler(final ActorScheduler actorScheduler) {
    this.actorScheduler = actorScheduler;
    return this;
  }

  public StreamProcessorBuilder nodeId(final int nodeId) {
    this.nodeId = nodeId;
    return this;
  }

  public StreamProcessorBuilder logStream(final LogStream stream) {
    processingContext.logStream(stream);
    return this;
  }

  public StreamProcessorBuilder commandResponseWriter(
      final CommandResponseWriter commandResponseWriter) {
    processingContext.commandResponseWriter(commandResponseWriter);
    return this;
  }

  public StreamProcessorBuilder onProcessedListener(final Consumer<TypedRecord> onProcessed) {
    processingContext.onProcessedListener(onProcessed);
    return this;
  }

  public StreamProcessorBuilder zeebeDb(final ZeebeDb zeebeDb) {
    this.zeebeDb = zeebeDb;
    return this;
  }

  public StreamProcessorBuilder eventApplierFactory(
      final Function<MutableZeebeState, EventApplier> eventApplierFactory) {
    this.eventApplierFactory = eventApplierFactory;
    return this;
  }

  public TypedRecordProcessorFactory getTypedRecordProcessorFactory() {
    return typedRecordProcessorFactory;
  }

  public ProcessingContext getProcessingContext() {
    return processingContext;
  }

  public ActorScheduler getActorScheduler() {
    return actorScheduler;
  }

  public List<StreamProcessorLifecycleAware> getLifecycleListeners() {
    return lifecycleListeners;
  }

  public ZeebeDb getZeebeDb() {
    return zeebeDb;
  }

  public int getNodeId() {
    return nodeId;
  }

  public Function<MutableZeebeState, EventApplier> getEventApplierFactory() {
    return eventApplierFactory;
  }

  public StreamProcessor build() {
    validate();

    return new StreamProcessor(this);
  }

  private void validate() {
    Objects.requireNonNull(typedRecordProcessorFactory, "No stream processor factory provided.");
    Objects.requireNonNull(actorScheduler, "No task scheduler provided.");
    Objects.requireNonNull(processingContext.getLogStream(), "No log stream provided.");
    Objects.requireNonNull(
        processingContext.getWriters().response(), "No command response writer provided.");
    Objects.requireNonNull(zeebeDb, "No database provided.");
    Objects.requireNonNull(eventApplierFactory, "No factory for the event supplier provided.");
  }
}
