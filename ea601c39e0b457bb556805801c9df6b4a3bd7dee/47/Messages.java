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
package org.eclipse.jubula.app.testexec.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * @author BREDEX GmbH
 * @created 10.12.2010
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.jubula.app.testexec.i18n.messages"; //$NON-NLS-1$
    
    public static String ConnectingToAUTAgent;
    public static String ConnectionToAUTAgentFailed;
    public static String ClientNameShort;
    public static String ErrorMessageAUT_TOOLKIT_NOT_AVAILABLE;
    public static String ErrorWhileInitializingTestResult;
    public static String ErrorWhileStoppingAUT;
    public static String EventHandler;
    public static String ExecutionControllerAbort;
    public static String ExecutionControllerAUT;
    public static String ExecutionControllerAUTIsPossiblyStarted;
    public static String ExecutionControllerAUTStart;
    public static String ExecutionControllerAUTStartUnknownHost;
    public static String ExecutionControllerAUTStartVersionConflict;
    public static String ExecutionControllerAUTStartFailed;
    public static String ExecutionControllerCouldNotConnectToAUTWithAutrun;
    public static String ExecutionControllerAUTConnectionEstablished;
    public static String ExecutionControllerAUTStartError;
    public static String ExecutionControllerAUTConnectionLost;
    public static String ExecutionControllerAUTDisconnected;
    public static String ExecutionControllerDatabase;
    public static String ExecutionControllerDataBaseEnd;
    public static String ExecutionControllerDatabaseStart;
    public static String ExecutionControllerDotNetInstallProblem;
    public static String ExecutionControllerInvalidDataError;
    public static String ExecutionControllerInvalidDBDataError;
    public static String ExecutionControllerInvalidDBschemeError;
    public static String ExecutionControllerInvalidJarError;
    public static String ExecutionControllerInvalidJREError;
    public static String ExecutionControllerInvalidJDKError;
    public static String ExecutionControllerInvalidMainError;
    public static String ExecutionControllerLoadingProject;
    public static String ExecutionControllerLogPathError;
    public static String ExecutionControllerNoRunExecutionBegin;
    public static String ExecutionControllerProjectCompleteness;
    public static String ExecutionControllerProjectCompletenessFailed;
    public static String ExecutionControllerProjectLoaded;
    public static String ExecutionControllerResultNameError;
    public static String ExecutionControllerServer;
    public static String ExecutionControllerServerNotInstantiated;
    public static String ExecutionControllerTestJobBegin;
    public static String ExecutionControllerTestJobExecutedTestSuites;
    public static String ExecutionControllerTestJobExpectedTestSuites;
    public static String ExecutionControllerTestJobUnsuccessfulTestSuites;
    public static String ExecutionControllerTestSuiteCompleteness;
    public static String ExecutionControllerTestSuiteCompletenessOk;
    public static String ExecutionControllerTestSuiteCompletenessNOk;
    public static String ExecutionControllerTestSuiteCompletenessNOkSkipp;
    public static String ExecutionControllerTestSuiteBegin;
    public static String ExecutionControllerTestSuiteEnd;
    public static String ReceivedShutdownCommand;
    public static String RetryStep;
    public static String Step;
    public static String TestCase;
    public static String TestSuite;
    public static String Conditional;
    public static String Iterate;
    public static String DoWhile;
    public static String WhileDo;
    public static String Container;
    public static String WatchdogTimer;
    
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
