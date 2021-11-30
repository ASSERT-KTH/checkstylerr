/*******************************************************************************
 * Copyright (c) 2015 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.examples.extension.swing;

import org.eclipse.jubula.communication.CAP;
import org.eclipse.jubula.toolkit.CapBuilder;
import org.eclipse.jubula.toolkit.ToolkitInfo;
import org.eclipse.jubula.tools.ComponentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author BREDEX GmbH
 */
public class JSliderComponents {
    /** the logger */
    private static Logger log = LoggerFactory
            .getLogger(JSliderComponents.class);
    
    /** the component class name */
    public static final String COMPONENT_CLASS_NAME = "javax.swing.JSlider"; //$NON-NLS-1$
    /** the tester class name */
    public static final String TESTER_CLASS_NAME = "org.eclipse.jubula.ext.rc.swing.tester.JSliderTester"; //$NON-NLS-1$
    /** the extended toolkit information */
    private ToolkitInfo m_toolkitInfo = null;
    
    public JSliderComponents(ToolkitInfo toolkit){
        setToolkitInfo(toolkit);

        String registerTesterClass = getToolkitInfo().registerTesterClass(
                COMPONENT_CLASS_NAME, TESTER_CLASS_NAME);
        String extensionClassName = this.getClass().getName();
        if (registerTesterClass != null) {
            if (TESTER_CLASS_NAME.equals(registerTesterClass)) {
                log.warn(extensionClassName
                        + " extension has already been registered."); //$NON-NLS-1$
            } else {
                log.info(extensionClassName
                        + " extension has been registered and replaced the previous extension: " //$NON-NLS-1$
                        + registerTesterClass);
            }
        } else {
            log.info(extensionClassName + " extension has been registered."); //$NON-NLS-1$
        }
    }

    public ToolkitInfo getToolkitInfo() {
        return m_toolkitInfo;
    }

    public void setToolkitInfo(ToolkitInfo m_toolkitInfo) {
        this.m_toolkitInfo = m_toolkitInfo;
    }
    
    public CAP verifyLabelExists(ComponentIdentifier<?> ci, boolean value){
        return new CapBuilder("rcVerifLabelsExists") //$NON-NLS-1$
            .setDefaultMapping(false)
            .setComponentIdentifier(ci)
            .addParameter(value)
            .build();
    }
}
