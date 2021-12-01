/*******************************************************************************
 * Copyright (c) 2009, 2015 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.conn.gmf;

import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.CreateEditPoliciesOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.IEditPolicyProvider;

/**
 * The edit policy provider for the <i>apply layout</i> edit policy.
 * 
 * @author haf
 */
public class LayoutEditPolicyProvider extends AbstractProvider implements
        IEditPolicyProvider {

    /** the key used to install an <i>apply layout</i> edit policy. */
    public static final String APPLY_LAYOUT_ROLE = "ApplyLayoutEditPolicy";
    
    /**
     * {@inheritDoc}
     */
    public void createEditPolicies(final EditPart editPart) {
        if (editPart instanceof DiagramEditPart) {
            editPart.installEditPolicy(APPLY_LAYOUT_ROLE,
                    new GmfLayoutEditPolicy());
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean provides(final IOperation operation) {
        return operation instanceof CreateEditPoliciesOperation;
    }

}
