/*******************************************************************************
 * Copyright (c) 2016 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.autagent.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * @author BREDEX GmbH
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.jubula.autagent.i18n.messages"; //$NON-NLS-1$


    public static String AcceptedConnectionFrom;
    public static String AcceptingFailed;
    public static String AddressAlreadyInUse;
    public static String AutConnectionError;
    public static String AutInOMM;
    public static String AutStartError;
    public static String AutsSysError;
    public static String AutsSysOut;
    public static String CloseMenu;
    public static String CloseWindowQuestion;
    public static String CloseWindowTitle;
    public static String CommunicationError;
    public static String ComponentIdentifier;
    public static String ComponentName;
    public static String ComponentType;
    public static String ConnectionClosed;
    public static String ConnectionErrorInServer;
    public static String CopyToClipboard;
    public static String FileMenu;
    public static String InterruptedThread;
    public static String InvalidArguments;
    public static String InvalidNumOfArguments;
    public static String InvalidJDK;
    public static String IoException;
    public static String ListeningToPort;
    public static String NoAutInOMM;
    public static String ObjectMappingMenu;
    public static String ObjectMappingOpen;
    public static String ObjectMappingWindowMenu;
    public static String ObservingInterrupted;
    public static String OMPaneTitle;
    public static String OMSettingsClose;
    public static String OMSettingsSave;
    public static String OMSettingsText;
    public static String OMSettingsTitle;
    public static String ProcessMustNotBeNull;
    public static String RegistationSendingError;
    public static String RegularTermination;
    public static String RunningVM;
    public static String SecuritiViolation;
    public static String SecuritiViolationWhileGettingHostName;
    public static String SendingMessageFailed;
    public static String SendMessageToClient;
    public static String SettingsMenu;
    public static String StartMenu;
    public static String StopMenu;
    public static String TerminatedProcess;
    public static String UnknowExitCode;
    public static String UnknowIteClient;
    public static String UnsupportedClass;
    public static String VersionException;
    public static String VmStopped;

    /**
     * initialize resource bundle
     */
    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    /**
     * Constructor
     */
    private Messages() {
        // hide
    }
}
