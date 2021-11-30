/*******************************************************************************
 * Copyright (c) 2017 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.client.core.exporter.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * @author BREDEX GmbH
 */
public class Messages extends NLS {
    
    public static String errorType;
    public static String failureType;
    public static String expectedValue;
    public static String actualValue;
    public static String parameterName;
    public static String parameterType;
    public static String parameterValue;
    public static String guidancerExpectedValue;
    public static String guidancerActualValue;
    public static String stepName;
    public static String stepStatus;
    public static String timestamp;
    public static String componentName;
    public static String componentType;
    
    /**
     * the name of the bundle
     */
    private static final String BUNDLE_NAME = 
            "org.eclipse.jubula.client.core.exporter.i18n.messages"; //$NON-NLS-1$
    
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    /**
     * Constructor
     */
    private Messages() {
        // hide
    }
    
}
