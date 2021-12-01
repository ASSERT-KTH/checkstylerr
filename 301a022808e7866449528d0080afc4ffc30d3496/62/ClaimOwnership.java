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
import org.eclipse.vorto.repository.domain.RepositoryRole;
import org.eclipse.vorto.repository.workflow.model.IWorkflowFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

public class ClaimOwnership implements IWorkflowFunction {

  private IModelRepositoryFactory repositoryFactory;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClaimOwnership.class);

	
	public ClaimOwnership(IModelRepositoryFactory repositoryFactory) {
	  this.repositoryFactory = repositoryFactory;
	}
	
	@Deprecated
	@Override
	public void execute(ModelInfo model, IUserContext user,Map<String,Object> context) {
		LOGGER.info(
				String.format(
						"Claiming model [%s]", model.getId()
				)
		);
		
		Collection<PolicyEntry> policies = repositoryFactory
				.getPolicyManager(user.getWorkspaceId())
		    .getPolicyEntries(model.getId());

		for (PolicyEntry entry : policies) {
		  LOGGER.info(String.format("Removing [%s]", entry));

		  repositoryFactory.getPolicyManager(user.getWorkspaceId())
		    .removePolicyEntry(model.getId(), entry);
		}
		
		repositoryFactory.getPolicyManager(user.getWorkspaceId())
		  .addPolicyEntry(
		  		model.getId(),
					PolicyEntry.of(user.getUsername(), PrincipalType.User, Permission.FULL_ACCESS),
					PolicyEntry.of(
						RepositoryRole.SYS_ADMIN.getName(), PrincipalType.Role, Permission.FULL_ACCESS)
			);
        
        model.setAuthor(user.getUsername());  
        repositoryFactory.getRepository(user.getWorkspaceId())
          .updateMeta(model);
	}
}
