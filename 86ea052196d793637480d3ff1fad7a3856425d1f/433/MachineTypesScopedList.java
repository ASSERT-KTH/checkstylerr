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
package com.google.cloud.compute.v1;

import com.google.api.core.BetaApi;
import com.google.api.gax.httpjson.ApiMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@Generated("by GAPIC")
@BetaApi
public final class MachineTypesScopedList implements ApiMessage {
  private final List<MachineType> machineTypes;
  private final Warning warning;

  private MachineTypesScopedList() {
    this.machineTypes = null;
    this.warning = null;
  }

  private MachineTypesScopedList(List<MachineType> machineTypes, Warning warning) {
    this.machineTypes = machineTypes;
    this.warning = warning;
  }

  @Override
  public Object getFieldValue(String fieldName) {
    if (fieldName.equals("machineTypes")) {
      return machineTypes;
    }
    if (fieldName.equals("warning")) {
      return warning;
    }
    return null;
  }

  @Nullable
  @Override
  public ApiMessage getApiMessageRequestBody() {
    return null;
  }

  @Nullable
  @Override
  public List<String> getFieldMask() {
    return null;
  }

  public List<MachineType> getMachineTypesList() {
    return machineTypes;
  }

  public Warning getWarning() {
    return warning;
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(MachineTypesScopedList prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  public static MachineTypesScopedList getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final MachineTypesScopedList DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new MachineTypesScopedList();
  }

  public static class Builder {
    private List<MachineType> machineTypes;
    private Warning warning;

    Builder() {}

    public Builder mergeFrom(MachineTypesScopedList other) {
      if (other == MachineTypesScopedList.getDefaultInstance()) return this;
      if (other.getMachineTypesList() != null) {
        this.machineTypes = other.machineTypes;
      }
      if (other.getWarning() != null) {
        this.warning = other.warning;
      }
      return this;
    }

    Builder(MachineTypesScopedList source) {
      this.machineTypes = source.machineTypes;
      this.warning = source.warning;
    }

    public List<MachineType> getMachineTypesList() {
      return machineTypes;
    }

    public Builder addAllMachineTypes(List<MachineType> machineTypes) {
      if (this.machineTypes == null) {
        this.machineTypes = new ArrayList<>(machineTypes.size());
      }
      this.machineTypes.addAll(machineTypes);
      return this;
    }

    public Builder addMachineTypes(MachineType machineTypes) {
      this.machineTypes.add(machineTypes);
      return this;
    }

    public Warning getWarning() {
      return warning;
    }

    public Builder setWarning(Warning warning) {
      this.warning = warning;
      return this;
    }

    public MachineTypesScopedList build() {

      return new MachineTypesScopedList(machineTypes, warning);
    }

    public Builder clone() {
      Builder newBuilder = new Builder();
      newBuilder.addAllMachineTypes(this.machineTypes);
      newBuilder.setWarning(this.warning);
      return newBuilder;
    }
  }

  @Override
  public String toString() {
    return "MachineTypesScopedList{"
        + "machineTypes="
        + machineTypes
        + ", "
        + "warning="
        + warning
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof MachineTypesScopedList) {
      MachineTypesScopedList that = (MachineTypesScopedList) o;
      return Objects.equals(this.machineTypes, that.getMachineTypesList())
          && Objects.equals(this.warning, that.getWarning());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(machineTypes, warning);
  }
}
