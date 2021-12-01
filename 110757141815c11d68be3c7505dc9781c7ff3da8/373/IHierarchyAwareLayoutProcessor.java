/*******************************************************************************
 * Copyright (c) 2017 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.layered;

/**
 * Marker interface to mark processors that access the complete hierarchy. In this case, when
 * {@link org.eclipse.elk.alg.layered.options.LayeredOptions#HIERARCHY_HANDLING} is set to
 * ({@link org.eclipse.elk.core.options.HierarchyHandling#INCLUDE_CHILDREN}), all the processing will be executed
 * recursively for all processors preceding this one and this processor will have access to the root graph.
 */
public interface IHierarchyAwareLayoutProcessor {
    
}
