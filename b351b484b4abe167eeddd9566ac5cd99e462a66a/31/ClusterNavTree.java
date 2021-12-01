package com.ctrip.apollo.portal.entity;

import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.dto.ClusterDTO;

import java.util.LinkedList;
import java.util.List;

public class ClusterNavTree {

  private List<Node> nodes;

  public void addNode(Node node){
    if (nodes == null){
      nodes = new LinkedList<>();
    }

    nodes.add(node);
  }

    public static class Node{
      private Env env;
      private List<ClusterDTO> clusters;

      public Node(Env env){
        this.env = env;
      }

      public Env getEnv() {
        return env;
      }

      public void setEnv(Env env) {
        this.env = env;
      }

      public List<ClusterDTO> getClusters() {
        return clusters;
      }

      public void setClusters(List<ClusterDTO> clusters) {
        this.clusters = clusters;
      }
    }


  public List<Node> getNodes() {
    return nodes;
  }

  public void setNodes(List<Node> nodes) {
    this.nodes = nodes;
  }
}
