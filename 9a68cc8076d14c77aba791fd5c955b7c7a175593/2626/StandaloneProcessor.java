/*
 * Copyright (c) 2005, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * StandaloneProcessor.java
 *
 * Created on January 7, 2005, 9:26 AM
 */

package devtests.deployment.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.*;

import javax.enterprise.deploy.shared.ModuleType;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;

import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationHandler;
import org.glassfish.apf.AnnotationProcessor;
import org.glassfish.apf.ProcessingContext;
import org.glassfish.apf.ProcessingResult;
import org.glassfish.apf.Scanner;
import com.sun.enterprise.deployment.annotation.context.AppClientContext;
import com.sun.enterprise.deployment.annotation.context.EjbBundleContext;
import com.sun.enterprise.deployment.annotation.context.WebBundleContext;
import com.sun.enterprise.deployment.annotation.factory.SJSASFactory;
import org.glassfish.apf.impl.AnnotationUtils;
import com.sun.enterprise.deployment.annotation.impl.AppClientScanner;
import org.glassfish.apf.impl.DirectoryScanner;
import org.glassfish.ejb.deployment.annotation.impl.EjbJarScanner;
import org.glassfish.web.deployment.annotation.impl.WarScanner;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;
import org.glassfish.web.deployment.descriptor.WebComponentDescriptorImpl;
import com.sun.enterprise.deployment.annotation.impl.ModuleScanner;

import com.sun.enterprise.deployment.io.AppClientDeploymentDescriptorFile;
import org.glassfish.ejb.deployment.io.EjbDeploymentDescriptorFile;
import org.glassfish.web.deployment.io.WebDeploymentDescriptorFile;
import org.glassfish.webservices.io.WebServicesDeploymentDescriptorFile;

import com.sun.enterprise.deployment.util.ApplicationValidator;
import com.sun.enterprise.deployment.util.AppClientVisitor;
import com.sun.enterprise.deployment.util.AppClientValidator;
import org.glassfish.ejb.deployment.util.EjbBundleValidator;
import com.sun.enterprise.deployment.util.EjbBundleVisitor;
import org.glassfish.web.deployment.util.WebBundleVisitor;
import org.glassfish.web.deployment.util.WebBundleValidator;

import org.glassfish.api.deployment.archive.ReadableArchive;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.module.ModulesRegistry;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.enterprise.module.bootstrap.StartupContext;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;


/**
 *
 * @author dochez
 */
public class StandaloneProcessor {
    private BundleDescriptor bundleDescriptor = null;
    private AnnotatedElementHandler aeHandler = null;
    private ModuleType type = null;
    private Set<String> compClassNames = null;
    private static ServiceLocator serviceLocator = null;

    /** Creates a new instance of StandaloneProcessor */
    public StandaloneProcessor() {
        this(ModuleType.EJB);
    }

    public StandaloneProcessor(ModuleType type) {
        this.type = type;
        if (ModuleType.EJB.equals(type)) {
            bundleDescriptor = new EjbBundleDescriptorImpl();
            aeHandler = new EjbBundleContext((EjbBundleDescriptorImpl) bundleDescriptor);

        } else if (ModuleType.WAR.equals(type)) {
            bundleDescriptor = new WebBundleDescriptorImpl();
            aeHandler = new WebBundleContext(
                    (WebBundleDescriptor)bundleDescriptor);
        } else if (ModuleType.CAR.equals(type)) {
            bundleDescriptor = new ApplicationClientDescriptor();
            aeHandler = new AppClientContext(
                    (ApplicationClientDescriptor)bundleDescriptor);
        } else {
            throw new UnsupportedOperationException(
                    "ModuleType : " + type + " is not supported.");
        }
    }

    public void setComponentClassNames(Set compClassNames) {
        this.compClassNames = compClassNames;
    }

    public static void main(String[] args) throws Exception {
        StandaloneProcessor processor = new StandaloneProcessor();
        processor.run(args);
        processor.generateEjbJarXmlFile(".");
        processor.generateWebServicesXmlFile(".");
    }

    public int run(String[] args) throws Exception {
        for (String arg : args) {
            System.out.println("Annotation log is set to " + System.getProperty("annotation.log"));
            String logWhat = System.getProperty("annotation.log");
            if (logWhat!=null) {
                AnnotationUtils.setLoggerTarget(logWhat);
                initLogger();
            }
            AnnotationUtils.getLogger().info("processing " + arg);
            File f = new File(arg);
            if (f.exists()) {
                try {
                    prepareServiceLocator();
                    ArchiveFactory archiveFactory = serviceLocator.getService(ArchiveFactory.class);
                    ReadableArchive archive = archiveFactory.openArchive(f);
                    ClassLoader classLoader = null;
                    if (ModuleType.WAR.equals(type)) {
                        classLoader = new URLClassLoader(
                            new URL[] { new File(f, "WEB-INF/classes").toURL() });
                    } else {
                        classLoader = new URLClassLoader(new URL[]{ f.toURL() });
                    }
                    ModuleScanner scanner = null;

                    if (ModuleType.EJB.equals(type)) {
                        EjbBundleDescriptorImpl ejbBundleDesc =
                                (EjbBundleDescriptorImpl)bundleDescriptor;
                        scanner = serviceLocator.getService(EjbJarScanner.class);
                        scanner.process(archive, ejbBundleDesc, classLoader, null);

                    } else if (ModuleType.WAR.equals(type)) {
                        WebBundleDescriptor webBundleDesc =
                                (WebBundleDescriptor)bundleDescriptor;
                        for (String cname : compClassNames) {
                            WebComponentDescriptor webCompDesc =
                                    new WebComponentDescriptorImpl();
                            webCompDesc.setServlet(true);
                            webCompDesc.setWebComponentImplementation(cname);
                            webBundleDesc.addWebComponentDescriptor(webCompDesc);
                        }
                        scanner = serviceLocator.getService(WarScanner.class);
                        scanner.process(archive, webBundleDesc, classLoader, null);

                    } else if (ModuleType.CAR.equals(type)) {
                        String mainClassName = compClassNames.iterator().next();
                        ApplicationClientDescriptor appClientDesc =
                                (ApplicationClientDescriptor)bundleDescriptor;
                        appClientDesc.setMainClassName(mainClassName);
                        scanner = serviceLocator.getService(AppClientScanner.class);
                        scanner.process(archive, appClientDesc, classLoader, null);

                    }

                    AnnotationProcessor ap = serviceLocator.<SJSASFactory>getService(SJSASFactory.class).getAnnotationProcessor(false);


                    // if the user indicated a directory for handlers, time to add the
                    String handlersDir = System.getProperty("handlers.dir");
                    if (handlersDir!=null) {
                        addHandlers(ap, handlersDir);
                    }

                    ProcessingContext ctx = ap.createContext();
                    ctx.setErrorHandler(new StandaloneErrorHandler());
                    bundleDescriptor.setClassLoader(scanner.getClassLoader());
                    ctx.setProcessingInput(scanner);
                    ctx.pushHandler(aeHandler);
                    ProcessingResult result = ap.process(ctx);
                    if (ModuleType.EJB.equals(type)) {
                        EjbBundleDescriptorImpl ebd = (EjbBundleDescriptorImpl)bundleDescriptor;
                        ebd.visit(new EjbBundleValidator());
                    } else if (ModuleType.WAR.equals(type)) {
                        WebBundleDescriptor wbd = (WebBundleDescriptor)bundleDescriptor;
                        wbd.visit(new WebBundleValidator());
                    } else if (ModuleType.CAR.equals(type)) {
                        ApplicationClientDescriptor acbd = (ApplicationClientDescriptor)bundleDescriptor;
                        acbd.visit(new AppClientValidator());
                    }
                    // display the result ejb bundle...
                    // AnnotationUtils.getLogger().info("Resulting " + bundleDescriptor);

                } catch(Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        return 0;
    }

    public void generateAppClientXmlFile(String dir) throws IOException {
         AppClientDeploymentDescriptorFile appClientdf = new AppClientDeploymentDescriptorFile();
         appClientdf.write(bundleDescriptor, dir);

    }

    public void generateEjbJarXmlFile(String dir) throws IOException {
         EjbDeploymentDescriptorFile ejbdf = new EjbDeploymentDescriptorFile();
         ejbdf.write(bundleDescriptor, dir);

    }

    public void generateWebXmlFile(String dir) throws IOException {
         WebDeploymentDescriptorFile webdf = new WebDeploymentDescriptorFile();
         webdf.write((WebBundleDescriptorImpl)bundleDescriptor, dir);
    }

    public void generateWebServicesXmlFile(String dir) throws IOException {
         WebServicesDeploymentDescriptorFile ddf = new WebServicesDeploymentDescriptorFile(bundleDescriptor);
         ddf.write(bundleDescriptor, dir);
    }

    private void addHandlers(AnnotationProcessor ap, String handlersDir) {
        try {
        System.out.println("Handlers dir set at " + handlersDir);
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.process(new File(handlersDir),null,null);

        Set<Class> elements = scanner.getElements();
        for (Class handlerClass : elements) {
            Class[] interfaces = handlerClass.getInterfaces();
            for (Class interf : interfaces) {
                if (interf.equals(org.glassfish.apf.AnnotationHandler.class)) {
                    AnnotationHandler handler = (AnnotationHandler) handlerClass.newInstance();
                    if (AnnotationUtils.shouldLog("handler")) {
                        AnnotationUtils.getLogger().fine("Registering handler " + handlerClass
                               + " for type " + handler.getAnnotationType());
                    }
                    ap.pushAnnotationHandler(handler);
                }
            }
        }
        } catch(Exception e) {
            AnnotationUtils.getLogger().severe("Exception while registering handlers : " + e.getMessage());
        }
    }

    private void initLogger() {
        try {
            FileHandler handler = new FileHandler("annotation.log");
            handler.setFormatter(new LogFormatter());
            handler.setLevel(Level.FINE);
            Logger logger = Logger.global;
            logger.setLevel(Level.FINE);
            logger.addHandler(handler);} catch(Exception e) {
                e.printStackTrace();
            }
    }

    private void prepareServiceLocator() {
        if ( (serviceLocator == null) ) {
            // Bootstrap a hk2 environment.
            ModulesRegistry registry = new StaticModulesRegistry(getClass().getClassLoader());
            serviceLocator = registry.createServiceLocator("default");

            StartupContext startupContext = new StartupContext();

            ServiceLocatorUtilities.addOneConstant(serviceLocator, startupContext);
            ServiceLocatorUtilities.addOneConstant(serviceLocator,
                new ProcessEnvironment(ProcessEnvironment.ProcessType.Other));
        }
    }

}
