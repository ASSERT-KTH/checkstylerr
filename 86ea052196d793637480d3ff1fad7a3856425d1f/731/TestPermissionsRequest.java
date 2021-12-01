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
public final class TestPermissionsRequest implements ApiMessage {
  private final List<String> permissions;

  private TestPermissionsRequest() {
    this.permissions = null;
  }

  private TestPermissionsRequest(List<String> permissions) {
    this.permissions = permissions;
  }

  @Override
  public Object getFieldValue(String fieldName) {
    if (fieldName.equals("permissions")) {
      return permissions;
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

  public List<String> getPermissionsList() {
    return permissions;
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(TestPermissionsRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  public static TestPermissionsRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final TestPermissionsRequest DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new TestPermissionsRequest();
  }

  public static class Builder {
    private List<String> permissions;

    Builder() {}

    public Builder mergeFrom(TestPermissionsRequest other) {
      if (other == TestPermissionsRequest.getDefaultInstance()) return this;
      if (other.getPermissionsList() != null) {
        this.permissions = other.permissions;
      }
      return this;
    }

    Builder(TestPermissionsRequest source) {
      this.permissions = source.permissions;
    }

    public List<String> getPermissionsList() {
      return permissions;
    }

    public Builder addAllPermissions(List<String> permissions) {
      if (this.permissions == null) {
        this.permissions = new ArrayList<>(permissions.size());
      }
      this.permissions.addAll(permissions);
      return this;
    }

    public Builder addPermissions(String permissions) {
      this.permissions.add(permissions);
      return this;
    }

    public TestPermissionsRequest build() {
      return new TestPermissionsRequest(permissions);
    }

    public Builder clone() {
      Builder newBuilder = new Builder();
      newBuilder.addAllPermissions(this.permissions);
      return newBuilder;
    }
  }

  @Override
  public String toString() {
    return "TestPermissionsRequest{" + "permissions=" + permissions + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof TestPermissionsRequest) {
      TestPermissionsRequest that = (TestPermissionsRequest) o;
      return Objects.equals(this.permissions, that.getPermissionsList());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(permissions);
  }
}
