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
package org.eclipse.vorto.repository.workflow.impl.functions;

import org.eclipse.vorto.repository.core.IModelRepositoryFactory;
import org.eclipse.vorto.repository.core.IUserContext;
import org.eclipse.vorto.repository.core.ModelInfo;
import org.eclipse.vorto.repository.core.PolicyEntry;
import org.eclipse.vorto.repository.core.PolicyEntry.Permission;
import org.eclipse.vorto.repository.core.PolicyEntry.PrincipalType;
import org.eclipse.vorto.repository.workflow.model.IWorkflowFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GrantReviewerModelPolicy implements IWorkflowFunction {

  private IModelRepositoryFactory repositoryFactory;

  private static final Logger LOGGER = LoggerFactory.getLogger(GrantReviewerModelPolicy.class);


  public GrantReviewerModelPolicy(IModelRepositoryFactory repositoryFactory) {
    this.repositoryFactory = repositoryFactory;
  }

  @Deprecated
  @Override
  public void execute(ModelInfo model, IUserContext user, Map<String,Object> context) {
    LOGGER.info(
        String.format(
            "Granting permission of model [%s] to reviewer role", model.getId()
        )
    );
    repositoryFactory.getPolicyManager(user.getWorkspaceId()).addPolicyEntry(
        model.getId(),
        PolicyEntry.of("model_reviewer", PrincipalType.Role, Permission.FULL_ACCESS));

  }
}
