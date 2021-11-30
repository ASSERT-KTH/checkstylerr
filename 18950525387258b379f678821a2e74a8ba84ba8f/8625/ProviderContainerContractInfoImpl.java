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

package org.glassfish.appclient.client.acc;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.HashSet;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.validation.ValidatorFactory;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.persistence.jpa.ProviderContainerContractInfoBase;

/**
 * Implements the internal GlassFish interface which all persistence provider
 * containers must.
 *
 * @author tjquinn
 */
public class ProviderContainerContractInfoImpl extends ProviderContainerContractInfoBase {

    private final ACCClassLoader classLoader;
    private final Instrumentation inst;
    private final String applicationLocation;

    private final Collection<EntityManagerFactory> emfs = new HashSet<EntityManagerFactory>();
    /**
     * Creates a new instance of the ACC's implementation of the contract.
     * The ACC uses its agent to register a VM transformer which can then
     * delegate to transformers registered with this class by the
     * persistence logic.
     *
     * @param classLoader ACC's class loader
     * @param inst VM's instrumentation object
     */
    public ProviderContainerContractInfoImpl(
            final ACCClassLoader classLoader,
            final Instrumentation inst,
            final String applicationLocation,
            final ConnectorRuntime connectorRuntime) {
        super(connectorRuntime);
        this.classLoader = classLoader;
        this.inst = inst;
        this.applicationLocation = applicationLocation;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public ClassLoader getTempClassloader() {
        return AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {

                @Override
                public URLClassLoader run() {
                    return new URLClassLoader(classLoader.getURLs());
                }

            });
    }

    public void addTransformer(ClassTransformer transformer) {
        final TransformerWrapper tw = new TransformerWrapper(transformer, classLoader);
        if (inst != null) {
            inst.addTransformer(tw);
        } else {
            classLoader.addTransformer(tw);
        }
    }

    public String getApplicationLocation() {
        return applicationLocation;
    }

    public ValidatorFactory getValidatorFactory() {
        // TODO: Need to implement this correctly.
        return null;
    }

    // TODO: remove after persistence is refactored.
    public DeploymentContext getDeploymentContext() {
        return null;
    }

    public boolean isJava2DBRequired() {
        // Returns whether Java2DB is required or not. For an AppClient it is always false
        return false;
    }

    public void registerEMF(String unitName, String persistenceRootUri, RootDeploymentDescriptor containingBundle, EntityManagerFactory emf) {
        emfs.add(emf);
    }

    public String getJTADataSourceOverride() {
        // Returns whether JTA datasource is overridden. For an appclient it is never the case.
        return null;
    }

    public Collection<EntityManagerFactory> emfs() {
        return emfs;
    }

    /**
     * Wraps a persistence transformer in a java.lang.instrumentation.ClassFileTransformer
     * suitable for addition as a transformer to the JVM-provided instrumentation
     * class.
     */
    public static class TransformerWrapper implements ClassFileTransformer {

        private final ClassTransformer persistenceTransformer;
        private final ClassLoader classLoader;

        TransformerWrapper(final ClassTransformer persistenceTransformer,
                final ClassLoader classLoader) {
            this.persistenceTransformer = persistenceTransformer;
            this.classLoader = classLoader;
        }

        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            /*
             * Do not even bother running the transformer unless the loader
             * loading the class is the ACC's class loader.
             */
            return (loader.equals(classLoader) ?
                persistenceTransformer.transform(loader, className,
                    classBeingRedefined, protectionDomain, classfileBuffer)
                : null);
        }
    }

}
