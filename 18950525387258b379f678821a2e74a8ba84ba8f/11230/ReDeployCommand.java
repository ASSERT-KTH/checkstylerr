/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.deployment.admin;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.config.support.TargetType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.common.util.admin.ParameterMapExtractor;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Inject;
import org.glassfish.hk2.api.PerLookup;

import java.util.Properties;
import java.util.Collection;
import java.util.ArrayList;
import java.io.File;
import org.glassfish.api.admin.AccessRequired;

/**
 *
 * ReDeploy command
 *
 * @author Jerome Dochez
 *
 */
@Service(name="redeploy")
@PerLookup
@I18n("redeploy.command")
@ExecuteOn(value={RuntimeType.DAS})
@TargetType(value={CommandTarget.DOMAIN, CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@AccessRequired.Delegate(DeployCommand.class)
public class ReDeployCommand extends DeployCommandParameters implements AdminCommand {

    @Inject
    CommandRunner commandRunner;

    @Inject
    Deployment deployment;

    @Param(optional=false)
    String name;

    @Param(primary=true, optional=true)
    File path = null;

    @Inject
    private ConfigBeansUtilities configBeansUtilities;

    //define this variable to skip parameter valadation.
    //Param validation will be done when referening deploy command.
    boolean skipParamValidation = true;

    private final Collection<String> excludedDeployCommandParamNames =
            initExcludedDeployCommandParamNames();

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ReDeployCommand.class);

    /**
     * Executes the command.
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        if (!validateParameters(name, report)) {
            return;
        }
        final ParameterMap paramMap;
        final ParameterMapExtractor extractor = new ParameterMapExtractor(this);
        try {
            paramMap = extractor.extract(excludedDeployCommandParamNames);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }

        paramMap.set("force", String.valueOf(true));

        CommandRunner.CommandInvocation inv = commandRunner.getCommandInvocation("deploy", report, context.getSubject());
        inv.parameters(paramMap).inbound(context.getInboundPayload()).outbound(context.getOutboundPayload()).execute();
    }

        /**
         * Validate the parameters, name and path.
         * Check if name is registered.  For redeployment, the
         * application must be previously deployed.
         * Verify that path is valid and not null.
         *
         * @param name - Application name
         * @param report - ActionReport.
         *
         * @returns true if validation successfully else return false.
         */
    boolean validateParameters(final String name, final ActionReport report) {
        if (!deployment.isRegistered(name)) {
            report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", name));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }
        else if (path == null) {
            /**
             * If path is not specified on the command line but the application
             * is not directory deployed then throw an exception since we don't
             * want to undeploy and then deploy from the domain_root.
             */
            if (!Boolean.parseBoolean(configBeansUtilities.getDirectoryDeployed(name))) {
                report.setMessage(localStrings.getLocalString("redeploy.command.cannot.redeploy","Cannot redeploy this app {0} without specify the operand.", name));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return false;
            }
        }

        //if path not specified on the command line then get it from domain.xml
        super.path = (path==null)?new File(configBeansUtilities.getLocation(name)):path;
        if (!super.path.exists()) {
                //if unable to get path from domain.xml then return error.
            report.setMessage(localStrings.getLocalString("redeploy.command.invalid.path", "Cannot determine the path of application."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }
        return true;
    }

    private Collection<String> initExcludedDeployCommandParamNames() {
        final Collection<String> result = new ArrayList<String>();
        result.add("force");
        return result;
    }

}
