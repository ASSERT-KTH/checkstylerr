/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.glassfish.bootstrap.osgi;

import com.sun.enterprise.glassfish.bootstrap.GlassFishRuntimeDecorator;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

/**
 * This is a special implementation used in non-embedded environment. It assumes that it has launched the
 * framework during bootstrap and hence can stop it upon shutdown.
 * It also creates a specialized GlassFishImpl called {@link OSGiGlassFishImpl}
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiGlassFishRuntime extends GlassFishRuntimeDecorator {

    // cache the value, because we can't use bundleContext after this bundle is stopped.
    private volatile Framework framework; // system bundle is the framework

    public OSGiGlassFishRuntime(GlassFishRuntime embeddedGfr, final Framework framework) {
        super(embeddedGfr);
        this.framework = framework;
    }

    @Override
    public void shutdown() throws GlassFishException {
        if (framework == null) {
            return; // already shutdown
        }
        try {
            super.shutdown();

            framework.stop();
            framework.waitForStop(0);
        } catch (InterruptedException ex) {
            throw new GlassFishException(ex);
        } catch (BundleException ex) {
            throw new GlassFishException(ex);
        }
        finally {
            framework = null; // guard against repeated calls.
        }
    }

    @Override
    public GlassFish newGlassFish(GlassFishProperties glassfishProperties) throws GlassFishException {
        GlassFish embeddedGf = super.newGlassFish(glassfishProperties);
        int finalStartLevel = Integer.parseInt(glassfishProperties.getProperties().getProperty(
                Constants.FINAL_START_LEVEL_PROP, "2"));
        return new OSGiGlassFishImpl(embeddedGf, framework.getBundleContext(), finalStartLevel);
    }

}
