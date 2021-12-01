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
public final class PatchRegionAutoscalerHttpRequest implements ApiMessage {
  private final String access_token;
  private final String autoscaler;
  private final Autoscaler autoscalerResource;
  private final String callback;
  private final List<String> fieldMask;
  private final String fields;
  private final String key;
  private final String prettyPrint;
  private final String quotaUser;
  private final String region;
  private final String requestId;
  private final String userIp;

  private PatchRegionAutoscalerHttpRequest() {
    this.access_token = null;
    this.autoscaler = null;
    this.autoscalerResource = null;
    this.callback = null;
    this.fieldMask = null;
    this.fields = null;
    this.key = null;
    this.prettyPrint = null;
    this.quotaUser = null;
    this.region = null;
    this.requestId = null;
    this.userIp = null;
  }

  private PatchRegionAutoscalerHttpRequest(
      String access_token,
      String autoscaler,
      Autoscaler autoscalerResource,
      String callback,
      List<String> fieldMask,
      String fields,
      String key,
      String prettyPrint,
      String quotaUser,
      String region,
      String requestId,
      String userIp) {
    this.access_token = access_token;
    this.autoscaler = autoscaler;
    this.autoscalerResource = autoscalerResource;
    this.callback = callback;
    this.fieldMask = fieldMask;
    this.fields = fields;
    this.key = key;
    this.prettyPrint = prettyPrint;
    this.quotaUser = quotaUser;
    this.region = region;
    this.requestId = requestId;
    this.userIp = userIp;
  }

  @Override
  public Object getFieldValue(String fieldName) {
    if (fieldName.equals("access_token")) {
      return access_token;
    }
    if (fieldName.equals("autoscaler")) {
      return autoscaler;
    }
    if (fieldName.equals("autoscalerResource")) {
      return autoscalerResource;
    }
    if (fieldName.equals("callback")) {
      return callback;
    }
    if (fieldName.equals("fieldMask")) {
      return fieldMask;
    }
    if (fieldName.equals("fields")) {
      return fields;
    }
    if (fieldName.equals("key")) {
      return key;
    }
    if (fieldName.equals("prettyPrint")) {
      return prettyPrint;
    }
    if (fieldName.equals("quotaUser")) {
      return quotaUser;
    }
    if (fieldName.equals("region")) {
      return region;
    }
    if (fieldName.equals("requestId")) {
      return requestId;
    }
    if (fieldName.equals("userIp")) {
      return userIp;
    }
    return null;
  }

  @Nullable
  @Override
  public Autoscaler getApiMessageRequestBody() {
    return autoscalerResource;
  }

  @Nullable
  @Override
  public List<String> getFieldMask() {
    return fieldMask;
  }

  public String getAccessToken() {
    return access_token;
  }

  public String getAutoscaler() {
    return autoscaler;
  }

  public Autoscaler getAutoscalerResource() {
    return autoscalerResource;
  }

  public String getCallback() {
    return callback;
  }

  public String getFields() {
    return fields;
  }

  public String getKey() {
    return key;
  }

  public String getPrettyPrint() {
    return prettyPrint;
  }

  public String getQuotaUser() {
    return quotaUser;
  }

  public String getRegion() {
    return region;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getUserIp() {
    return userIp;
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(PatchRegionAutoscalerHttpRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  public static PatchRegionAutoscalerHttpRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final PatchRegionAutoscalerHttpRequest DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new PatchRegionAutoscalerHttpRequest();
  }

  public static class Builder {
    private String access_token;
    private String autoscaler;
    private Autoscaler autoscalerResource;
    private String callback;
    private List<String> fieldMask;
    private String fields;
    private String key;
    private String prettyPrint;
    private String quotaUser;
    private String region;
    private String requestId;
    private String userIp;

    Builder() {}

    public Builder mergeFrom(PatchRegionAutoscalerHttpRequest other) {
      if (other == PatchRegionAutoscalerHttpRequest.getDefaultInstance()) return this;
      if (other.getAccessToken() != null) {
        this.access_token = other.access_token;
      }
      if (other.getAutoscaler() != null) {
        this.autoscaler = other.autoscaler;
      }
      if (other.getAutoscalerResource() != null) {
        this.autoscalerResource = other.autoscalerResource;
      }
      if (other.getCallback() != null) {
        this.callback = other.callback;
      }
      if (other.getFieldMask() != null) {
        this.fieldMask = other.fieldMask;
      }
      if (other.getFields() != null) {
        this.fields = other.fields;
      }
      if (other.getKey() != null) {
        this.key = other.key;
      }
      if (other.getPrettyPrint() != null) {
        this.prettyPrint = other.prettyPrint;
      }
      if (other.getQuotaUser() != null) {
        this.quotaUser = other.quotaUser;
      }
      if (other.getRegion() != null) {
        this.region = other.region;
      }
      if (other.getRequestId() != null) {
        this.requestId = other.requestId;
      }
      if (other.getUserIp() != null) {
        this.userIp = other.userIp;
      }
      return this;
    }

    Builder(PatchRegionAutoscalerHttpRequest source) {
      this.access_token = source.access_token;
      this.autoscaler = source.autoscaler;
      this.autoscalerResource = source.autoscalerResource;
      this.callback = source.callback;
      this.fieldMask = source.fieldMask;
      this.fields = source.fields;
      this.key = source.key;
      this.prettyPrint = source.prettyPrint;
      this.quotaUser = source.quotaUser;
      this.region = source.region;
      this.requestId = source.requestId;
      this.userIp = source.userIp;
    }

    public String getAccessToken() {
      return access_token;
    }

    public Builder setAccessToken(String access_token) {
      this.access_token = access_token;
      return this;
    }

    public String getAutoscaler() {
      return autoscaler;
    }

    public Builder setAutoscaler(String autoscaler) {
      this.autoscaler = autoscaler;
      return this;
    }

    public Autoscaler getAutoscalerResource() {
      return autoscalerResource;
    }

    public Builder setAutoscalerResource(Autoscaler autoscalerResource) {
      this.autoscalerResource = autoscalerResource;
      return this;
    }

    public String getCallback() {
      return callback;
    }

    public Builder setCallback(String callback) {
      this.callback = callback;
      return this;
    }

    public List<String> getFieldMask() {
      return fieldMask;
    }

    public Builder setFieldMask(List<String> fieldMask) {
      this.fieldMask = fieldMask;
      return this;
    }

    public String getFields() {
      return fields;
    }

    public Builder setFields(String fields) {
      this.fields = fields;
      return this;
    }

    public String getKey() {
      return key;
    }

    public Builder setKey(String key) {
      this.key = key;
      return this;
    }

    public String getPrettyPrint() {
      return prettyPrint;
    }

    public Builder setPrettyPrint(String prettyPrint) {
      this.prettyPrint = prettyPrint;
      return this;
    }

    public String getQuotaUser() {
      return quotaUser;
    }

    public Builder setQuotaUser(String quotaUser) {
      this.quotaUser = quotaUser;
      return this;
    }

    public String getRegion() {
      return region;
    }

    public Builder setRegion(String region) {
      this.region = region;
      return this;
    }

    public String getRequestId() {
      return requestId;
    }

    public Builder setRequestId(String requestId) {
      this.requestId = requestId;
      return this;
    }

    public String getUserIp() {
      return userIp;
    }

    public Builder setUserIp(String userIp) {
      this.userIp = userIp;
      return this;
    }

    public PatchRegionAutoscalerHttpRequest build() {
      String missing = "";

      if (region == null) {
        missing += " region";
      }

      if (!missing.isEmpty()) {
        throw new IllegalStateException("Missing required properties:" + missing);
      }
      return new PatchRegionAutoscalerHttpRequest(
          access_token,
          autoscaler,
          autoscalerResource,
          callback,
          fieldMask,
          fields,
          key,
          prettyPrint,
          quotaUser,
          region,
          requestId,
          userIp);
    }

    public Builder clone() {
      Builder newBuilder = new Builder();
      newBuilder.setAccessToken(this.access_token);
      newBuilder.setAutoscaler(this.autoscaler);
      newBuilder.setAutoscalerResource(this.autoscalerResource);
      newBuilder.setCallback(this.callback);
      newBuilder.setFieldMask(this.fieldMask);
      newBuilder.setFields(this.fields);
      newBuilder.setKey(this.key);
      newBuilder.setPrettyPrint(this.prettyPrint);
      newBuilder.setQuotaUser(this.quotaUser);
      newBuilder.setRegion(this.region);
      newBuilder.setRequestId(this.requestId);
      newBuilder.setUserIp(this.userIp);
      return newBuilder;
    }
  }

  @Override
  public String toString() {
    return "PatchRegionAutoscalerHttpRequest{"
        + "access_token="
        + access_token
        + ", "
        + "autoscaler="
        + autoscaler
        + ", "
        + "autoscalerResource="
        + autoscalerResource
        + ", "
        + "callback="
        + callback
        + ", "
        + "fieldMask="
        + fieldMask
        + ", "
        + "fields="
        + fields
        + ", "
        + "key="
        + key
        + ", "
        + "prettyPrint="
        + prettyPrint
        + ", "
        + "quotaUser="
        + quotaUser
        + ", "
        + "region="
        + region
        + ", "
        + "requestId="
        + requestId
        + ", "
        + "userIp="
        + userIp
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof PatchRegionAutoscalerHttpRequest) {
      PatchRegionAutoscalerHttpRequest that = (PatchRegionAutoscalerHttpRequest) o;
      return Objects.equals(this.access_token, that.getAccessToken())
          && Objects.equals(this.autoscaler, that.getAutoscaler())
          && Objects.equals(this.autoscalerResource, that.getAutoscalerResource())
          && Objects.equals(this.callback, that.getCallback())
          && Objects.equals(this.fieldMask, that.getFieldMask())
          && Objects.equals(this.fields, that.getFields())
          && Objects.equals(this.key, that.getKey())
          && Objects.equals(this.prettyPrint, that.getPrettyPrint())
          && Objects.equals(this.quotaUser, that.getQuotaUser())
          && Objects.equals(this.region, that.getRegion())
          && Objects.equals(this.requestId, that.getRequestId())
          && Objects.equals(this.userIp, that.getUserIp());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        access_token,
        autoscaler,
        autoscalerResource,
        callback,
        fieldMask,
        fields,
        key,
        prettyPrint,
        quotaUser,
        region,
        requestId,
        userIp);
  }
}
