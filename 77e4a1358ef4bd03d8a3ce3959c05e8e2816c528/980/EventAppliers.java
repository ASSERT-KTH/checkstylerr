/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.zeebe.engine.state.appliers;

import io.zeebe.engine.Loggers;
import io.zeebe.engine.state.EventApplier;
import io.zeebe.engine.state.TypedEventApplier;
import io.zeebe.engine.state.mutable.MutableProcessMessageSubscriptionState;
import io.zeebe.engine.state.mutable.MutableZeebeState;
import io.zeebe.protocol.record.RecordValue;
import io.zeebe.protocol.record.intent.DeploymentDistributionIntent;
import io.zeebe.protocol.record.intent.DeploymentIntent;
import io.zeebe.protocol.record.intent.IncidentIntent;
import io.zeebe.protocol.record.intent.Intent;
import io.zeebe.protocol.record.intent.JobBatchIntent;
import io.zeebe.protocol.record.intent.JobIntent;
import io.zeebe.protocol.record.intent.MessageIntent;
import io.zeebe.protocol.record.intent.MessageStartEventSubscriptionIntent;
import io.zeebe.protocol.record.intent.MessageSubscriptionIntent;
import io.zeebe.protocol.record.intent.ProcessEventIntent;
import io.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.zeebe.protocol.record.intent.ProcessIntent;
import io.zeebe.protocol.record.intent.ProcessMessageSubscriptionIntent;
import io.zeebe.protocol.record.intent.TimerIntent;
import io.zeebe.protocol.record.intent.VariableIntent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;

/**
 * Applies state changes from events to the {@link MutableZeebeState}.
 *
 * <p>Finds the correct {@link TypedEventApplier} and delegates.
 */
public final class EventAppliers implements EventApplier {

  private static final Logger LOG = Loggers.STREAM_PROCESSING;

  // todo (#6202): after migration this should log at WARN level
  private static final Function<Intent, TypedEventApplier<?, ?>> UNIMPLEMENTED_EVENT_APPLIER =
      intent ->
          (key, value) ->
              LOG.trace(
                  "No state changed: tried to use unimplemented event applier {}.{}",
                  intent.getClass().getSimpleName(),
                  intent);

  @SuppressWarnings("rawtypes")
  private final Map<Intent, TypedEventApplier> mapping = new HashMap<>();

  public EventAppliers(final MutableZeebeState state) {
    registerProcessInstanceEventAppliers(state);

    register(ProcessIntent.CREATED, new ProcessCreatedApplier(state));
    registerDeploymentAppliers(state);

    registerMessageAppliers(state);
    registerMessageSubscriptionAppliers(state);
    registerMessageStartEventSubscriptionAppliers(state);

    registerJobIntentEventAppliers(state);
    registerVariableEventAppliers(state);
    register(JobBatchIntent.ACTIVATED, new JobBatchActivatedApplier(state));
    registerIncidentEventAppliers(state);
    registerProcessMessageSubscriptionEventAppliers(state);
    registerTimeEventAppliers(state);
    registerProcessEventAppliers(state);
  }

  private void registerTimeEventAppliers(final MutableZeebeState state) {
    register(TimerIntent.CREATED, new TimerCreatedApplier(state.getTimerState()));
    register(TimerIntent.CANCELED, new TimerCancelledApplier(state.getTimerState()));
    register(TimerIntent.TRIGGERED, new TimerTriggeredApplier(state.getTimerState()));
  }

  private void registerDeploymentAppliers(final MutableZeebeState state) {
    register(DeploymentDistributionIntent.DISTRIBUTING, new DeploymentDistributionApplier(state));
    register(
        DeploymentDistributionIntent.COMPLETED,
        new DeploymentDistributionCompletedApplier(state.getDeploymentState()));

    register(DeploymentIntent.CREATED, new DeploymentCreatedApplier(state.getDeploymentState()));
    register(
        DeploymentIntent.DISTRIBUTED, new DeploymentDistributedApplier(state.getProcessState()));
    register(
        DeploymentIntent.FULLY_DISTRIBUTED,
        new DeploymentFullyDistributedApplier(state.getDeploymentState()));
  }

  private void registerVariableEventAppliers(final MutableZeebeState state) {
    final VariableApplier variableApplier = new VariableApplier(state.getVariableState());
    register(VariableIntent.CREATED, variableApplier);
    register(VariableIntent.UPDATED, variableApplier);
  }

  private void registerProcessInstanceEventAppliers(final MutableZeebeState state) {
    final var elementInstanceState = state.getElementInstanceState();
    final var eventScopeInstanceState = state.getEventScopeInstanceState();
    final var processState = state.getProcessState();
    final var variableState = state.getVariableState();
    final var bufferedStartMessageEventStateApplier =
        new BufferedStartMessageEventStateApplier(processState, state.getMessageState());

    register(
        ProcessInstanceIntent.ELEMENT_ACTIVATING,
        new ProcessInstanceElementActivatingApplier(
            elementInstanceState, processState, variableState, eventScopeInstanceState));
    register(
        ProcessInstanceIntent.ELEMENT_ACTIVATED,
        new ProcessInstanceElementActivatedApplier(elementInstanceState));
    register(
        ProcessInstanceIntent.ELEMENT_COMPLETING,
        new ProcessInstanceElementCompletingApplier(elementInstanceState));
    register(
        ProcessInstanceIntent.ELEMENT_COMPLETED,
        new ProcessInstanceElementCompletedApplier(
            elementInstanceState,
            eventScopeInstanceState,
            variableState,
            processState,
            bufferedStartMessageEventStateApplier));
    register(
        ProcessInstanceIntent.ELEMENT_TERMINATING,
        new ProcessInstanceElementTerminatingApplier(elementInstanceState));
    register(
        ProcessInstanceIntent.ELEMENT_TERMINATED,
        new ProcessInstanceElementTerminatedApplier(
            elementInstanceState, eventScopeInstanceState, bufferedStartMessageEventStateApplier));
    register(
        ProcessInstanceIntent.SEQUENCE_FLOW_TAKEN,
        new ProcessInstanceSequenceFlowTakenApplier(elementInstanceState, processState));
  }

  private void registerJobIntentEventAppliers(final MutableZeebeState state) {
    register(JobIntent.CANCELED, new JobCanceledApplier(state));
    register(JobIntent.COMPLETED, new JobCompletedApplier(state));
    register(JobIntent.CREATED, new JobCreatedApplier(state));
    register(JobIntent.ERROR_THROWN, new JobErrorThrownApplier(state));
    register(JobIntent.FAILED, new JobFailedApplier(state));
    register(JobIntent.RETRIES_UPDATED, new JobRetriesUpdatedApplier(state));
    register(JobIntent.TIMED_OUT, new JobTimedOutApplier(state));
  }

  private void registerMessageAppliers(final MutableZeebeState state) {
    register(MessageIntent.PUBLISHED, new MessagePublishedApplier(state.getMessageState()));
    register(MessageIntent.EXPIRED, new MessageExpiredApplier(state.getMessageState()));
  }

  private void registerMessageSubscriptionAppliers(final MutableZeebeState state) {
    register(
        MessageSubscriptionIntent.CREATED,
        new MessageSubscriptionCreatedApplier(state.getMessageSubscriptionState()));
    register(
        MessageSubscriptionIntent.CORRELATING,
        new MessageSubscriptionCorrelatingApplier(
            state.getMessageSubscriptionState(), state.getMessageState()));
    register(
        MessageSubscriptionIntent.CORRELATED,
        new MessageSubscriptionCorrelatedApplier(state.getMessageSubscriptionState()));
    register(
        MessageSubscriptionIntent.REJECTED,
        new MessageSubscriptionRejectedApplier(state.getMessageState()));
    register(
        MessageSubscriptionIntent.DELETED,
        new MessageSubscriptionDeletedApplier(state.getMessageSubscriptionState()));
  }

  private void registerMessageStartEventSubscriptionAppliers(final MutableZeebeState state) {
    register(
        MessageStartEventSubscriptionIntent.CREATED,
        new MessageStartEventSubscriptionCreatedApplier(
            state.getMessageStartEventSubscriptionState(), state.getEventScopeInstanceState()));
    register(
        MessageStartEventSubscriptionIntent.CORRELATED,
        new MessageStartEventSubscriptionCorrelatedApplier(
            state.getMessageState(), state.getEventScopeInstanceState()));
    register(
        MessageStartEventSubscriptionIntent.DELETED,
        new MessageStartEventSubscriptionDeletedApplier(
            state.getMessageStartEventSubscriptionState(), state.getEventScopeInstanceState()));
  }

  private void registerIncidentEventAppliers(final MutableZeebeState state) {
    register(
        IncidentIntent.CREATED,
        new IncidentCreatedApplier(state.getIncidentState(), state.getJobState()));
    register(
        IncidentIntent.RESOLVED,
        new IncidentResolvedApplier(state.getIncidentState(), state.getJobState()));
  }

  private void registerProcessMessageSubscriptionEventAppliers(final MutableZeebeState state) {
    final MutableProcessMessageSubscriptionState subscriptionState =
        state.getProcessMessageSubscriptionState();

    register(
        ProcessMessageSubscriptionIntent.CREATING,
        new ProcessMessageSubscriptionCreatingApplier(subscriptionState));
    register(
        ProcessMessageSubscriptionIntent.CREATED,
        new ProcessMessageSubscriptionCreatedApplier(subscriptionState));
    register(
        ProcessMessageSubscriptionIntent.CORRELATED,
        new ProcessMessageSubscriptionCorrelatedApplier(
            subscriptionState, state.getVariableState()));
    register(
        ProcessMessageSubscriptionIntent.DELETING,
        new ProcessMessageSubscriptionDeletingApplier(subscriptionState));
    register(
        ProcessMessageSubscriptionIntent.DELETED,
        new ProcessMessageSubscriptionDeletedApplier(subscriptionState));
  }

  private void registerProcessEventAppliers(final MutableZeebeState state) {
    register(
        ProcessEventIntent.TRIGGERING,
        new ProcessEventTriggeringApplier(
            state.getEventScopeInstanceState(),
            state.getElementInstanceState(),
            state.getProcessState()));
    register(
        ProcessEventIntent.TRIGGERED,
        new ProcessEventTriggeredApplier(state.getEventScopeInstanceState()));
  }

  private <I extends Intent> void register(final I intent, final TypedEventApplier<I, ?> applier) {
    mapping.put(intent, applier);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void applyState(final long key, final Intent intent, final RecordValue value) {
    final var eventApplier =
        mapping.getOrDefault(intent, UNIMPLEMENTED_EVENT_APPLIER.apply(intent));
    eventApplier.applyState(key, value);
  }
}
