/*******************************************************************************
 * Copyright (c) 2004, 2011 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.launch.ui.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * @author BREDEX GmbH
 * @created 20.04.2011
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.jubula.launch.ui.i18n.messages"; //$NON-NLS-1$
    public static String AutLaunchConfigurationTab_ActiveCheckbox_info;
    public static String AutLaunchConfigurationTab_ActiveCheckbox_label;
    public static String AutLaunchConfigurationTab_AutIdTextField_info;
    public static String AutLaunchConfigurationTab_AutIdTextField_label;
    public static String AutLaunchConfigurationTab_name;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
