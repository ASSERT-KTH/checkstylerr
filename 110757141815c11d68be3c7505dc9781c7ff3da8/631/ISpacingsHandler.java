/*******************************************************************************
 * Copyright (c) 2017 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.common.compaction.oned;

/**
 * An implementation of this class is able to report both the vertical and the horizontal spacing
 * between any pair of {@link CNode}s. Different implementations of {@link CNode}s may have
 * different special requirements. In such a case a special spacings handler should be implemented.
 * For a default implementation, use the {@link #DEFAULT_SPACING_HANDLER}. It returns for either spacing
 * the maximum of the two spacings returned by the nodes (e.g. {@link CNode#getVerticalSpacing()}).

 */
public interface ISpacingsHandler {

    /**
     * @param cNode1
     *            the first involved node.
     * @param cNode2
     *            the second involved node.
     * @return the horizontal spacing that should be preserve between the two passed nodes.
     */
    double getHorizontalSpacing(CNode cNode1, CNode cNode2);

    /**
     * @param cNode1
     *            the first involved node.
     * @param cNode2
     *            the second involved node.
     * @return the vertical spacing that should be preserved between the two passed nodes.
     */
    double getVerticalSpacing(CNode cNode1, CNode cNode2);

    /**
     * A default implementation, returning <b>no</b> spacing in either direction.
     */
    ISpacingsHandler DEFAULT_SPACING_HANDLER = new ISpacingsHandler() {
        @Override
        public double getHorizontalSpacing(final CNode cNode1, final CNode cNode2) {
            return 0;
        }

        @Override
        public double getVerticalSpacing(final CNode cNode1, final CNode cNode2) {
            return 0;
        }
    };
}
