/*******************************************************************************
 * Copyright (c) 2017 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.elk.alg.radial.p2routing;

import org.eclipse.elk.graph.ElkNode;

/**
 * An interface for routing edges in a radial layout.
 */
public interface IRadialEdgeRouter {
    /**
     * Route edges of a graph.
     * 
     * @param root
     *            The root node of the graph.
     */
    void routeEdges(ElkNode root);

}
