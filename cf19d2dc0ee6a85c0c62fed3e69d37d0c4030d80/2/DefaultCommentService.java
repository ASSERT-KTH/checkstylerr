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
package org.eclipse.vorto.repository.comment.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.jcr.PathNotFoundException;
import org.eclipse.vorto.model.ModelId;
import org.eclipse.vorto.repository.account.impl.DefaultUserAccountService;
import org.eclipse.vorto.repository.comment.ICommentService;
import org.eclipse.vorto.repository.core.IModelRepository;
import org.eclipse.vorto.repository.core.IModelRepositoryFactory;
import org.eclipse.vorto.repository.core.ModelInfo;
import org.eclipse.vorto.repository.core.ModelNotFoundException;
import org.eclipse.vorto.repository.core.impl.ModelRepositoryFactory;
import org.eclipse.vorto.repository.domain.Comment;
import org.eclipse.vorto.repository.domain.NamespaceRole;
import org.eclipse.vorto.repository.domain.User;
import org.eclipse.vorto.repository.notification.INotificationService;
import org.eclipse.vorto.repository.notification.message.CommentReplyMessage;
import org.eclipse.vorto.repository.services.NamespaceService;
import org.eclipse.vorto.repository.services.UserNamespaceRoleService;
import org.eclipse.vorto.repository.services.exceptions.DoesNotExistException;
import org.eclipse.vorto.repository.services.exceptions.OperationForbiddenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Alexander Edelmann - Robert Bosch (SEA) Pte. Ltd.
 */
@Service
public class DefaultCommentService implements ICommentService {

  private IModelRepositoryFactory modelRepositoryFactory;

  private INotificationService notificationService;

  private CommentRepository commentRepository;

  private DefaultUserAccountService accountService;

  private NamespaceService namespaceService;

  private UserNamespaceRoleService userNamespaceRoleService;

  public DefaultCommentService(@Autowired ModelRepositoryFactory modelRepositoryFactory,
      @Autowired INotificationService notificationService,
      @Autowired CommentRepository commentRepository,
      @Autowired DefaultUserAccountService defaultUserAccountService,
      @Autowired NamespaceService namespaceService,
      @Autowired UserNamespaceRoleService userNamespaceRoleService) {
    this.modelRepositoryFactory = modelRepositoryFactory;
    this.notificationService = notificationService;
    this.commentRepository = commentRepository;
    this.accountService = defaultUserAccountService;
    this.namespaceService = namespaceService;
    this.userNamespaceRoleService = userNamespaceRoleService;
  }

  public void createComment(Comment comment) throws DoesNotExistException {

    final ModelId id = ModelId.fromPrettyFormat(comment.getModelId());

    Optional<String> workspaceId = namespaceService
        .resolveWorkspaceIdForNamespace(id.getNamespace());
    if (!workspaceId.isPresent()) {
      throw new DoesNotExistException(
          String.format("Namespace [%s] does not exist.", id.getNamespace()));
    }

    IModelRepository modelRepo = modelRepositoryFactory
        .getRepository(workspaceId.get());

    if (modelRepo.exists(id)) {
      comment.setAuthor(comment.getAuthor());
      comment.setModelId(id.getPrettyFormat());
      comment.setDate(new Date());
      commentRepository.save(comment);

      notifyAllCommentAuthors(comment, modelRepo.getById(id));

    } else {
      throw new ModelNotFoundException("Model not found", new PathNotFoundException());
    }
  }

  private void notifyAllCommentAuthors(Comment comment, ModelInfo model) {
    Set<String> recipients = new HashSet<>();

    recipients.add(model.getAuthor());

    List<Comment> existingComments = this.commentRepository.findByModelId(comment.getModelId());
    for (Comment c : existingComments) {
      recipients.add(c.getAuthor());
    }

    recipients.stream().filter(recipient -> !User.USER_ANONYMOUS.equalsIgnoreCase(recipient))
        .forEach(recipient -> {
          User user = accountService.getUser(recipient);
          if (user != null) {
            notificationService.sendNotification(
                new CommentReplyMessage(user, model, comment.getContent()));
          }
        });
  }

  public List<Comment> getCommentsforModelId(ModelId modelId) {
    return commentRepository.findByModelId(modelId.getPrettyFormat());
  }

  public List<Comment> getCommentsByAuthor(String author) {
    return commentRepository.findByAuthor(author);
  }

  @Override
  public void saveComment(Comment comment) {
    this.commentRepository.save(comment);
  }

  public IModelRepositoryFactory getModelRepositoryFactory() {
    return modelRepositoryFactory;
  }

  public void setModelRepositoryFactory(IModelRepositoryFactory modelRepositoryFactory) {
    this.modelRepositoryFactory = modelRepositoryFactory;
  }

  public CommentRepository getCommentRepository() {
    return commentRepository;
  }

  public void setCommentRepository(CommentRepository commentRepository) {
    this.commentRepository = commentRepository;
  }

  public INotificationService getNotificationService() {
    return notificationService;
  }

  public void setNotificationService(INotificationService notificationService) {
    this.notificationService = notificationService;
  }

  /**
   * Deletes the given comment, provided that either:
   * <ul>
   *   <li>
   *     The user requesting is the same user who wrote the comment (comparison by username)
   *   </li>
   *   <li>
   *     The user requesting has the {@literal namespace_admin} role on the namespace where the
   *     commented model is stored.
   *   </li>
   * </ul>
   * @param username
   * @param id
   * @throws OperationForbiddenException
   * @throws DoesNotExistException
   */
  @Override
  public void deleteComment(String username, long id)
      throws OperationForbiddenException, DoesNotExistException {

    Comment comment = commentRepository.findOne(id);

    if (Objects.isNull(comment)) {
      throw new DoesNotExistException(
          String.format(
              "Comment with id [%s] not found", id
          )
      );
    }

    authorizeCommentRemoval(username, comment);

    commentRepository.delete(comment.getId());
  }

  private void authorizeCommentRemoval(String username, Comment comment)
      throws OperationForbiddenException, DoesNotExistException {
    String namespace = ModelId.fromPrettyFormat(comment.getModelId()).getNamespace();

    if (!username.equals(comment.getAuthor())) {

      if (!userNamespaceRoleService
          .hasRole(username, namespace, userNamespaceRoleService.namespaceAdminRole().getName())) {

        throw new OperationForbiddenException(
            String.format(
                "Cannot delete comment [%s] - user requesting is neither the author nor is namespace_admin on namespace [%s]",
                comment.getId(),
                namespace
            )
        );
      }
    }
  }
}
