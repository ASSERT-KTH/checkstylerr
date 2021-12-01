/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.firestore;

import com.google.protobuf.Timestamp;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.threeten.bp.Instant;

/** Preconditions that can be used to restrict update() calls. */
public final class Precondition {

  /** An empty Precondition that adds no enforcements */
  public static final Precondition NONE = new Precondition(null, null);

  private Boolean exists;
  private Instant updateTime;

  private Precondition(Boolean exists, Instant updateTime) {
    this.exists = exists;
    this.updateTime = updateTime;
  }

  /**
   * Creates a Precondition that enforces that a document exists.
   *
   * @param exists Whether the document should exist.
   * @return A new Precondition
   */
  // TODO: Make public once backend supports verify.
  @Nonnull
  static Precondition exists(Boolean exists) {
    return new Precondition(exists, null);
  }

  /**
   * Creates a Precondition that enforces that the existing document was written at the specified
   * time.
   *
   * @param updateTime The write time to enforce on the existing document.
   * @return A new Precondition
   */
  @Nonnull
  public static Precondition updatedAt(Instant updateTime) {
    return new Precondition(null, updateTime);
  }

  com.google.firestore.v1beta1.Precondition toPb() {
    com.google.firestore.v1beta1.Precondition.Builder precondition =
        com.google.firestore.v1beta1.Precondition.newBuilder();

    if (exists != null) {
      precondition.setExists(exists);
    }

    if (updateTime != null) {
      Timestamp.Builder timestamp = Timestamp.newBuilder();
      timestamp.setSeconds(updateTime.getEpochSecond());
      timestamp.setNanos(updateTime.getNano());
      precondition.setUpdateTime(timestamp.build());
    }

    return precondition.build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Precondition that = (Precondition) o;
    return Objects.equals(exists, that.exists) && Objects.equals(updateTime, that.updateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(exists, updateTime);
  }
}
