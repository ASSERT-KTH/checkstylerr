/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

package org.jboss.as.clustering.controller;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;

/**
 * A {@link SimpleResourceServiceHandler} that uses a recursively read model, to include child resources.
 * @author Paul Ferraro
 */
public class ParentResourceServiceHandler<T> extends SimpleResourceServiceHandler<T> {

    public ParentResourceServiceHandler(ResourceServiceBuilderFactory<T> builderFactory) {
        super(builderFactory);
    }

    @Override
    public void installServices(OperationContext context, ModelNode model) throws OperationFailedException {
        super.installServices(context, Resource.Tools.readModel(context.readResource(PathAddress.EMPTY_ADDRESS)));
    }
}
