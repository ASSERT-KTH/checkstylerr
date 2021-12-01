package com.ctrip.framework.apollo.portal.api;


import com.ctrip.framework.apollo.core.dto.AppNamespaceDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.dto.AppDTO;
import com.ctrip.framework.apollo.core.dto.ClusterDTO;
import com.ctrip.framework.apollo.core.dto.ItemChangeSets;
import com.ctrip.framework.apollo.core.dto.ItemDTO;
import com.ctrip.framework.apollo.core.dto.NamespaceDTO;
import com.ctrip.framework.apollo.core.dto.ReleaseDTO;

import org.springframework.boot.actuate.health.Health;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;


@Service
public class AdminServiceAPI {

  @Service
  public static class HealthAPI extends API {

    public Health health(Env env) {
      return restTemplate.getForObject(getAdminServiceHost(env) + "/health", Health.class);
    }
  }

  @Service
  public static class AppAPI extends API {

    public List<AppDTO> findApps(Env env) {
      AppDTO[] appDTOs =
          restTemplate.getForObject("{host}/apps", AppDTO[].class, getAdminServiceHost(env));
      return Arrays.asList(appDTOs);
    }

    public AppDTO loadApp(Env env, String appId) {
      return restTemplate.getForObject("{host}/apps/{appId}", AppDTO.class, getAdminServiceHost(env), appId);
    }

    public AppDTO createApp(Env env, AppDTO app) {
      return restTemplate.postForEntity("{host}/apps", app, AppDTO.class, getAdminServiceHost(env))
          .getBody();
    }
  }


  @Service
  public static class NamespaceAPI extends API {

    public List<NamespaceDTO> findNamespaceByCluster(String appId, Env env, String clusterName) {
      NamespaceDTO[] namespaceDTOs = restTemplate.getForObject("{host}/apps/{appId}/clusters/{clusterName}/namespaces",
                                                               NamespaceDTO[].class, getAdminServiceHost(env), appId,
                                                               clusterName);
      return Arrays.asList(namespaceDTOs);
    }

    public NamespaceDTO loadNamespace(String appId, Env env, String clusterName,
                                      String namespaceName) {
      NamespaceDTO dto = restTemplate.getForObject("{host}/apps/{appId}/clusters/{clusterName}/namespaces/" + namespaceName,
                                       NamespaceDTO.class, getAdminServiceHost(env), appId, clusterName);
      return dto;
    }

    public List<AppNamespaceDTO> findPublicAppNamespaces(Env env) {
      AppNamespaceDTO[]
          appNamespaceDTOs =
          restTemplate.getForObject("{host}/appnamespaces/public", AppNamespaceDTO[].class
              , getAdminServiceHost(env));
      return Arrays.asList(appNamespaceDTOs);
    }

    public NamespaceDTO createNamespace(Env env, NamespaceDTO namespace) {
      return restTemplate
          .postForEntity("{host}/apps/{appId}/clusters/{clusterName}/namespaces", namespace, NamespaceDTO.class,
                         getAdminServiceHost(env), namespace.getAppId(), namespace.getClusterName()).getBody();
    }

    public AppNamespaceDTO createOrUpdate(Env env, AppNamespaceDTO appNamespace) {
      return restTemplate.postForEntity("{host}/apps/{appId}/appnamespaces", appNamespace, AppNamespaceDTO.class,
                                        getAdminServiceHost(env), appNamespace.getAppId()).getBody();
    }

  }

  @Service
  public static class ItemAPI extends API {

    public List<ItemDTO> findItems(String appId, Env env, String clusterName, String namespaceName) {
      ItemDTO[] itemDTOs =
          restTemplate
              .getForObject("{host}/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items",
                            ItemDTO[].class,
                            getAdminServiceHost(env), appId, clusterName, namespaceName);
      return Arrays.asList(itemDTOs);
    }

    public void updateItems(String appId, Env env, String clusterName, String namespace,
                            ItemChangeSets changeSets) {
      restTemplate.postForEntity("{host}/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/itemset",
                                 changeSets, Void.class, getAdminServiceHost(env), appId, clusterName, namespace);
    }

    public ItemDTO createOrUpdateItem(String appId, Env env, String clusterName, String namespace, ItemDTO item) {
      return restTemplate.postForEntity("{host}/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items",
                                        item, ItemDTO.class, getAdminServiceHost(env), appId, clusterName, namespace)
          .getBody();
    }

    public void deleteItem(Env env, long itemId, String operator) {

      restTemplate.delete("{host}/items/{itemId}?operator={operator}", getAdminServiceHost(env), itemId, operator);
    }
  }

  @Service
  public static class ClusterAPI extends API {

    public List<ClusterDTO> findClustersByApp(String appId, Env env) {
      ClusterDTO[] clusterDTOs = restTemplate.getForObject("{host}/apps/{appId}/clusters", ClusterDTO[].class,
                                                           getAdminServiceHost(env), appId);
      return Arrays.asList(clusterDTOs);
    }
  }

  @Service
  public static class ReleaseAPI extends API {

    public ReleaseDTO loadLatestRelease(String appId, Env env, String clusterName,
                                        String namespace) {
      ReleaseDTO
          releaseDTO =
          restTemplate
              .getForObject("{host}/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/latest",
                            ReleaseDTO.class, getAdminServiceHost(env), appId, clusterName, namespace);
      return releaseDTO;
    }

    public ReleaseDTO release(String appId, Env env, String clusterName, String namespace,
                              String releaseBy, String comment, String operator) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
      parameters.add("name", releaseBy);
      parameters.add("comment", comment);
      parameters.add("operator", operator);
      HttpEntity<MultiValueMap<String, String>> entity =
          new HttpEntity<MultiValueMap<String, String>>(parameters, headers);
      ResponseEntity<ReleaseDTO> response =
          restTemplate
              .postForEntity("{host}/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases", entity,
                             ReleaseDTO.class,
                             getAdminServiceHost(env), appId, clusterName, namespace);
      return response.getBody();
    }
  }

}
