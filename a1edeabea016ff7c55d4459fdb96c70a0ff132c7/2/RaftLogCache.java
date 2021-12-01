/*
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
package org.apache.ratis.server.storage;

import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.server.RaftServerConfigKeys;
import org.apache.ratis.server.impl.RaftServerConstants;
import org.apache.ratis.server.impl.ServerProtoUtils;
import org.apache.ratis.server.protocol.TermIndex;
import org.apache.ratis.server.storage.CacheInvalidationPolicy.CacheInvalidationPolicyDefault;
import org.apache.ratis.server.storage.LogSegment.LogRecord;
import org.apache.ratis.server.storage.RaftStorageDirectory.LogPathAndIndex;
import org.apache.ratis.proto.RaftProtos.LogEntryProto;
import org.apache.ratis.util.AutoCloseableLock;
import org.apache.ratis.util.AutoCloseableReadWriteLock;
import org.apache.ratis.util.JavaUtils;
import org.apache.ratis.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static org.apache.ratis.server.impl.RaftServerConstants.INVALID_LOG_INDEX;

/**
 * In-memory RaftLog Cache. Currently we provide a simple implementation that
 * caches all the segments in the memory. The cache is not thread-safe and
 * requires external lock protection.
 */
class RaftLogCache {
  public static final Logger LOG = LoggerFactory.getLogger(RaftLogCache.class);

  static class SegmentFileInfo {
    final long startIndex; // start index of the segment
    final long endIndex; // original end index
    final boolean isOpen;
    final long targetLength; // position for truncation
    final long newEndIndex; // new end index after the truncation

    SegmentFileInfo(long start, long end, boolean isOpen, long targetLength,
        long newEndIndex) {
      this.startIndex = start;
      this.endIndex = end;
      this.isOpen = isOpen;
      this.targetLength = targetLength;
      this.newEndIndex = newEndIndex;
    }

    @Override
    public String toString() {
      return "(" + startIndex + ", " + endIndex
          + ") isOpen? " + isOpen + ", length=" + targetLength
          + ", newEndIndex=" + newEndIndex;
    }
  }

  static class TruncationSegments {
    final SegmentFileInfo toTruncate; // name of the file to be truncated
    final SegmentFileInfo[] toDelete; // names of the files to be deleted

    TruncationSegments(SegmentFileInfo toTruncate,
        List<SegmentFileInfo> toDelete) {
      this.toDelete = toDelete == null ? null :
          toDelete.toArray(new SegmentFileInfo[toDelete.size()]);
      this.toTruncate = toTruncate;
    }

    @Override
    public String toString() {
      return "toTruncate: " + toTruncate
          + "\n  toDelete: " + Arrays.toString(toDelete);
    }
  }

  static class LogSegmentList {
    private final Object name;
    private final List<LogSegment> segments = new ArrayList<>();
    private final AutoCloseableReadWriteLock lock;

    LogSegmentList(Object name) {
      this.name = name;
      this.lock = new AutoCloseableReadWriteLock(name);
    }

    AutoCloseableLock readLock() {
      final StackTraceElement caller = LOG.isTraceEnabled()? JavaUtils.getCallerStackTraceElement(): null;
      return lock.readLock(caller, LOG::trace);
    }

    AutoCloseableLock writeLock() {
      final StackTraceElement caller = LOG.isTraceEnabled()? JavaUtils.getCallerStackTraceElement(): null;
      return lock.writeLock(caller, LOG::trace);
    }

    boolean isEmpty() {
      try(AutoCloseableLock readLock = readLock()) {
        return segments.isEmpty();
      }
    }

    int size() {
      try(AutoCloseableLock readLock = readLock()) {
        return segments.size();
      }
    }

    long countCached() {
      try(AutoCloseableLock readLock = readLock()) {
        return segments.stream().filter(LogSegment::hasCache).count();
      }
    }

    LogSegment getLast() {
      try(AutoCloseableLock readLock = readLock()) {
        return segments.isEmpty()? null: segments.get(segments.size() - 1);
      }
    }

    LogSegment get(int i) {
      try(AutoCloseableLock readLock = readLock()) {
        return segments.get(i);
      }
    }

    int binarySearch(long index) {
      try(AutoCloseableLock readLock = readLock()) {
        return Collections.binarySearch(segments, index);
      }
    }

    LogSegment search(long index) {
      try(AutoCloseableLock readLock = readLock()) {
        final int i = Collections.binarySearch(segments, index);
        return i < 0? null: segments.get(i);
      }
    }

    TermIndex[] getTermIndex(long startIndex, long realEnd, LogSegment openSegment) {
      final TermIndex[] entries = new TermIndex[Math.toIntExact(realEnd - startIndex)];
      final int searchIndex;
      long index = startIndex;

      try(AutoCloseableLock readLock = readLock()) {
        searchIndex = Collections.binarySearch(segments, startIndex);
        if (searchIndex >= 0) {
          for(int i = searchIndex; i < segments.size() && index < realEnd; i++) {
            final LogSegment s = segments.get(i);
            final int numberFromSegment = Math.toIntExact(Math.min(realEnd - index, s.getEndIndex() - index + 1));
            getFromSegment(s, index, entries, Math.toIntExact(index - startIndex), numberFromSegment);
            index += numberFromSegment;
          }
        }
      }

      // openSegment is read outside the lock.
      if (searchIndex < 0) {
        getFromSegment(openSegment, startIndex, entries, 0, entries.length);
      } else if (index < realEnd) {
        getFromSegment(openSegment, index, entries,
            Math.toIntExact(index - startIndex), Math.toIntExact(realEnd - index));
      }
      return entries;
    }

    boolean add(LogSegment logSegment) {
      try(AutoCloseableLock writeLock = writeLock()) {
        return segments.add(logSegment);
      }
    }

    void clear() {
      try(AutoCloseableLock writeLock = writeLock()) {
        segments.forEach(LogSegment::clear);
        segments.clear();
      }
    }

    TruncationSegments truncate(long index, LogSegment openSegment, Runnable clearOpenSegment) {
      try(AutoCloseableLock writeLock = writeLock()) {
        final int segmentIndex = binarySearch(index);
        if (segmentIndex == -segments.size() - 1) {
          if (openSegment != null && openSegment.getEndIndex() >= index) {
            final long oldEnd = openSegment.getEndIndex();
            if (index == openSegment.getStartIndex()) {
              // the open segment should be deleted
              final SegmentFileInfo deleted = deleteOpenSegment(openSegment, clearOpenSegment);
              return new TruncationSegments(null, Collections.singletonList(deleted));
            } else {
              openSegment.truncate(index);
              Preconditions.assertTrue(!openSegment.isOpen());
              final SegmentFileInfo info = new SegmentFileInfo(openSegment.getStartIndex(),
                  oldEnd, true, openSegment.getTotalSize(), openSegment.getEndIndex());
              segments.add(openSegment);
              clearOpenSegment.run();
              return new TruncationSegments(info, Collections.emptyList());
            }
          }
        } else if (segmentIndex >= 0) {
          final LogSegment ts = segments.get(segmentIndex);
          final long oldEnd = ts.getEndIndex();
          final List<SegmentFileInfo> list = new ArrayList<>();
          ts.truncate(index);
          final int size = segments.size();
          for(int i = size - 1;
              i >= (ts.numOfEntries() == 0? segmentIndex: segmentIndex + 1);
              i--) {
            LogSegment s = segments.remove(i);
            final long endOfS = i == segmentIndex? oldEnd: s.getEndIndex();
            s.clear();
            list.add(new SegmentFileInfo(s.getStartIndex(), endOfS, false, 0, s.getEndIndex()));
          }
          if (openSegment != null) {
            list.add(deleteOpenSegment(openSegment, clearOpenSegment));
          }
          SegmentFileInfo t = ts.numOfEntries() == 0? null:
              new SegmentFileInfo(ts.getStartIndex(), oldEnd, false, ts.getTotalSize(), ts.getEndIndex());
          return new TruncationSegments(t, list);
        }
        return null;
      }
    }

    static SegmentFileInfo deleteOpenSegment(LogSegment openSegment, Runnable clearOpenSegment) {
      final long oldEnd = openSegment.getEndIndex();
      openSegment.clear();
      final SegmentFileInfo info = new SegmentFileInfo(openSegment.getStartIndex(), oldEnd, true,
          0, openSegment.getEndIndex());
      clearOpenSegment.run();
      return info;
    }
  }

  private final String name;
  private volatile LogSegment openSegment;
  private final LogSegmentList closedSegments;
  private final RaftStorage storage;

  private final int maxCachedSegments;
  private final CacheInvalidationPolicy evictionPolicy = new CacheInvalidationPolicyDefault();

  RaftLogCache(RaftPeerId selfId, RaftStorage storage, RaftProperties properties) {
    this.name = selfId + "-" + getClass().getSimpleName();
    this.closedSegments = new LogSegmentList(name);
    this.storage = storage;
    maxCachedSegments = RaftServerConfigKeys.Log.maxCachedSegmentNum(properties);
  }

  int getMaxCachedSegments() {
    return maxCachedSegments;
  }

  void loadSegment(LogPathAndIndex pi, boolean keepEntryInCache,
      Consumer<LogEntryProto> logConsumer) throws IOException {
    LogSegment logSegment = LogSegment.loadSegment(storage, pi.getPath().toFile(),
        pi.startIndex, pi.endIndex, pi.isOpen(), keepEntryInCache, logConsumer);
    if (logSegment != null) {
      addSegment(logSegment);
    }
  }

  long getCachedSegmentNum() {
    return closedSegments.countCached();
  }

  boolean shouldEvict() {
    return closedSegments.countCached() > maxCachedSegments;
  }

  void evictCache(long[] followerIndices, long flushedIndex,
      long lastAppliedIndex) {
    List<LogSegment> toEvict = evictionPolicy.evict(followerIndices,
        flushedIndex, lastAppliedIndex, closedSegments, maxCachedSegments);
    for (LogSegment s : toEvict) {
      s.evictCache();
    }
  }


  private void validateAdding(LogSegment segment) {
    final LogSegment lastClosed = closedSegments.getLast();
    if (lastClosed != null) {
      Preconditions.assertTrue(!lastClosed.isOpen());
      Preconditions.assertTrue(lastClosed.getEndIndex() + 1 == segment.getStartIndex());
    }
  }

  void addSegment(LogSegment segment) {
    validateAdding(segment);
    if (segment.isOpen()) {
      setOpenSegment(segment);
    } else {
      closedSegments.add(segment);
    }
  }

  void addOpenSegment(long startIndex) {
    setOpenSegment(LogSegment.newOpenSegment(storage, startIndex));
  }

  private void setOpenSegment(LogSegment openSegment) {
    LOG.trace("{}: setOpenSegment to {}", name, openSegment);
    Preconditions.assertTrue(this.openSegment == null);
    this.openSegment = Objects.requireNonNull(openSegment);
  }

  private void clearOpenSegment() {
    LOG.trace("{}: clearOpenSegment {}", name, openSegment);
    Objects.requireNonNull(openSegment);
    this.openSegment = null;
  }

  LogSegment getOpenSegment() {
    return openSegment;
  }

  /**
   * finalize the current open segment, and start a new open segment
   */
  void rollOpenSegment(boolean createNewOpen) {
    Preconditions.assertTrue(openSegment != null
        && openSegment.numOfEntries() > 0);
    final long nextIndex = openSegment.getEndIndex() + 1;
    openSegment.close();
    closedSegments.add(openSegment);
    clearOpenSegment();
    if (createNewOpen) {
      addOpenSegment(nextIndex);
    }
  }

  LogSegment getSegment(long index) {
    if (openSegment != null && index >= openSegment.getStartIndex()) {
      return openSegment;
    } else {
      return closedSegments.search(index);
    }
  }

  LogRecord getLogRecord(long index) {
    LogSegment segment = getSegment(index);
    return segment == null ? null : segment.getLogRecord(index);
  }

  /**
   * @param startIndex inclusive
   * @param endIndex exclusive
   */
  TermIndex[] getTermIndices(final long startIndex, final long endIndex) {
    if (startIndex < 0 || startIndex < getStartIndex()) {
      throw new IndexOutOfBoundsException("startIndex = " + startIndex
          + ", log cache starts from index " + getStartIndex());
    }
    if (startIndex > endIndex) {
      throw new IndexOutOfBoundsException("startIndex(" + startIndex
          + ") > endIndex(" + endIndex + ")");
    }
    final long realEnd = Math.min(getEndIndex() + 1, endIndex);
    if (startIndex >= realEnd) {
      return TermIndex.EMPTY_TERMINDEX_ARRAY;
    }
    return closedSegments.getTermIndex(startIndex, realEnd, openSegment);
  }

  private static void getFromSegment(LogSegment segment, long startIndex,
      TermIndex[] entries, int offset, int size) {
    long endIndex = segment.getEndIndex();
    endIndex = Math.min(endIndex, startIndex + size - 1);
    int index = offset;
    for (long i = startIndex; i <= endIndex; i++) {
      LogRecord r = segment.getLogRecord(i);
      entries[index++] = r == null ? null : r.getTermIndex();
    }
  }

  boolean isConfigEntry(TermIndex ti) {
    LogSegment segment = getSegment(ti.getIndex());
    return segment != null && segment.isConfigEntry(ti);
  }

  long getStartIndex() {
    if (closedSegments.isEmpty()) {
      return openSegment != null ? openSegment.getStartIndex() :
          RaftServerConstants.INVALID_LOG_INDEX;
    } else {
      return closedSegments.get(0).getStartIndex();
    }
  }

  long getEndIndex() {
    return openSegment != null ? openSegment.getEndIndex() :
        (closedSegments.isEmpty() ?
            INVALID_LOG_INDEX :
            closedSegments.get(closedSegments.size() - 1).getEndIndex());
  }

  TermIndex getLastTermIndex() {
    return (openSegment != null && openSegment.numOfEntries() > 0) ?
        openSegment.getLastTermIndex() :
        (closedSegments.isEmpty() ? null :
            closedSegments.get(closedSegments.size() - 1).getLastTermIndex());
  }

  void appendEntry(LogEntryProto entry) {
    // SegmentedRaftLog does the segment creation/rolling work. Here we just
    // simply append the entry into the open segment.
    Preconditions.assertTrue(openSegment != null);
    openSegment.appendToOpenSegment(entry);
  }

  /**
   * truncate log entries starting from the given index (inclusive)
   */
  TruncationSegments truncate(long index) {
    return closedSegments.truncate(index, openSegment, this::clearOpenSegment);
  }

  Iterator<TermIndex> iterator(long startIndex) {
    return new EntryIterator(startIndex);
  }

  static class TruncateIndices {
    final int arrayIndex;
    final long truncateIndex;

    TruncateIndices(int arrayIndex, long truncateIndex) {
      this.arrayIndex = arrayIndex;
      this.truncateIndex = truncateIndex;
    }

    int getArrayIndex() {
      return arrayIndex;
    }

    long getTruncateIndex() {
      return truncateIndex;
    }
  }

  TruncateIndices computeTruncateIndices(Consumer<TermIndex> failClientRequest, LogEntryProto... entries) {
    int arrayIndex = 0;
    long truncateIndex = -1;

    try(AutoCloseableLock readLock = closedSegments.readLock()) {
      final Iterator<TermIndex> i = iterator(entries[0].getIndex());
      for(; i.hasNext() && arrayIndex < entries.length; arrayIndex++) {
        final TermIndex storedEntry = i.next();
        Preconditions.assertTrue(storedEntry.getIndex() == entries[arrayIndex].getIndex(),
            "The stored entry's index %s is not consistent with the received entries[%s]'s index %s",
            storedEntry.getIndex(), arrayIndex, entries[arrayIndex].getIndex());

        if (storedEntry.getTerm() != entries[arrayIndex].getTerm()) {
          // we should truncate from the storedEntry's arrayIndex
          truncateIndex = storedEntry.getIndex();
          if (LOG.isTraceEnabled()) {
            LOG.trace("{}: truncate to {}, arrayIndex={}, ti={}, storedEntry={}, entries={}",
                name, truncateIndex, arrayIndex,
                ServerProtoUtils.toTermIndex(entries[arrayIndex]), storedEntry,
                ServerProtoUtils.toString(entries));
          }

          // fail all requests starting at truncateIndex
          failClientRequest.accept(storedEntry);
          for(; i.hasNext(); ) {
            failClientRequest.accept(i.next());
          }
          break;
        }
      }
    }
    return new TruncateIndices(arrayIndex, truncateIndex);
  }

  private class EntryIterator implements Iterator<TermIndex> {
    private long nextIndex;
    private LogSegment currentSegment;
    private int segmentIndex;

    EntryIterator(long start) {
      this.nextIndex = start;
      segmentIndex = closedSegments.binarySearch(nextIndex);
      if (segmentIndex >= 0) {
        currentSegment = closedSegments.get(segmentIndex);
      } else {
        segmentIndex = -segmentIndex - 1;
        if (segmentIndex == closedSegments.size()) {
          currentSegment = openSegment;
        } else {
          // the start index is smaller than the first closed segment's start
          // index. We no longer keep the log entry (because of the snapshot) or
          // the start index is invalid.
          Preconditions.assertTrue(segmentIndex == 0);
          throw new IndexOutOfBoundsException();
        }
      }
    }

    @Override
    public boolean hasNext() {
      return currentSegment != null &&
          currentSegment.getLogRecord(nextIndex) != null;
    }

    @Override
    public TermIndex next() {
      LogRecord record;
      if (currentSegment == null ||
          (record = currentSegment.getLogRecord(nextIndex)) == null) {
        throw new NoSuchElementException();
      }
      if (++nextIndex > currentSegment.getEndIndex()) {
        if (currentSegment != openSegment) {
          segmentIndex++;
          currentSegment = segmentIndex == closedSegments.size() ?
              openSegment : closedSegments.get(segmentIndex);
        }
      }
      return record.getTermIndex();
    }
  }

  int getNumOfSegments() {
    return closedSegments.size() + (openSegment == null ? 0 : 1);
  }

  boolean isEmpty() {
    return closedSegments.isEmpty() && openSegment == null;
  }

  void clear() {
    if (openSegment != null) {
      openSegment.clear();
      clearOpenSegment();
    }
    closedSegments.clear();
  }
}
