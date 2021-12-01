/*******************************************************************************
 * Copyright (c) 2009, 2017 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.core.options;

/**
 * Definition of placement positions for edge labels. To be accessed using {@link CoreOptions#EDGE_LABEL_PLACEMENT}.
 */
public enum EdgeLabelPlacement {

    /** undefined label placement. */
    UNDEFINED,
    /** label is centered on the edge. */
    CENTER,
    /** label is at the head (target) of the edge. */
    HEAD,
    /** label is at the tail (source) of the edge. */
    TAIL;
    
    
    /**
     * Checks whether this edge label placement is one of the two end label placements.
     * 
     * @return {@code true} iff this is {@link #HEAD} or {@link #TAIL}.
     */
    public boolean isEndLabelPlacement() {
        return this == HEAD || this == TAIL;
    }
    
}
