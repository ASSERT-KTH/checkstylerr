package com.ctrip.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.Namespace;
import com.ctrip.apollo.biz.service.NamespaceService;
import com.ctrip.apollo.biz.service.ViewService;
import com.ctrip.apollo.biz.utils.BeanUtils;
import com.ctrip.apollo.core.dto.NamespaceDTO;
import com.ctrip.apollo.core.exception.NotFoundException;

@RestController
public class NamespaceController {

  @Autowired
  private ViewService viewService;

  @Autowired
  private NamespaceService namespaceService;

  @RequestMapping(path = "/apps/{appId}/clusters/{clusterName}/namespaces", method = RequestMethod.POST)
  public ResponseEntity<NamespaceDTO> create(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName, @RequestBody NamespaceDTO dto) {
    Namespace entity = BeanUtils.transfrom(Namespace.class, dto);
    entity = namespaceService.save(entity);
    dto = BeanUtils.transfrom(NamespaceDTO.class, entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @RequestMapping(path = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}", method = RequestMethod.DELETE)
  public void delete(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName) {
    Namespace entity = namespaceService.findOne(appId, clusterName, namespaceName);
    if (entity == null)
      throw new NotFoundException("namespace not found for namespaceName " + namespaceName);
    namespaceService.delete(entity.getId());
  }

  @RequestMapping("/apps/{appId}/clusters/{clusterName}/namespaces")
  public List<NamespaceDTO> find(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName) {
    List<Namespace> groups = viewService.findNamespaces(appId, clusterName);
    return BeanUtils.batchTransform(NamespaceDTO.class, groups);
  }

  @RequestMapping("/namespaces/{namespaceId}")
  public NamespaceDTO get(@PathVariable("namespaceId") Long namespaceId) {
    Namespace namespace = namespaceService.findOne(namespaceId);
    return BeanUtils.transfrom(NamespaceDTO.class, namespace);
  }

  @RequestMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}")
  public NamespaceDTO get(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName) {
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    return BeanUtils.transfrom(NamespaceDTO.class, namespace);
  }

  @RequestMapping(path = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}", method = RequestMethod.PUT)
  public NamespaceDTO update(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName, @RequestBody NamespaceDTO dto) {
    if (!namespaceName.equals(dto.getNamespaceName())) {
      throw new IllegalArgumentException(
          String.format("Path variable %s is not equals to object field %s", namespaceName,
              dto.getNamespaceName()));
    }
    Namespace entity = namespaceService.findOne(appId, clusterName, namespaceName);
    if (entity == null)
      throw new NotFoundException("namespace not found for name " + namespaceName);
    entity = namespaceService.update(BeanUtils.transfrom(Namespace.class, dto));
    return BeanUtils.transfrom(NamespaceDTO.class, entity);
  }
}
