/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.zeebe.engine.processing.common;

import static io.zeebe.util.buffer.BufferUtil.cloneBuffer;
import static io.zeebe.util.buffer.BufferUtil.wrapString;

import io.zeebe.el.Expression;
import io.zeebe.engine.processing.bpmn.BpmnElementContext;
import io.zeebe.engine.processing.common.ExpressionProcessor.EvaluationException;
import io.zeebe.engine.processing.deployment.model.element.ExecutableCatchEvent;
import io.zeebe.engine.processing.deployment.model.element.ExecutableCatchEventSupplier;
import io.zeebe.engine.processing.deployment.model.element.ExecutableMessage;
import io.zeebe.engine.processing.message.MessageCorrelationKeyException;
import io.zeebe.engine.processing.message.MessageNameException;
import io.zeebe.engine.processing.message.command.SubscriptionCommandSender;
import io.zeebe.engine.processing.streamprocessor.MigratedStreamProcessors;
import io.zeebe.engine.processing.streamprocessor.sideeffect.SideEffects;
import io.zeebe.engine.processing.streamprocessor.writers.StateWriter;
import io.zeebe.engine.processing.streamprocessor.writers.TypedCommandWriter;
import io.zeebe.engine.processing.timer.DueDateTimerChecker;
import io.zeebe.engine.state.KeyGenerator;
import io.zeebe.engine.state.immutable.ProcessMessageSubscriptionState;
import io.zeebe.engine.state.immutable.TimerInstanceState;
import io.zeebe.engine.state.instance.TimerInstance;
import io.zeebe.engine.state.message.ProcessMessageSubscription;
import io.zeebe.engine.state.mutable.MutableEventScopeInstanceState;
import io.zeebe.engine.state.mutable.MutableZeebeState;
import io.zeebe.model.bpmn.util.time.Timer;
import io.zeebe.protocol.impl.SubscriptionUtil;
import io.zeebe.protocol.impl.record.value.message.ProcessMessageSubscriptionRecord;
import io.zeebe.protocol.impl.record.value.timer.TimerRecord;
import io.zeebe.protocol.record.intent.ProcessMessageSubscriptionIntent;
import io.zeebe.protocol.record.intent.TimerIntent;
import io.zeebe.protocol.record.value.BpmnElementType;
import io.zeebe.util.Either;
import io.zeebe.util.buffer.BufferUtil;
import io.zeebe.util.sched.clock.ActorClock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.agrona.DirectBuffer;

public final class CatchEventBehavior {

  private final ExpressionProcessor expressionProcessor;
  private final SubscriptionCommandSender subscriptionCommandSender;
  private final int partitionsCount;
  private final StateWriter stateWriter;

  private final MutableEventScopeInstanceState eventScopeInstanceState;
  private final ProcessMessageSubscriptionState processMessageSubscriptionState;
  private final TimerInstanceState timerInstanceState;

  private final ProcessMessageSubscriptionRecord subscription =
      new ProcessMessageSubscriptionRecord();
  private final TimerRecord timerRecord = new TimerRecord();
  private final Map<DirectBuffer, DirectBuffer> extractedCorrelationKeys = new HashMap<>();
  private final Map<DirectBuffer, Timer> evaluatedTimers = new HashMap<>();
  private final DueDateTimerChecker timerChecker;
  private final KeyGenerator keyGenerator;

  public CatchEventBehavior(
      final MutableZeebeState zeebeState,
      final ExpressionProcessor expressionProcessor,
      final SubscriptionCommandSender subscriptionCommandSender,
      final StateWriter stateWriter,
      final DueDateTimerChecker timerChecker,
      final int partitionsCount) {
    this.expressionProcessor = expressionProcessor;
    this.subscriptionCommandSender = subscriptionCommandSender;
    this.stateWriter = stateWriter;
    this.partitionsCount = partitionsCount;

    eventScopeInstanceState = zeebeState.getEventScopeInstanceState();
    timerInstanceState = zeebeState.getTimerState();
    processMessageSubscriptionState = zeebeState.getProcessMessageSubscriptionState();

    keyGenerator = zeebeState.getKeyGenerator();

    this.timerChecker = timerChecker;
  }

  public void unsubscribeFromEvents(
      final BpmnElementContext context,
      final TypedCommandWriter commandWriter,
      final SideEffects sideEffects) {

    unsubscribeFromTimerEvents(context, commandWriter);
    unsubscribeFromMessageEvents(context, sideEffects);

    // todo: remove after all are migrated
    if (!MigratedStreamProcessors.isMigrated(context.getBpmnElementType())) {
      eventScopeInstanceState.deleteInstance(context.getElementInstanceKey());
    }
  }

  public void subscribeToEvents(
      final BpmnElementContext context,
      final ExecutableCatchEventSupplier supplier,
      final SideEffects sideEffects,
      final TypedCommandWriter commandWriter)
      throws MessageCorrelationKeyException {

    final List<ExecutableCatchEvent> events = supplier.getEvents();

    // collect all message names from their respective variables, as this might fail and
    // we might need to raise an incident
    final Map<DirectBuffer, DirectBuffer> extractedMessageNames =
        extractMessageNames(events, context);
    // collect all message correlation keys from their respective variables, as this might fail and
    // we might need to raise an incident. This works the same for timers
    final Map<DirectBuffer, DirectBuffer> extractedCorrelationKeys =
        extractMessageCorrelationKeys(events, context);
    final Map<DirectBuffer, Timer> evaluatedTimers =
        evaluateTimers(events, context.getElementInstanceKey());

    // if all subscriptions are valid then open the subscriptions
    for (final ExecutableCatchEvent event : events) {
      if (event.isTimer()) {
        subscribeToTimerEvent(
            context.getElementInstanceKey(),
            context.getProcessInstanceKey(),
            context.getProcessDefinitionKey(),
            event.getId(),
            evaluatedTimers.get(event.getId()),
            commandWriter,
            sideEffects);
      } else if (event.isMessage()) {
        subscribeToMessageEvent(
            context,
            event,
            extractedCorrelationKeys.get(event.getId()),
            extractedMessageNames.get((event.getId())),
            sideEffects);
      }
    }

    // todo: remove after all are migrated
    if (!MigratedStreamProcessors.isMigrated(context.getBpmnElementType()) && !events.isEmpty()) {
      eventScopeInstanceState.createIfNotExists(
          context.getElementInstanceKey(), supplier.getInterruptingElementIds());
    }
  }

  public void subscribeToTimerEvent(
      final long elementInstanceKey,
      final long processInstanceKey,
      final long processDefinitionKey,
      final DirectBuffer handlerNodeId,
      final Timer timer,
      final TypedCommandWriter commandWriter,
      final SideEffects sideEffects) {
    final long dueDate = timer.getDueDate(ActorClock.currentTimeMillis());
    timerRecord.reset();
    timerRecord
        .setRepetitions(timer.getRepetitions())
        .setDueDate(dueDate)
        .setElementInstanceKey(elementInstanceKey)
        .setProcessInstanceKey(processInstanceKey)
        .setTargetElementId(handlerNodeId)
        .setProcessDefinitionKey(processDefinitionKey);

    sideEffects.add(
        () -> {
          /* timerChecker implements onRecovered to recover from restart, so no need to schedule
          this in TimerCreatedApplier.*/
          timerChecker.scheduleTimer(dueDate);
          return true;
        });

    stateWriter.appendFollowUpEvent(keyGenerator.nextKey(), TimerIntent.CREATED, timerRecord);
  }

  private void unsubscribeFromTimerEvents(
      final BpmnElementContext context, final TypedCommandWriter commandWriter) {
    timerInstanceState.forEachTimerForElementInstance(
        context.getElementInstanceKey(), t -> unsubscribeFromTimerEvent(t, commandWriter));
  }

  public void unsubscribeFromTimerEvent(
      final TimerInstance timer, final TypedCommandWriter commandWriter) {
    timerRecord.reset();
    timerRecord
        .setElementInstanceKey(timer.getElementInstanceKey())
        .setProcessInstanceKey(timer.getProcessInstanceKey())
        .setDueDate(timer.getDueDate())
        .setRepetitions(timer.getRepetitions())
        .setTargetElementId(timer.getHandlerNodeId())
        .setProcessDefinitionKey(timer.getProcessDefinitionKey());

    commandWriter.appendFollowUpCommand(timer.getKey(), TimerIntent.CANCEL, timerRecord);
  }

  private void subscribeToMessageEvent(
      final BpmnElementContext context,
      final ExecutableCatchEvent catchEvent,
      final DirectBuffer correlationKey,
      final DirectBuffer messageName,
      final SideEffects sideEffects) {

    final long processInstanceKey = context.getProcessInstanceKey();
    final DirectBuffer bpmnProcessId = cloneBuffer(context.getBpmnProcessId());
    final long elementInstanceKey = context.getElementInstanceKey();

    final int subscriptionPartitionId =
        SubscriptionUtil.getSubscriptionPartitionId(correlationKey, partitionsCount);

    subscription.setSubscriptionPartitionId(subscriptionPartitionId);
    subscription.setMessageName(messageName);
    subscription.setElementInstanceKey(elementInstanceKey);
    subscription.setProcessInstanceKey(processInstanceKey);
    subscription.setBpmnProcessId(bpmnProcessId);
    subscription.setCorrelationKey(correlationKey);
    subscription.setElementId(catchEvent.getId());
    subscription.setInterrupting(catchEvent.isInterrupting());

    final var subscriptionKey = keyGenerator.nextKey();
    stateWriter.appendFollowUpEvent(
        subscriptionKey, ProcessMessageSubscriptionIntent.CREATING, subscription);

    sideEffects.add(
        () ->
            sendOpenMessageSubscription(
                subscriptionPartitionId,
                processInstanceKey,
                elementInstanceKey,
                bpmnProcessId,
                messageName,
                correlationKey,
                catchEvent.isInterrupting()));
  }

  private void unsubscribeFromMessageEvents(
      final BpmnElementContext context, final SideEffects sideEffects) {
    processMessageSubscriptionState.visitElementSubscriptions(
        context.getElementInstanceKey(),
        subscription -> unsubscribeFromMessageEvent(subscription, sideEffects));
  }

  private boolean unsubscribeFromMessageEvent(
      final ProcessMessageSubscription subscription, final SideEffects sideEffects) {

    final DirectBuffer messageName = cloneBuffer(subscription.getRecord().getMessageNameBuffer());
    final int subscriptionPartitionId = subscription.getRecord().getSubscriptionPartitionId();
    final long processInstanceKey = subscription.getRecord().getProcessInstanceKey();
    final long elementInstanceKey = subscription.getRecord().getElementInstanceKey();

    stateWriter.appendFollowUpEvent(
        subscription.getKey(), ProcessMessageSubscriptionIntent.DELETING, subscription.getRecord());
    sideEffects.add(
        () ->
            sendCloseMessageSubscriptionCommand(
                subscriptionPartitionId, processInstanceKey, elementInstanceKey, messageName));

    return true;
  }

  private String extractCorrelationKey(
      final ExecutableMessage message, final long variableScopeKey) {

    final Expression correlationKeyExpression = message.getCorrelationKeyExpression();

    return expressionProcessor.evaluateMessageCorrelationKeyExpression(
        correlationKeyExpression, variableScopeKey);
  }

  private Either<Failure, String> extractMessageName(
      final ExecutableMessage message, final long scopeKey) {

    final Expression messageNameExpression = message.getMessageNameExpression();
    return expressionProcessor.evaluateStringExpression(messageNameExpression, scopeKey);
  }

  private boolean sendCloseMessageSubscriptionCommand(
      final int subscriptionPartitionId,
      final long processInstanceKey,
      final long elementInstanceKey,
      final DirectBuffer messageName) {
    return subscriptionCommandSender.closeMessageSubscription(
        subscriptionPartitionId, processInstanceKey, elementInstanceKey, messageName);
  }

  private boolean sendOpenMessageSubscription(
      final int subscriptionPartitionId,
      final long processInstanceKey,
      final long elementInstanceKey,
      final DirectBuffer bpmnProcessId,
      final DirectBuffer messageName,
      final DirectBuffer correlationKey,
      final boolean closeOnCorrelate) {
    return subscriptionCommandSender.openMessageSubscription(
        subscriptionPartitionId,
        processInstanceKey,
        elementInstanceKey,
        bpmnProcessId,
        messageName,
        correlationKey,
        closeOnCorrelate);
  }

  private Map<DirectBuffer, DirectBuffer> extractMessageCorrelationKeys(
      final List<ExecutableCatchEvent> events, final BpmnElementContext context) {
    extractedCorrelationKeys.clear();

    // TODO (4799): extract general variable scope behavior of boundary events
    for (final ExecutableCatchEvent event : events) {
      if (event.isMessage()) {
        final long variableScopeKey =
            event.getElementType() == BpmnElementType.BOUNDARY_EVENT
                ? context.getFlowScopeKey()
                : context.getElementInstanceKey();
        final String correlationKey = extractCorrelationKey(event.getMessage(), variableScopeKey);

        extractedCorrelationKeys.put(event.getId(), BufferUtil.wrapString(correlationKey));
      }
    }

    return extractedCorrelationKeys;
  }

  private Map<DirectBuffer, Timer> evaluateTimers(
      final List<ExecutableCatchEvent> events, final long key) {
    evaluatedTimers.clear();

    for (final ExecutableCatchEvent event : events) {
      if (event.isTimer()) {
        final Either<Failure, Timer> timerOrError =
            event.getTimerFactory().apply(expressionProcessor, key);
        if (timerOrError.isLeft()) {
          // todo(#4323): deal with this exceptional case without throwing an exception
          throw new EvaluationException(timerOrError.getLeft().getMessage());
        }
        evaluatedTimers.put(event.getId(), timerOrError.get());
      }
    }

    return evaluatedTimers;
  }

  private Map<DirectBuffer, DirectBuffer> extractMessageNames(
      final List<ExecutableCatchEvent> events, final BpmnElementContext context) {
    final Map<DirectBuffer, DirectBuffer> extractedMessageNames = new HashMap<>();

    final var scopeKey = context.getElementInstanceKey();
    for (final ExecutableCatchEvent event : events) {
      if (event.isMessage()) {
        final Either<Failure, String> messageNameOrFailure =
            extractMessageName(event.getMessage(), scopeKey);
        messageNameOrFailure.ifRightOrLeft(
            messageName -> extractedMessageNames.put(event.getId(), wrapString(messageName)),
            failure -> {
              // todo(#4323): deal with this exceptional case without throwing an exception
              throw new MessageNameException(failure, event.getId());
            });
      }
    }

    return extractedMessageNames;
  }
}
