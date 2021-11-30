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
package org.eclipse.jubula.client.wiki.ui.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * I18n class
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.jubula.client.wiki.ui.i18n.messages"; //$NON-NLS-1$

    public static String EditDescriptionDialogDescription;
    public static String EditDescriptionDialogPreview;
    public static String EditDescriptionDialogSourceViewer;
    public static String EditDescriptionDialogTitle;
    public static String EditDescriptionDialogHeader;
    public static String NoDescriptionAvailable;
    public static String OpenDescriptionViewTitle;
    public static String OpenDescriptionViewQuestion;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    
    /** private constructor */
    private Messages() {
    }
}
