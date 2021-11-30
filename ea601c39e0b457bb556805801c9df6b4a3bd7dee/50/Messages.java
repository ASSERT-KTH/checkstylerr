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
package org.eclipse.jubula.app.dbtool.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * @author BREDEX GmbH
 * @created 10.12.2010
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.jubula.app.dbtool.i18n.messages"; //$NON-NLS-1$
    
    public static String DBToolCreateNewVersion;
    public static String DBToolCreateNewVersionFailed;
    public static String DBToolDelete;
    public static String DBToolPerforming;
    public static String DBToolDeleteAll;
    public static String DBToolDeleteFailed;
    public static String DBToolDeleteFinished;
    public static String DBToolDeleteAllTRDetailsOlder;
    public static String DBToolDeleteProjectTRDetailsOlder;
    public static String DBToolDeleteTRDetailsOfProject;
    public static String DBToolDeleteTRDetailsVersion;
    public static String DBToolDeleteTRDetailsVersionDay;
    public static String DBToolDeleteKeepSummary;
    public static String DBToolDeletingProject;
    public static String DBToolDeleteDays;
    public static String DBToolDeletingTRDetails;
    public static String DBToolDeletingTRDetailsFinished;
    public static String DBToolDeletingTRSummaries;
    public static String DBToolDeletingTRSummariesFinished;
    public static String DBToolDeleteTRSummariesOlder;
    public static String DBToolDeleteTRSummariesOfProject;
    public static String DBToolDeleteTRSummariesVersion;
    public static String DBToolDeleteTRSummariesVersionDay;
    public static String DBToolDeleteProjectTRSummariesOlder;
    public static String DBToolProjectNameNotDefinedForVersion;
    public static String DBToolDeletingAllProjects;
    public static String DBToolDir;
    public static String DBToolExport;
    public static String DBToolExportAll;
    public static String DBToolExportAllFailed;
    public static String DBToolImport;
    public static String DBToolInvalidExportDirectory;
    public static String DBToolInvalidVersion;
    public static String DBToolExistingProject;
    public static String DBToolMissingProject;
    public static String DBToolInvalidDays;
    public static String DBToolName;
    public static String DBToolNonEmptyExportDirectory;
    public static String ExecutionControllerInvalidDBDataError;

    
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
