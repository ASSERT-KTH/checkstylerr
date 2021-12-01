package com.ctrip.apollo.portal.api;


import com.ctrip.apollo.core.dto.AppNamespaceDTO;
import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.dto.AppDTO;
import com.ctrip.apollo.core.dto.ClusterDTO;
import com.ctrip.apollo.core.dto.ItemChangeSets;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.dto.NamespaceDTO;
import com.ctrip.apollo.core.dto.ReleaseDTO;

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
  public static class HealthAPI extends API{

    public Health health(Env env){
      return restTemplate.getForObject(getAdminServiceHost(env) + "/health", Health.class);
    }
  }

  @Service
  public static class AppAPI extends API {

    public static String APP_API = "/apps";

    public List<AppDTO> findApps(Env env) {
      AppDTO[] appDTOs =
          restTemplate.getForObject(getAdminServiceHost(env) + APP_API, AppDTO[].class);
      return Arrays.asList(appDTOs);
    }

    public AppDTO loadApp(Env env, String appId){
      return restTemplate.getForObject(getAdminServiceHost(env) + APP_API + "/" + appId, AppDTO.class);
    }

    public AppDTO createApp(Env env, AppDTO app) {
      return restTemplate.postForEntity(getAdminServiceHost(env) + APP_API, app, AppDTO.class)
          .getBody();
    }
  }


  @Service
  public static class NamespaceAPI extends API {

    public List<NamespaceDTO> findNamespaceByCluster(String appId, Env env, String clusterName) {
      NamespaceDTO[] namespaceDTOs = restTemplate.getForObject(
          getAdminServiceHost(env)
              + String.format("apps/%s/clusters/%s/namespaces", appId, clusterName),
          NamespaceDTO[].class);
      return Arrays.asList(namespaceDTOs);
    }

    public NamespaceDTO loadNamespace(String appId, Env env, String clusterName,
        String namespaceName) {
      return restTemplate.getForObject(getAdminServiceHost(env)
          + String.format("apps/%s/clusters/%s/namespaces/%s", appId, clusterName, namespaceName),
          NamespaceDTO.class);
    }

    public List<AppNamespaceDTO> findPublicAppNamespaces(Env env){
      AppNamespaceDTO[] appNamespaceDTOs = restTemplate.getForObject(
          getAdminServiceHost(env)+ "appnamespaces/public",
          AppNamespaceDTO[].class);
      return Arrays.asList(appNamespaceDTOs);
    }

    public NamespaceDTO createNamespace(Env env, NamespaceDTO namespace) {
      return restTemplate.postForEntity(getAdminServiceHost(env) +
                                        String.format("/apps/%s/clusters/%s/namespaces", namespace.getAppId(),
                                                      namespace.getClusterName()), namespace, NamespaceDTO.class)
          .getBody();
    }

    public AppNamespaceDTO createAppNamespace(Env env, AppNamespaceDTO appNamespace) {
      return restTemplate.postForEntity(getAdminServiceHost(env) +
                                        String.format("/apps/%s/appnamespaces", appNamespace.getAppId()), appNamespace, AppNamespaceDTO.class)
          .getBody();
    }

  }

  @Service
  public static class ItemAPI extends API {

    public List<ItemDTO> findItems(String appId, Env env, String clusterName, String namespace) {
      ItemDTO[] itemDTOs =
          restTemplate
              .getForObject(
                  getAdminServiceHost(env) + String.format(
                      "apps/%s/clusters/%s/namespaces/%s/items", appId, clusterName, namespace),
                  ItemDTO[].class);
      return Arrays.asList(itemDTOs);
    }

    public void updateItems(String appId, Env env, String clusterName, String namespace,
        ItemChangeSets changeSets) {
      restTemplate.postForEntity(getAdminServiceHost(env) + String
          .format("apps/%s/clusters/%s/namespaces/%s/itemset", appId, clusterName, namespace),
          changeSets, Void.class);
    }
  }

  @Service
  public static class ClusterAPI extends API {

    public List<ClusterDTO> findClustersByApp(String appId, Env env) {
      ClusterDTO[] clusterDTOs = restTemplate.getForObject(
          getAdminServiceHost(env) + String.format("apps/%s/clusters", appId), ClusterDTO[].class);
      return Arrays.asList(clusterDTOs);
    }
  }

  @Service
  public static class ReleaseAPI extends API {

    public ReleaseDTO loadLatestRelease(String appId, Env env, String clusterName,
        String namespace) {
      ReleaseDTO releaseDTO = restTemplate.getForObject(
          getAdminServiceHost(env) + String.format(
              "apps/%s/clusters/%s/namespaces/%s/releases/latest", appId, clusterName, namespace),
          ReleaseDTO.class);
      return releaseDTO;
    }

    public ReleaseDTO release(String appId, Env env, String clusterName, String namespace,
        String releaseBy, String comment) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
      parameters.add("name", releaseBy);
      parameters.add("comment", comment);
      HttpEntity<MultiValueMap<String, String>> entity =
          new HttpEntity<MultiValueMap<String, String>>(parameters, headers);
      ResponseEntity<ReleaseDTO> response =
          restTemplate
              .postForEntity(
                  getAdminServiceHost(env) + String.format(
                      "apps/%s/clusters/%s/namespaces/%s/releases", appId, clusterName, namespace),
                  entity, ReleaseDTO.class);
      return response.getBody();
    }
  }
}
