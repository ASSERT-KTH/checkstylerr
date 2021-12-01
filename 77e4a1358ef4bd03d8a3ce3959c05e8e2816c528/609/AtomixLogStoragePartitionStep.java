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
import io.zeebe.logstreams.storage.atomix.AtomixLogStorage;
import io.zeebe.util.sched.future.ActorFuture;
import io.zeebe.util.sched.future.CompletableActorFuture;

public class AtomixLogStoragePartitionStep implements PartitionStep {
  private static final String WRONG_TERM_ERROR_MSG =
      "Expected that current term '%d' is same as raft term '%d', but was not. Failing installation of 'AtomixLogStoragePartitionStep' on partition %d.";

  @Override
  public ActorFuture<Void> open(final long currentTerm, final PartitionContext context) {
    final var openFuture = new CompletableActorFuture<Void>();
    final var server = context.getRaftPartition().getServer();

    final var appenderOptional = server.getAppender();
    appenderOptional.ifPresentOrElse(
        logAppender -> {
          final var raftTerm = server.getTerm();

          if (raftTerm != currentTerm) {
            openFuture.completeExceptionally(
                new IllegalStateException(
                    String.format(
                        WRONG_TERM_ERROR_MSG, currentTerm, raftTerm, context.getPartitionId())));
          } else {
            context.setAtomixLogStorage(
                AtomixLogStorage.ofPartition(server::openReader, logAppender));
            openFuture.complete(null);
          }
        },
        () ->
            openFuture.completeExceptionally(
                new IllegalStateException("Not leader of partition " + context.getPartitionId())));

    return openFuture;
  }

  @Override
  public ActorFuture<Void> close(final PartitionContext context) {
    context.setAtomixLogStorage(null);
    return CompletableActorFuture.completed(null);
  }

  @Override
  public String getName() {
    return "AtomixLogStorage";
  }
}
