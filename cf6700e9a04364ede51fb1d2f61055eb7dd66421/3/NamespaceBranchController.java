package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.GrayReleaseRule;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.message.MessageSender;
import com.ctrip.framework.apollo.biz.message.Topics;
import com.ctrip.framework.apollo.biz.service.NamespaceBranchService;
import com.ctrip.framework.apollo.biz.utils.ReleaseMessageKeyGenerator;
import com.ctrip.framework.apollo.common.constants.NamespaceBranchStatus;
import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.common.utils.GrayReleaseRuleItemTransformer;

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
  private MessageSender messageSender;
  @Autowired
  private NamespaceBranchService namespaceBranchService;


  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches", method = RequestMethod.POST)
  public NamespaceDTO createBranch(@PathVariable String appId,
                                   @PathVariable String clusterName,
                                   @PathVariable String namespaceName,
                                   @RequestParam("operator") String operator) {

    Namespace createdBranch = namespaceBranchService.createBranch(appId, clusterName, namespaceName, operator);
    return BeanUtils.transfrom(NamespaceDTO.class, createdBranch);
  }
  @RequestMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules")
  public GrayReleaseRuleDTO findBranchGrayRules(@PathVariable String appId,
                                                @PathVariable String clusterName,
                                                @PathVariable String namespaceName,
                                                @PathVariable String branchName) {
    GrayReleaseRule rules = namespaceBranchService.findBranchGrayRules(appId, clusterName, namespaceName, branchName);
    if (rules == null) {
      return null;
    }
    GrayReleaseRuleDTO ruleDTO =
        new GrayReleaseRuleDTO(rules.getAppId(), rules.getClusterName(), rules.getNamespaceName(),
                               rules.getBranchName());

    ruleDTO.setReleaseId(rules.getReleaseId());

    ruleDTO.setRuleItems(GrayReleaseRuleItemTransformer.batchTransformFromJSON(rules.getRules()));

    return ruleDTO;
  }

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/rules", method = RequestMethod.PUT)
  public void updateBranchGrayRules(@PathVariable String appId, @PathVariable String clusterName,
                                    @PathVariable String namespaceName, @PathVariable String branchName,
                                    @RequestBody GrayReleaseRuleDTO newRuleDto) {

    GrayReleaseRule newRules = BeanUtils.transfrom(GrayReleaseRule.class, newRuleDto);
    newRules.setRules(GrayReleaseRuleItemTransformer.batchTransformToJSON(newRuleDto.getRuleItems()));
    newRules.setBranchStatus(NamespaceBranchStatus.ACTIVE);

    namespaceBranchService.updateBranchGrayRules(appId, clusterName, namespaceName, branchName, newRules);

    messageSender.sendMessage(ReleaseMessageKeyGenerator.generate(appId, clusterName, namespaceName),
                              Topics.APOLLO_RELEASE_TOPIC);
  }

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}", method = RequestMethod.DELETE)
  public void deleteBranch(@PathVariable String appId, @PathVariable String clusterName,
                           @PathVariable String namespaceName, @PathVariable String branchName,
                           @RequestParam("operator") String operator) {

    namespaceBranchService.deleteBranch(appId, clusterName, namespaceName, branchName, NamespaceBranchStatus.DELETED, operator);

    messageSender.sendMessage(ReleaseMessageKeyGenerator.generate(appId, clusterName, namespaceName),
          Topics.APOLLO_RELEASE_TOPIC);

  }

  @RequestMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/branches")
  public NamespaceDTO loadNamespaceBranch(@PathVariable String appId, @PathVariable String clusterName,
                                          @PathVariable String namespaceName) {

    Namespace childNamespace = namespaceBranchService.findBranch(appId, clusterName, namespaceName);
    if (childNamespace == null) {
      return null;
    }

    return BeanUtils.transfrom(NamespaceDTO.class, childNamespace);
  }


}
