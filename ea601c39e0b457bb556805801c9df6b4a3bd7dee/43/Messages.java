/*******************************************************************************
 * Copyright (c) 2014 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.client.api.converter.ui.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * @author BREDEX GmbH
 * @created 27.10.2014
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.jubula.client.api.converter.ui.i18n.messages"; //$NON-NLS-1$

    public static String ConvertProjectTaskName;
    public static String DuplicateNode;
    public static String ErrorWhileConverting;
    public static String InputDialogName;
    public static String InputDialogMessage;
    public static String InvalidNode;
    public static String InvalidNodeName;
    public static String InvalidPackageName;
    public static String NoAutInProject;
    public static String NoPackageNameSpecified;
    public static String PreparingConvertProjectTaskName;

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
