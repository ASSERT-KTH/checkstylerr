/*******************************************************************************
 * Copyright (c) 2015 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.layered.p4nodes.bk;

/**
 * For documentation see {@link BKNodePlacer}.
 * 
 * @author uru
 */
public interface ICompactor {
    
    /**
     * In this step, actual coordinates are calculated for blocks and its nodes.
     * 
     * <p>First, all blocks are placed, trying to avoid any crossing of the blocks. Then, the blocks are
     * shifted towards each other if there is any space for compaction.</p>
     * 
     * @param bal One of the four layouts which shall be used in this step
     */
    void horizontalCompaction(BKAlignedLayout bal);
    
}
