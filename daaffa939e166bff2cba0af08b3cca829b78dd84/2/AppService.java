package com.ctrip.apollo.portal.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.Apollo.Env;
import com.ctrip.apollo.portal.PortalSettings;
import com.ctrip.apollo.portal.entity.ClusterNavTree;


@Service
public class AppService {

  @Autowired
  private ClusterService clusterService;

  @Autowired
  private PortalSettings portalSettings;

  public ClusterNavTree buildClusterNavTree(String appId) {
    ClusterNavTree tree = new ClusterNavTree();

    List<Env> envs = portalSettings.getEnvs();
    for (Env env : envs) {
      ClusterNavTree.Node clusterNode = new ClusterNavTree.Node(env);
      clusterNode.setClusters(clusterService.findClusters(env, appId));
      tree.addNode(clusterNode);
    }
    // ClusterNavTree.Node uatNode = new ClusterNavTree.Node(Apollo.Env.UAT);
    // List<ClusterDTO> uatClusters = new LinkedList<>();
    // uatClusters.add(defaultCluster);
    // uatNode.setClusters(uatClusters);
    // tree.addNode(uatNode);

    return tree;
  }
}
