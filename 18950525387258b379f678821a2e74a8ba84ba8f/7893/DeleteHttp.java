/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.admin.cli;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.glassfish.internal.api.Target;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Protocols;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.web.admin.LogFacade;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Delete Protocol command
 */
@Service(name = "delete-http")
@PerLookup
@I18n("delete.http")

@org.glassfish.api.admin.ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=Protocol.class,
        opType=RestEndpoint.OpType.DELETE,
        path="delete-http",
        description="Delete",
        params={
            @RestParam(name="protocol", value="$parent")
        })
})
public class DeleteHttp implements AdminCommand {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    @Param(name = "protocolname", primary = true)
    String protocolName;
    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    String target;
    Protocol protocolToBeRemoved = null;
    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;
    @Inject
    Domain domain;
    @Inject
    ServiceLocator services;

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and
     * the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        Target targetUtil = services.getService(Target.class);
        Config newConfig = targetUtil.getConfig(target);
        if (newConfig!=null) {
            config = newConfig;
        }
        ActionReport report = context.getActionReport();
        NetworkConfig networkConfig = config.getNetworkConfig();
        Protocols protocols = networkConfig.getProtocols();
        try {
            for (Protocol protocol : protocols.getProtocol()) {
                if (protocolName.equalsIgnoreCase(protocol.getName())) {
                    protocolToBeRemoved = protocol;
                }
            }
            if (protocolToBeRemoved == null) {
                report.setMessage(MessageFormat.format(rb.getString(LogFacade.DELETE_HTTP_NOTEXISTS), protocolName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
            // check if the protocol whose http-redirect to be deleted is being used by
            // any network listener, then do not delete it.
            List<NetworkListener> nwlsnrList =
                protocolToBeRemoved.findNetworkListeners();
            for (NetworkListener nwlsnr : nwlsnrList) {
                if (protocolToBeRemoved.getName().equals(nwlsnr.getProtocol())) {
                    report.setMessage(
                            MessageFormat.format(
                                    rb.getString(LogFacade.DELETE_PROTOCOL_BEING_USED),
                                    protocolName,
                                    nwlsnr.getName()));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
            ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                public Object run(Protocol param) {
                    param.setHttp(null);
                    return null;
                }
            }, protocolToBeRemoved);

        } catch (TransactionFailure e) {
            report.setMessage(
                    MessageFormat.format(rb.getString(LogFacade.DELETE_HTTP_REDIRECT_FAIL), protocolName) +
                            e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
