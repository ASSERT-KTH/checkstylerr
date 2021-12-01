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
public final class RouterInterface implements ApiMessage {
  private final String ipRange;
  private final String linkedInterconnectAttachment;
  private final String linkedVpnTunnel;
  private final String name;

  private RouterInterface() {
    this.ipRange = null;
    this.linkedInterconnectAttachment = null;
    this.linkedVpnTunnel = null;
    this.name = null;
  }

  private RouterInterface(
      String ipRange, String linkedInterconnectAttachment, String linkedVpnTunnel, String name) {
    this.ipRange = ipRange;
    this.linkedInterconnectAttachment = linkedInterconnectAttachment;
    this.linkedVpnTunnel = linkedVpnTunnel;
    this.name = name;
  }

  @Override
  public Object getFieldValue(String fieldName) {
    if (fieldName.equals("ipRange")) {
      return ipRange;
    }
    if (fieldName.equals("linkedInterconnectAttachment")) {
      return linkedInterconnectAttachment;
    }
    if (fieldName.equals("linkedVpnTunnel")) {
      return linkedVpnTunnel;
    }
    if (fieldName.equals("name")) {
      return name;
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

  public String getIpRange() {
    return ipRange;
  }

  public String getLinkedInterconnectAttachment() {
    return linkedInterconnectAttachment;
  }

  public String getLinkedVpnTunnel() {
    return linkedVpnTunnel;
  }

  public String getName() {
    return name;
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(RouterInterface prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  public static RouterInterface getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final RouterInterface DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new RouterInterface();
  }

  public static class Builder {
    private String ipRange;
    private String linkedInterconnectAttachment;
    private String linkedVpnTunnel;
    private String name;

    Builder() {}

    public Builder mergeFrom(RouterInterface other) {
      if (other == RouterInterface.getDefaultInstance()) return this;
      if (other.getIpRange() != null) {
        this.ipRange = other.ipRange;
      }
      if (other.getLinkedInterconnectAttachment() != null) {
        this.linkedInterconnectAttachment = other.linkedInterconnectAttachment;
      }
      if (other.getLinkedVpnTunnel() != null) {
        this.linkedVpnTunnel = other.linkedVpnTunnel;
      }
      if (other.getName() != null) {
        this.name = other.name;
      }
      return this;
    }

    Builder(RouterInterface source) {
      this.ipRange = source.ipRange;
      this.linkedInterconnectAttachment = source.linkedInterconnectAttachment;
      this.linkedVpnTunnel = source.linkedVpnTunnel;
      this.name = source.name;
    }

    public String getIpRange() {
      return ipRange;
    }

    public Builder setIpRange(String ipRange) {
      this.ipRange = ipRange;
      return this;
    }

    public String getLinkedInterconnectAttachment() {
      return linkedInterconnectAttachment;
    }

    public Builder setLinkedInterconnectAttachment(String linkedInterconnectAttachment) {
      this.linkedInterconnectAttachment = linkedInterconnectAttachment;
      return this;
    }

    public String getLinkedVpnTunnel() {
      return linkedVpnTunnel;
    }

    public Builder setLinkedVpnTunnel(String linkedVpnTunnel) {
      this.linkedVpnTunnel = linkedVpnTunnel;
      return this;
    }

    public String getName() {
      return name;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public RouterInterface build() {

      return new RouterInterface(ipRange, linkedInterconnectAttachment, linkedVpnTunnel, name);
    }

    public Builder clone() {
      Builder newBuilder = new Builder();
      newBuilder.setIpRange(this.ipRange);
      newBuilder.setLinkedInterconnectAttachment(this.linkedInterconnectAttachment);
      newBuilder.setLinkedVpnTunnel(this.linkedVpnTunnel);
      newBuilder.setName(this.name);
      return newBuilder;
    }
  }

  @Override
  public String toString() {
    return "RouterInterface{"
        + "ipRange="
        + ipRange
        + ", "
        + "linkedInterconnectAttachment="
        + linkedInterconnectAttachment
        + ", "
        + "linkedVpnTunnel="
        + linkedVpnTunnel
        + ", "
        + "name="
        + name
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof RouterInterface) {
      RouterInterface that = (RouterInterface) o;
      return Objects.equals(this.ipRange, that.getIpRange())
          && Objects.equals(
              this.linkedInterconnectAttachment, that.getLinkedInterconnectAttachment())
          && Objects.equals(this.linkedVpnTunnel, that.getLinkedVpnTunnel())
          && Objects.equals(this.name, that.getName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ipRange, linkedInterconnectAttachment, linkedVpnTunnel, name);
  }
}
