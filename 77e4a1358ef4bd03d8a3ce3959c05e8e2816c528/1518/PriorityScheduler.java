/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.zeebe.util.sched;

import static java.lang.Math.floorMod;

import io.zeebe.util.sched.clock.ActorClock;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

/**
 * Logic and state for the priority scheduling. Each {@link ActorThread} maintains a local instance
 * if this class.
 */
public final class PriorityScheduler implements TaskScheduler {
  private static final int TIME_SLICES_PER_SECOND = 100;
  private static final long TIME_SLICE_LENTH_NS =
      TimeUnit.MILLISECONDS.toNanos(1000 / TIME_SLICES_PER_SECOND);
  /** the function used to acquire a task for a given priority */
  private final IntFunction<ActorTask> getTaskFn;
  /** the current run */
  private final Run currentRun;
  /** how many priorities there are */
  private final int priorityCount;
  /** pre-calculated priorities for time slices */
  private final int[] slicePriorities;

  /**
   * @param getTaskFn function which can be used to get a task by a given priority class.
   * @param quotas the quotas by priority class. Must be an array of doubles, assigning a quota to
   *     the priority denoted by the index in the array (if quotas[0] = 0.3, then the quota for the
   *     priority class '0' is 0.3). A quota must be a number between [0..1], such that the sum of
   *     all quotas in the array is exactly 1. The quota multiplied by 100 corresponds to the number
   *     of time slices assigned to this priority class in a second.
   */
  public PriorityScheduler(final IntFunction<ActorTask> getTaskFn, final double[] quotas) {
    this.getTaskFn = getTaskFn;
    priorityCount = quotas.length;
    slicePriorities = calclateSlicePriorities(quotas);
    currentRun = new Run();
  }

  /*
   * TODO: ask smarter person to make this better :)
   */
  private static int[] calclateSlicePriorities(final double[] quotas) {
    final int[] slicePriorities = new int[TIME_SLICES_PER_SECOND];

    final int[] sliceBudgetByPriority = new int[quotas.length];
    for (int i = 0; i < sliceBudgetByPriority.length; i++) {
      sliceBudgetByPriority[i] = (int) (TIME_SLICES_PER_SECOND * quotas[i]);
    }

    for (int i = 0; i < slicePriorities.length; i++) {
      int assignedPriority = 0;

      for (int p = 0; p < quotas.length; p++) {
        if (sliceBudgetByPriority[p] > 0) {
          final int budget = (int) (TIME_SLICES_PER_SECOND * quotas[p]);
          final int q = TIME_SLICES_PER_SECOND / budget;

          if (i / q > budget - sliceBudgetByPriority[p]) {
            assignedPriority = p;
            break;
          }
        }
      }

      for (int p = assignedPriority; p < assignedPriority + quotas.length; p++) {
        final int offset = p % quotas.length;

        if (sliceBudgetByPriority[offset] > 0) {
          sliceBudgetByPriority[offset]--;
          slicePriorities[i] = offset;
          break;
        }
      }
    }

    return slicePriorities;
  }

  /** calculates and returns the next task to execute or null if no such task can be determined. */
  @Override
  public ActorTask getNextTask(final ActorClock clock) {
    final int priority = currentRun.getTimeSlicePriority(clock.getNanoTime());

    // if no task at the given priority level is available, try executing a
    // task at the next highest priority etc.
    // terminate after each priority class has been tried once.

    ActorTask nextTask = null;

    for (int p = priority; nextTask == null && p > priority - priorityCount; p--) {
      final int priorityClass = p >= 0 ? p : floorMod(p, priorityCount);
      nextTask = getTaskFn.apply(priorityClass);
    }

    return nextTask;
  }

  /** Corresponds to one second. A new run is started every second */
  class Run {
    /** the nanotime when this run was started */
    long startNs = 0;

    long sliceId = 0;

    int getTimeSlicePriority(final long now) {
      sliceId = ((now - startNs) / TIME_SLICE_LENTH_NS);

      if (sliceId >= TIME_SLICES_PER_SECOND) {
        startNs = now;
        sliceId = sliceId % TIME_SLICES_PER_SECOND;
      }

      return slicePriorities[(int) sliceId];
    }
  }
}
