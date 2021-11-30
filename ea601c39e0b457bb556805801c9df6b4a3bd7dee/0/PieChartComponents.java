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
package org.eclipse.jubula.examples.extension.javafx;

import org.eclipse.jubula.communication.CAP;
import org.eclipse.jubula.toolkit.CapBuilder;
import org.eclipse.jubula.toolkit.ToolkitInfo;
import org.eclipse.jubula.tools.ComponentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author BREDEX GmbH
 */
@SuppressWarnings("nls")
public class PieChartComponents {
    /** the logger */
    private static Logger log = LoggerFactory
            .getLogger(PieChartComponents.class);

    /** the component class name */
    public static final String COMPONENT_CLASS_NAME = "javafx.scene.chart.PieChart";
    /** the tester class name */
    public static final String TESTER_CLASS_NAME = "org.eclipse.jubula.ext.rc.javafx.tester.PieChartTester";
    /** the extended toolkit information */
    private ToolkitInfo m_toolkitInfo = null;

    /** Constructor */
    public PieChartComponents(ToolkitInfo toolkit) {
        setToolkitInfo(toolkit);

        String registerTesterClass = getToolkitInfo().registerTesterClass(
                COMPONENT_CLASS_NAME, TESTER_CLASS_NAME);

        String extensionClassName = this.getClass().getName();
        if (registerTesterClass != null) {
            if (TESTER_CLASS_NAME.equals(registerTesterClass)) {
                log.warn(extensionClassName
                        + " extension has already been registered.");
            } else {
                log.info(extensionClassName
                        + " extension has been registered and replaced the previous extension: "
                        + registerTesterClass);
            }
        } else {
            log.info(extensionClassName + " extension has been registered.");
        }
    }

    /**
     * @return the toolkit
     */
    public ToolkitInfo getToolkitInfo() {
        return m_toolkitInfo;
    }

    /**
     * @param toolkit
     *            the toolkit to set
     */
    private void setToolkitInfo(ToolkitInfo toolkit) {
        m_toolkitInfo = toolkit;
    }

    public CAP checkNumberOfItems(ComponentIdentifier<?> ci,
            int expectedNumberOfItems) {
        return new CapBuilder("rcVerifyNrItems")
                .setDefaultMapping(false)
                .setComponentIdentifier(ci)
                .addParameter(expectedNumberOfItems)
                .build();
    }
}