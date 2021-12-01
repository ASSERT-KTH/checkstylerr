package com.ctrip.framework.apollo.adminservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.framework.apollo.biz.service.ItemSetService;
import com.ctrip.framework.apollo.core.dto.ItemChangeSets;

@RestController
public class ItemSetController {

  @Autowired
  private ItemSetService itemSetService;

  @RequestMapping(path = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/itemset", method = RequestMethod.POST)
  public ResponseEntity<Void> create(@RequestBody ItemChangeSets changeSet) {
    itemSetService.updateSet(changeSet);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
