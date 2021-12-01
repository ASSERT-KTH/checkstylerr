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
public final class TargetHttpsProxiesSetSslCertificatesRequest implements ApiMessage {
  private final List<String> sslCertificates;

  private TargetHttpsProxiesSetSslCertificatesRequest() {
    this.sslCertificates = null;
  }

  private TargetHttpsProxiesSetSslCertificatesRequest(List<String> sslCertificates) {
    this.sslCertificates = sslCertificates;
  }

  @Override
  public Object getFieldValue(String fieldName) {
    if (fieldName.equals("sslCertificates")) {
      return sslCertificates;
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

  public List<String> getSslCertificatesList() {
    return sslCertificates;
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(TargetHttpsProxiesSetSslCertificatesRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  public static TargetHttpsProxiesSetSslCertificatesRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final TargetHttpsProxiesSetSslCertificatesRequest DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new TargetHttpsProxiesSetSslCertificatesRequest();
  }

  public static class Builder {
    private List<String> sslCertificates;

    Builder() {}

    public Builder mergeFrom(TargetHttpsProxiesSetSslCertificatesRequest other) {
      if (other == TargetHttpsProxiesSetSslCertificatesRequest.getDefaultInstance()) return this;
      if (other.getSslCertificatesList() != null) {
        this.sslCertificates = other.sslCertificates;
      }
      return this;
    }

    Builder(TargetHttpsProxiesSetSslCertificatesRequest source) {
      this.sslCertificates = source.sslCertificates;
    }

    public List<String> getSslCertificatesList() {
      return sslCertificates;
    }

    public Builder addAllSslCertificates(List<String> sslCertificates) {
      if (this.sslCertificates == null) {
        this.sslCertificates = new ArrayList<>(sslCertificates.size());
      }
      this.sslCertificates.addAll(sslCertificates);
      return this;
    }

    public Builder addSslCertificates(String sslCertificates) {
      this.sslCertificates.add(sslCertificates);
      return this;
    }

    public TargetHttpsProxiesSetSslCertificatesRequest build() {
      return new TargetHttpsProxiesSetSslCertificatesRequest(sslCertificates);
    }

    public Builder clone() {
      Builder newBuilder = new Builder();
      newBuilder.addAllSslCertificates(this.sslCertificates);
      return newBuilder;
    }
  }

  @Override
  public String toString() {
    return "TargetHttpsProxiesSetSslCertificatesRequest{"
        + "sslCertificates="
        + sslCertificates
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof TargetHttpsProxiesSetSslCertificatesRequest) {
      TargetHttpsProxiesSetSslCertificatesRequest that =
          (TargetHttpsProxiesSetSslCertificatesRequest) o;
      return Objects.equals(this.sslCertificates, that.getSslCertificatesList());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sslCertificates);
  }
}
