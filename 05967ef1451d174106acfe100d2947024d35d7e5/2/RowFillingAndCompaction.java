/*******************************************************************************
 * Copyright (c) 2018 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.packing.rectangles.seconditeration;

import java.util.List;

import org.eclipse.elk.alg.packing.rectangles.util.Block;
import org.eclipse.elk.alg.packing.rectangles.util.DrawingData;
import org.eclipse.elk.alg.packing.rectangles.util.DrawingDataDescriptor;
import org.eclipse.elk.alg.packing.rectangles.util.RectRow;
import org.eclipse.elk.graph.ElkNode;

/**
 * Second iteration of the algorithm. Actual placement of the boxes inside the approximated bounding box. Rectangles are
 * placed in rows, which are compacted and then filled with elements from the subsequent row.
 */
public class RowFillingAndCompaction {
    //////////////////////////////////////////////////////////////////
    // Fields
    /** Current drawing width. */
    private double drawingWidth;
    /** Current drawing height. */
    private double drawingHeight;
    /** Desired aspect ratio. */
    private double dar;
    /** Indicates whether to expand the nodes in the end. */
    private boolean expandNodes;

    //////////////////////////////////////////////////////////////////
    // Constructor
    /**
     * Creates an {@link RowFillingAndCompaction} object to execute the second iteration on.
     * 
     * @param desiredAr
     *            desired aspect ratio.
     * @param expandNodes
     *            indicates whether to expand the nodes in the end.
     */
    public RowFillingAndCompaction(final double desiredAr, final boolean expandNodes) {
        this.dar = desiredAr;
        this.expandNodes = expandNodes;
    }

    //////////////////////////////////////////////////////////////////
    // Starting method.
    /**
     * Placement of the rectangles given by {@link ElkNode} inside the given bounding box.
     * 
     * @param rectangles
     *            given set of rectangles to be placed inside the bounding box.
     * @param boundingBoxWidth
     *            width of the given bounding box.
     * @param nodeNodeSpacing
     *            The spacing between two nodes.
     * @return Drawing data for a produced drawing.
     */
    public DrawingData start(final List<ElkNode> rectangles, final double boundingBoxWidth, final boolean compaction, final double nodeNodeSpacing) {
        List<RectRow> rows = InitialPlacement.place(rectangles, boundingBoxWidth, nodeNodeSpacing);

        // Compaction steps
        if (compaction) {
            for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
                int runs = 0;
                if (rowIdx != 0) {
                    RectRow previousRow = rows.get(rowIdx - 1);
                    rows.get(rowIdx).setY(previousRow.getY() + previousRow.getHeight());
                }
                boolean somethingChanged = true;
                while (somethingChanged && runs < 1000) {
                    somethingChanged = Compaction.compact(rowIdx, rows, boundingBoxWidth, nodeNodeSpacing);
                    System.out.println("Compact is " + somethingChanged + " in run " + runs);
                    runs++;
                }
                adjustHeight(rows.get(rowIdx));
            }
        }
        calculateDimensions(rows);
//
//        // expand notes if configured.
////        if (this.expandNodes) {
////            RectangleExpansion.expand(rows, this.drawingWidth, nodeNodeSpacing);
////        }

        return new DrawingData(this.dar, this.drawingWidth, this.drawingHeight, DrawingDataDescriptor.WHOLE_DRAWING);
    }

    /**
     * @param rectRow
     */
    private void adjustHeight(RectRow rectRow) {
        double maxHeight = 0;
        for (Block block : rectRow.getChildren()) {
            maxHeight = Math.max(maxHeight, block.getHeight());
        }
        rectRow.setHeight(maxHeight);
    }

    //////////////////////////////////////////////////////////////////
    // Helping method.

    /**
     * Calculates the maximum width and height for the given list of {@link RectRow}s.
     */
    private void calculateDimensions(final List<RectRow> rows) {
        // new calculation of drawings dimensions.
        double maxWidth = Double.MIN_VALUE;
        double newHeight = 0;
        for (RectRow row : rows) {
            maxWidth = Math.max(maxWidth, row.getWidth());
            newHeight += row.getHeight();
        }

        this.drawingHeight = newHeight;
        this.drawingWidth = maxWidth;
    }

    //////////////////////////////////////////////////////////////////
    // Helping enumerate.
    /**
     * Enumerate to identify a row-filling strategy.
     */
    protected enum RowFillStrat {
        /** Fill row with whole stacks. */
        WHOLE_STACK,
        /** Fill row with single rectangles. */
        SINGLE_RECT;
    }
}
