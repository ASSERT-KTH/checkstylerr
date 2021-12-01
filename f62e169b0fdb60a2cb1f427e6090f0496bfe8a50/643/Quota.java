/*
 * Copyright 2019 Google LLC
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
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@Generated("by GAPIC")
@BetaApi
public final class Quota implements ApiMessage {
  private final Double limit;
  private final String metric;
  private final String owner;
  private final Double usage;

  private Quota() {
    this.limit = null;
    this.metric = null;
    this.owner = null;
    this.usage = null;
  }

  private Quota(Double limit, String metric, String owner, Double usage) {
    this.limit = limit;
    this.metric = metric;
    this.owner = owner;
    this.usage = usage;
  }

  @Override
  public Object getFieldValue(String fieldName) {
    if ("limit".equals(fieldName)) {
      return limit;
    }
    if ("metric".equals(fieldName)) {
      return metric;
    }
    if ("owner".equals(fieldName)) {
      return owner;
    }
    if ("usage".equals(fieldName)) {
      return usage;
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

  public Double getLimit() {
    return limit;
  }

  public String getMetric() {
    return metric;
  }

  public String getOwner() {
    return owner;
  }

  public Double getUsage() {
    return usage;
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(Quota prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  public static Quota getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final Quota DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new Quota();
  }

  public static class Builder {
    private Double limit;
    private String metric;
    private String owner;
    private Double usage;

    Builder() {}

    public Builder mergeFrom(Quota other) {
      if (other == Quota.getDefaultInstance()) return this;
      if (other.getLimit() != null) {
        this.limit = other.limit;
      }
      if (other.getMetric() != null) {
        this.metric = other.metric;
      }
      if (other.getOwner() != null) {
        this.owner = other.owner;
      }
      if (other.getUsage() != null) {
        this.usage = other.usage;
      }
      return this;
    }

    Builder(Quota source) {
      this.limit = source.limit;
      this.metric = source.metric;
      this.owner = source.owner;
      this.usage = source.usage;
    }

    public Double getLimit() {
      return limit;
    }

    public Builder setLimit(Double limit) {
      this.limit = limit;
      return this;
    }

    public String getMetric() {
      return metric;
    }

    public Builder setMetric(String metric) {
      this.metric = metric;
      return this;
    }

    public String getOwner() {
      return owner;
    }

    public Builder setOwner(String owner) {
      this.owner = owner;
      return this;
    }

    public Double getUsage() {
      return usage;
    }

    public Builder setUsage(Double usage) {
      this.usage = usage;
      return this;
    }

    public Quota build() {

      return new Quota(limit, metric, owner, usage);
    }

    public Builder clone() {
      Builder newBuilder = new Builder();
      newBuilder.setLimit(this.limit);
      newBuilder.setMetric(this.metric);
      newBuilder.setOwner(this.owner);
      newBuilder.setUsage(this.usage);
      return newBuilder;
    }
  }

  @Override
  public String toString() {
    return "Quota{" + "limit=" + limit + ", " + "metric=" + metric + ", " + "owner=" + owner + ", "
        + "usage=" + usage + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Quota) {
      Quota that = (Quota) o;
      return Objects.equals(this.limit, that.getLimit())
          && Objects.equals(this.metric, that.getMetric())
          && Objects.equals(this.owner, that.getOwner())
          && Objects.equals(this.usage, that.getUsage());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(limit, metric, owner, usage);
  }
}
