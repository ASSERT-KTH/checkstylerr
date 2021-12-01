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

import org.eclipse.elk.alg.radial.intermediate.overlaps.IOverlapRemoval;
import org.eclipse.elk.alg.radial.intermediate.overlaps.RadiusExtensionOverlapRemoval;

/**
 * The list of strategies for removing overlaps in the graph.
 *
 */
public enum OverlapRemovalStrategy {
    /** Extend the radii until overlap are removed. */
    EXTENT_RADII;

    /**
     * Create the overlap strategy.
     * 
     * @return
     */
    public IOverlapRemoval create() {
        switch (this) {
        case EXTENT_RADII:
            return new RadiusExtensionOverlapRemoval();
        default:
            throw new IllegalArgumentException(
                    "No implementation is available for the layout option " + this.toString());
        }
    }

}