/*******************************************************************************
 * Copyright (c) 2017 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.spore.options;

import org.eclipse.elk.alg.spore.SPOrEPhases;
import org.eclipse.elk.alg.spore.graph.Graph;
import org.eclipse.elk.alg.spore.p3execution.ShrinkTreeCompactionPhase;
import org.eclipse.elk.core.alg.ILayoutPhase;
import org.eclipse.elk.core.alg.ILayoutPhaseFactory;

/**
 * Definition of the compaction strategy for compaction by shrinking a tree.
 */
public enum CompactionStrategy implements ILayoutPhaseFactory<SPOrEPhases, Graph> {
    /** Simple compaction strategy. */
    DEPTH_FIRST;

    /**
     * {@inheritDoc}
     */
    @Override
    public ILayoutPhase<SPOrEPhases, Graph> create() {
        switch (this) {
        case DEPTH_FIRST:
            return new ShrinkTreeCompactionPhase();

        default:
            throw new IllegalArgumentException(
                    "No implementation available for " + this.toString());
        }
    }
}
