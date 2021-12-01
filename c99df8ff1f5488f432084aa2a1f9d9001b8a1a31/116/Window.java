/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.metron.profiler.client.window;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A window is intended to compute the set of window intervals across time based on a reference time.
 * The intervals are specified using a Window selector statement, which is a quasi-natural language grammar.
 * Windows are intended to compute the set of intervals relative to a timestamp.
 */
public class Window {
  private Function<Long, Long> startMillis ;
  private Function<Long, Long> endMillis;
  private List<Function<Long, Predicate<Long>>> includes = new ArrayList<>();
  private List<Function<Long, Predicate<Long>>> excludes = new ArrayList<>();
  private Optional<Long> binWidth = Optional.empty();
  private Optional<Long> skipDistance = Optional.empty();

  /**
   * Return the start of the interval relative to the timestamp passed.
   * @param now
   * @return
   */
  public long getStartMillis(long now) {
    return startMillis.apply(now);
  }

  void setStartMillis(Function<Long, Long> startMillis) {
    this.startMillis = startMillis;
  }

  /**
   * Return the end of the interval relative to the timestamp passed.
   * @param now
   * @return
   */
  public Long getEndMillis(long now) {
    return endMillis.apply(now);
  }

  void setEndMillis(Function<Long, Long> endMillis) {
    this.endMillis = endMillis;
  }

  /**
   * Get the set of inclusion predicates.  If any of these are true as applied to the window interval start time,
   * then a field is included unless it's explicitly excluded.
   * @param now
   * @return
   */
  public Iterable<Predicate<Long>> getIncludes(long now) {
    return Iterables.transform(includes, f -> f.apply(now));
  }

  void setIncludes(List<Function<Long, Predicate<Long>>> includes) {
    this.includes = includes;
  }

  /**
   * Get the set of exclusion predicates.  If any of these exclusion predicates are true as applied to the window
   * interval start time, then the interval is excluded.  NOTE: Exclusions trump inclusions.
   * @param now
   * @return
   */
  public Iterable<Predicate<Long>> getExcludes(long now){
    return Iterables.transform(excludes, f -> f.apply(now));
  }

  void setExcludes(List<Function<Long, Predicate<Long>>> excludes) {
    this.excludes = excludes;
  }

  /**
   * The bin width.  This is fixed regardless of relative time.
   * @return
   */
  public Optional<Long> getBinWidth() {
    return binWidth;
  }

  void setBinWidth(long binWidth) {
    this.binWidth = Optional.of(binWidth);
  }

  /**
   * The skip distance.  How long between interval windows that one must go.
   * @return
   */
  public Optional<Long> getSkipDistance() {
    return skipDistance;
  }

  void setSkipDistance(long skipDistance) {
    this.skipDistance = Optional.of(skipDistance);
  }

  /**
   * Compute the set of sorted (oldest to newest) window intervals relative to the passed timestamp
   * given inclusion and exclusion predicates.
   *
   * @param now
   * @return
   */
  public List<Range<Long>> toIntervals(long now) {
    List<Range<Long>> intervals = new ArrayList<>();
    long startMillis = getStartMillis(now);
    long endMillis = getEndMillis(now);
    Iterable<Predicate<Long>> includes = getIncludes(now);
    Iterable<Predicate<Long>> excludes = getExcludes(now);
    //if we don't have a skip distance, then we just skip past everything to make the window dense
    long skipDistance = getSkipDistance().orElse(Long.MAX_VALUE);
    //if we don't have a window width, then we want the window to be completely dense.
    Optional<Long> binWidthOpt = getBinWidth();
    long binWidth = binWidthOpt.isPresent()?binWidthOpt.get():endMillis-startMillis;

    for(long left = startMillis;left >= 0 && left + binWidth <= endMillis;left += skipDistance) {
      Range<Long> interval = Range.between(left, left + binWidth);
      boolean include = includes.iterator().hasNext()?false:true;
      for(Predicate<Long> inclusionPredicate : includes) {
        include |= inclusionPredicate.test(left);
      }
      if(include) {
        for(Predicate<Long> exclusionPredicate : excludes) {
          include &= !exclusionPredicate.test(left);
        }
      }
      if(include) {
        intervals.add(interval);
      }
    }
    return intervals;
  }
}
