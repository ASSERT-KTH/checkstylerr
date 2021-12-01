/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.bigtable.data.v2.models;

import com.google.bigtable.v2.Mutation.DeleteFromColumn;
import com.google.bigtable.v2.Mutation.DeleteFromFamily;
import com.google.bigtable.v2.Mutation.DeleteFromRow;
import com.google.bigtable.v2.Mutation.SetCell;
import com.google.cloud.bigtable.data.v2.models.Range.TimestampRange;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * The concrete implementation of {@link MutationApi} that can be used to create and represent a
 * list of mutations. This class is meant to used as child of other classes.
 */
public final class Mutation implements MutationApi<Mutation> {
  private final ImmutableList.Builder<com.google.bigtable.v2.Mutation> mutations =
      ImmutableList.builder();

  public static Mutation create() {
    return new Mutation();
  }

  private Mutation() {}

  @Override
  public Mutation setCell(
      @Nonnull String familyName, @Nonnull String qualifier, @Nonnull String value) {
    return setCell(familyName, wrapByteString(qualifier), wrapByteString(value));
  }

  @Override
  public Mutation setCell(
      @Nonnull String familyName,
      @Nonnull String qualifier,
      long timestamp,
      @Nonnull String value) {
    return setCell(familyName, wrapByteString(qualifier), timestamp, wrapByteString(value));
  }

  @Override
  public Mutation setCell(
      @Nonnull String familyName, @Nonnull ByteString qualifier, @Nonnull ByteString value) {
    long timestamp = System.currentTimeMillis() * 1_000;

    return setCell(familyName, qualifier, timestamp, value);
  }

  @Override
  public Mutation setCell(
      @Nonnull String familyName,
      @Nonnull ByteString qualifier,
      long timestamp,
      @Nonnull ByteString value) {
    Validations.validateFamily(familyName);
    Preconditions.checkNotNull(qualifier, "qualifier can't be null.");
    Preconditions.checkNotNull(value, "value can't be null.");
    Preconditions.checkArgument(timestamp != -1, "Serverside timestamps are not supported");

    com.google.bigtable.v2.Mutation mutation =
        com.google.bigtable.v2.Mutation.newBuilder()
            .setSetCell(
                SetCell.newBuilder()
                    .setFamilyName(familyName)
                    .setColumnQualifier(qualifier)
                    .setTimestampMicros(timestamp)
                    .setValue(value)
                    .build())
            .build();

    mutations.add(mutation);
    return this;
  }

  @Override
  public Mutation deleteCells(@Nonnull String familyName, @Nonnull String qualifier) {
    return deleteCells(familyName, wrapByteString(qualifier));
  }

  @Override
  public Mutation deleteCells(@Nonnull String familyName, @Nonnull ByteString qualifier) {
    Validations.validateFamily(familyName);
    Preconditions.checkNotNull(qualifier, "qualifier can't be null.");

    return deleteCells(familyName, qualifier, TimestampRange.unbounded());
  }

  @Override
  public Mutation deleteCells(
      @Nonnull String familyName,
      @Nonnull ByteString qualifier,
      @Nonnull TimestampRange timestampRange) {
    Validations.validateFamily(familyName);
    Preconditions.checkNotNull(qualifier, "qualifier can't be null.");
    Preconditions.checkNotNull(timestampRange, "timestampRange can't be null.");

    DeleteFromColumn.Builder builder =
        DeleteFromColumn.newBuilder().setFamilyName(familyName).setColumnQualifier(qualifier);

    switch (timestampRange.getStartBound()) {
      case CLOSED:
        builder.getTimeRangeBuilder().setStartTimestampMicros(timestampRange.getStart());
        break;
      case OPEN:
        builder.getTimeRangeBuilder().setStartTimestampMicros(timestampRange.getStart() + 1);
        break;
      case UNBOUNDED:
        break;
      default:
        throw new IllegalArgumentException(
            "Unknown start bound: " + timestampRange.getStartBound());
    }
    switch (timestampRange.getEndBound()) {
      case CLOSED:
        builder.getTimeRangeBuilder().setEndTimestampMicros(timestampRange.getEnd() + 1);
        break;
      case OPEN:
        builder.getTimeRangeBuilder().setEndTimestampMicros(timestampRange.getEnd());
        break;
      case UNBOUNDED:
        break;
      default:
        throw new IllegalArgumentException("Unknown end bound: " + timestampRange.getEndBound());
    }

    com.google.bigtable.v2.Mutation mutation =
        com.google.bigtable.v2.Mutation.newBuilder().setDeleteFromColumn(builder.build()).build();
    mutations.add(mutation);

    return this;
  }

  @Override
  public Mutation deleteFamily(@Nonnull String familyName) {
    Validations.validateFamily(familyName);

    com.google.bigtable.v2.Mutation mutation =
        com.google.bigtable.v2.Mutation.newBuilder()
            .setDeleteFromFamily(DeleteFromFamily.newBuilder().setFamilyName(familyName).build())
            .build();
    mutations.add(mutation);

    return this;
  }

  @Override
  public Mutation deleteRow() {
    com.google.bigtable.v2.Mutation mutation =
        com.google.bigtable.v2.Mutation.newBuilder()
            .setDeleteFromRow(DeleteFromRow.getDefaultInstance())
            .build();
    mutations.add(mutation);

    return this;
  }

  private static ByteString wrapByteString(String str) {
    if (str == null) {
      return null;
    } else {
      return ByteString.copyFromUtf8(str);
    }
  }

  /** Returns the mutation protos. */
  List<com.google.bigtable.v2.Mutation> getMutations() {
    return mutations.build();
  }
}
