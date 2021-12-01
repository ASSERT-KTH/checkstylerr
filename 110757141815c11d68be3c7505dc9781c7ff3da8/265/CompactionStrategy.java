/*******************************************************************************
 * Copyright (c) 2017 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.radial.options;

import org.eclipse.elk.alg.radial.intermediate.compaction.AnnulusWedgeCompaction;
import org.eclipse.elk.alg.radial.intermediate.compaction.IRadialCompactor;
import org.eclipse.elk.alg.radial.intermediate.compaction.RadialCompaction;

/**
 * The list of selectable compaction algorithms.
 */
public enum CompactionStrategy {

    /** No compaction. **/
    NONE,
    /** Contract each radius. */
    RADIAL_COMPACTION,
    /** Contract each wedge. */
    WEDGE_COMPACTION;

    /**
     * Instantiate the chosen compaction strategy.
     * 
     * @return A wedge compactor.
     */
    public IRadialCompactor create() {
        switch (this) {
        case RADIAL_COMPACTION:
            return new RadialCompaction();
        case WEDGE_COMPACTION:
            return new AnnulusWedgeCompaction();
        default:
            throw new IllegalArgumentException(
                    "No implementation is available for the layout option " + this.toString());
        }
    }
}