/*******************************************************************************
 * Copyright (c) 2016 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.core.service;

import org.eclipse.elk.core.util.IElkProgressMonitor;

/**
 * Listener interface for automatic layout done through {@link DiagramLayoutEngine}. Instances can be
 * registered in {@link LayoutConnectorsService}.
 */
public interface ILayoutListener {
    
    /**
     * Called when layout is about to be executed.
     */
    void layoutAboutToStart(LayoutMapping mapping, IElkProgressMonitor progressMonitor);
    
    /**
     * Called when layout has been executed.
     */
    void layoutDone(LayoutMapping mapping, IElkProgressMonitor progressMonitor);

}
