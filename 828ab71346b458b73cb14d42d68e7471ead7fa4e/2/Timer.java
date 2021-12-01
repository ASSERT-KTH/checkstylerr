/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.storm.kafka.spout.internal;

import java.util.concurrent.TimeUnit;

/*
 This file is pulled from Apache Storm, with some modification to support lower version of
 Apache Storm.

 - Time.nanoTime() is introduced in Storm 1.1.0 so we changed to System.nanoTime()
 -- Time.nanoTime() calls System.nanoTime() when it's not in time simulation mode.
*/

public class Timer {
  private final long delay;
  private final long period;
  private final TimeUnit timeUnit;
  private final long periodNanos;
  private long start;

  public Timer(long delay, long period, TimeUnit timeUnit) {
    this.delay = delay;
    this.period = period;
    this.timeUnit = timeUnit;
    this.periodNanos = timeUnit.toNanos(period);
    this.start = System.nanoTime() + timeUnit.toNanos(delay);
  }

  public long period() {
    return this.period;
  }

  public long delay() {
    return this.delay;
  }

  public TimeUnit getTimeUnit() {
    return this.timeUnit;
  }

  public boolean isExpiredResetOnTrue() {
    boolean expired = System.nanoTime() - this.start >= this.periodNanos;
    if(expired) {
      this.start = System.nanoTime();
    }

    return expired;
  }
}
