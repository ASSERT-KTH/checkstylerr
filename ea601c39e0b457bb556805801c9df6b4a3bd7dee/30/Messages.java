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
package org.eclipse.jubula.app.autagent.i18n;

import org.eclipse.osgi.util.NLS;
    

/**
 * I18n class
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.jubula.app.autagent.i18n.messages"; //$NON-NLS-1$

    public static String AUTAgentNotFound;
    public static String CommandlineOptionHelp;
    public static String CommandlineOptionLenient;
    public static String CommandlineOptionPort;
    public static String CommandlineOptionQuiet;
    public static String CommandlineOptionOMM;
    public static String CommandlineOptionStart;
    public static String CommandlineOptionVerbose;
    public static String InfoDefaultPort;
    public static String IOExceptionNotOpenSocket;
    public static String NullPointerExceptionNoCommandLine;
    public static String NumberFormatException;
    public static String NumberFormatExceptionInvalidValue;
    public static String OptionStopDescription;
    public static String ParseExceptionInvalidOption;
    public static String SecurityExceptionViolation;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    
    /**
     * private constructor
     */
    private Messages() {
    }
}
