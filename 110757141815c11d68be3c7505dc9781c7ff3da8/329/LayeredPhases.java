/*******************************************************************************
 * Copyright (c) 2017 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.layered;

/**
 * All phases the layered approach is divided into.
 */
public enum LayeredPhases {
    
    /** Elimination of cycles through edge reversal. */
    P1_CYCLE_BREAKING,
    
    /** Division of nodes into distinct layers. */
    P2_LAYERING,
    
    /** Computation of an order of nodes in each layer, usually to reduce crossings. */
    P3_NODE_ORDERING,
    
    /** Assignment of y coordinates. */
    P4_NODE_PLACEMENT,
    
    /** Edge routing and assignment of x coordinates. */
    P5_EDGE_ROUTING;
    
}
