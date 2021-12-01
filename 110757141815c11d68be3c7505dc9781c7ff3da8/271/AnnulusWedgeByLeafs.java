/*******************************************************************************
 * Copyright (c) 2017 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.radial.p1position.wedge;

import org.eclipse.elk.alg.radial.RadialUtil;
import org.eclipse.elk.graph.ElkNode;

/**
 * Determine the annulus wedge size by the number of leafs a node has.
 */
public class AnnulusWedgeByLeafs implements IAnnulusWedgeCriteria {

    /**
     * Determine the space a node needs for its wedge by calculating the number of leaves.
     */
    @Override
    public double calculateWedgeSpace(final ElkNode node) {
        return RadialUtil.getNumberOfLeaves(node);
    }

}