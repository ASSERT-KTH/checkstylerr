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
package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Commit;
import com.ctrip.framework.apollo.biz.repository.CommitRepository;
import java.util.Date;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommitService {

  private final CommitRepository commitRepository;

  public CommitService(final CommitRepository commitRepository) {
    this.commitRepository = commitRepository;
  }

  @Transactional
  public Commit save(Commit commit){
    commit.setId(0);//protection
    return commitRepository.save(commit);
  }

  public List<Commit> find(String appId, String clusterName, String namespaceName, Pageable page){
    return commitRepository.findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(appId, clusterName, namespaceName, page);
  }

  public List<Commit> find(String appId, String clusterName, String namespaceName,
      Date lastModifiedTime, Pageable page) {
    return commitRepository
        .findByAppIdAndClusterNameAndNamespaceNameAndDataChangeLastModifiedTimeGreaterThanEqualOrderByIdDesc(
            appId, clusterName, namespaceName, lastModifiedTime, page);
  }

  @Transactional
  public int batchDelete(String appId, String clusterName, String namespaceName, String operator){
    return commitRepository.batchDelete(appId, clusterName, namespaceName, operator);
  }

}
