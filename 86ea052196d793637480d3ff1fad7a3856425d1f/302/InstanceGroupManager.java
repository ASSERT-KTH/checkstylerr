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
public final class InstanceGroupManager implements ApiMessage {
  private final String baseInstanceName;
  private final String creationTimestamp;
  private final InstanceGroupManagerActionsSummary currentActions;
  private final String description;
  private final String fingerprint;
  private final String id;
  private final String instanceGroup;
  private final String instanceTemplate;
  private final String kind;
  private final String name;
  private final List<NamedPort> namedPorts;
  private final String region;
  private final String selfLink;
  private final List<String> targetPools;
  private final Integer targetSize;
  private final String zone;

  private InstanceGroupManager() {
    this.baseInstanceName = null;
    this.creationTimestamp = null;
    this.currentActions = null;
    this.description = null;
    this.fingerprint = null;
    this.id = null;
    this.instanceGroup = null;
    this.instanceTemplate = null;
    this.kind = null;
    this.name = null;
    this.namedPorts = null;
    this.region = null;
    this.selfLink = null;
    this.targetPools = null;
    this.targetSize = null;
    this.zone = null;
  }

  private InstanceGroupManager(
      String baseInstanceName,
      String creationTimestamp,
      InstanceGroupManagerActionsSummary currentActions,
      String description,
      String fingerprint,
      String id,
      String instanceGroup,
      String instanceTemplate,
      String kind,
      String name,
      List<NamedPort> namedPorts,
      String region,
      String selfLink,
      List<String> targetPools,
      Integer targetSize,
      String zone) {
    this.baseInstanceName = baseInstanceName;
    this.creationTimestamp = creationTimestamp;
    this.currentActions = currentActions;
    this.description = description;
    this.fingerprint = fingerprint;
    this.id = id;
    this.instanceGroup = instanceGroup;
    this.instanceTemplate = instanceTemplate;
    this.kind = kind;
    this.name = name;
    this.namedPorts = namedPorts;
    this.region = region;
    this.selfLink = selfLink;
    this.targetPools = targetPools;
    this.targetSize = targetSize;
    this.zone = zone;
  }

  @Override
  public Object getFieldValue(String fieldName) {
    if (fieldName.equals("baseInstanceName")) {
      return baseInstanceName;
    }
    if (fieldName.equals("creationTimestamp")) {
      return creationTimestamp;
    }
    if (fieldName.equals("currentActions")) {
      return currentActions;
    }
    if (fieldName.equals("description")) {
      return description;
    }
    if (fieldName.equals("fingerprint")) {
      return fingerprint;
    }
    if (fieldName.equals("id")) {
      return id;
    }
    if (fieldName.equals("instanceGroup")) {
      return instanceGroup;
    }
    if (fieldName.equals("instanceTemplate")) {
      return instanceTemplate;
    }
    if (fieldName.equals("kind")) {
      return kind;
    }
    if (fieldName.equals("name")) {
      return name;
    }
    if (fieldName.equals("namedPorts")) {
      return namedPorts;
    }
    if (fieldName.equals("region")) {
      return region;
    }
    if (fieldName.equals("selfLink")) {
      return selfLink;
    }
    if (fieldName.equals("targetPools")) {
      return targetPools;
    }
    if (fieldName.equals("targetSize")) {
      return targetSize;
    }
    if (fieldName.equals("zone")) {
      return zone;
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

  public String getBaseInstanceName() {
    return baseInstanceName;
  }

  public String getCreationTimestamp() {
    return creationTimestamp;
  }

  public InstanceGroupManagerActionsSummary getCurrentActions() {
    return currentActions;
  }

  public String getDescription() {
    return description;
  }

  public String getFingerprint() {
    return fingerprint;
  }

  public String getId() {
    return id;
  }

  public String getInstanceGroup() {
    return instanceGroup;
  }

  public String getInstanceTemplate() {
    return instanceTemplate;
  }

  public String getKind() {
    return kind;
  }

  public String getName() {
    return name;
  }

  public List<NamedPort> getNamedPortsList() {
    return namedPorts;
  }

  public String getRegion() {
    return region;
  }

  public String getSelfLink() {
    return selfLink;
  }

  public List<String> getTargetPoolsList() {
    return targetPools;
  }

  public Integer getTargetSize() {
    return targetSize;
  }

  public String getZone() {
    return zone;
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(InstanceGroupManager prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  public static InstanceGroupManager getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final InstanceGroupManager DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new InstanceGroupManager();
  }

  public static class Builder {
    private String baseInstanceName;
    private String creationTimestamp;
    private InstanceGroupManagerActionsSummary currentActions;
    private String description;
    private String fingerprint;
    private String id;
    private String instanceGroup;
    private String instanceTemplate;
    private String kind;
    private String name;
    private List<NamedPort> namedPorts;
    private String region;
    private String selfLink;
    private List<String> targetPools;
    private Integer targetSize;
    private String zone;

    Builder() {}

    public Builder mergeFrom(InstanceGroupManager other) {
      if (other == InstanceGroupManager.getDefaultInstance()) return this;
      if (other.getBaseInstanceName() != null) {
        this.baseInstanceName = other.baseInstanceName;
      }
      if (other.getCreationTimestamp() != null) {
        this.creationTimestamp = other.creationTimestamp;
      }
      if (other.getCurrentActions() != null) {
        this.currentActions = other.currentActions;
      }
      if (other.getDescription() != null) {
        this.description = other.description;
      }
      if (other.getFingerprint() != null) {
        this.fingerprint = other.fingerprint;
      }
      if (other.getId() != null) {
        this.id = other.id;
      }
      if (other.getInstanceGroup() != null) {
        this.instanceGroup = other.instanceGroup;
      }
      if (other.getInstanceTemplate() != null) {
        this.instanceTemplate = other.instanceTemplate;
      }
      if (other.getKind() != null) {
        this.kind = other.kind;
      }
      if (other.getName() != null) {
        this.name = other.name;
      }
      if (other.getNamedPortsList() != null) {
        this.namedPorts = other.namedPorts;
      }
      if (other.getRegion() != null) {
        this.region = other.region;
      }
      if (other.getSelfLink() != null) {
        this.selfLink = other.selfLink;
      }
      if (other.getTargetPoolsList() != null) {
        this.targetPools = other.targetPools;
      }
      if (other.getTargetSize() != null) {
        this.targetSize = other.targetSize;
      }
      if (other.getZone() != null) {
        this.zone = other.zone;
      }
      return this;
    }

    Builder(InstanceGroupManager source) {
      this.baseInstanceName = source.baseInstanceName;
      this.creationTimestamp = source.creationTimestamp;
      this.currentActions = source.currentActions;
      this.description = source.description;
      this.fingerprint = source.fingerprint;
      this.id = source.id;
      this.instanceGroup = source.instanceGroup;
      this.instanceTemplate = source.instanceTemplate;
      this.kind = source.kind;
      this.name = source.name;
      this.namedPorts = source.namedPorts;
      this.region = source.region;
      this.selfLink = source.selfLink;
      this.targetPools = source.targetPools;
      this.targetSize = source.targetSize;
      this.zone = source.zone;
    }

    public String getBaseInstanceName() {
      return baseInstanceName;
    }

    public Builder setBaseInstanceName(String baseInstanceName) {
      this.baseInstanceName = baseInstanceName;
      return this;
    }

    public String getCreationTimestamp() {
      return creationTimestamp;
    }

    public Builder setCreationTimestamp(String creationTimestamp) {
      this.creationTimestamp = creationTimestamp;
      return this;
    }

    public InstanceGroupManagerActionsSummary getCurrentActions() {
      return currentActions;
    }

    public Builder setCurrentActions(InstanceGroupManagerActionsSummary currentActions) {
      this.currentActions = currentActions;
      return this;
    }

    public String getDescription() {
      return description;
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public String getFingerprint() {
      return fingerprint;
    }

    public Builder setFingerprint(String fingerprint) {
      this.fingerprint = fingerprint;
      return this;
    }

    public String getId() {
      return id;
    }

    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    public String getInstanceGroup() {
      return instanceGroup;
    }

    public Builder setInstanceGroup(String instanceGroup) {
      this.instanceGroup = instanceGroup;
      return this;
    }

    public String getInstanceTemplate() {
      return instanceTemplate;
    }

    public Builder setInstanceTemplate(String instanceTemplate) {
      this.instanceTemplate = instanceTemplate;
      return this;
    }

    public String getKind() {
      return kind;
    }

    public Builder setKind(String kind) {
      this.kind = kind;
      return this;
    }

    public String getName() {
      return name;
    }

    public Builder setName(String name) {
      this.name = name;
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

    public String getRegion() {
      return region;
    }

    public Builder setRegion(String region) {
      this.region = region;
      return this;
    }

    public String getSelfLink() {
      return selfLink;
    }

    public Builder setSelfLink(String selfLink) {
      this.selfLink = selfLink;
      return this;
    }

    public List<String> getTargetPoolsList() {
      return targetPools;
    }

    public Builder addAllTargetPools(List<String> targetPools) {
      if (this.targetPools == null) {
        this.targetPools = new ArrayList<>(targetPools.size());
      }
      this.targetPools.addAll(targetPools);
      return this;
    }

    public Builder addTargetPools(String targetPools) {
      this.targetPools.add(targetPools);
      return this;
    }

    public Integer getTargetSize() {
      return targetSize;
    }

    public Builder setTargetSize(Integer targetSize) {
      this.targetSize = targetSize;
      return this;
    }

    public String getZone() {
      return zone;
    }

    public Builder setZone(String zone) {
      this.zone = zone;
      return this;
    }

    public InstanceGroupManager build() {

      return new InstanceGroupManager(
          baseInstanceName,
          creationTimestamp,
          currentActions,
          description,
          fingerprint,
          id,
          instanceGroup,
          instanceTemplate,
          kind,
          name,
          namedPorts,
          region,
          selfLink,
          targetPools,
          targetSize,
          zone);
    }

    public Builder clone() {
      Builder newBuilder = new Builder();
      newBuilder.setBaseInstanceName(this.baseInstanceName);
      newBuilder.setCreationTimestamp(this.creationTimestamp);
      newBuilder.setCurrentActions(this.currentActions);
      newBuilder.setDescription(this.description);
      newBuilder.setFingerprint(this.fingerprint);
      newBuilder.setId(this.id);
      newBuilder.setInstanceGroup(this.instanceGroup);
      newBuilder.setInstanceTemplate(this.instanceTemplate);
      newBuilder.setKind(this.kind);
      newBuilder.setName(this.name);
      newBuilder.addAllNamedPorts(this.namedPorts);
      newBuilder.setRegion(this.region);
      newBuilder.setSelfLink(this.selfLink);
      newBuilder.addAllTargetPools(this.targetPools);
      newBuilder.setTargetSize(this.targetSize);
      newBuilder.setZone(this.zone);
      return newBuilder;
    }
  }

  @Override
  public String toString() {
    return "InstanceGroupManager{"
        + "baseInstanceName="
        + baseInstanceName
        + ", "
        + "creationTimestamp="
        + creationTimestamp
        + ", "
        + "currentActions="
        + currentActions
        + ", "
        + "description="
        + description
        + ", "
        + "fingerprint="
        + fingerprint
        + ", "
        + "id="
        + id
        + ", "
        + "instanceGroup="
        + instanceGroup
        + ", "
        + "instanceTemplate="
        + instanceTemplate
        + ", "
        + "kind="
        + kind
        + ", "
        + "name="
        + name
        + ", "
        + "namedPorts="
        + namedPorts
        + ", "
        + "region="
        + region
        + ", "
        + "selfLink="
        + selfLink
        + ", "
        + "targetPools="
        + targetPools
        + ", "
        + "targetSize="
        + targetSize
        + ", "
        + "zone="
        + zone
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof InstanceGroupManager) {
      InstanceGroupManager that = (InstanceGroupManager) o;
      return Objects.equals(this.baseInstanceName, that.getBaseInstanceName())
          && Objects.equals(this.creationTimestamp, that.getCreationTimestamp())
          && Objects.equals(this.currentActions, that.getCurrentActions())
          && Objects.equals(this.description, that.getDescription())
          && Objects.equals(this.fingerprint, that.getFingerprint())
          && Objects.equals(this.id, that.getId())
          && Objects.equals(this.instanceGroup, that.getInstanceGroup())
          && Objects.equals(this.instanceTemplate, that.getInstanceTemplate())
          && Objects.equals(this.kind, that.getKind())
          && Objects.equals(this.name, that.getName())
          && Objects.equals(this.namedPorts, that.getNamedPortsList())
          && Objects.equals(this.region, that.getRegion())
          && Objects.equals(this.selfLink, that.getSelfLink())
          && Objects.equals(this.targetPools, that.getTargetPoolsList())
          && Objects.equals(this.targetSize, that.getTargetSize())
          && Objects.equals(this.zone, that.getZone());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        baseInstanceName,
        creationTimestamp,
        currentActions,
        description,
        fingerprint,
        id,
        instanceGroup,
        instanceTemplate,
        kind,
        name,
        namedPorts,
        region,
        selfLink,
        targetPools,
        targetSize,
        zone);
  }
}
