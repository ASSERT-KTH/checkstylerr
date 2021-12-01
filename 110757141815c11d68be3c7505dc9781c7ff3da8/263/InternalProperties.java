/*******************************************************************************
 * Copyright (c) 2017 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.radial;

import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.Property;

/**
 * The internal properties of the radial layouter. Properties which are used throughout the code but doesn't need to be
 * public are stored here.
 */
public final class InternalProperties {

    /** The root node of the graph. */
    public static final IProperty<ElkNode> ROOT_NODE = new Property<ElkNode>("root");
}
