package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.apollo.biz.entity.App;
import com.ctrip.apollo.biz.entity.AppNamespace;
import com.ctrip.apollo.biz.entity.Audit;
import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.entity.Namespace;
import com.ctrip.apollo.biz.repository.AppNamespaceRepository;
import com.ctrip.apollo.biz.repository.AppRepository;
import com.ctrip.apollo.biz.repository.ClusterRepository;
import com.ctrip.apollo.biz.repository.NamespaceRepository;
import com.ctrip.apollo.core.ConfigConsts;

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
  private AuditService auditService;

  @Transactional
  public App createNewApp(App app) {
    String createBy = app.getDataChangeCreatedBy();
    App createdApp = appRepository.save(app);

    auditService.audit(App.class.getSimpleName(), createdApp.getId(), Audit.OP.INSERT, createBy);

    String appId = createdApp.getAppId();

    createDefaultAppNamespace(appId, createBy);

    createDefaultCluster(appId, createBy);

    createDefaultNamespace(appId, createBy);

    return app;
  }

  private void createDefaultAppNamespace(String appId, String createBy) {
    AppNamespace appNs = new AppNamespace();
    appNs.setAppId(appId);
    appNs.setName(appId);
    appNs.setComment("default app namespace");
    appNs.setDataChangeCreatedBy(createBy);
    appNs.setDataChangeLastModifiedBy(createBy);
    appNamespaceRepository.save(appNs);

    auditService.audit(AppNamespace.class.getSimpleName(), appNs.getId(), Audit.OP.INSERT,
        createBy);
  }

  private void createDefaultCluster(String appId, String createBy) {
    Cluster cluster = new Cluster();
    cluster.setName(ConfigConsts.CLUSTER_NAME_DEFAULT);
    cluster.setAppId(appId);
    cluster.setDataChangeCreatedBy(createBy);
    cluster.setDataChangeLastModifiedBy(createBy);
    clusterRepository.save(cluster);

    auditService.audit(Cluster.class.getSimpleName(), cluster.getId(), Audit.OP.INSERT, createBy);
  }

  private void createDefaultNamespace(String appId, String createBy) {
    Namespace ns = new Namespace();
    ns.setAppId(appId);
    ns.setClusterName(ConfigConsts.CLUSTER_NAME_DEFAULT);
    ns.setNamespaceName(appId);
    ns.setDataChangeCreatedBy(createBy);
    ns.setDataChangeLastModifiedBy(createBy);
    namespaceRepository.save(ns);

    auditService.audit(Namespace.class.getSimpleName(), ns.getId(), Audit.OP.INSERT, createBy);
  }
}
