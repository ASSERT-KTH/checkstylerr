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
public final class InstanceWithNamedPorts implements ApiMessage {
  private final String instance;
  private final List<NamedPort> namedPorts;
  private final String status;

  private InstanceWithNamedPorts() {
    this.instance = null;
    this.namedPorts = null;
    this.status = null;
  }

  private InstanceWithNamedPorts(String instance, List<NamedPort> namedPorts, String status) {
    this.instance = instance;
    this.namedPorts = namedPorts;
    this.status = status;
  }

  @Override
  public Object getFieldValue(String fieldName) {
    if (fieldName.equals("instance")) {
      return instance;
    }
    if (fieldName.equals("namedPorts")) {
      return namedPorts;
    }
    if (fieldName.equals("status")) {
      return status;
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

  public String getInstance() {
    return instance;
  }

  public List<NamedPort> getNamedPortsList() {
    return namedPorts;
  }

  public String getStatus() {
    return status;
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(InstanceWithNamedPorts prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  public static InstanceWithNamedPorts getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final InstanceWithNamedPorts DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new InstanceWithNamedPorts();
  }

  public static class Builder {
    private String instance;
    private List<NamedPort> namedPorts;
    private String status;

    Builder() {}

    public Builder mergeFrom(InstanceWithNamedPorts other) {
      if (other == InstanceWithNamedPorts.getDefaultInstance()) return this;
      if (other.getInstance() != null) {
        this.instance = other.instance;
      }
      if (other.getNamedPortsList() != null) {
        this.namedPorts = other.namedPorts;
      }
      if (other.getStatus() != null) {
        this.status = other.status;
      }
      return this;
    }

    Builder(InstanceWithNamedPorts source) {
      this.instance = source.instance;
      this.namedPorts = source.namedPorts;
      this.status = source.status;
    }

    public String getInstance() {
      return instance;
    }

    public Builder setInstance(String instance) {
      this.instance = instance;
      return this;
    }

    public List<NamedPort> getNamedPortsList() {
      return namedPorts;
    }

    public Builder addAllNamedPorts(List<NamedPort> namedPorts) {
      if (this.namedPorts == null) {
        this.namedPorts = new ArrayList<>(namedPorts.size());
      }
      this.namedPorts.addAll(namedPorts);
      return this;
    }

    public Builder addNamedPorts(NamedPort namedPorts) {
      this.namedPorts.add(namedPorts);
      return this;
    }

    public String getStatus() {
      return status;
    }

    public Builder setStatus(String status) {
      this.status = status;
      return this;
    }

    public InstanceWithNamedPorts build() {

      return new InstanceWithNamedPorts(instance, namedPorts, status);
    }

    public Builder clone() {
      Builder newBuilder = new Builder();
      newBuilder.setInstance(this.instance);
      newBuilder.addAllNamedPorts(this.namedPorts);
      newBuilder.setStatus(this.status);
      return newBuilder;
    }
  }

  @Override
  public String toString() {
    return "InstanceWithNamedPorts{"
        + "instance="
        + instance
        + ", "
        + "namedPorts="
        + namedPorts
        + ", "
        + "status="
        + status
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof InstanceWithNamedPorts) {
      InstanceWithNamedPorts that = (InstanceWithNamedPorts) o;
      return Objects.equals(this.instance, that.getInstance())
          && Objects.equals(this.namedPorts, that.getNamedPortsList())
          && Objects.equals(this.status, that.getStatus());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(instance, namedPorts, status);
  }
}
