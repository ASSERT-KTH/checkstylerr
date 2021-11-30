/*******************************************************************************
 * Copyright (c) 2013 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.client.alm.mylyn.ui.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * I18n class
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.jubula.client.alm.mylyn.ui.i18n.messages"; //$NON-NLS-1$
    
    public static String TaskTitle;
    public static String TaskDescription;
    public static String BatchALMReporting;
    
    public static String InspectorTitle;
    public static String InspectorMessage;
    public static String InspectorTableAttributeID;
    public static String InspectorTableValue;
    public static String InspecotrTableOptions;
    public static String InspectorTableProperties;
    public static String InspectorTablePropertieDisabled;
    public static String InspectorTablePropertieReadOnly;
    
    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    
    /** private constructor */
    private Messages() {
    }
}
