/*******************************************************************************
 * Copyright (c) 2010, 2015 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.core.options;

/**
 * Definition of the edge types. To be accessed using {@link CoreOptions#EDGE_TYPE}.
 * 
 * @author mri
 */
public enum EdgeType {
    
    /** no special type. */
    NONE,
    /** the edge is directed. */
    DIRECTED,
    /** the edge is undirected. */
    UNDIRECTED,
    /** the edge represents an association. */
    ASSOCIATION,
    /** the edge represents a generalization. */
    GENERALIZATION,
    /** the edge represents a dependency. */
    DEPENDENCY;
    
}
