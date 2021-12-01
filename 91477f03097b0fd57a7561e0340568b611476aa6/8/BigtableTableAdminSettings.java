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
package com.google.cloud.bigtable.admin.v2;

import com.google.cloud.bigtable.admin.v2.stub.BigtableTableAdminStubSettings;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Settings class to configure an instance of {@link BigtableTableAdminClient}.
 *
 * <p>It must be configured with a project id and instance id.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * BigtableTableAdminSettings.Builder tableAdminSettingsBuilder = BigtableTableAdminSettings.newBuilder()
 *   .setProjectId("my-project")
 *   .setInstanceId("my-instance");
 *
 * tableAdminSettingsBuilder.stubSettings().createTableSettings()
 *   .setRetrySettings(
 *     RetrySettings.newBuilder()
 *       .setTotalTimeout(Duration.ofMinutes(15))
 *       .build());
 *
 * BigtableTableAdminSettings tableAdminSettings = tableAdminSettingsBuilder.build();
 * }</pre>
 */
public final class BigtableTableAdminSettings {
  private final String projectId;
  private final String instanceId;
  private final BigtableTableAdminStubSettings stubSettings;

  private BigtableTableAdminSettings(Builder builder) throws IOException {
    this.projectId = Preconditions.checkNotNull(builder.projectId, "Project id must be set");
    this.instanceId = Preconditions.checkNotNull(builder.instanceId, "Instance id must be set");
    this.stubSettings =
        Verify.verifyNotNull(builder.stubSettings, "stubSettings should never be null").build();
  }

  /** Gets the project id of instance whose tables the client will manage. */
  public String getProjectId() {
    return projectId;
  }

  /** Gets the instance id whose tables the client will manage. */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * Gets the name of instance whose tables the client will manage.
   *
   * @deprecated Please use {@link #getProjectId()} and {@link #getInstanceId()}.
   */
  @Deprecated
  @Nonnull
  public com.google.bigtable.admin.v2.InstanceName getInstanceName() {
    return com.google.bigtable.admin.v2.InstanceName.of(projectId, instanceId);
  }

  /** Gets the underlying RPC settings. */
  public BigtableTableAdminStubSettings getStubSettings() {
    return stubSettings;
  }

  /** Returns a builder containing all the values of this settings class. */
  public Builder toBuilder() {
    return new Builder(this);
  }

  /** Returns a new builder for this class. */
  public static Builder newBuilder() {
    return new Builder();
  }

  /** Builder for BigtableTableAdminSettings. */
  public static final class Builder {
    @Nullable private String projectId;
    @Nullable private String instanceId;
    private final BigtableTableAdminStubSettings.Builder stubSettings;

    private Builder() {
      stubSettings = BigtableTableAdminStubSettings.newBuilder();
    }

    private Builder(BigtableTableAdminSettings settings) {
      this.projectId = settings.projectId;
      this.instanceId = settings.instanceId;
      this.stubSettings = settings.stubSettings.toBuilder();
    }

    /** Sets the project id of the instance whose tables the client will manage. */
    public Builder setProjectId(@Nullable String projectId) {
      Preconditions.checkNotNull(projectId);
      this.projectId = projectId;
      return this;
    }

    /** Gets the project id of the instance whose tables the client will manage. */
    @Nullable
    public String getProjectId() {
      return projectId;
    }

    /** Sets the instance id of the instance whose tables the client will manage. */
    public Builder setInstanceId(@Nullable String instanceId) {
      Preconditions.checkNotNull(instanceId);
      this.instanceId = instanceId;
      return this;
    }

    /** Gets the instance id of the instance whose tables the client will manage. */
    @Nullable
    public String getInstanceId() {
      return instanceId;
    }

    /**
     * Sets the name of instance whose tables the client will manage.
     *
     * @deprecated Please use {@link #setProjectId(String)} and {@link #setInstanceId(String)}.
     */
    @Deprecated
    public Builder setInstanceName(
        @Nonnull com.google.bigtable.admin.v2.InstanceName instanceName) {
      Preconditions.checkNotNull(instanceName);
      this.projectId = instanceName.getProject();
      this.instanceId = instanceName.getInstance();
      return this;
    }

    /**
     * Gets the name of instance whose tables the client will manage.
     *
     * @deprecated Please use {@link #getProjectId()} and {@link #getInstanceId()}.
     */
    @Deprecated
    @Nullable
    public com.google.bigtable.admin.v2.InstanceName getInstanceName() {
      if (projectId != null && instanceId != null) {
        return com.google.bigtable.admin.v2.InstanceName.of(projectId, instanceId);
      }
      return null;
    }

    /**
     * Returns the builder for the settings used for all RPCs.
     *
     * <p>This is meant for advanced usage. The default RPC settings are set to their recommended
     * values.
     */
    public BigtableTableAdminStubSettings.Builder stubSettings() {
      return stubSettings;
    }

    /** Builds an instance of the settings. */
    public BigtableTableAdminSettings build() throws IOException {
      return new BigtableTableAdminSettings(this);
    }
  }
}
