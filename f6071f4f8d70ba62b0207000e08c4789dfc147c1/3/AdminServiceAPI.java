package com.ctrip.framework.apollo.portal.api;


import com.ctrip.framework.apollo.core.dto.AppNamespaceDTO;
import com.ctrip.framework.apollo.core.dto.CommitDTO;
import com.ctrip.framework.apollo.core.dto.NamespaceLockDTO;
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
      NamespaceDTO
          dto =
          restTemplate.getForObject("{host}/apps/{appId}/clusters/{clusterName}/namespaces/" + namespaceName,
                                    NamespaceDTO.class, getAdminServiceHost(env), appId, clusterName);
      return dto;
    }


    public NamespaceDTO createNamespace(Env env, NamespaceDTO namespace) {
      return restTemplate
          .postForEntity("{host}/apps/{appId}/clusters/{clusterName}/namespaces", namespace, NamespaceDTO.class,
                         getAdminServiceHost(env), namespace.getAppId(), namespace.getClusterName()).getBody();
    }

    public AppNamespaceDTO createOrUpdateAppNamespace(Env env, AppNamespaceDTO appNamespace) {
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

    public void updateItemsByChangeSet(String appId, Env env, String clusterName, String namespace,
                                       ItemChangeSets changeSets) {
      restTemplate.postForEntity("{host}/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/itemset",
                                 changeSets, Void.class, getAdminServiceHost(env), appId, clusterName, namespace);
    }

    public void updateItem(String appId, Env env, String clusterName, String namespace, long itemId, ItemDTO item) {
      restTemplate.put("{host}/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/{itemId}",
                                        item, getAdminServiceHost(env), appId, clusterName, namespace, itemId);

    }

    public ItemDTO createItem(String appId, Env env, String clusterName, String namespace, ItemDTO item) {
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

    public ClusterDTO loadCluster(String appId, Env env, String clusterName) {
      return restTemplate.getForObject("{host}/apps/{appId}/clusters/{clusterName}", ClusterDTO.class,
                                       getAdminServiceHost(env), appId, clusterName);
    }

    public boolean isClusterUnique(String appId, Env env, String clusterName) {
      return restTemplate
          .getForObject("{host}/apps/{appId}/cluster/{clusterName}/unique", Boolean.class, getAdminServiceHost(env),
                        appId, clusterName);

    }

    public ClusterDTO createOrUpdate(Env env, ClusterDTO cluster) {
      return restTemplate.postForObject("{host}/apps/{appId}/clusters", cluster, ClusterDTO.class,
                                        getAdminServiceHost(env), cluster.getAppId());
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

  @Service
  public static class CommitAPI extends API {

    public List<CommitDTO> find(String appId, Env env, String clusterName, String namespaceName, int page, int size) {

      CommitDTO[]
          commitDTOs =
          restTemplate.getForObject(
              "{host}/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/commit?page={page}&size={size}",
              CommitDTO[].class,
              getAdminServiceHost(env), appId, clusterName, namespaceName, page, size);

      return Arrays.asList(commitDTOs);
    }
  }

  @Service
  public static class NamespaceLockAPI extends API {

    public NamespaceLockDTO getNamespaceLockOwner(String appId, Env env, String clusterName, String namespaceName) {
      return restTemplate.getForObject("{host}/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/lock",
                                       NamespaceLockDTO.class,
                                       getAdminServiceHost(env), appId, clusterName, namespaceName);

    }
  }

}
