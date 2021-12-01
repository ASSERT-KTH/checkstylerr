/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.zeebe.broker.system.partitions.impl.steps;

import io.zeebe.broker.system.partitions.PartitionContext;
import io.zeebe.broker.system.partitions.PartitionStep;
import io.zeebe.engine.processing.streamprocessor.StreamProcessor;
import io.zeebe.engine.state.appliers.EventAppliers;
import io.zeebe.engine.state.mutable.MutableZeebeState;
import io.zeebe.util.sched.ActorControl;
import io.zeebe.util.sched.future.ActorFuture;
import io.zeebe.util.sched.future.CompletableActorFuture;

public class StreamProcessorPartitionStep implements PartitionStep {

  @Override
  public ActorFuture<Void> open(final PartitionContext context) {
    final StreamProcessor streamProcessor = createStreamProcessor(context);
    final ActorFuture<Void> openFuture = streamProcessor.openAsync(!context.shouldProcess());
    final CompletableActorFuture<Void> future = new CompletableActorFuture<>();

    openFuture.onComplete(
        (nothing, err) -> {
          if (err == null) {
            context.setStreamProcessor(streamProcessor);

            // Have to pause/resume it here in case the state changed after streamProcessor was
            // created
            if (!context.shouldProcess()) {
              streamProcessor.pauseProcessing();
            } else {
              streamProcessor.resumeProcessing();
            }

            context
                .getComponentHealthMonitor()
                .registerComponent(streamProcessor.getName(), streamProcessor);
            future.complete(null);
          } else {
            future.completeExceptionally(err);
          }
        });

    return future;
  }

  @Override
  public ActorFuture<Void> close(final PartitionContext context) {
    context.getComponentHealthMonitor().removeComponent(context.getStreamProcessor().getName());
    final ActorFuture<Void> future = context.getStreamProcessor().closeAsync();
    context.setStreamProcessor(null);
    return future;
  }

  @Override
  public String getName() {
    return "StreamProcessor";
  }

  private StreamProcessor createStreamProcessor(final PartitionContext state) {
    return StreamProcessor.builder()
        .logStream(state.getLogStream())
        .actorScheduler(state.getScheduler())
        .zeebeDb(state.getZeebeDb())
        .eventApplierFactory(EventAppliers::new)
        .nodeId(state.getNodeId())
        .commandResponseWriter(state.getCommandApiService().newCommandResponseWriter())
        .onProcessedListener(
            state.getCommandApiService().getOnProcessedListener(state.getPartitionId()))
        .streamProcessorFactory(
            processingContext -> {
              final ActorControl actor = processingContext.getActor();
              final MutableZeebeState zeebeState = processingContext.getZeebeState();
              return state
                  .getTypedRecordProcessorsFactory()
                  .createTypedStreamProcessor(actor, zeebeState, processingContext);
            })
        .build();
  }
}
