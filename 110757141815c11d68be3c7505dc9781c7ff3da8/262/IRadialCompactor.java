/*******************************************************************************
 * Copyright (c) 2017 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.radial.intermediate.compaction;

import org.eclipse.elk.graph.ElkNode;

/**
 * An interface for compacting the radial layout.
 */
public interface IRadialCompactor {
    /**
     * Compact the graph.
     * 
     * @param graph
     *            The graph which is already radial.
     */
    void compact(ElkNode graph);
}