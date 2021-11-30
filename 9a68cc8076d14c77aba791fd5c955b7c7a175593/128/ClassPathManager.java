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

package org.glassfish.appclient.client.jws.boot;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Abstract superclass of classes that manage the class path needed to run
 * the Java Web Start-aware ACC.
 *<p>
 * Some details vary among releases of the Java runtime  This abstract class
 * and its concrete implementation subclasses isolate those dependencies.
 *
 * @author tjquinn
 */
public abstract class ClassPathManager {

    public static final String PERSISTENCE_JAR_CLASSES =
            "org.apache.derby.client.ClientDataSourceFactory," /* derbyclient.jar */ +
            "persistence.antlr.ActionElement," /* toplink-essentials */ +
            "org.netbeans.modules.dbschema.ColumnElement," /* dbschema */
            ;

    /** instance of appropriate type of class path manager, depending on the Java runtime version */
    private static volatile ClassPathManager mgr = null;

    private final boolean keepJWSClassLoader;

    /**
     *Returns the appropriate type of ClassPathManager.
     *@return an instance of the correct implementation subclass class path manager
     */
    public static ClassPathManager getClassPathManager(boolean keepJWSClassLoader) {
        if (mgr == null) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            /*
             *Distinguish between 1.6 and earlier by seeing if the curent
             *class loader - the JNLP class loader - is also a URLClassLoader
             *or not.
             */
            if (loader instanceof URLClassLoader) {
                mgr = new ClassPathManager16(loader, keepJWSClassLoader);
            } else {
                mgr = new ClassPathManager15(loader, keepJWSClassLoader);
            }
        }
        return mgr;
    }

    /** the JNLP class loader active during the instantiation of this mgr */
    private ClassLoader jnlpClassLoader = null;

    /**
     *Returns a new instance of the manager.
     *@param loader the class loader provided by Java Web Start
     */
    protected ClassPathManager(ClassLoader loader, boolean keepJWSClassLoader) {
        jnlpClassLoader = loader;
        this.keepJWSClassLoader = keepJWSClassLoader;
    }

    protected boolean keepJWSClassLoader() {
        return keepJWSClassLoader;
    }

    protected ClassLoader getJnlpClassLoader() {
        return jnlpClassLoader;
    }

    /**
     *Locates the URI for the JAR containing the specified class
     *@param className the name of the class to be located
     *@return the URI for the JAR file containing the class of interest
     */
    public URI locateClass(String className) throws
            IllegalAccessException,
            InvocationTargetException,
            MalformedURLException,
            URISyntaxException, ClassNotFoundException {
        String resourceName = classNameToResourceName(className);
        URL classURL = locateResource(resourceName);
        File f = findContainingJar(classURL);
        if (f == null) {
            /*
             *Could not locate the class we expected.
             */
            throw new ClassNotFoundException(className + "->" + resourceName);
        }
        return f.toURI();
    }

    /**
     *Returns the appropriate parent class loader for the ACC.
     *@return the correct class loader instance
     */
    public abstract ClassLoader getParentClassLoader();

    /*
     *Returns the jar that contains the specified resource.
     *@param resourceURL URL to look for
     *@return File object for the jar or directory containing the entry
     */
    public abstract File findContainingJar(URL resourceURL) throws
            IllegalArgumentException,
            URISyntaxException,
            MalformedURLException,
            IllegalAccessException,
            InvocationTargetException;

    /**
     *Returns the Java Web Start-provided class loader recorded when the
     *class path manager was created.
     *@return the Java Web Start class loader
     */
    protected ClassLoader getJNLPClassLoader() {
        return jnlpClassLoader;
    }

    /**
     *Converts a class name to a resource name.
     *@param className the name of the class of interest in x.y.z format
     *@return the resource name in x/y/z.class format
     */
    protected String classNameToResourceName(String className) {
        return className.replace(".", "/") + ".class";
    }

    /**
     *Finds a resource using the class's class loader.
     *@param resourceName the class to find
     *@return URL for the resource; null if not found
     */
    protected URL locateResource(String resourceName) {
        URL resourceURL = getClass().getClassLoader().getResource(resourceName);
        return resourceURL;
    }

    /**
     *Reports URLs for the locally-cached copies of the JARs downloaded by
     *Java Web Start needed for the ACC's class path and policy settings.
     *@return array of URLs, one entry for each downloaded JAR
     */
    public URL[] locateDownloadedJars() throws
            ClassNotFoundException,
            URISyntaxException,
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            MalformedURLException {
        /*
         *For each downloaded unsigned app client jar, get a URL that locates
         *it and add the URL to the list.
         *
         *This set of values should be automated on the server side and
         *communicated via a property setting in the JNLP document so
         *any changes in the list of downloaded files does not need to be made
         *there and here.
         */
        String probeClassNames = System.getProperty("com.sun.aas.jar.probe.class.names",
                "com.sun.enterprise.appclient.jws.boot.JWSACCMain," /* appserv-jwsacc */ +
                "com.sun.enterprise.appclient.Main," /* appserv-rt */ +
                "com.sun.jdo.api.persistence.enhancer.ByteCodeEnhancer," /* appserv-cmp */ +
                "com.sun.enterprise.admin.servermgmt.DomainConfig," /* appserv-admin */ +
                "com.sun.enterprise.deployment.client.DeploymentClientUtils," /* appserv-deployment-client */ +
                "jakarta.ejb.EJB," /* jakarta ee */ +
                "jakarta.security.auth.message.module.ServerAuthModule," /* jmac-api */ +
                "com.sun.appserv.management.ext.logging.LogAnalyzer," /* appserv-ext */ +
                "com.sun.mail.iap.Argument," /* mail */ +
                "com.sun.activation.viewers.ImageViewer," /* activation */ +
                "com.sun.xml.ws.api.server.WSEndpoint," /* webservices-rt */ +
                "com.sun.tools.ws.wsdl.parser.W3CAddressingExtensionHandler," /* webservices-tools */ +
                "com.sun.jms.spi.xa.JMSXAConnection," /* imqjmsra */ +
                "com.sun.jndi.fscontext.FSContext" /* fscontext */
                );

        return locateJARs(probeClassNames);
    }

    public URL[] locateJARs(String classNamesString) throws
            IllegalAccessException, InvocationTargetException,
            MalformedURLException, URISyntaxException, ClassNotFoundException {

        String [] classNames = classNamesString.split(",");

        /*
         *For each class name, find the jar that contains it by getting a URL
         *to the class and then using that URL to find the jar.
         */
        URL [] urls = new URL[classNames.length];
        int nextURL = 0;

        for (String className : classNames) {
            URI jarFileURI = locateClass(className);
            URL url = jarFileURI.toURL();
            urls[nextURL++] = url;
        }
        return urls;
    }

    public URL[] locatePersistenceJARs() throws
            IllegalAccessException, InvocationTargetException,
            MalformedURLException, URISyntaxException, ClassNotFoundException {
        return locateJARs(PERSISTENCE_JAR_CLASSES);
    }
}
