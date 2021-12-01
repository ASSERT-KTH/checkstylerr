/*******************************************************************************
 * Copyright (c) 2019 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.core.debug.actions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.elk.core.debug.model.ExecutionInfo;
import org.eclipse.elk.core.debug.views.AbstractLayoutDebugView;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.program.Program;

/**
 * Action that reveals a monitor's log folder in the system explorer.
 */
public class RevealLogFolderAction extends Action {

    /** identifier string for this action. */
    private static final String ACTION_ID = "org.eclipse.elk.debug.actions.revealLogFolder";
    
    /** The layout graph view associated with this action. */
    private AbstractLayoutDebugView view;

    public RevealLogFolderAction(final AbstractLayoutDebugView theview) {
        setId(ACTION_ID);
        setText("&Reveal Log Folder");
        setToolTipText("Shows the monitor's debug output folder in the system explorer.");
        
        this.view = theview;
    }
    
    /**
     * Updates the enabled property of this action.
     */
    public void updateEnablement() {
        ExecutionInfo selectedInfo = getSingleSelectedExecutionInfoOrNull();
        if (selectedInfo == null) {
            setEnabled(false);
        } else {
            // We're enabled if the folder exists
            Path debugFolder = selectedInfo.getDebugFolder();
            setEnabled(debugFolder != null && Files.isDirectory(debugFolder));
        }
    }

    @Override
    public void run() {
        ExecutionInfo selectedInfo = getSingleSelectedExecutionInfoOrNull();
        if (selectedInfo != null && Files.isDirectory(selectedInfo.getDebugFolder())) {
            Program.launch(selectedInfo.getDebugFolder().toString());
        }
    }
    
    private ExecutionInfo getSingleSelectedExecutionInfoOrNull() {
        List<ExecutionInfo> selectedInfos = view.getSelectedExecutionInfos();
        if (selectedInfos.size() == 1) {
            return selectedInfos.get(0);
        } else {
            return null;
        }
    }

}
