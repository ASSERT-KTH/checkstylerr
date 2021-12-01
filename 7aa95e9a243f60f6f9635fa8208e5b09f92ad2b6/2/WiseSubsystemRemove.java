/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.wildfly.extension.wise;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.registry.ImmutableManagementResourceRegistration;
import org.jboss.dmr.ModelNode;

/**
 * The "wise" subsystem remove update handler.
 *
 * User: rsearls
 */
public class WiseSubsystemRemove extends AbstractRemoveStepHandler {

   static final WiseSubsystemRemove INSTANCE = new WiseSubsystemRemove();

   private WiseSubsystemRemove() {
   }

   @Override
   protected void performRemove(OperationContext context, ModelNode operation,
                                ModelNode model) throws OperationFailedException {

      // Add a step to remove the wise.war deployment
      if (requiresRuntime(context)) {  // only add the step if we are going to actually undeploy the war

         PathAddress deploymentAddress = PathAddress.pathAddress(PathElement.pathElement(DEPLOYMENT, "wise.war"));
         ModelNode op = Util.createOperation(REMOVE, deploymentAddress);

         ImmutableManagementResourceRegistration rootResourceRegistration = context.getRootResourceRegistration();
         OperationStepHandler handler = rootResourceRegistration.getOperationHandler(deploymentAddress, REMOVE);

         context.addStep(op, handler, OperationContext.Stage.MODEL);
      }

      super.performRemove(context, operation, model);

   }
}