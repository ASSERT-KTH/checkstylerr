/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.client.impl.response;

import io.zeebe.client.api.JsonMapper;
import io.zeebe.client.api.response.ActivateJobsResponse;
import io.zeebe.client.api.response.ActivatedJob;
import java.util.ArrayList;
import java.util.List;

public final class ActivateJobsResponseImpl implements ActivateJobsResponse {

  private final JsonMapper jsonMapper;
  private final List<ActivatedJob> jobs = new ArrayList<>();

  public ActivateJobsResponseImpl(final JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  public void addResponse(
      final io.zeebe.gateway.protocol.GatewayOuterClass.ActivateJobsResponse activateJobsResponse) {
    activateJobsResponse.getJobsList().stream()
        .map(r -> new ActivatedJobImpl(jsonMapper, r))
        .forEach(jobs::add);
  }

  @Override
  public List<ActivatedJob> getJobs() {
    return jobs;
  }
}
