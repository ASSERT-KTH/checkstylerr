/*******************************************************************************
 * Copyright (c) 2016 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.layered.compaction.oned.algs;

import org.eclipse.elk.alg.layered.compaction.oned.OneDimensionalCompactor;

/**
 * An algorithm that compacts a given set of rectangles in one dimension. This can, for example,
 * be a longest path-based algorithm that compacts everything as much as possible "to the left"
 * or a network simplex based compaction algorithm that leverages some underlying graph
 * structure to keep edges as short as possible.
 */
@FunctionalInterface
public interface ICompactionAlgorithm {

    /**
     * @param compactor
     *            the instance of the surrounding {@link OneDimensionalCompactor}.
     */
    void compact(OneDimensionalCompactor compactor);
    
}
