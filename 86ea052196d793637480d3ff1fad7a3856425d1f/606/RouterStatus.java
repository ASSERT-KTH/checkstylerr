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
public final class RouterStatus implements ApiMessage {
  private final List<Route> bestRoutes;
  private final List<Route> bestRoutesForRouter;
  private final List<RouterStatusBgpPeerStatus> bgpPeerStatus;
  private final String network;

  private RouterStatus() {
    this.bestRoutes = null;
    this.bestRoutesForRouter = null;
    this.bgpPeerStatus = null;
    this.network = null;
  }

  private RouterStatus(
      List<Route> bestRoutes,
      List<Route> bestRoutesForRouter,
      List<RouterStatusBgpPeerStatus> bgpPeerStatus,
      String network) {
    this.bestRoutes = bestRoutes;
    this.bestRoutesForRouter = bestRoutesForRouter;
    this.bgpPeerStatus = bgpPeerStatus;
    this.network = network;
  }

  @Override
  public Object getFieldValue(String fieldName) {
    if (fieldName.equals("bestRoutes")) {
      return bestRoutes;
    }
    if (fieldName.equals("bestRoutesForRouter")) {
      return bestRoutesForRouter;
    }
    if (fieldName.equals("bgpPeerStatus")) {
      return bgpPeerStatus;
    }
    if (fieldName.equals("network")) {
      return network;
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

  public List<Route> getBestRoutesList() {
    return bestRoutes;
  }

  public List<Route> getBestRoutesForRouterList() {
    return bestRoutesForRouter;
  }

  public List<RouterStatusBgpPeerStatus> getBgpPeerStatusList() {
    return bgpPeerStatus;
  }

  public String getNetwork() {
    return network;
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(RouterStatus prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  public static RouterStatus getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final RouterStatus DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new RouterStatus();
  }

  public static class Builder {
    private List<Route> bestRoutes;
    private List<Route> bestRoutesForRouter;
    private List<RouterStatusBgpPeerStatus> bgpPeerStatus;
    private String network;

    Builder() {}

    public Builder mergeFrom(RouterStatus other) {
      if (other == RouterStatus.getDefaultInstance()) return this;
      if (other.getBestRoutesList() != null) {
        this.bestRoutes = other.bestRoutes;
      }
      if (other.getBestRoutesForRouterList() != null) {
        this.bestRoutesForRouter = other.bestRoutesForRouter;
      }
      if (other.getBgpPeerStatusList() != null) {
        this.bgpPeerStatus = other.bgpPeerStatus;
      }
      if (other.getNetwork() != null) {
        this.network = other.network;
      }
      return this;
    }

    Builder(RouterStatus source) {
      this.bestRoutes = source.bestRoutes;
      this.bestRoutesForRouter = source.bestRoutesForRouter;
      this.bgpPeerStatus = source.bgpPeerStatus;
      this.network = source.network;
    }

    public List<Route> getBestRoutesList() {
      return bestRoutes;
    }

    public Builder addAllBestRoutes(List<Route> bestRoutes) {
      if (this.bestRoutes == null) {
        this.bestRoutes = new ArrayList<>(bestRoutes.size());
      }
      this.bestRoutes.addAll(bestRoutes);
      return this;
    }

    public Builder addBestRoutes(Route bestRoutes) {
      this.bestRoutes.add(bestRoutes);
      return this;
    }

    public List<Route> getBestRoutesForRouterList() {
      return bestRoutesForRouter;
    }

    public Builder addAllBestRoutesForRouter(List<Route> bestRoutesForRouter) {
      if (this.bestRoutesForRouter == null) {
        this.bestRoutesForRouter = new ArrayList<>(bestRoutesForRouter.size());
      }
      this.bestRoutesForRouter.addAll(bestRoutesForRouter);
      return this;
    }

    public Builder addBestRoutesForRouter(Route bestRoutesForRouter) {
      this.bestRoutesForRouter.add(bestRoutesForRouter);
      return this;
    }

    public List<RouterStatusBgpPeerStatus> getBgpPeerStatusList() {
      return bgpPeerStatus;
    }

    public Builder addAllBgpPeerStatus(List<RouterStatusBgpPeerStatus> bgpPeerStatus) {
      if (this.bgpPeerStatus == null) {
        this.bgpPeerStatus = new ArrayList<>(bgpPeerStatus.size());
      }
      this.bgpPeerStatus.addAll(bgpPeerStatus);
      return this;
    }

    public Builder addBgpPeerStatus(RouterStatusBgpPeerStatus bgpPeerStatus) {
      this.bgpPeerStatus.add(bgpPeerStatus);
      return this;
    }

    public String getNetwork() {
      return network;
    }

    public Builder setNetwork(String network) {
      this.network = network;
      return this;
    }

    public RouterStatus build() {

      return new RouterStatus(bestRoutes, bestRoutesForRouter, bgpPeerStatus, network);
    }

    public Builder clone() {
      Builder newBuilder = new Builder();
      newBuilder.addAllBestRoutes(this.bestRoutes);
      newBuilder.addAllBestRoutesForRouter(this.bestRoutesForRouter);
      newBuilder.addAllBgpPeerStatus(this.bgpPeerStatus);
      newBuilder.setNetwork(this.network);
      return newBuilder;
    }
  }

  @Override
  public String toString() {
    return "RouterStatus{"
        + "bestRoutes="
        + bestRoutes
        + ", "
        + "bestRoutesForRouter="
        + bestRoutesForRouter
        + ", "
        + "bgpPeerStatus="
        + bgpPeerStatus
        + ", "
        + "network="
        + network
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof RouterStatus) {
      RouterStatus that = (RouterStatus) o;
      return Objects.equals(this.bestRoutes, that.getBestRoutesList())
          && Objects.equals(this.bestRoutesForRouter, that.getBestRoutesForRouterList())
          && Objects.equals(this.bgpPeerStatus, that.getBgpPeerStatusList())
          && Objects.equals(this.network, that.getNetwork());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(bestRoutes, bestRoutesForRouter, bgpPeerStatus, network);
  }
}
