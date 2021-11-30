/*******************************************************************************
 * Copyright (c) 2004, 2012 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.client.teststyle.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * @author BREDEX GmbH
 * @created 10.12.2010
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.jubula.client.teststyle.i18n.messages"; //$NON-NLS-1$
    
    public static String AtrributeNotFoundExceptionText;
    public static String ContextCategoryName;
    public static String ContextCategoryDescription;
    public static String ContextCapName;
    public static String ContextCapDescription;
    public static String ContextCentralTestDataName;
    public static String ContextCentralTestDataDescription;
    public static String ContextCommentName;
    public static String ContextCommentDescription;
    public static String ContextComponentName;
    public static String ContextComponentDescription;
    public static String ContextEventHandlerName;
    public static String ContextEventHandlerDescription;
    public static String ContextExecTestCaseName;
    public static String ContextExecTestCaseDescription;
    public static String ContextOMCategoryName;
    public static String ContextOMCategoryDescription;
    public static String ContextProjectName;
    public static String ContextProjectDescription;
    public static String ContextSpecTestCaseName;
    public static String ContextSpecTestCaseDescription;
    public static String ContextTestJobName;
    public static String ContextTestJobDescription;
    public static String ContextTestResultSummaryNodeName;
    public static String ContextTestResultSummaryNodeDescription;
    public static String ContextTestSuiteName;
    public static String ContextTestSuiteDescription;
    public static String EditAttributeColumnValue;
    public static String EditAttributeColumnDescription;
    public static String EditAttributeDialogTitle;
    public static String EditContextDialogTitle;
    public static String EditContextColumnName;
    public static String EditContextColumnDescription;
    public static String PropertyFullGroup;
    public static String PropertyEnableRadio;
    public static String PropertyEditGroup;
    public static String PropertyButtonAttribute;
    public static String PropertyButtonContext;
    public static String PropertyButtonSelectAll;
    public static String PropertyButtonDeselectAll;
    public static String PropertyLabelDescription;
    public static String QuickfixOpenTestCase;
    public static String QuickfixOpenTestSuite;
    public static String QuickfixOpenCTDEditor;
    public static String QuickfixSelectCategory;
    public static String QuickfixOpenTestJob;
    public static String TestStyleRunningOperation;

    

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
