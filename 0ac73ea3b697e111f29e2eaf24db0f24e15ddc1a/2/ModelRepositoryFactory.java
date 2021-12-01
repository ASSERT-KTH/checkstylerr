/**
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.vorto.repository.core.impl;

import org.eclipse.vorto.model.ModelId;
import org.eclipse.vorto.repository.core.*;
import org.eclipse.vorto.repository.core.impl.cache.UserRolesRequestCache;
import org.eclipse.vorto.repository.core.impl.parser.ModelParserFactory;
import org.eclipse.vorto.repository.core.impl.utils.ModelSearchUtil;
import org.eclipse.vorto.repository.core.impl.validation.AttachmentValidator;
import org.eclipse.vorto.repository.diagnostics.ModeshapeDoctor;
import org.eclipse.vorto.repository.domain.IRole;
import org.eclipse.vorto.repository.repositories.UserRepository;
import org.eclipse.vorto.repository.services.*;
import org.eclipse.vorto.repository.services.exceptions.DoesNotExistException;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.RepositoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component("modelRepositoryFactory")
public class ModelRepositoryFactory implements IModelRepositoryFactory,
    ApplicationEventPublisherAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModelRepositoryFactory.class);

  @Autowired
  private ModelSearchUtil modelSearchUtil;

  @Autowired
  private AttachmentValidator attachmentValidator;

  @Autowired
  private ModelParserFactory modelParserFactory;

  @Autowired
  private RepositoryDiagnostics repoDiagnostics;

  @Autowired
  private RepositoryConfiguration repositoryConfiguration;

  @Autowired
  private RequestRepositorySessionHelper sessionHelper;

  @Autowired
  private NamespaceService namespaceService;

  @Autowired
  private UserNamespaceRoleService userNamespaceRoleService;

  @Autowired
  private UserRepositoryRoleService userRepositoryRoleService;

  @Autowired
  private PrivilegeService privilegeService;

  @Autowired
  private RoleService roleService;

  @Autowired
  private RoleUtil roleUtil;

  @Autowired
  private UserRepository userRepository;

  private ApplicationEventPublisher eventPublisher = null;

  private Repository repository;

  private static final ModeShapeEngine ENGINE = new ModeShapeEngine();

  private final Supplier<Collection<String>> allWorkspaceIdSupplier = () -> namespaceService
      .findAllWorkspaceIds();

  private final Supplier<Collection<String>> visibleWorkspaceIdSupplier = () -> namespaceService
      .findWorkspaceIdsOfPossibleReferences();

  public ModelRepositoryFactory() {
  }

  public ModelRepositoryFactory(
      ModelSearchUtil modelSearchUtil,
      AttachmentValidator attachmentValidator,
      ModelParserFactory modelParserFactory,
      RepositoryDiagnostics repoDiagnostics,
      RepositoryConfiguration repoConfig,
      RequestRepositorySessionHelper sessionHelper,
      NamespaceService namespaceService,
      UserNamespaceRoleService userNamespaceRoleService,
      PrivilegeService privilegeService,
      UserRepositoryRoleService userRepositoryRoleService,
      UserRepository userRepository
  ) {
    this.modelSearchUtil = modelSearchUtil;
    this.attachmentValidator = attachmentValidator;
    this.modelParserFactory = modelParserFactory;
    this.repoDiagnostics = repoDiagnostics;
    this.repositoryConfiguration = repoConfig;
    this.namespaceService = namespaceService;
    this.sessionHelper = sessionHelper;
    this.userNamespaceRoleService = userNamespaceRoleService;
    this.privilegeService = privilegeService;
    this.userRepositoryRoleService = userRepositoryRoleService;
    this.userRepository = userRepository;
  }

  @PostConstruct
  public void start() throws Exception {
    LOGGER.info("Starting Vorto Modeshape Repository -start-");
    ENGINE.start();
    repository = ENGINE.deploy(repositoryConfiguration);
    ENGINE.startRepository(repositoryConfiguration.getName()).get();
    LOGGER.info("Starting Vorto Modeshape Repository -finished-");
  }

  @PreDestroy
  public void stop() {
    try {
      ENGINE.shutdown().get(10, TimeUnit.SECONDS);
    } catch (Exception e) {
      LOGGER.error("Error while waiting for the ModeShape engine to shutdown", e);
    }
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.eventPublisher = applicationEventPublisher;
  }

  @Override
  public IModelRetrievalService getModelRetrievalService(Authentication user) {
    return new ModelRetrievalService(getMatchingWorkspaceIdSupplier(user.getName()),
        workspaceId -> getRepository(workspaceId, user));
  }

  @Override
  public IModelRetrievalService getModelRetrievalServiceWithoutSessionHelper(Authentication user) {
    return new ModelRetrievalService(getMatchingWorkspaceIdSupplier(user.getName()),
        workspaceId -> getRepositoryWithoutSessionHelper(workspaceId, user));
  }

  @Override
  public IModelRetrievalService getModelRetrievalService(IUserContext userContext) {
    return new ModelRetrievalService(getMatchingWorkspaceIdSupplier(userContext.getUsername()),
        workspaceId -> getRepository(workspaceId, userContext.getAuthentication()));
  }

  @Override
  public IModelRetrievalService getModelRetrievalService() {
    return new ModelRetrievalService(getMatchingWorkspaceIdSupplier(
        SecurityContextHolder.getContext().getAuthentication().getName()),
        workspaceId -> getRepository(workspaceId,
            SecurityContextHolder.getContext().getAuthentication()));
  }

  @Override
  public IDiagnostics getDiagnosticsService(String workspaceId, Authentication user) {
    Diagnostician diagnostics = new Diagnostician(repoDiagnostics);
    diagnostics
        .setRepositorySessionHelperSupplier(namedWorkspaceSessionSupplier(workspaceId, user));
    return diagnostics;
  }

  @Override
  public IRepositoryManager getRepositoryManager(String workspaceId, Authentication user) {
    RepositoryManager repoManager = new RepositoryManager();
    repoManager
        .setRepositorySessionHelperSupplier(namedWorkspaceSessionSupplier(workspaceId, user));
    repoManager.setDefaultSessionSupplier(defaultWorkspaceSessionSupplier(user));
    return repoManager;
  }

  @Override
  public IModeshapeDoctor getModeshapeDoctor(String workspaceId, Authentication user) {
    ModeshapeDoctor doctor = new ModeshapeDoctor();
    doctor.setRepositorySessionHelperSupplier(namedWorkspaceSessionSupplier(workspaceId, user));
    return doctor;
  }

  @Override
  public IModelPolicyManager getPolicyManager(String workspaceId, Authentication user) {
    ModelPolicyManager policyManager = new ModelPolicyManager(
        userNamespaceRoleService, roleUtil, this, roleService, namespaceService);
    policyManager
        .setRepositorySessionHelperSupplier(namedWorkspaceSessionSupplier(workspaceId, user));
    return policyManager;
  }

  @Override
  public IModelPolicyManager getPolicyManager(IUserContext userContext) {
    return getPolicyManager(userContext.getWorkspaceId(), userContext.getAuthentication());
  }

  @Override
  public IModelRepository getRepository(String workspaceId, Authentication user) {
    ModelRepository modelRepository = new ModelRepository(this.modelSearchUtil,
        this.attachmentValidator,
        this.modelParserFactory,
        getModelRetrievalService(user),
        this,
        getPolicyManager(workspaceId, user),
        namespaceService,
        privilegeService);

    modelRepository
        .setRepositorySessionHelperSupplier(namedWorkspaceSessionSupplier(workspaceId, user));
    modelRepository.setApplicationEventPublisher(eventPublisher);
    return modelRepository;
  }

  @Override
  public IModelRepository getRepository(IUserContext userContext) {
    return getRepository(userContext.getWorkspaceId(), userContext.getAuthentication());
  }

  @Override
  public IModelRepository getRepository(String workspaceId) {
    return getRepository(workspaceId, SecurityContextHolder.getContext().getAuthentication());
  }

  @Override
  public IModelRepository getRepositoryByNamespace(String namespace) {
    return getRepositoryByNamespace(namespace,
        SecurityContextHolder.getContext().getAuthentication());
  }

  @Override
  public IModelRepository getRepositoryByModel(ModelId modelId) {
    return getRepositoryByModel(modelId, SecurityContextHolder.getContext().getAuthentication());
  }

  @Override
  public IModelRepository getRepositoryByModelWithoutSessionHelper(ModelId modelId) {
    return getRepositoryByModelWithoutSessionHelper(modelId,
        SecurityContextHolder.getContext().getAuthentication());
  }

  @Override
  public IModelRepository getRepositoryByModel(ModelId modelId, IUserContext userContext) {
    return getRepositoryByModel(modelId, userContext.getAuthentication());
  }

  private Supplier<RequestRepositorySessionHelper> namedWorkspaceSessionSupplier(String workspaceId,
      Authentication user) {
    return () -> {
      if (sessionHelper == null) {
        sessionHelper = new RequestRepositorySessionHelper(false, privilegeService);
      }
      sessionHelper.setRepository(repository);
      sessionHelper.setWorkspaceId(workspaceId);
      sessionHelper.setUserRoles(getUserRoles(workspaceId, user.getName()));
      sessionHelper.setUser(user);
      return sessionHelper;
    };
  }

  private Supplier<Session> defaultWorkspaceSessionSupplier(Authentication user) {
    return () -> {
      try {
        return repository.login();
      } catch (LoginException e) {
        throw new UserLoginException(user.getName(), e);
      } catch (RepositoryException e) {
        throw new FatalModelRepositoryException(
            "Error while getting default repository for user [" + user.getName() + "]", e);
      }
    };
  }

  /**
   * This method accesses a request-scoped {@link UserRolesRequestCache} bean that maps composite
   * workspace+user IDs to roles. <br/>
   * This implies values for multiple calls with same workspace ID and username are cached within
   * the current request, and expire in the next one. <br/>
   * Not a perfect system as there are also numerous individual ID calls per request at times,
   * but better than nothing (i.e. at least caches the large amount of repeated calls within e.g. a
   * request to load a model).
   *
   * @param workspaceId
   * @param username
   * @return
   */
  private Collection<IRole> getUserRoles(String workspaceId, String username) {

    if (UserContext.isAnonymous(username)) {
      return new HashSet<>();
    }
    try {
      return userNamespaceRoleService.getRolesByWorkspaceIdAndUser(workspaceId, username);
    } catch (DoesNotExistException dnee) {
      LOGGER.debug("User or namespace not found. ", dnee);
      return Collections.emptySet();
    }
  }

  private IModelRepository getRepositoryByNamespace(String namespace, Authentication auth) {
    return namespaceService.resolveWorkspaceIdForNamespace(namespace)
        .map(workspaceId -> getRepository(workspaceId, auth))
        .orElse(null);
  }

  private IModelRepository getRepositoryByNamespaceWithoutSessionHelper(String namespace,
      Authentication auth) {
    return namespaceService.resolveWorkspaceIdForNamespace(namespace)
        .map(workspaceId -> getRepositoryWithoutSessionHelper(workspaceId, auth))
        .orElse(null);
  }

  private IModelRepository getRepositoryByModel(ModelId modelId, Authentication auth) {
    IModelRepository foundRepository = getRepositoryByNamespace(modelId.getNamespace(), auth);
    if (foundRepository == null) {
      throw new ModelNotFoundException(
          "Namespace " + modelId.getNamespace() + " does not exist in the system.");
    } else {
      return foundRepository;
    }
  }

  private IModelRepository getRepositoryByModelWithoutSessionHelper(ModelId modelId,
      Authentication auth) {
    IModelRepository foundRepository =
        getRepositoryByNamespaceWithoutSessionHelper(modelId.getNamespace(), auth);
    if (foundRepository == null) {
      throw new ModelNotFoundException(
          "Namespace " + modelId.getNamespace() + " does not exist in the system.");
    } else {
      return foundRepository;
    }
  }

  private Supplier<Collection<String>> getMatchingWorkspaceIdSupplier(String username) {
    if (!UserContext.isAnonymous(username) && userRepositoryRoleService.isSysadmin(username)) {
      return allWorkspaceIdSupplier;
    }
    return visibleWorkspaceIdSupplier;
  }

  protected IModelRepository getRepositoryWithoutSessionHelper(String workspaceId,
      Authentication user) {
    ModelRepository modelRepository = new ModelRepository(this.modelSearchUtil,
        this.attachmentValidator,
        this.modelParserFactory,
        getModelRetrievalServiceWithoutSessionHelper(user),
        this,
        getPolicyManager(workspaceId, user),
        namespaceService,
        privilegeService);

    modelRepository.setRepositorySessionHelperSupplier(() -> {
      RequestRepositorySessionHelper s =
          new RequestRepositorySessionHelper(false, privilegeService);
      s.setRepository(repository);
      s.setWorkspaceId(workspaceId);
      s.setUserRoles(getUserRoles(workspaceId, user.getName()));
      s.setUser(user);
      return s;
    });
    modelRepository.setApplicationEventPublisher(eventPublisher);
    return modelRepository;
  }
}
