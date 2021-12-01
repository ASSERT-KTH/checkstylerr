/*******************************************************************************
 * Copyright (c) 2013, 2015 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.mrtree.options;

/**
 * Enumeration of treeifying order options.
 *
 * @author msp
 */
public enum TreeifyingOrder {
    
    /** treeify in depth-first order. */
    DFS,
    /** treeify in breadth-first order. */
    BFS;

}
