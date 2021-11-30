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
package org.eclipse.jubula.autagent.common.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jubula.tools.internal.constants.AutConfigConstants;
import org.eclipse.jubula.tools.internal.constants.CommandConstants;
import org.eclipse.jubula.tools.internal.constants.StringConstants;
import org.eclipse.jubula.tools.internal.utils.MonitoringUtil;

/**
 * @author BREDEX GmbH
 * @created September 25, 2013
 * 
 */
public class StartJavaFXAutServerCommand extends AbstractStartJavaAutServer {
    /** the classpath of the AUT Server */
    private String m_autServerClasspath = "AutServerClasspath"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    protected String[] createCmdArray(String baseCmd, 
        Map<String, String> parameters) {
        List<String> cmds = new Vector<String>();
        cmds.add(baseCmd);

        StringBuffer autServerClasspath = new StringBuffer();
        createServerClasspath(autServerClasspath);

        List<String> autAgentArgs = new ArrayList<String>();
        autAgentArgs.add(String.valueOf(parameters
                .get(AutConfigConstants.AUT_AGENT_HOST)));
        autAgentArgs.add(String.valueOf(parameters
                .get(AutConfigConstants.AUT_AGENT_PORT)));
        autAgentArgs.add(String.valueOf(parameters
                .get(AutConfigConstants.AUT_NAME)));

        if (!isRunningFromExecutable(parameters)) {
            createAutServerLauncherClasspath(cmds, autServerClasspath,
                    parameters);
            createAutServerClasspath(autServerClasspath, cmds, parameters);
            cmds.addAll(autAgentArgs);
            // information for AUT server that agent is not used
            cmds.add(CommandConstants.RC_COMMON_AGENT_INACTIVE);
        } else {
            String serverBasePath = createServerBasePath();
            autServerClasspath.append(PATH_SEPARATOR).append(serverBasePath)
                    .append(PATH_SEPARATOR).append(getRcBundleClassPath());
            m_autServerClasspath = autServerClasspath.toString();

        }
        if (Boolean.parseBoolean(parameters.get(AutConfigConstants.AUT_JAVA9_SUPPORT))) {
            cmds.add("--illegal-access=permit"); //$NON-NLS-1$
            cmds.add("--add-opens=javafx.controls/javafx.scene.control.skin=ALL-UNNAMED"); //$NON-NLS-1$
        }
        cmds.addAll(createAutArguments(parameters));
        return cmds.toArray(new String[cmds.size()]);
    }

    /**
     * {@inheritDoc}
     */
    protected String getServerClassName() {
        return CommandConstants.AUT_JAVAFX_SERVER;
    }

    /**
     * {@inheritDoc}
     */
    public String getRcBundleId() {
        return CommandConstants.RC_JAVAFX_BUNDLE_ID;
    }

    @Override
    protected String[] createEnvArray(Map<String, String> parameters, 
        boolean isAgentSet) {
        if (isRunningFromExecutable(parameters) 
                || MonitoringUtil.shouldAndCanRunWithMonitoring(parameters)) {
            setEnv(parameters, m_autServerClasspath);
            boolean agentActive = true;
            return super.createEnvArray(parameters, agentActive);
        }       
          
        return super.createEnvArray(parameters, isAgentSet);
    }

    /**
     * 
     * @return the class path corresponding to the receiver's RC bundle.
     */
    protected String getRcBundleClassPath() {
        List<String> classList = new ArrayList<String>();
        classList.add(getClasspathForBundleId(getRcBundleId()));
        classList.add(getClasspathForBundleId(
                CommandConstants.RC_JAVAFX_J8U40_BUNDLE_ID));
        return AbstractStartToolkitAut.createClassPath(
                classList.toArray(new String[classList.size()]));
    }

    @Override
    protected String getMainClassFromManifest(Map parameters) {
        String jarFile = createAbsoluteJarPath(parameters);
        String attr = getAttributeFromManifest("main-class", jarFile); //$NON-NLS-1$
        /*
         * Replacing "/" with "." because, in the Manifest file of an
         * Application that was build with JavaFX-Ant-Tasks, the path to the
         * JavaFX loader class has slashes but for the Classloader we need the
         * qualified class name with dots.
         */
        if (attr != null) {
            return attr.replace(StringConstants.SLASH, StringConstants.DOT);
        }
        return null;
    }
}
