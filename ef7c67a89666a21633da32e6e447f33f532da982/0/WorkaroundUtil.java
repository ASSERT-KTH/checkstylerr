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
package org.eclipse.jubula.rc.common.util;

import org.eclipse.jubula.tools.internal.utils.EnvironmentUtils;

/**
 * This class contains utility methods for workarounds within the 
 * AUT Server component.
 *
 * @author BREDEX GmbH
 * @created Jun 23, 2009
 */
public class WorkaroundUtil {
    /**
     * <code>CHAR_9</code>
     */
    public static final char CHAR_9 = '9';
    /**
     * the lower case char <code>'b'</code>
     */
    public static final char CHAR_B = 'b';
    
    /** 
     * Name of the environment variable that defines whether the client should
     * ignore server-side timeouts that occur during test execution.
     */
    private static final String IGNORE_TIMEOUT_VAR = "TEST_RC_IGNORE_TIMEOUT"; //$NON-NLS-1$
    /** 
     * Name of the environment variable that defines whether the rc should
     * disable the highlighting of supported components
     */
    private static final String DISABLE_HIGHLIGHTING_VAR = "TEST_RC_DISABLE_HIGHLIGHTING"; //$NON-NLS-1$
    
    /**
     * Name of the environment variable that defines the value for the delay after a
     * value was selected in the ComboBox
     */
    public static final String COMBOBOX_DELAY_AFTER_SELECTION = "TEST_COMBOBOX_DELAY_AFTER_SELECTION"; //$NON-NLS-1$
    
    /**
     * Private constructor
     */
    private WorkaroundUtil() {
        // Nothing to initialize
    }
    
    /**
     * Allows server-side timeouts to be ignored. This is used, for example,
     * to work around the fact that a specific configuration of Linux/GTK/SWT
     * does not produce mouse click events for left click on a TabFolder.
     * 
     * @return <code>true</code> if server-side timeouts should be ignored. 
     *         Otherwise <code>false</code>.
     */
    public static boolean isIgnoreTimeout() {
        String value = EnvironmentUtils
                .getProcessOrSystemProperty(IGNORE_TIMEOUT_VAR);

        return Boolean.valueOf(value).booleanValue();
    }
    
    /**
     * Allows to disable the highlighting of supported components. This might be
     * necessary because it is using to much performance or behavior which
     * leads to a non usable AUT.
     * @return <code>true</code> if highlighting of supported components should
     * be disabled. Otherwise <code>false</code>.
     */
    public static boolean isHighlightingDisabled() {
        String value = EnvironmentUtils
                .getProcessOrSystemProperty(DISABLE_HIGHLIGHTING_VAR);

        return Boolean.valueOf(value).booleanValue();
    }
}
