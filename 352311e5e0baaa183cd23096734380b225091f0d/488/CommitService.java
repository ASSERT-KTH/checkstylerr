/*
 * Copyright 2021 Apollo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.CommitDTO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommitService {


  private final AdminServiceAPI.CommitAPI commitAPI;

  public CommitService(final AdminServiceAPI.CommitAPI commitAPI) {
    this.commitAPI = commitAPI;
  }

  public List<CommitDTO> find(String appId, Env env, String clusterName, String namespaceName, int page, int size) {
    return commitAPI.find(appId, env, clusterName, namespaceName, page, size);
  }

}
