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
package org.eclipse.jubula.toolkit.common.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * @author BREDEX GmbH
 * @created 10.12.2010
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.jubula.toolkit.common.i18n.messages"; //$NON-NLS-1$
    
    public static String ComponenConfigurationNotFound;
    public static String CouldNotCreateToolkitProvider;
    public static String CouldNotResolvePath;
    public static String ErrorWhileReadingAttributes;
    public static String FailedLoading;
    public static String NoAutConfigFound;
    public static String NoI18n;
    public static String NoToolkitPluginDescriptorFound;
    public static String OfPlugin;
    public static String ResourceBundleAvailable;
    public static String ToolkitNameIsNull;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    
    /**
     * Constructor
     */
    private Messages() {
        super();
    }
    
}
