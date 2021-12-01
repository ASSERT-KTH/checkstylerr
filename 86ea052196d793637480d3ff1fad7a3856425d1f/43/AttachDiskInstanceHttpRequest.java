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
public final class AttachDiskInstanceHttpRequest implements ApiMessage {
  private final String access_token;
  private final AttachedDisk attachedDiskResource;
  private final String callback;
  private final String fields;
  private final Boolean forceAttach;
  private final String instance;
  private final String key;
  private final String prettyPrint;
  private final String quotaUser;
  private final String requestId;
  private final String userIp;

  private AttachDiskInstanceHttpRequest() {
    this.access_token = null;
    this.attachedDiskResource = null;
    this.callback = null;
    this.fields = null;
    this.forceAttach = null;
    this.instance = null;
    this.key = null;
    this.prettyPrint = null;
    this.quotaUser = null;
    this.requestId = null;
    this.userIp = null;
  }

  private AttachDiskInstanceHttpRequest(
      String access_token,
      AttachedDisk attachedDiskResource,
      String callback,
      String fields,
      Boolean forceAttach,
      String instance,
      String key,
      String prettyPrint,
      String quotaUser,
      String requestId,
      String userIp) {
    this.access_token = access_token;
    this.attachedDiskResource = attachedDiskResource;
    this.callback = callback;
    this.fields = fields;
    this.forceAttach = forceAttach;
    this.instance = instance;
    this.key = key;
    this.prettyPrint = prettyPrint;
    this.quotaUser = quotaUser;
    this.requestId = requestId;
    this.userIp = userIp;
  }

  @Override
  public Object getFieldValue(String fieldName) {
    if (fieldName.equals("access_token")) {
      return access_token;
    }
    if (fieldName.equals("attachedDiskResource")) {
      return attachedDiskResource;
    }
    if (fieldName.equals("callback")) {
      return callback;
    }
    if (fieldName.equals("fields")) {
      return fields;
    }
    if (fieldName.equals("forceAttach")) {
      return forceAttach;
    }
    if (fieldName.equals("instance")) {
      return instance;
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
  public AttachedDisk getApiMessageRequestBody() {
    return attachedDiskResource;
  }

  @Nullable
  @Override
  public List<String> getFieldMask() {
    return null;
  }

  public String getAccessToken() {
    return access_token;
  }

  public AttachedDisk getAttachedDiskResource() {
    return attachedDiskResource;
  }

  public String getCallback() {
    return callback;
  }

  public String getFields() {
    return fields;
  }

  public Boolean getForceAttach() {
    return forceAttach;
  }

  public String getInstance() {
    return instance;
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

  public String getRequestId() {
    return requestId;
  }

  public String getUserIp() {
    return userIp;
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(AttachDiskInstanceHttpRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  public static AttachDiskInstanceHttpRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final AttachDiskInstanceHttpRequest DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new AttachDiskInstanceHttpRequest();
  }

  public static class Builder {
    private String access_token;
    private AttachedDisk attachedDiskResource;
    private String callback;
    private String fields;
    private Boolean forceAttach;
    private String instance;
    private String key;
    private String prettyPrint;
    private String quotaUser;
    private String requestId;
    private String userIp;

    Builder() {}

    public Builder mergeFrom(AttachDiskInstanceHttpRequest other) {
      if (other == AttachDiskInstanceHttpRequest.getDefaultInstance()) return this;
      if (other.getAccessToken() != null) {
        this.access_token = other.access_token;
      }
      if (other.getAttachedDiskResource() != null) {
        this.attachedDiskResource = other.attachedDiskResource;
      }
      if (other.getCallback() != null) {
        this.callback = other.callback;
      }
      if (other.getFields() != null) {
        this.fields = other.fields;
      }
      if (other.getForceAttach() != null) {
        this.forceAttach = other.forceAttach;
      }
      if (other.getInstance() != null) {
        this.instance = other.instance;
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
      if (other.getRequestId() != null) {
        this.requestId = other.requestId;
      }
      if (other.getUserIp() != null) {
        this.userIp = other.userIp;
      }
      return this;
    }

    Builder(AttachDiskInstanceHttpRequest source) {
      this.access_token = source.access_token;
      this.attachedDiskResource = source.attachedDiskResource;
      this.callback = source.callback;
      this.fields = source.fields;
      this.forceAttach = source.forceAttach;
      this.instance = source.instance;
      this.key = source.key;
      this.prettyPrint = source.prettyPrint;
      this.quotaUser = source.quotaUser;
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

    public AttachedDisk getAttachedDiskResource() {
      return attachedDiskResource;
    }

    public Builder setAttachedDiskResource(AttachedDisk attachedDiskResource) {
      this.attachedDiskResource = attachedDiskResource;
      return this;
    }

    public String getCallback() {
      return callback;
    }

    public Builder setCallback(String callback) {
      this.callback = callback;
      return this;
    }

    public String getFields() {
      return fields;
    }

    public Builder setFields(String fields) {
      this.fields = fields;
      return this;
    }

    public Boolean getForceAttach() {
      return forceAttach;
    }

    public Builder setForceAttach(Boolean forceAttach) {
      this.forceAttach = forceAttach;
      return this;
    }

    public String getInstance() {
      return instance;
    }

    public Builder setInstance(String instance) {
      this.instance = instance;
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

    public AttachDiskInstanceHttpRequest build() {
      String missing = "";

      if (instance == null) {
        missing += " instance";
      }

      if (!missing.isEmpty()) {
        throw new IllegalStateException("Missing required properties:" + missing);
      }
      return new AttachDiskInstanceHttpRequest(
          access_token,
          attachedDiskResource,
          callback,
          fields,
          forceAttach,
          instance,
          key,
          prettyPrint,
          quotaUser,
          requestId,
          userIp);
    }

    public Builder clone() {
      Builder newBuilder = new Builder();
      newBuilder.setAccessToken(this.access_token);
      newBuilder.setAttachedDiskResource(this.attachedDiskResource);
      newBuilder.setCallback(this.callback);
      newBuilder.setFields(this.fields);
      newBuilder.setForceAttach(this.forceAttach);
      newBuilder.setInstance(this.instance);
      newBuilder.setKey(this.key);
      newBuilder.setPrettyPrint(this.prettyPrint);
      newBuilder.setQuotaUser(this.quotaUser);
      newBuilder.setRequestId(this.requestId);
      newBuilder.setUserIp(this.userIp);
      return newBuilder;
    }
  }

  @Override
  public String toString() {
    return "AttachDiskInstanceHttpRequest{"
        + "access_token="
        + access_token
        + ", "
        + "attachedDiskResource="
        + attachedDiskResource
        + ", "
        + "callback="
        + callback
        + ", "
        + "fields="
        + fields
        + ", "
        + "forceAttach="
        + forceAttach
        + ", "
        + "instance="
        + instance
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
    if (o instanceof AttachDiskInstanceHttpRequest) {
      AttachDiskInstanceHttpRequest that = (AttachDiskInstanceHttpRequest) o;
      return Objects.equals(this.access_token, that.getAccessToken())
          && Objects.equals(this.attachedDiskResource, that.getAttachedDiskResource())
          && Objects.equals(this.callback, that.getCallback())
          && Objects.equals(this.fields, that.getFields())
          && Objects.equals(this.forceAttach, that.getForceAttach())
          && Objects.equals(this.instance, that.getInstance())
          && Objects.equals(this.key, that.getKey())
          && Objects.equals(this.prettyPrint, that.getPrettyPrint())
          && Objects.equals(this.quotaUser, that.getQuotaUser())
          && Objects.equals(this.requestId, that.getRequestId())
          && Objects.equals(this.userIp, that.getUserIp());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        access_token,
        attachedDiskResource,
        callback,
        fields,
        forceAttach,
        instance,
        key,
        prettyPrint,
        quotaUser,
        requestId,
        userIp);
  }
}
