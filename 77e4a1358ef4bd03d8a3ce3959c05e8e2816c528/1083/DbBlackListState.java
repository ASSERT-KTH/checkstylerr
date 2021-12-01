/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.zeebe.engine.state.processing;

import io.zeebe.db.ColumnFamily;
import io.zeebe.db.TransactionContext;
import io.zeebe.db.ZeebeDb;
import io.zeebe.db.impl.DbLong;
import io.zeebe.db.impl.DbNil;
import io.zeebe.engine.Loggers;
import io.zeebe.engine.metrics.BlacklistMetrics;
import io.zeebe.engine.processing.streamprocessor.TypedRecord;
import io.zeebe.engine.state.ZbColumnFamilies;
import io.zeebe.engine.state.mutable.MutableBlackListState;
import io.zeebe.msgpack.UnpackedObject;
import io.zeebe.protocol.record.intent.Intent;
import io.zeebe.protocol.record.intent.ProcessInstanceRelatedIntent;
import io.zeebe.protocol.record.value.ProcessInstanceRelated;
import java.util.function.Consumer;
import org.slf4j.Logger;

public final class DbBlackListState implements MutableBlackListState {

  private static final Logger LOG = Loggers.STREAM_PROCESSING;

  private static final String BLACKLIST_INSTANCE_MESSAGE =
      "Blacklist process instance {}, due to previous errors.";

  private final ColumnFamily<DbLong, DbNil> blackListColumnFamily;
  private final DbLong processInstanceKey;
  private final BlacklistMetrics blacklistMetrics;

  public DbBlackListState(
      final ZeebeDb<ZbColumnFamilies> zeebeDb,
      final TransactionContext transactionContext,
      final int partitionId) {
    processInstanceKey = new DbLong();
    blackListColumnFamily =
        zeebeDb.createColumnFamily(
            ZbColumnFamilies.BLACKLIST, transactionContext, processInstanceKey, DbNil.INSTANCE);
    blacklistMetrics = new BlacklistMetrics(partitionId);
  }

  private void blacklist(final long key) {
    if (key >= 0) {
      LOG.warn(BLACKLIST_INSTANCE_MESSAGE, key);

      processInstanceKey.wrapLong(key);
      blackListColumnFamily.put(processInstanceKey, DbNil.INSTANCE);
      blacklistMetrics.countBlacklistedInstance();
    }
  }

  private boolean isOnBlacklist(final long key) {
    processInstanceKey.wrapLong(key);
    return blackListColumnFamily.exists(processInstanceKey);
  }

  @Override
  public boolean isOnBlacklist(final TypedRecord record) {
    final UnpackedObject value = record.getValue();
    if (value instanceof ProcessInstanceRelated) {
      final long processInstanceKey = ((ProcessInstanceRelated) value).getProcessInstanceKey();
      if (processInstanceKey >= 0) {
        return isOnBlacklist(processInstanceKey);
      }
    }
    return false;
  }

  @Override
  public boolean tryToBlacklist(
      final TypedRecord<?> typedRecord, final Consumer<Long> onBlacklistingInstance) {
    final Intent intent = typedRecord.getIntent();
    if (shouldBeBlacklisted(intent)) {
      final UnpackedObject value = typedRecord.getValue();
      if (value instanceof ProcessInstanceRelated) {
        final long processInstanceKey = ((ProcessInstanceRelated) value).getProcessInstanceKey();
        blacklist(processInstanceKey);
        onBlacklistingInstance.accept(processInstanceKey);
      }
    }
    return false;
  }

  private boolean shouldBeBlacklisted(final Intent intent) {

    if (intent instanceof ProcessInstanceRelatedIntent) {
      final ProcessInstanceRelatedIntent processInstanceRelatedIntent =
          (ProcessInstanceRelatedIntent) intent;

      return processInstanceRelatedIntent.shouldBlacklistInstanceOnError();
    }

    return false;
  }
}
