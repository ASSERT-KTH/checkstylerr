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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.vorto.core.api.model.model.Model;
import org.eclipse.vorto.model.ModelId;
import org.eclipse.vorto.model.ModelType;
import org.eclipse.vorto.model.refactor.ChangeSet;
import org.eclipse.vorto.model.refactor.RefactoringTask;
import org.eclipse.vorto.plugin.generator.adapter.ObjectMapperFactory;
import org.eclipse.vorto.repository.core.*;
import org.eclipse.vorto.repository.core.events.AppEvent;
import org.eclipse.vorto.repository.core.events.EventType;
import org.eclipse.vorto.repository.core.impl.parser.IModelParser;
import org.eclipse.vorto.repository.core.impl.parser.ModelParserFactory;
import org.eclipse.vorto.repository.core.impl.utils.DependencyManager;
import org.eclipse.vorto.repository.core.impl.utils.ModelIdHelper;
import org.eclipse.vorto.repository.core.impl.utils.ModelReferencesHelper;
import org.eclipse.vorto.repository.core.impl.utils.ModelSearchUtil;
import org.eclipse.vorto.repository.core.impl.validation.AttachmentValidator;
import org.eclipse.vorto.repository.core.impl.validation.ValidationException;
import org.eclipse.vorto.repository.domain.Namespace;
import org.eclipse.vorto.repository.services.NamespaceService;
import org.eclipse.vorto.repository.services.PrivilegeService;
import org.eclipse.vorto.repository.tenant.NewNamespacesNotSupersetException;
import org.eclipse.vorto.repository.utils.ModelUtils;
import org.eclipse.vorto.repository.web.api.v1.dto.ModelLink;
import org.eclipse.vorto.repository.web.core.exceptions.NotAuthorizedException;
import org.eclipse.vorto.repository.workflow.ModelState;
import org.eclipse.vorto.utilities.reader.IModelWorkspace;
import org.eclipse.vorto.utilities.reader.ModelWorkspaceReader;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import javax.jcr.*;
import javax.jcr.query.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.eclipse.vorto.repository.core.Attachment.*;

public class ModelRepository extends AbstractRepositoryOperation
    implements IModelRepository, ApplicationEventPublisherAware {

  public static final String VORTO_VISIBILITY = "vorto:visibility";

  public static final String VORTO_TAGS = "vorto:tags";

  public static final String VORTO_REFERENCES = "vorto:references";

  public static final String VORTO_META = "vorto:meta";

  public static final String VORTO_AUTHOR = "vorto:author";

  public static final String VORTO_TARGETPLATFORM = "vorto:targetplatform";

  public static final String VORTO_STATE = "vorto:state";

  public static final String VORTO_DISPLAYNAME = "vorto:displayname";

  public static final String VORTO_LINKS = "vorto:links";

  public static final String VORTO_NODE_TYPE = "vorto:type";

  public static final String VORTO_DESCRIPTION = "vorto:description";

  public static final String JCR_LAST_MODIFIED_BY = "jcr:lastModifiedBy";

  public static final String JCR_LAST_MODIFIED = "jcr:lastModified";

  public static final String JCR_CREATED = "jcr:created";

  public static final String JCR_DATA = "jcr:data";

  public static final String JCR_CONTENT = "jcr:content";

  public static final String MIX_LAST_MODIFIED = "mix:lastModified";

  public static final String MIX_REFERENCEABLE = "mix:referenceable";

  public static final String NT_FOLDER = "nt:folder";

  public static final String NT_FILE = "nt:file";

  public static final String NT_RESOURCE = "nt:resource";

  public static final String MODE_ACCESS_CONTROLLABLE = "mode:accessControllable";

  public static final String ATTACHMENTS_NODE = "attachments";

  public static final String LINKS_NODE = "links";

  private static final Function<ModelInfo, String> VERSION_COMPARATOR = m -> m.getId().getVersion();

  private static final Logger LOGGER = Logger.getLogger(ModelRepository.class);

  private IModelRetrievalService modelRetrievalService;

  private ModelSearchUtil modelSearchUtil;

  private AttachmentValidator attachmentValidator;

  private ModelParserFactory modelParserFactory;

  private ApplicationEventPublisher eventPublisher = null;

  private ModelRepositoryFactory repositoryFactory;

  private NamespaceService namespaceService;

  private IModelPolicyManager policyManager;

  private PrivilegeService privilegeService;


  public ModelRepository(ModelSearchUtil modelSearchUtil, AttachmentValidator attachmentValidator,
      ModelParserFactory modelParserFactory, IModelRetrievalService modelRetrievalService,
      ModelRepositoryFactory repositoryFactory, IModelPolicyManager policyManager,
      NamespaceService namespaceService, PrivilegeService privilegeService) {
    this.modelSearchUtil = modelSearchUtil;
    this.attachmentValidator = attachmentValidator;
    this.modelParserFactory = modelParserFactory;
    this.modelRetrievalService = modelRetrievalService;
    this.repositoryFactory = repositoryFactory;
    this.namespaceService = namespaceService;
    this.policyManager = policyManager;
    this.privilegeService = privilegeService;
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.eventPublisher = applicationEventPublisher;
  }

  @Override
  public List<ModelInfo> search(final String expression) {
    return doInSession(session -> {
      String queryExpression = Optional.ofNullable(expression).orElse("");

      List<ModelInfo> modelResources = new ArrayList<>();
      Query query = ModelSearchUtil.createQueryFromExpression(session, queryExpression);

      LOGGER.debug("Searching repository with expression " + query.getStatement());
      QueryResult result = query.execute();
      RowIterator rowIterator = result.getRows();
      while (rowIterator.hasNext()) {
        Row row = rowIterator.nextRow();
        Node currentNode = row.getNode();
        if (currentNode.hasProperty(VORTO_NODE_TYPE)) {
          try {
            modelResources.add(createMinimalModelInfo(currentNode));
          } catch (Exception ex) {
            LOGGER.debug("Error while converting node to a ModelInfo", ex);
          }
        }
      }

      return modelResources;
    });
  }

  private ModelInfo createMinimalModelInfo(Node fileNode, boolean doInElevatedSession, IUserContext context)
      throws RepositoryException {
    Node folderNode = fileNode.getParent();
    ModelInfo resource = new ModelInfo(ModelIdHelper.fromPath(folderNode.getPath()),
        fileNode.getProperty(VORTO_NODE_TYPE).getString());
    resource.setDescription(fileNode.getProperty(VORTO_DESCRIPTION).getString());
    resource.setDisplayName(fileNode.getProperty(VORTO_DISPLAYNAME).getString());
    resource.setCreationDate(fileNode.getProperty(JCR_CREATED).getDate().getTime());
    if (fileNode.hasProperty(JCR_LAST_MODIFIED)) {
      resource.setModificationDate(fileNode.getProperty(JCR_LAST_MODIFIED).getDate().getTime());
    }
    if (fileNode.hasProperty(JCR_LAST_MODIFIED_BY)) {
      resource.setLastModifiedBy(fileNode.getProperty(JCR_LAST_MODIFIED_BY).getString());
    }
    if (fileNode.hasProperty(VORTO_STATE)) {
      resource.setState(fileNode.getProperty(VORTO_STATE).getString());
    }
    if (fileNode.hasProperty(VORTO_AUTHOR)) {
      resource.setAuthor(fileNode.getProperty(VORTO_AUTHOR).getString());
    }
    if (fileNode.hasProperty(VORTO_TARGETPLATFORM)) {
      resource.setTargetPlatformKey(fileNode.getProperty(VORTO_TARGETPLATFORM).getString());
    }
    if (fileNode.hasProperty(VORTO_VISIBILITY)) {
      resource.setVisibility(fileNode.getProperty(VORTO_VISIBILITY).getString());
    } else {
      resource.setVisibility(VISIBILITY_PRIVATE);
    }

    setReferencesOnResource(folderNode, resource);

    if (resource.getType() == ModelType.InformationModel) {
      resource
          .setHasImage(!this.getAttachmentsByTag(resource.getId(), TAG_IMAGE, doInElevatedSession, context)
              .isEmpty());
    }

    return resource;
  }

  private ModelInfo createMinimalModelInfo(Node fileNode) throws RepositoryException {
    return createMinimalModelInfo(fileNode, false, null);
  }

  @Override
  public ModelFileContent getModelContent(ModelId modelId, boolean validate) {
    return doInSession(session -> {
      try {
        ModelIdHelper modelIdHelper = new ModelIdHelper(modelId);
        Node folderNode = session.getNode(modelIdHelper.getFullPath());
        Node fileNode = (Node) folderNode.getNodes(FILE_NODES).next();
        Node fileItem = (Node) fileNode.getPrimaryItem();
        InputStream is = fileItem.getProperty(JCR_DATA).getBinary().getStream();

        final String fileContent = IOUtils.toString(is);

        IModelParser parser = modelParserFactory.getParser(fileNode.getName());

        ModelResource resource = (ModelResource) parser.parse(IOUtils.toInputStream(fileContent));
        return new ModelFileContent(resource.getModel(), fileNode.getName(),
            fileContent.getBytes());
      } catch (IOException e) {
        throw new FatalModelRepositoryException("Something went wrong accessing the repository", e);
      }
    });
  }

  private Node createNodeForModelId(Session session, ModelId id) throws RepositoryException {
    ModelIdHelper modelIdHelper = new ModelIdHelper(id);
    StringBuilder pathBuilder = new StringBuilder();
    Iterator<String> modelIdIterator = modelIdHelper.iterator();
    Node rootNode = session.getRootNode();
    while (modelIdIterator.hasNext()) {
      String nextPathFragment = modelIdIterator.next();
      pathBuilder.append(nextPathFragment).append("/");
      try {
        rootNode.getNode(pathBuilder.toString());
      } catch (PathNotFoundException pathNotFound) {
        Node addedNode = rootNode.addNode(pathBuilder.toString(), NT_FOLDER);
        addedNode.setPrimaryType(NT_FOLDER);
      }
    }

    return rootNode.getNode(modelIdHelper.getFullPath().substring(1));
  }

  @Override
  public ModelInfo save(ModelId modelId, byte[] content, String fileName, IUserContext userContext,
      boolean validate) {
    Objects.requireNonNull(content);
    Objects.requireNonNull(modelId);

    IModelParser parser =
        modelParserFactory.getParser("model" + ModelType.fromFileName(fileName).getExtension());
    if (validate) {
      parser.enableValidation();
    }

    ModelResource modelInfo = (ModelResource) parser.parse(new ByteArrayInputStream(content));

    save(modelInfo, userContext);

    return modelInfo;
  }

  @Override
  public ModelInfo save(ModelId modelId, byte[] content, String fileName,
      IUserContext userContext) {
    return save(modelId, content, fileName, userContext, true);
  }

  @Override
  public ModelInfo save(final ModelResource modelInfo, IUserContext userContext) {

    return doInSession(jcrSession -> {
      org.modeshape.jcr.api.Session session = (org.modeshape.jcr.api.Session) jcrSession;

      LOGGER.info("Saving " + modelInfo.toString() + " as " + modelInfo.getFileName()
          + " in Workspace: " + session.getWorkspace().getName());

      try {
        Node folderNode = createNodeForModelId(session, modelInfo.getId());
        folderNode.addMixin(MIX_REFERENCEABLE);
        folderNode.addMixin(VORTO_META);
        folderNode.addMixin(MIX_LAST_MODIFIED);

        NodeIterator nodeIt = folderNode.getNodes(FILE_NODES);
        if (!nodeIt.hasNext()) { // new node
          Node fileNode = folderNode.addNode(modelInfo.getFileName(), NT_FILE);
          fileNode.addMixin(VORTO_META);
          fileNode.setProperty(VORTO_AUTHOR, userContext.getUsername());
          fileNode.setProperty(VORTO_VISIBILITY, VISIBILITY_PRIVATE);
          fileNode.addMixin(MODE_ACCESS_CONTROLLABLE);
          fileNode.addMixin(MIX_LAST_MODIFIED);
          Node contentNode = fileNode.addNode(JCR_CONTENT, NT_RESOURCE);
          Binary binary =
              session.getValueFactory().createBinary(new ByteArrayInputStream(modelInfo.toDSL()));
          Property input = contentNode.setProperty(JCR_DATA, binary);
          boolean success = session.sequence("Vorto Sequencer", input, fileNode);
          if (!success) {
            throw new FatalModelRepositoryException(
                "Problem indexing new node for search" + modelInfo.getId(), null);
          }
        } else { // node already exists, so just update it.
          Node fileNode = nodeIt.nextNode();
          fileNode.addMixin(VORTO_META);
          fileNode.addMixin(MIX_LAST_MODIFIED);
          Node contentNode = fileNode.getNode(JCR_CONTENT);
          Binary binary =
              session.getValueFactory().createBinary(new ByteArrayInputStream(modelInfo.toDSL()));
          Property input = contentNode.setProperty(JCR_DATA, binary);
          boolean success = session.sequence("Vorto Sequencer", input, fileNode);
          if (!success) {
            throw new FatalModelRepositoryException(
                "Problem indexing new node for search" + modelInfo.getId(), null);
          }
        }

        session.save();
        LOGGER.info("Model was saved successfully");

        ModelInfo createdModel = getById(modelInfo.getId());

        eventPublisher
            .publishEvent(new AppEvent(this, createdModel, userContext, EventType.MODEL_CREATED));

        return createdModel;
      } catch (Exception e) {
        LOGGER.error("Error checking in model", e);
        throw new FatalModelRepositoryException("Problem saving model " + modelInfo.getId(), e);
      }
    });
  }

  @Override
  public ModelInfo getById(ModelId modelId) {
    final ModelId finalModelId = getLatestModelVersionIfLatestTagIsSet(modelId);
    return doInSession(session -> {
      try {
        ModelIdHelper modelIdHelper = new ModelIdHelper(finalModelId);
        Node folderNode = session.getNode(modelIdHelper.getFullPath());
        return getModelResource(finalModelId, folderNode);
      } catch (PathNotFoundException e) {
        return null;
      } catch (AccessDeniedException e) {
        throw new NotAuthorizedException(finalModelId, e);
      }
    });
  }

  @Override
  public ModelId getLatestModelVersionIfLatestTagIsSet(ModelId modelId) {
    if (!"latest".equalsIgnoreCase(modelId.getVersion())) {
      return modelId;
    }
    return getModelVersions(modelId).stream()
        .filter(m -> ModelState.Released.getName().equals(m.getState()))
        .max(Comparator.comparing(VERSION_COMPARATOR))
        .map(ModelInfo::getId)
        .orElse(null);
  }

  private List<ModelInfo> getModelVersions(ModelId modelId) {
    return doInSession(session -> {
      modelId.setVersion("");
      ModelIdHelper modelIdHelper = new ModelIdHelper(modelId);
      try {
        Node folderNode = session.getNode(modelIdHelper.getFullPath());
        List<ModelInfo> models = new ArrayList<>();
        NodeIterator nodeIterator = folderNode.getNodes();
        while (nodeIterator.hasNext()) {
          Node node = nodeIterator.nextNode();
          models.add(getModelResource(modelId, node));
        }
        return models;
      } catch (PathNotFoundException e) {
        throw new ModelNotFoundException(
            String.format("Model [%s] does not exist", modelId.getPrettyFormat()), null);
      } catch (AccessDeniedException e) {
        throw new NotAuthorizedException(modelId, e);
      }
    });
  }

  private ModelInfo getModelResource(ModelId modelId, Node folderNode) throws RepositoryException {
    if (!folderNode.getNodes(FILE_NODES).hasNext()) {
      throw new NotAuthorizedException(modelId, null);
    }

    ModelInfo modelResource = createModelResource(folderNode);

    if (!getAttachmentsByTag(modelId, TAG_IMAGE).isEmpty()) {
      modelResource.setHasImage(true);
    }

    if (!getAttachmentsByTag(modelId, TAG_IMPORTED).isEmpty()) {
      modelResource.setImported(true);
    }

    return modelResource;
  }

  @Override
  public ModelInfo getByIdWithPlatformMappings(ModelId modelId) throws NotAuthorizedException {
    ModelInfo model = this.getById(modelId);

    if (model != null && model.getType() != ModelType.Mapping) {
      Map<String, List<ModelInfo>> referencingModels =
          modelRetrievalService.getModelsReferencing(model.getId());

      for (Map.Entry<String, List<ModelInfo>> entry : referencingModels.entrySet()) {
        for (ModelInfo modelInfo : entry.getValue()) {
          if (modelInfo.getType() == ModelType.Mapping) {
            try {
              model.addPlatformMapping(modelInfo.getTargetPlatformKey(), modelInfo.getId());
            } catch (ValidationException e) {
              LOGGER.warn("Stored Vorto Model is corrupt: " + modelInfo.getId().getPrettyFormat(),
                  e);
            } catch (Exception e) {
              LOGGER.warn("Error while getting a platform mapping", e);
            }
          }
        }
      }

    }

    return model;
  }

  public ModelInfo getBasicInfoInElevatedSession(ModelId modelId, IUserContext context) {
    return doInElevatedSession(
        session -> {
          try {
            ModelIdHelper modelIdHelper = new ModelIdHelper(modelId);

            Node folderNode = session.getNode(modelIdHelper.getFullPath());

            Node modelFileNode = folderNode.getNodes(FILE_NODES).nextNode();
            ModelInfo modelInfo = createMinimalModelInfo(modelFileNode, true, context);

            setReferencesOnResource(folderNode, modelInfo);

            Map<String, List<ModelInfo>> referencingModels =
                modelRetrievalService.getModelsReferencing(modelId);

            for (Map.Entry<String, List<ModelInfo>> entry : referencingModels.entrySet()) {
              for (ModelInfo referencee : entry.getValue()) {
                modelInfo.getReferencedBy().add(referencee.getId());
              }
            }

            return modelInfo;
          } catch (PathNotFoundException e) {
            return null;
          } catch (AccessDeniedException e) {
            throw new NotAuthorizedException(modelId, e);
          }
        },
        context,
        privilegeService
    );
  }

  public ModelInfo getBasicInfo(ModelId modelId) {
    return doInSession(session -> {
      try {
        ModelIdHelper modelIdHelper = new ModelIdHelper(modelId);

        Node folderNode = session.getNode(modelIdHelper.getFullPath());

        Node modelFileNode = folderNode.getNodes(FILE_NODES).nextNode();
        ModelInfo modelInfo = createMinimalModelInfo(modelFileNode);

        setReferencesOnResource(folderNode, modelInfo);

        Map<String, List<ModelInfo>> referencingModels =
            modelRetrievalService.getModelsReferencing(modelId);

        for (Map.Entry<String, List<ModelInfo>> entry : referencingModels.entrySet()) {
          for (ModelInfo referencee : entry.getValue()) {
            modelInfo.getReferencedBy().add(referencee.getId());
          }
        }

        return modelInfo;
      } catch (PathNotFoundException e) {
        return null;
      } catch (AccessDeniedException e) {
        throw new NotAuthorizedException(modelId, e);
      }
    });
  }

  @Override
  public List<ModelInfo> getModelsReferencing(ModelId modelId) {
    return doInSession(session -> {
      List<ModelInfo> referencingModels = Lists.newArrayList();
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(
          "SELECT * FROM [vorto:meta] WHERE [vorto:references] = '" + modelId.toString() + "'",
          Query.JCR_SQL2);

      QueryResult result = query.execute();
      RowIterator rowIterator = result.getRows();
      while (rowIterator.hasNext()) {
        Row row = rowIterator.nextRow();
        Node currentNode = row.getNode();
        try {
          referencingModels
              .add(createMinimalModelInfo(currentNode.getNodes(FILE_NODES).nextNode()));
        } catch (Exception ex) {
          LOGGER.error("Error while converting node to a ModelId", ex);
        }
      }

      return referencingModels;
    });
  }

  @Override
  public List<ModelInfo> getMappingModelsForTargetPlatform(ModelId modelId, String targetPlatform,
      Optional<String> version) {
    LOGGER.info("Fetching mapping models for model ID " + modelId.getPrettyFormat() + " and key "
        + targetPlatform);
    Set<ModelInfo> mappingResources = new HashSet<>();
    ModelInfo modelResource = getBasicInfo(modelId);
    if (modelResource != null) {
      for (ModelInfo referenceeModelInfo : this.getModelsReferencing(modelId)) {
        if (referenceeModelInfo.getType() != ModelType.Mapping || version.isPresent()
            && !referenceeModelInfo.getId().getVersion().equals(version.get())) {
          continue;
        }

        if (referenceeModelInfo.getTargetPlatformKey() != null) {
          if (targetPlatform.equalsIgnoreCase(referenceeModelInfo.getTargetPlatformKey())) {
            mappingResources.add(referenceeModelInfo);
          }
        } else if (getEMFResource(referenceeModelInfo.getId())
            .matchesTargetPlatform(targetPlatform)) {
          mappingResources.add(referenceeModelInfo);

        }
      }

      for (ModelId referencedModelId : modelResource.getReferences()) {

        mappingResources.addAll(this.repositoryFactory.getRepositoryByModel(referencedModelId)
            .getMappingModelsForTargetPlatform(referencedModelId, targetPlatform, version));
      }
    }
    return new ArrayList<>(mappingResources);
  }

  @Override
  public ModelResource getEMFResource(ModelId modelId) {
    return doInSession(session -> {
      try {
        ModelIdHelper modelIdHelper = new ModelIdHelper(modelId);
        Node folderNode = session.getNode(modelIdHelper.getFullPath());
        if (!folderNode.getNodes(FILE_NODES).hasNext()) {
          LOGGER.warn("Folder Node :" + folderNode
              + " does not have any files as children. Cannot load EMF Model.");
          return null;
        }
        Node fileNode = (Node) folderNode.getNodes(FILE_NODES).next();
        Node fileItem = (Node) fileNode.getPrimaryItem();
        InputStream is = fileItem.getProperty(JCR_DATA).getBinary().getStream();
        IModelParser parser = modelParserFactory.getParser(fileNode.getName());
        return (ModelResource) parser.parse(is);
      } catch (AccessDeniedException e) {
        throw new NotAuthorizedException(modelId, e);
      }
    });
  }

  @Override
  public void removeModel(ModelId modelId) {
    doInSession(session -> {
      try {
        ModelInfo modelResource = this.getById(modelId);
        if (modelResource == null) {
          throw new ModelNotFoundException("Cannot find '" + modelId.getPrettyFormat() + "' in '"
              + session.getWorkspace().getName() + "'");
        }

        if (modelResource.getReferencedBy() != null && !modelResource.getReferencedBy().isEmpty()) {
          throw new ModelReferentialIntegrityException(
              "Cannot remove model because it is referenced by other model(s)",
              modelResource.getReferencedBy());
        }
        ModelIdHelper modelIdHelper = new ModelIdHelper(modelId);
        Item item = session.getItem(modelIdHelper.getFullPath());
        item.remove();
        session.save();

        eventPublisher.publishEvent(new AppEvent(this, modelId, null, EventType.MODEL_DELETED));

        return null;
      } catch (AccessDeniedException e) {
        throw new NotAuthorizedException(modelId, e);
      }
    });
  }

  @Override
  public ModelInfo updateMeta(ModelInfo model) {
    updateProperty(model.getId(), fileNode -> {
      fileNode.setProperty(VORTO_AUTHOR, model.getAuthor());
      fileNode.setProperty(VORTO_STATE, model.getState());
    });

    return model;
  }

  @Override
  public ModelId updateState(ModelId modelId, String state) {
    return updateProperty(modelId, node -> node.setProperty(VORTO_STATE, state));
  }

  @Override
  public ModelId updateVisibility(ModelId modelId, String visibility) {
    return updateProperty(modelId, node -> node.setProperty(VORTO_VISIBILITY, visibility));
  }

  @Override
  public ModelId updatePropertyInElevatedSession(ModelId modelId, Map<String, String> properties,
      IUserContext context) {
    return doInElevatedSession(
        session -> {
          try {
            Node folderNode = createNodeForModelId(session, modelId);
            Node fileNode =
                folderNode.getNodes(FILE_NODES).hasNext() ? folderNode.getNodes(FILE_NODES)
                    .nextNode()
                    : null;

            for (Map.Entry<String, String> entry : properties.entrySet()) {
              fileNode.setProperty(entry.getKey(), entry.getValue());
            }
            fileNode.addMixin(MIX_LAST_MODIFIED);

            session.save();

            eventPublisher
                .publishEvent(
                    new AppEvent(this, getBasicInfoInElevatedSession(modelId, context), null,
                        EventType.MODEL_UPDATED));

            return modelId;
          } catch (AccessDeniedException e) {
            throw new NotAuthorizedException(modelId, e);
          }
        },
        context,
        privilegeService
    );
  }

  private ModelId updateProperty(ModelId modelId, NodeConsumer nodeConsumer) {
    return doInSession(session -> {
      try {
        Node folderNode = createNodeForModelId(session, modelId);
        Node fileNode =
            folderNode.getNodes(FILE_NODES).hasNext() ? folderNode.getNodes(FILE_NODES).nextNode()
                : null;

        nodeConsumer.accept(fileNode);
        fileNode.addMixin(MIX_LAST_MODIFIED);

        session.save();

        eventPublisher
            .publishEvent(new AppEvent(this, getBasicInfo(modelId), null, EventType.MODEL_UPDATED));

        return modelId;
      } catch (AccessDeniedException e) {
        throw new NotAuthorizedException(modelId, e);
      }
    });
  }

  @FunctionalInterface
  private interface NodeConsumer {

    void accept(Node node) throws RepositoryException;
  }

  public ModelSearchUtil getModelSearchUtil() {
    return modelSearchUtil;
  }

  public void setModelSearchUtil(ModelSearchUtil modelSearchUtil) {
    this.modelSearchUtil = modelSearchUtil;
  }

  @Override
  public void addFileContent(ModelId modelId, FileContent fileContent) {
    doInSession(session -> {
      ModelIdHelper modelIdHelper = new ModelIdHelper(modelId);
      Node folderNode = session.getNode(modelIdHelper.getFullPath());

      Node contentNode = null;

      if (folderNode.hasNode(fileContent.getFileName())) {
        Node fileNode = (Node) folderNode.getNode(fileContent.getFileName());
        contentNode = (Node) fileNode.getPrimaryItem();
      } else {
        Node fileNode = folderNode.addNode(fileContent.getFileName(), NT_FILE);
        contentNode = fileNode.addNode(JCR_CONTENT, NT_RESOURCE);
      }

      Binary binary = session.getValueFactory()
          .createBinary(new ByteArrayInputStream(fileContent.getContent()));
      contentNode.setProperty(JCR_DATA, binary);
      session.save();

      return null;
    });
  }

  @Override
  public Optional<FileContent> getFileContent(ModelId modelId, Optional<String> fileName) {
    return doInSession(session -> {
      try {
        ModelId finalModelId = getLatestModelVersionIfLatestTagIsSet(modelId);
        ModelIdHelper modelIdHelper = new ModelIdHelper(finalModelId);

        Node folderNode = session.getNode(modelIdHelper.getFullPath());

        Node fileNode;
        if (fileName.isPresent()) {
          fileNode = folderNode.getNode(fileName.get());
        } else {
          if (!folderNode.getNodes(FILE_NODES).hasNext()) {
            throw new NotAuthorizedException(finalModelId);
          }
          fileNode = (Node) folderNode.getNodes(FILE_NODES).next();
        }

        Node fileItem = (Node) fileNode.getPrimaryItem();
        InputStream is = fileItem.getProperty(JCR_DATA).getBinary().getStream();

        final String fileContent = IOUtils.toString(is);
        return Optional.of(new FileContent(fileNode.getName(), fileContent.getBytes()));

      } catch (PathNotFoundException e) {
        return Optional.empty();
      } catch (IOException e) {
        throw new FatalModelRepositoryException("Something went wrong accessing the repository", e);
      } catch (AccessDeniedException e) {
        throw new NotAuthorizedException(modelId, e);
      }
    });
  }

  @Override
  public void attachFile(ModelId modelId, FileContent fileContent, IUserContext userContext,
      Tag... tags) throws AttachmentException {

    attachmentValidator.validateAttachment(fileContent, modelId);
    doInSession(session -> doAttachFileInSession(modelId, fileContent, userContext, session, tags));
  }

  @Override
  public void attachLink(ModelId modelId, ModelLink url) {
    doInSession(session -> doAttachLinkInSession(modelId, url, session));
  }

  @Override
  public Set<ModelLink> getLinks(ModelId modelID) {
    return doInSession(session -> doGetLinksInSession(modelID, session));
  }

  @Override
  public void deleteLink(ModelId modelID, ModelLink link) {
    doInSession(session -> doDeleteLinkInSession(modelID, link, session));
  }

  @Override
  public void attachFileInElevatedSession(ModelId modelId, FileContent fileContent,
      IUserContext userContext,
      Tag... tags) throws AttachmentException {

    attachmentValidator.validateAttachment(fileContent, modelId);
    doInElevatedSession(
        session -> doAttachFileInSession(modelId, fileContent, userContext, session, tags),
        userContext, privilegeService);
  }


  private boolean doAttachFileInSession(ModelId modelId, FileContent fileContent,
      IUserContext userContext, Session session, Tag[] tags) throws RepositoryException {
    try {
      ModelIdHelper modelIdHelper = new ModelIdHelper(modelId);
      Node modelFolderNode = session.getNode(modelIdHelper.getFullPath());

      Node attachmentFolderNode;
      if (!modelFolderNode.hasNode(ATTACHMENTS_NODE)) {
        attachmentFolderNode = modelFolderNode.addNode(ATTACHMENTS_NODE, NT_FOLDER);
      } else {
        attachmentFolderNode = modelFolderNode.getNode(ATTACHMENTS_NODE);
      }

      String[] tagIds = Arrays.stream(tags).filter(Objects::nonNull).map(Tag::getId)
          .collect(Collectors.toList())
          .toArray(new String[tags.length]);

      // if the display image tag is present (we're uploading a new image for presentational
      // purposes), removes the tag from all other attachments
      if (Arrays.asList(tags).contains(TAG_DISPLAY_IMAGE)) {
        NodeIterator attachments = attachmentFolderNode.getNodes();
        while (attachments.hasNext()) {
          Node next = attachments.nextNode();
          Property attachmentTags = next.getProperty(VORTO_TAGS);
          Value[] attachmentTagsValuesFiltered = Arrays.stream(attachmentTags.getValues())
              .filter(
                  v -> {
                    try {
                      return !v.getString().equals(TAG_DISPLAY_IMAGE.getId());
                      // swallowing here
                    } catch (RepositoryException re) {
                      return false;
                    }
                  }
              )
              .toArray(Value[]::new);
          next.setProperty(VORTO_TAGS, attachmentTagsValuesFiltered);
        }
      }

      Node contentNode;
      if (attachmentFolderNode.hasNode(fileContent.getFileName())) {
        Node attachmentNode = attachmentFolderNode.getNode(fileContent.getFileName());
        attachmentNode.addMixin(VORTO_META);
        attachmentNode.setProperty(VORTO_TAGS, tagIds, PropertyType.STRING);
        contentNode = (Node) attachmentNode.getPrimaryItem();
      } else {
        Node attachmentNode = attachmentFolderNode.addNode(fileContent.getFileName(), NT_FILE);
        attachmentNode.addMixin(VORTO_META);
        attachmentNode.setProperty(VORTO_TAGS, tagIds, PropertyType.STRING);
        contentNode = attachmentNode.addNode(JCR_CONTENT, NT_RESOURCE);
      }

      Binary binary = session.getValueFactory()
          .createBinary(new ByteArrayInputStream(fileContent.getContent()));
      contentNode.setProperty(JCR_DATA, binary);
      session.save();

      eventPublisher.publishEvent(
          new AppEvent(this, getById(modelId), userContext, EventType.MODEL_UPDATED));
      return true;
    } catch (AccessDeniedException e) {
      throw new NotAuthorizedException(modelId, e);
    }
  }

  @Override
  public List<Attachment> getAttachments(ModelId modelId) {
    return doInSession(session -> {
      try {
        ModelIdHelper modelIdHelper = new ModelIdHelper(modelId);
        Node modelFolderNode = session.getNode(modelIdHelper.getFullPath());

        if (modelFolderNode.hasNode(ATTACHMENTS_NODE)) {
          Node attachmentFolderNode = modelFolderNode.getNode(ATTACHMENTS_NODE);
          List<Attachment> attachments = new ArrayList<>();
          NodeIterator nodeIt = attachmentFolderNode.getNodes();
          while (nodeIt.hasNext()) {
            Node fileNode = (Node) nodeIt.next();
            Attachment attachment = Attachment.newInstance(modelId, fileNode.getName());
            if (fileNode.hasProperty(VORTO_TAGS)) {
              final List<Value> tags = Arrays.asList(fileNode.getProperty(VORTO_TAGS).getValues());
              attachment.setTags(
                  tags.stream().map(ModelRepository::fromModeshapeValue).filter(Objects::nonNull)
                      .collect(Collectors.toList()));
            }
            attachments.add(attachment);
          }
          return attachments;
        }
        return Collections.emptyList();
      } catch (AccessDeniedException e) {
        throw new NotAuthorizedException(modelId, e);
      }
    });
  }

  @Override
  public List<Attachment> getAttachmentsInElevatedSession(ModelId modelId, IUserContext context) {
    return doInElevatedSession(session -> {
          try {
            ModelIdHelper modelIdHelper = new ModelIdHelper(modelId);
            Node modelFolderNode = session.getNode(modelIdHelper.getFullPath());

            if (modelFolderNode.hasNode(ATTACHMENTS_NODE)) {
              Node attachmentFolderNode = modelFolderNode.getNode(ATTACHMENTS_NODE);
              List<Attachment> attachments = new ArrayList<>();
              NodeIterator nodeIt = attachmentFolderNode.getNodes();
              while (nodeIt.hasNext()) {
                Node fileNode = (Node) nodeIt.next();
                Attachment attachment = Attachment.newInstance(modelId, fileNode.getName());
                if (fileNode.hasProperty(VORTO_TAGS)) {
                  final List<Value> tags = Arrays.asList(fileNode.getProperty(VORTO_TAGS).getValues());
                  attachment.setTags(
                      tags.stream().map(ModelRepository::fromModeshapeValue).filter(Objects::nonNull)
                          .collect(Collectors.toList()));
                }
                attachments.add(attachment);
              }
              return attachments;
            }
            return Collections.emptyList();
          } catch (AccessDeniedException e) {
            throw new NotAuthorizedException(modelId, e);
          }
        },
        context,
        privilegeService
    );
  }


  private static final Set<Tag> TAGS = new HashSet<>();

  static {
    TAGS.add(TAG_IMAGE);
    TAGS.add(TAG_DISPLAY_IMAGE);
    TAGS.add(TAG_DOCUMENTATION);
    TAGS.add(TAG_IMPORTED);
  }

  /**
   * Transforms a modeshape {@link Value} to a {@link Tag} if possible.
   *
   * @param value
   * @return one of the known Vorto tags or null.
   */
  public static Tag fromModeshapeValue(Value value) {
    final String tagValue;
    try {
      tagValue = value.getString();
    } catch (RepositoryException re) {
      LOGGER.warn("Could not retrieve tag value from JCR node.");
      return null;
    }
    return TAGS.stream()
        .filter(t -> t.getId().equals(tagValue))
        .findAny()
        .orElse(
            new Tag(tagValue)
        );
  }

  @Override
  public List<Attachment> getAttachmentsByTag(final ModelId modelId, final Tag tag) {
    return getAttachmentsByTag(modelId, tag, false, null);
  }

  @Override
  public List<Attachment> getAttachmentsByTag(final ModelId modelId, final Tag tag,
      boolean doInElevatedSession, IUserContext context) {
    List<Attachment> attachments =
        doInElevatedSession ? getAttachmentsInElevatedSession(modelId, context) : getAttachments(modelId);
    return attachments.stream().filter(attachment -> attachment.getTags().contains(tag))
        .collect(Collectors.toList());
  }

  @Override
  public List<Attachment> getAttachmentsByTags(final ModelId modelId, final List<Tag> tag) {
    return getAttachments(modelId).stream()
        .filter(attachment -> attachment.getTags().size() == tag.size())
        .filter(attachment -> attachment.getTags().containsAll(tag))
        .collect(Collectors.toList());
  }

  @Override
  public Optional<FileContent> getAttachmentContent(ModelId modelId, String fileName) {
    return doInSession(session -> {
      try {
        ModelIdHelper modelIdHelper = new ModelIdHelper(modelId);
        Node modelFolderNode = session.getNode(modelIdHelper.getFullPath());

        if (modelFolderNode.hasNode(ATTACHMENTS_NODE)) {
          Node attachmentFolderNode = modelFolderNode.getNode(ATTACHMENTS_NODE);
          if (attachmentFolderNode.hasNode(fileName)) {
            Node attachment = (Node) attachmentFolderNode.getNode(fileName).getPrimaryItem();
            return Optional.of(new FileContent(fileName,
                IOUtils.toByteArray(attachment.getProperty(JCR_DATA).getBinary().getStream())));
          }
        }
        return Optional.empty();
      } catch (PathNotFoundException e) {
        return Optional.empty();
      } catch (AccessDeniedException e) {
        throw new NotAuthorizedException(modelId);
      } catch (IOException | RepositoryException e) {
        throw new FatalModelRepositoryException("Something went wrong accessing the repository", e);
      }
    });
  }

  @Override
  public boolean deleteAttachment(ModelId modelId, String fileName) {
    if (getAttachments(modelId).stream()
        .anyMatch(attachment -> (attachment.getTags().contains(TAG_IMPORTED)
            && attachment.getFilename().equals(fileName)))) {
      return false;
    }

    return doInSession(session -> {
      try {
        ModelIdHelper modelIdHelper = new ModelIdHelper(modelId);
        Node modelFolderNode = session.getNode(modelIdHelper.getFullPath());

        if (modelFolderNode.hasNode(ATTACHMENTS_NODE)) {
          Node attachmentFolderNode = modelFolderNode.getNode(ATTACHMENTS_NODE);
          if (attachmentFolderNode.hasNode(fileName)) {
            Node attachmentNode = attachmentFolderNode.getNode(fileName);
            attachmentNode.remove();
            session.save();
            return true;
          }
        }
        return false;
      } catch (PathNotFoundException e) {
        return false;
      }
    });
  }

  @Override
  public ModelResource createVersion(ModelId existingId, String newVersion, IUserContext user) {
    ModelInfo existingModel = this.getById(existingId);
    if (existingModel == null) {
      throw new ModelNotFoundException("Model could not be found");

    } else if (existingId.getVersion().equals(newVersion)) {
      throw new ModelAlreadyExistsException();
    } else {
      ModelId newModelId = ModelId.newVersion(existingId, newVersion);
      if (this.exists(newModelId)) {
        throw new ModelAlreadyExistsException();
      }

      ModelFileContent existingModelContent = this.getModelContent(existingId, false);
      Model model = existingModelContent.getModel();
      model.setVersion(newVersion);
      ModelResource resource = new ModelResource(model);
      try {
        this.save(newModelId, resource.toDSL(),
            existingId.getName() + existingModel.getType().getExtension(), user);
      } catch (Exception e) {
        throw new FatalModelRepositoryException(e.getMessage(), e);
      }

      return resource;
    }
  }

  public void setModelParserFactory(ModelParserFactory modelParserFactory) {
    this.modelParserFactory = modelParserFactory;
  }

  @Override
  public boolean exists(ModelId modelId) {
    ModelId latestModelId = getLatestModelVersionIfLatestTagIsSet(modelId);
    return doInSession(session -> {
      try {
        ModelIdHelper modelIdHelper = new ModelIdHelper(latestModelId);
        return session.itemExists(modelIdHelper.getFullPath());
      } catch (NullPointerException e) {
        return false;
      } catch (AccessDeniedException e) {
        return true;
      }
    });
  }

  @Override
  public String getWorkspaceId() {
    return doInSession(session -> session.getWorkspace().getName());
  }

  @Override
  public ModelInfo rename(ModelId oldModelId, ModelId newModelId, IUserContext user) {
    Namespace namespace = namespaceService.findNamespaceByWorkspaceId(getWorkspaceId());

    if (getById(newModelId) != null) {
      throw new ModelAlreadyExistsException();
    } else if (!newModelId.getNamespace().startsWith(namespace.getName())) {
      throw new NewNamespacesNotSupersetException();
    }

    ModelInfo oldModel = getById(oldModelId);
    ChangeSet changeSet = refactorModelWithNewId(oldModel, newModelId);
    saveChangeSetIntoRepository(changeSet, user);

    ModelInfo newModel = getById(newModelId);
    newModel = copy(oldModel, newModel, user);
    removeModel(oldModel.getId());

    return newModel;
  }

  /**
   * copies merely attachments and policies and workflow meta data from the source model to the
   * target model
   *
   * @param sourceModel source model to copy
   * @param targetModel target model to add data from source model
   * @param user
   * @return new model with all new meta data
   */
  private ModelInfo copy(ModelInfo sourceModel, ModelInfo targetModel, IUserContext user) {
    updateState(targetModel.getId(), sourceModel.getState());
    targetModel.setState(sourceModel.getState());

    // Copy all attachments over to new node
    this.getAttachments(sourceModel.getId()).forEach(oldAttachment -> {
      Optional<FileContent> fileContent =
          this.getAttachmentContent(sourceModel.getId(), oldAttachment.getFilename());
      this.attachFile(targetModel.getId(), fileContent.get(), user,
          oldAttachment.getTags().toArray(new Tag[oldAttachment.getTags().size()]));
    });

    // Copy all policies over to new node
    this.policyManager.copyPolicyEntries(sourceModel.getId(), targetModel.getId());

    return targetModel;
  }

  /**
   * saves the given changeset in a sorted way into the repository in the context of the given user
   *
   * @param changeSet
   * @param user
   */
  private void saveChangeSetIntoRepository(ChangeSet changeSet, IUserContext user) {
    DependencyManager dm = new DependencyManager();
    changeSet.getChanges().forEach(model -> dm.addResource(new ModelResource(model)));
    dm.getSorted().forEach(sortedModel -> save((ModelResource) sortedModel, user));
  }

  private ChangeSet refactorModelWithNewId(ModelInfo oldModel, ModelId newModelId) {
    IModelWorkspace workspace = createWorkspaceFromModelAndReferences(oldModel.getId());

    return RefactoringTask.from(workspace)
        .toModelId(ModelUtils.toEMFModelId(oldModel.getId(), oldModel.getType()),
            ModelUtils.toEMFModelId(newModelId, oldModel.getType()))
        .execute();
  }

  private IModelWorkspace createWorkspaceFromModelAndReferences(ModelId modelId) {
    ModelWorkspaceReader reader = IModelWorkspace.newReader();
    ModelResource resource = getEMFResource(modelId);
    reader.addFile(new ByteArrayInputStream(resource.toDSL()), resource.getType());

    getModelsReferencing(modelId).forEach(reference -> {
      ModelResource referencingModel = getEMFResource(reference.getId());
      reader.addFile(new ByteArrayInputStream(referencingModel.toDSL()),
          referencingModel.getType());
    });
    return reader.read();
  }

  private ModelInfo createModelResource(Node folderNode) throws RepositoryException {
    Node fileNode = folderNode.getNodes(FILE_NODES).nextNode();
    ModelInfo resource = createMinimalModelInfo(fileNode);
    resource.setFileName(fileNode.getName());

    setReferencesOnResource(folderNode, resource);

    Map<String, List<ModelInfo>> referencingModels = modelRetrievalService
        .getModelsReferencing(resource.getId());

    for (Map.Entry<String, List<ModelInfo>> entry : referencingModels.entrySet()) {
      for (ModelInfo modelInfo : entry.getValue()) {
        resource.getReferencedBy().add(modelInfo.getId());
      }
    }
    return resource;
  }

  private void setReferencesOnResource(Node folderNode, ModelInfo resource)
      throws RepositoryException {
    if (folderNode.hasProperty(VORTO_REFERENCES)) {
      Value[] referenceValues;
      try {
        referenceValues = folderNode.getProperty(VORTO_REFERENCES).getValues();
      } catch (Exception ex) {
        referenceValues = new Value[]{folderNode.getProperty(VORTO_REFERENCES).getValue()};
      }

      if (referenceValues != null) {
        ModelReferencesHelper referenceHelper = new ModelReferencesHelper();
        for (Value referValue : referenceValues) {
          referenceHelper.addModelReference(referValue.getString());
        }
        resource.setReferences(referenceHelper.getReferences());
      }
    }
  }

  private boolean doAttachLinkInSession(ModelId modelId, ModelLink url, Session session) throws RepositoryException {
    Node fileNode = getFileNode(modelId, session);
    ObjectMapper objectMapper = ObjectMapperFactory.getInstance();
    if (fileNode.hasProperty(VORTO_LINKS)) {
      Property property = fileNode.getProperty(VORTO_LINKS);
      Set<String> values = getVortoLinksPropertyValues(property);
      values.add(writeLinkObjectAsJson(url, objectMapper));
      fileNode.setProperty(VORTO_LINKS, values.toArray(new String[0]), PropertyType.STRING);
    } else {
      fileNode.setProperty(VORTO_LINKS, new String[]{writeLinkObjectAsJson(url, objectMapper)}, PropertyType.STRING);
    }
    session.save();
    return true;
  }

  private String writeLinkObjectAsJson(ModelLink url, ObjectMapper objectMapper) throws RepositoryException {
    try {
      return objectMapper.writeValueAsString(url);
    } catch (JsonProcessingException e) {
      throw new RepositoryException("Error while writing link object to JSON", e);
    }
  }

  private Set<ModelLink> doGetLinksInSession(ModelId modelID, Session session) throws RepositoryException {
    ObjectMapper objectMapper = ObjectMapperFactory.getInstance();
    Node fileNode = getFileNode(modelID, session);
    if (fileNode.hasProperty(VORTO_LINKS)) {
      return getVortoLinksPropertyValues(fileNode.getProperty(VORTO_LINKS)).stream()
          .map(value -> deserializeLinkDto(objectMapper, value)).collect(Collectors.toSet());
    }
    return Collections.emptySet();
  }

  private Set<String> getVortoLinksPropertyValues(Property property) throws RepositoryException {
    return Arrays.stream(property.getValues())
        .map(this::getStringValue)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  private ModelLink deserializeLinkDto(ObjectMapper objectMapper, String value) {
    try {
      return objectMapper.readValue(value, ModelLink.class);
    } catch (IOException e) {
      LOGGER.warn("Unable to deserialize Link: " + value, e);
      return null;
    }
  }

  private String getStringValue(Value v) {
    try {
      return v.getString();
    } catch (Exception e) {
      return null;
    }
  }

  private boolean doDeleteLinkInSession(ModelId modelID, ModelLink link, Session session) throws RepositoryException {
    Node fileNode = getFileNode(modelID, session);
    if (fileNode.hasProperty(VORTO_LINKS)) {
      ObjectMapper objectMapper = ObjectMapperFactory.getInstance();
      Property property = fileNode.getProperty(VORTO_LINKS);
      Set<String> values = getVortoLinksPropertyValues(property);
      values.remove(writeLinkObjectAsJson(link, objectMapper));
      fileNode.setProperty(VORTO_LINKS, values.toArray(new String[0]), PropertyType.STRING);
      session.save();
    }
    return true;
  }

  private Node getFileNode(ModelId modelID, Session session) throws RepositoryException {
    ModelIdHelper modelIdHelper = new ModelIdHelper(modelID);
    Node modelFolderNode = session.getNode(modelIdHelper.getFullPath());
    return modelFolderNode.getNodes(FILE_NODES).nextNode();
  }
}
