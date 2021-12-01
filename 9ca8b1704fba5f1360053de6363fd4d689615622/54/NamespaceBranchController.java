package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.constants.NamespaceBranchStatus;
import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceReleaseModel;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceVO;
import com.ctrip.framework.apollo.portal.service.NamespaceBranchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NamespaceBranchController {

  @Autowired
  private NamespaceBranchService namespaceBranchService;

  @RequestMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/branches")
  public NamespaceVO findBranch(@PathVariable String appId,
                                @PathVariable String env,
                                @PathVariable String clusterName,
                                @PathVariable String namespaceName) {
    return namespaceBranchService.findBranch(appId, Env.valueOf(env), clusterName, namespaceName);
  }

  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/branches", method = RequestMethod.POST)
  public NamespaceDTO createBranch(@PathVariable String appId,
                                   @PathVariable String env,
                                   @PathVariable String clusterName,
                                   @PathVariable String namespaceName) {

    return namespaceBranchService.createBranch(appId, Env.valueOf(env), clusterName, namespaceName);
  }

  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}", method = RequestMethod.DELETE)
  public void deleteBranch(@PathVariable String appId,
                           @PathVariable String env,
                           @PathVariable String clusterName,
                           @PathVariable String namespaceName,
                           @PathVariable String branchName) {
    namespaceBranchService.deleteBranch(appId, Env.valueOf(env), clusterName, namespaceName, branchName);

  }


  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/merge", method = RequestMethod.POST)
  public ReleaseDTO merge(@PathVariable String appId, @PathVariable String env,
                          @PathVariable String clusterName, @PathVariable String namespaceName,
                          @PathVariable String branchName, @RequestParam(value = "deleteBranch", defaultValue = "true") boolean deleteBranch,
                          @RequestBody NamespaceReleaseModel model) {

    ReleaseDTO createdRelease = namespaceBranchService.merge(appId, Env.valueOf(env), clusterName, namespaceName, branchName,
                                                             model.getReleaseTitle(), model.getReleaseComment(), deleteBranch);

    return createdRelease;
  }


  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules", method = RequestMethod.GET)
  public GrayReleaseRuleDTO getBranchGrayRules(@PathVariable String appId, @PathVariable String env,
                                                     @PathVariable String clusterName,
                                                     @PathVariable String namespaceName,
                                                     @PathVariable String branchName) {

    return namespaceBranchService.findBranchGrayRules(appId, Env.valueOf(env), clusterName, namespaceName, branchName);
  }

  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules", method = RequestMethod.PUT)
  public void updateBranchRules(@PathVariable String appId, @PathVariable String env,
                                @PathVariable String clusterName, @PathVariable String namespaceName,
                                @PathVariable String branchName, @RequestBody GrayReleaseRuleDTO rules) {

    namespaceBranchService
        .updateBranchGrayRules(appId, Env.valueOf(env), clusterName, namespaceName, branchName, rules);

  }

}
