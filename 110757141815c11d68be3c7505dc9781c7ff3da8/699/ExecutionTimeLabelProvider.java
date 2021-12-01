/*******************************************************************************
 * Copyright (c) 2009, 2019 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.core.debug.views.time;

import org.eclipse.elk.core.debug.ElkDebugPlugin;
import org.eclipse.elk.core.debug.model.ExecutionInfo;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for executions.
 */
final class ExecutionTimeLabelProvider extends LabelProvider implements IStyledLabelProvider {
    
    /** What the label provider can display. */
    public static enum DisplayMode { NAME, TIME_TOTAL, TIME_LOCAL };

    /** Path to the image used for elements. */
    private static final String IMAGE_PATH = "/icons/execution.gif";
    
    /** The image used for each element. */
    private Image elementImage;
    /** What we should display. */
    private DisplayMode displayMode;
    

    /**
     * Creates an execution label provider.
     * 
     * @param displayMode
     *            What this label provider should display.
     */
    public ExecutionTimeLabelProvider(final DisplayMode displayMode) {
        elementImage = ElkDebugPlugin.imageDescriptorFromPlugin(ElkDebugPlugin.PLUGIN_ID, IMAGE_PATH).createImage();
        this.displayMode = displayMode;
    }
    
    @Override
    public Image getImage(final Object element) {
        if (displayMode == DisplayMode.NAME && element instanceof ExecutionInfo) {
            return elementImage;
        } else {
            return null;
        }
    }

    @Override
    public StyledString getStyledText(Object element) {
        if (element instanceof ExecutionInfo) {
            ExecutionInfo execution = (ExecutionInfo) element;
            
            switch (displayMode) {
            case NAME:
                return new StyledString(execution.getName());
                
            case TIME_TOTAL:
                if (execution.isExecutionTimeMeasured()) {
                    return new StyledString(timeToString(execution.getExecutionTimeIncludingChildren()));
                } else {
                    return new StyledString("–");
                }
                
            case TIME_LOCAL:
                if (execution.isExecutionTimeMeasured()) {
                    return execution.getChildren().isEmpty()
                            ? new StyledString("")
                            : new StyledString(timeToString(execution.getExecutionTimeLocal()));
                } else {
                    return new StyledString("");
                }
            }
        }
        
        return null;
    }
    
    /**
     * Convert the given time (in seconds) into a string.
     */
    private String timeToString(final double time) {
        // SUPPRESS CHECKSTYLE NEXT MagicNumber
        return String.format("%1$.3f", time * 1000);
    }

    @Override
    public void dispose() {
        super.dispose();
        
        if (elementImage != null) {
            elementImage.dispose();
            elementImage = null;
        }
    }

}
