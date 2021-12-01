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
package org.apache.ratis.server.impl;

import org.apache.ratis.proto.RaftProtos.CommitInfoProto;
import org.apache.ratis.protocol.*;
import org.apache.ratis.proto.RaftProtos.RaftClientRequestProto;
import org.apache.ratis.statemachine.TransactionContext;
import org.apache.ratis.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class PendingRequests {
  public static final Logger LOG = LoggerFactory.getLogger(PendingRequests.class);

  private static class RequestMap {
    private final Object name;
    private final ConcurrentMap<Long, PendingRequest> map = new ConcurrentHashMap<>();

    RequestMap(Object name) {
      this.name = name;
    }

    void put(long index, PendingRequest p) {
      LOG.debug("{}: PendingRequests.put {} -> {}", name, index, p);
      final PendingRequest previous = map.put(index, p);
      Preconditions.assertTrue(previous == null);
    }

    PendingRequest get(long index) {
      final PendingRequest r = map.get(index);
      LOG.debug("{}: PendingRequests.get {} returns {}", name, index, r);
      return r;
    }

    PendingRequest remove(long index) {
      final PendingRequest r = map.remove(index);
      LOG.debug("{}: PendingRequests.remove {} returns {}", name, index, r);
      return r;
    }

    Collection<TransactionContext> setNotLeaderException(NotLeaderException nle, Collection<CommitInfoProto> commitInfos) {
      LOG.debug("{}: PendingRequests.setNotLeaderException", name);
      try {
        return map.values().stream()
            .map(p -> p.setNotLeaderException(nle, commitInfos))
            .collect(Collectors.toList());
      } finally {
        map.clear();
      }
    }
  }

  private PendingRequest pendingSetConf;
  private final String name;
  private final RequestMap pendingRequests;

  PendingRequests(RaftPeerId id) {
    this.name = id + "-" + getClass().getSimpleName();
    this.pendingRequests = new RequestMap(id);
  }

  PendingRequest add(RaftClientRequest request, TransactionContext entry) {
    // externally synced for now
    Preconditions.assertTrue(request.is(RaftClientRequestProto.TypeCase.WRITE));
    final long index = entry.getLogEntry().getIndex();
    LOG.debug("{}: addPendingRequest at index={}, request={}", name, index, request);
    final PendingRequest pending = new PendingRequest(index, request, entry);
    pendingRequests.put(index, pending);
    return pending;
  }

  PendingRequest addConfRequest(SetConfigurationRequest request) {
    Preconditions.assertTrue(pendingSetConf == null);
    pendingSetConf = new PendingRequest(request);
    return pendingSetConf;
  }

  void replySetConfiguration(Supplier<Collection<CommitInfoProto>> getCommitInfos) {
    // we allow the pendingRequest to be null in case that the new leader
    // commits the new configuration while it has not received the retry
    // request from the client
    if (pendingSetConf != null) {
      final RaftClientRequest request = pendingSetConf.getRequest();
      LOG.debug("{}: sends success for {}", name, request);
      // for setConfiguration we do not need to wait for statemachine. send back
      // reply after it's committed.
      pendingSetConf.setReply(new RaftClientReply(request, getCommitInfos.get()));
      pendingSetConf = null;
    }
  }

  void failSetConfiguration(RaftException e) {
    Preconditions.assertTrue(pendingSetConf != null);
    pendingSetConf.setException(e);
    pendingSetConf = null;
  }

  TransactionContext getTransactionContext(long index) {
    PendingRequest pendingRequest = pendingRequests.get(index);
    // it is possible that the pendingRequest is null if this peer just becomes
    // the new leader and commits transactions received by the previous leader
    return pendingRequest != null ? pendingRequest.getEntry() : null;
  }

  void replyPendingRequest(long index, RaftClientReply reply) {
    final PendingRequest pending = pendingRequests.remove(index);
    if (pending != null) {
      Preconditions.assertTrue(pending.getIndex() == index);
      pending.setReply(reply);
    }
  }

  /**
   * The leader state is stopped. Send NotLeaderException to all the pending
   * requests since they have not got applied to the state machine yet.
   */
  Collection<TransactionContext> sendNotLeaderResponses(NotLeaderException nle, Collection<CommitInfoProto> commitInfos) {
    LOG.info("{}: sendNotLeaderResponses", name);

    final Collection<TransactionContext> transactions = pendingRequests.setNotLeaderException(nle, commitInfos);
    if (pendingSetConf != null) {
      pendingSetConf.setNotLeaderException(nle, commitInfos);
    }
    return transactions;
  }
}
