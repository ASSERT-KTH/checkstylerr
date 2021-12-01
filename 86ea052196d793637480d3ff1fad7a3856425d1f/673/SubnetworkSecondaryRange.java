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
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@Generated("by GAPIC")
@BetaApi
public final class SubnetworkSecondaryRange implements ApiMessage {
  private final String ipCidrRange;
  private final String rangeName;

  private SubnetworkSecondaryRange() {
    this.ipCidrRange = null;
    this.rangeName = null;
  }

  private SubnetworkSecondaryRange(String ipCidrRange, String rangeName) {
    this.ipCidrRange = ipCidrRange;
    this.rangeName = rangeName;
  }

  @Override
  public Object getFieldValue(String fieldName) {
    if (fieldName.equals("ipCidrRange")) {
      return ipCidrRange;
    }
    if (fieldName.equals("rangeName")) {
      return rangeName;
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

  public String getIpCidrRange() {
    return ipCidrRange;
  }

  public String getRangeName() {
    return rangeName;
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(SubnetworkSecondaryRange prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  public static SubnetworkSecondaryRange getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final SubnetworkSecondaryRange DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new SubnetworkSecondaryRange();
  }

  public static class Builder {
    private String ipCidrRange;
    private String rangeName;

    Builder() {}

    public Builder mergeFrom(SubnetworkSecondaryRange other) {
      if (other == SubnetworkSecondaryRange.getDefaultInstance()) return this;
      if (other.getIpCidrRange() != null) {
        this.ipCidrRange = other.ipCidrRange;
      }
      if (other.getRangeName() != null) {
        this.rangeName = other.rangeName;
      }
      return this;
    }

    Builder(SubnetworkSecondaryRange source) {
      this.ipCidrRange = source.ipCidrRange;
      this.rangeName = source.rangeName;
    }

    public String getIpCidrRange() {
      return ipCidrRange;
    }

    public Builder setIpCidrRange(String ipCidrRange) {
      this.ipCidrRange = ipCidrRange;
      return this;
    }

    public String getRangeName() {
      return rangeName;
    }

    public Builder setRangeName(String rangeName) {
      this.rangeName = rangeName;
      return this;
    }

    public SubnetworkSecondaryRange build() {

      return new SubnetworkSecondaryRange(ipCidrRange, rangeName);
    }

    public Builder clone() {
      Builder newBuilder = new Builder();
      newBuilder.setIpCidrRange(this.ipCidrRange);
      newBuilder.setRangeName(this.rangeName);
      return newBuilder;
    }
  }

  @Override
  public String toString() {
    return "SubnetworkSecondaryRange{"
        + "ipCidrRange="
        + ipCidrRange
        + ", "
        + "rangeName="
        + rangeName
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SubnetworkSecondaryRange) {
      SubnetworkSecondaryRange that = (SubnetworkSecondaryRange) o;
      return Objects.equals(this.ipCidrRange, that.getIpCidrRange())
          && Objects.equals(this.rangeName, that.getRangeName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ipCidrRange, rangeName);
  }
}
