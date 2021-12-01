package com.ctrip.apollo.portal.service;

import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.portal.entity.ClusterNavTree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AppService {

  @Autowired
  private ClusterService clusterService;

  public ClusterNavTree buildClusterNavTree(String appId){
    ClusterNavTree tree = new ClusterNavTree();

    ClusterNavTree.Node localNode = new ClusterNavTree.Node(Apollo.Env.LOCAL);
    localNode.setClusters(clusterService.findClusters(Apollo.Env.LOCAL, appId));
    tree.addNode(localNode);

//    ClusterNavTree.Node uatNode = new ClusterNavTree.Node(Apollo.Env.UAT);
//    List<ClusterDTO> uatClusters = new LinkedList<>();
//    uatClusters.add(defaultCluster);
//    uatNode.setClusters(uatClusters);
//    tree.addNode(uatNode);

    return tree;
  }
}
