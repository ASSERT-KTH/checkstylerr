package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.App;
import com.ctrip.apollo.biz.entity.AppNamespace;
import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.entity.Namespace;
import com.ctrip.apollo.biz.repository.AppNamespaceRepository;
import com.ctrip.apollo.biz.repository.AppRepository;
import com.ctrip.apollo.biz.repository.ClusterRepository;
import com.ctrip.apollo.biz.repository.NamespaceRepository;

@Service
public class AdminService {

  @Autowired
  private AppRepository appRepository;

  @Autowired
  private AppNamespaceRepository appNamespaceRepository;

  @Autowired
  private NamespaceRepository namespaceRepository;

  @Autowired
  private ClusterRepository clusterRepository;

  @Autowired
  private CounterService counter;
  
  public App createNewApp(String appId, String appName, String ownerName, String ownerEmail,
      String namespace) {
    counter.increment("admin.createNewApp.start");
    App app = new App();
    app.setAppId(appId);
    app.setName(appName);
    app.setOwnerName(ownerName);
    app.setOwnerEmail(ownerEmail);
    appRepository.save(app);

    AppNamespace appNs = new AppNamespace();
    appNs.setAppId(appId);
    appNs.setName(namespace);
    appNamespaceRepository.save(appNs);

    Cluster cluster = new Cluster();
    cluster.setName("default");
    cluster.setAppId(appId);
    clusterRepository.save(cluster);

    Namespace ns = new Namespace();
    ns.setAppId(appId);
    ns.setClusterName(cluster.getName());
    ns.setNamespaceName(namespace);
    namespaceRepository.save(ns);
    counter.increment("admin.createNewApp.success");
    return app;
  }
}
