/*******************************************************************************
 * Copyright (c) 2017 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.common.overlaps;

/**
 * Classes implementing this interface know how to remove overlaps between a strip of rectangles.
 * 
 * @see RectangleStripOverlapRemover
 */
public interface IRectangleStripOverlapRemovalStrategy {

    /**
     * Removes overlaps for the given {@link RectangleStripOverlapRemover}.
     * 
     * @param overlapRemover
     *            the overlap remover that invokes overlap removal.
     * @return the height of the resulting strip of rectangles.
     */
    double removeOverlaps(RectangleStripOverlapRemover overlapRemover);
    
}
