package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.core.dto.CommitDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.service.CommitService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class CommitController {

  private final static int COMMIT_HISTORY_PAGE_SIZE = 10;

  @Autowired
  private CommitService commitService;

  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/commits")
  public List<CommitDTO> find(@PathVariable String appId, @PathVariable String env,
                              @PathVariable String clusterName, @PathVariable String namespaceName,
                              @RequestParam int page){

    if (page < 0){
      page = 0;
    }

    return commitService.find(appId, Env.valueOf(env), clusterName, namespaceName, page, COMMIT_HISTORY_PAGE_SIZE);

  }

}
