package com.ctrip.apollo.portal.api;

import com.google.common.base.Strings;

import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.core.dto.AppDTO;
import com.ctrip.apollo.core.dto.ClusterDTO;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.dto.ReleaseDTO;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceAPI {

  @Service
  public static class AppAPI extends API {
    public static String APP_API = "/apps";

    public AppDTO[] getApps(Apollo.Env env) {
      return restTemplate.getForObject(getAdminServiceHost(env) + APP_API, AppDTO[].class);
    }
  }

  @Service
  public static class ConfigAPI extends API {
    public static String CONFIG_RELEASE_API = "/configs/release/";

    public ReleaseDTO[] getConfigByReleaseId(Apollo.Env env, long releaseId) {
      if (releaseId <= 0) {
        return null;
      }

      return restTemplate.getForObject(getAdminServiceHost(env) + CONFIG_RELEASE_API + releaseId,
          ReleaseDTO[].class);
    }

    public ItemDTO[] getLatestConfigItemsByClusters(Apollo.Env env, List<Long> clusterIds) {
      if (clusterIds == null || clusterIds.size() == 0) {
        return null;
      }
      StringBuilder sb = new StringBuilder();
      for (long clusterId : clusterIds) {
        sb.append(clusterId).append(",");
      }

      return restTemplate.getForObject(getAdminServiceHost(env) + "/configs/latest?clusterIds="
          + sb.substring(0, sb.length() - 1), ItemDTO[].class);
    }
  }

  @Service
  public static class ClusterAPI extends API {

    public static String CLUSTER_APP_API = "/cluster/app/";

    public ClusterDTO[] getClustersByApp(Apollo.Env env, String appId) {
      if (Strings.isNullOrEmpty(appId)) {
        return null;
      }

      return restTemplate.getForObject(getAdminServiceHost(env) + CLUSTER_APP_API + appId,
          ClusterDTO[].class);
    }
  }

}
