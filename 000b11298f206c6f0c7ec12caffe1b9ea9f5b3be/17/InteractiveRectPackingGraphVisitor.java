/*******************************************************************************
 * Copyright (c) 2020 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0  
 *******************************************************************************/
package org.eclipse.elk.alg.rectpacking;

import org.eclipse.elk.alg.rectpacking.options.RectPackingOptions;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.util.IGraphElementVisitor;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkNode;

/**
 * Graph visitor which visits only the root node and recursively steps through the graph
 * to set interactive options for the rectpacking algorithm.
 * The rectpacking algorithm needs {@code Interactive} to be set to true in all root nodes.
 */
public class InteractiveRectPackingGraphVisitor implements IGraphElementVisitor {

    /* (non-Javadoc)
     * @see org.eclipse.elk.core.util.IGraphElementVisitor#visit(org.eclipse.elk.graph.ElkGraphElement)
     */
    @Override
    public void visit(final ElkGraphElement element) {
        // Only apply to root of the graph
        if (element instanceof ElkNode) {
            ElkNode root = (ElkNode) element;
            setInteractiveOptions(root);
        }
    }
    
    /**
     * Sets the required options for the interactive layout run with the {@code rectpacking} algorithm.
     */
    public void setInteractiveOptions(final ElkNode root) {
        String algorithm = root.getProperty(CoreOptions.ALGORITHM);
        if (algorithm != null && RectPackingOptions.ALGORITHM_ID.endsWith(algorithm) && !root.getChildren().isEmpty()) {
            root.setProperty(RectPackingOptions.INTERACTIVE, true);
        }
        return;
    }

}
