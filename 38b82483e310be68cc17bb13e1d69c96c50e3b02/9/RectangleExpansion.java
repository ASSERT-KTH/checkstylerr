/*******************************************************************************
 * Copyright (c) 2018, 2020 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.rectpacking.seconditeration;

import java.util.List;

import org.eclipse.elk.alg.rectpacking.util.RectRow;
import org.eclipse.elk.graph.ElkNode;

/**
 * Offer method to expand rectangles after the calculations of the second iteration.
 * 
 * @see RowFillingAndCompaction
 */
public final class RectangleExpansion {

    private RectangleExpansion() {
    }

    /**
     * Expands rectangles after the calculations of the second iteration, so that the bounding box is filled without
     * whitespace. First, every {@link RectRow} is enlarged to fit the drawings dimensions. Then, {@link RectStack} are
     * enlarged to fit their respective {@link RectRow}. Lastly, {@link ElkNode}s are enlarged to fill their respective
     * {@link RectStack}.
     * 
     * @param rows
     *            rows containing the rectangles given by {@link RectStack}s.
     * @param drawingWidth
     *            the desired width of the drawing
     * @param additionalHeight
     *            The height that is added and has to be distributed to the different rows.
     * @param nodeNodeSpacing
     *            The spacing between two nodes.
     */
    protected static void expand(final List<RectRow> rows, final double drawingWidth, final double additionalHeight, final double nodeNodeSpacing) {
        double heightPerRow = additionalHeight / rows.size();
        int index = 0;
        for (RectRow row : rows) {
            row.setY(row.getY() + heightPerRow * index);
            row.expand(drawingWidth, heightPerRow);
            index++;
        }
    }
}
