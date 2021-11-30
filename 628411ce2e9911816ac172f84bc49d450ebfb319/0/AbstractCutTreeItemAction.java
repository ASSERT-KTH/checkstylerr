/*******************************************************************************
 * Copyright (c) 2004, 2010 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.client.ui.rcp.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

/**
 * Abstract base class for implementations of the Cut action.
 *
 * @author BREDEX GmbH
 * @created 18.03.2008
 */
public abstract class AbstractCutTreeItemAction extends Action {

    /**
     * Constructor
     */
    public AbstractCutTreeItemAction() {
                super();
        IWorkbenchAction cutAction = ActionFactory.CUT.create(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow());

        setText(cutAction.getText());
        setToolTipText(cutAction.getToolTipText());
        setImageDescriptor(cutAction.getImageDescriptor());
        setDisabledImageDescriptor(cutAction.getDisabledImageDescriptor());
        setId(cutAction.getId());
        setActionDefinitionId(cutAction.getActionDefinitionId());

        cutAction.dispose();
    }
    
}
