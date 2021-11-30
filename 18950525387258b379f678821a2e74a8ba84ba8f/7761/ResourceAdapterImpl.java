/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.gjc.spi;

import com.sun.logging.LogDomains;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.resource.NotSupportedException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.AuthenticationMechanism;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.Connector;
import jakarta.resource.spi.UnavailableException;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

/**
 * <code>ResourceAdapterImpl</code> implementation for Generic JDBC Connector.
 *
 * @author Evani Sai Surya Kiran
 * @version 1.0, 02/08/05
 */
@Connector(
    description = "Resource adapter wrapping implementation of driver",
    displayName = "Resource Adapter",
    vendorName = "Sun Microsystems",
    eisType = "Database",
    version = "1.0",
    authMechanisms = {
        @AuthenticationMechanism(authMechanism="BasicPassword",
            credentialInterface=AuthenticationMechanism.CredentialInterface.PasswordCredential)
    }
)
public class ResourceAdapterImpl implements jakarta.resource.spi.ResourceAdapter {
    private static ResourceAdapterImpl ra;
    private BootstrapContext bootstrapContext;
    private Timer timer;
    private static Logger _logger = LogDomains.getLogger(ResourceAdapterImpl.class, LogDomains.RSR_LOGGER);

    public ResourceAdapterImpl() {
        if(ra == null){
            //we do not expect RA to be initialized multiple times as this is a System RAR
            ra = this;
        }
    }

    public static ResourceAdapterImpl getInstance() {
        if(ra == null) {
            throw new IllegalStateException("ResourceAdapter not initialized");
        }
        return ra;
    }

    /**
     * Empty method implementation for endpointActivation
     * which just throws <code>NotSupportedException</code>
     *
     * @param mef <code>MessageEndpointFactory</code>
     * @param as  <code>ActivationSpec</code>
     * @throws <code>NotSupportedException</code>
     *
     */
    public void endpointActivation(MessageEndpointFactory mef, ActivationSpec as) throws NotSupportedException {
        throw new NotSupportedException("This method is not supported for this JDBC connector");
    }

    /**
     * Empty method implementation for endpointDeactivation
     *
     * @param mef <code>MessageEndpointFactory</code>
     * @param as  <code>ActivationSpec</code>
     */
    public void endpointDeactivation(MessageEndpointFactory mef, ActivationSpec as) {

    }

    /**
     * Empty method implementation for getXAResources
     * which just throws <code>NotSupportedException</code>
     *
     * @param specs <code>ActivationSpec</code> array
     * @throws <code>NotSupportedException</code>
     *
     */
    public XAResource[] getXAResources(ActivationSpec[] specs) throws NotSupportedException {
        throw new NotSupportedException("This method is not supported for this JDBC connector");
    }

    /**
     * Empty implementation of start method
     *
     * @param ctx <code>BootstrapContext</code>
     */
    public void start(BootstrapContext ctx) {
        this.bootstrapContext = ctx;
    }

    /**
     * Empty implementation of stop method
     */
    public void stop() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Cancelling the timer");
        }
        if(timer != null) {
            timer.purge();
            timer.cancel();
        }
    }

    public Timer getTimer() {
        if(bootstrapContext != null) {
            if (timer == null) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Creating the timer");
                }
                try {
                    timer = bootstrapContext.createTimer();
                } catch (UnavailableException ex) {
                    _logger.log(Level.SEVERE, "jdbc-ra.timer_creation_exception", ex.getMessage());
                }
            }
        }
        return timer;
    }
}
