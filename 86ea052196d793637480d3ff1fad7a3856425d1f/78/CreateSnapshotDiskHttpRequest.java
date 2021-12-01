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
public final class CreateSnapshotDiskHttpRequest implements ApiMessage {
  private final String access_token;
  private final String callback;
  private final String disk;
  private final String fields;
  private final Boolean guestFlush;
  private final String key;
  private final String prettyPrint;
  private final String quotaUser;
  private final String requestId;
  private final Snapshot snapshotResource;
  private final String userIp;

  private CreateSnapshotDiskHttpRequest() {
    this.access_token = null;
    this.callback = null;
    this.disk = null;
    this.fields = null;
    this.guestFlush = null;
    this.key = null;
    this.prettyPrint = null;
    this.quotaUser = null;
    this.requestId = null;
    this.snapshotResource = null;
    this.userIp = null;
  }

  private CreateSnapshotDiskHttpRequest(
      String access_token,
      String callback,
      String disk,
      String fields,
      Boolean guestFlush,
      String key,
      String prettyPrint,
      String quotaUser,
      String requestId,
      Snapshot snapshotResource,
      String userIp) {
    this.access_token = access_token;
    this.callback = callback;
    this.disk = disk;
    this.fields = fields;
    this.guestFlush = guestFlush;
    this.key = key;
    this.prettyPrint = prettyPrint;
    this.quotaUser = quotaUser;
    this.requestId = requestId;
    this.snapshotResource = snapshotResource;
    this.userIp = userIp;
  }

  @Override
  public Object getFieldValue(String fieldName) {
    if (fieldName.equals("access_token")) {
      return access_token;
    }
    if (fieldName.equals("callback")) {
      return callback;
    }
    if (fieldName.equals("disk")) {
      return disk;
    }
    if (fieldName.equals("fields")) {
      return fields;
    }
    if (fieldName.equals("guestFlush")) {
      return guestFlush;
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
    if (fieldName.equals("snapshotResource")) {
      return snapshotResource;
    }
    if (fieldName.equals("userIp")) {
      return userIp;
    }
    return null;
  }

  @Nullable
  @Override
  public Snapshot getApiMessageRequestBody() {
    return snapshotResource;
  }

  @Nullable
  @Override
  public List<String> getFieldMask() {
    return null;
  }

  public String getAccessToken() {
    return access_token;
  }

  public String getCallback() {
    return callback;
  }

  public String getDisk() {
    return disk;
  }

  public String getFields() {
    return fields;
  }

  public Boolean getGuestFlush() {
    return guestFlush;
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

  public Snapshot getSnapshotResource() {
    return snapshotResource;
  }

  public String getUserIp() {
    return userIp;
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(CreateSnapshotDiskHttpRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  public static CreateSnapshotDiskHttpRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final CreateSnapshotDiskHttpRequest DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new CreateSnapshotDiskHttpRequest();
  }

  public static class Builder {
    private String access_token;
    private String callback;
    private String disk;
    private String fields;
    private Boolean guestFlush;
    private String key;
    private String prettyPrint;
    private String quotaUser;
    private String requestId;
    private Snapshot snapshotResource;
    private String userIp;

    Builder() {}

    public Builder mergeFrom(CreateSnapshotDiskHttpRequest other) {
      if (other == CreateSnapshotDiskHttpRequest.getDefaultInstance()) return this;
      if (other.getAccessToken() != null) {
        this.access_token = other.access_token;
      }
      if (other.getCallback() != null) {
        this.callback = other.callback;
      }
      if (other.getDisk() != null) {
        this.disk = other.disk;
      }
      if (other.getFields() != null) {
        this.fields = other.fields;
      }
      if (other.getGuestFlush() != null) {
        this.guestFlush = other.guestFlush;
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
      if (other.getSnapshotResource() != null) {
        this.snapshotResource = other.snapshotResource;
      }
      if (other.getUserIp() != null) {
        this.userIp = other.userIp;
      }
      return this;
    }

    Builder(CreateSnapshotDiskHttpRequest source) {
      this.access_token = source.access_token;
      this.callback = source.callback;
      this.disk = source.disk;
      this.fields = source.fields;
      this.guestFlush = source.guestFlush;
      this.key = source.key;
      this.prettyPrint = source.prettyPrint;
      this.quotaUser = source.quotaUser;
      this.requestId = source.requestId;
      this.snapshotResource = source.snapshotResource;
      this.userIp = source.userIp;
    }

    public String getAccessToken() {
      return access_token;
    }

    public Builder setAccessToken(String access_token) {
      this.access_token = access_token;
      return this;
    }

    public String getCallback() {
      return callback;
    }

    public Builder setCallback(String callback) {
      this.callback = callback;
      return this;
    }

    public String getDisk() {
      return disk;
    }

    public Builder setDisk(String disk) {
      this.disk = disk;
      return this;
    }

    public String getFields() {
      return fields;
    }

    public Builder setFields(String fields) {
      this.fields = fields;
      return this;
    }

    public Boolean getGuestFlush() {
      return guestFlush;
    }

    public Builder setGuestFlush(Boolean guestFlush) {
      this.guestFlush = guestFlush;
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

    public Snapshot getSnapshotResource() {
      return snapshotResource;
    }

    public Builder setSnapshotResource(Snapshot snapshotResource) {
      this.snapshotResource = snapshotResource;
      return this;
    }

    public String getUserIp() {
      return userIp;
    }

    public Builder setUserIp(String userIp) {
      this.userIp = userIp;
      return this;
    }

    public CreateSnapshotDiskHttpRequest build() {
      String missing = "";

      if (disk == null) {
        missing += " disk";
      }

      if (!missing.isEmpty()) {
        throw new IllegalStateException("Missing required properties:" + missing);
      }
      return new CreateSnapshotDiskHttpRequest(
          access_token,
          callback,
          disk,
          fields,
          guestFlush,
          key,
          prettyPrint,
          quotaUser,
          requestId,
          snapshotResource,
          userIp);
    }

    public Builder clone() {
      Builder newBuilder = new Builder();
      newBuilder.setAccessToken(this.access_token);
      newBuilder.setCallback(this.callback);
      newBuilder.setDisk(this.disk);
      newBuilder.setFields(this.fields);
      newBuilder.setGuestFlush(this.guestFlush);
      newBuilder.setKey(this.key);
      newBuilder.setPrettyPrint(this.prettyPrint);
      newBuilder.setQuotaUser(this.quotaUser);
      newBuilder.setRequestId(this.requestId);
      newBuilder.setSnapshotResource(this.snapshotResource);
      newBuilder.setUserIp(this.userIp);
      return newBuilder;
    }
  }

  @Override
  public String toString() {
    return "CreateSnapshotDiskHttpRequest{"
        + "access_token="
        + access_token
        + ", "
        + "callback="
        + callback
        + ", "
        + "disk="
        + disk
        + ", "
        + "fields="
        + fields
        + ", "
        + "guestFlush="
        + guestFlush
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
        + "snapshotResource="
        + snapshotResource
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
    if (o instanceof CreateSnapshotDiskHttpRequest) {
      CreateSnapshotDiskHttpRequest that = (CreateSnapshotDiskHttpRequest) o;
      return Objects.equals(this.access_token, that.getAccessToken())
          && Objects.equals(this.callback, that.getCallback())
          && Objects.equals(this.disk, that.getDisk())
          && Objects.equals(this.fields, that.getFields())
          && Objects.equals(this.guestFlush, that.getGuestFlush())
          && Objects.equals(this.key, that.getKey())
          && Objects.equals(this.prettyPrint, that.getPrettyPrint())
          && Objects.equals(this.quotaUser, that.getQuotaUser())
          && Objects.equals(this.requestId, that.getRequestId())
          && Objects.equals(this.snapshotResource, that.getSnapshotResource())
          && Objects.equals(this.userIp, that.getUserIp());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        access_token,
        callback,
        disk,
        fields,
        guestFlush,
        key,
        prettyPrint,
        quotaUser,
        requestId,
        snapshotResource,
        userIp);
  }
}
