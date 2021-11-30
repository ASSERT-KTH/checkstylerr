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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.deploy.shared.MemoryMappedArchive;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.api.ServiceLocator;
import org.xml.sax.SAXException;

/**
 * Represents a Launchable main class which the caller specifies by the
 * main class itself, rather than a facade JAR or an original developer-provided
 * JAR file.
 *
 * @author tjquinn
 */
public class
        MainClassLaunchable implements Launchable {

    private final Class mainClass;
    private ApplicationClientDescriptor acDesc = null;
    private ClassLoader classLoader = null;
    private AppClientArchivist archivist = null;

    MainClassLaunchable(final ServiceLocator habitat, final Class mainClass) {
        super();
        this.mainClass = mainClass;
    }

    public Class getMainClass() throws ClassNotFoundException {
        return mainClass;
    }

    public ApplicationClientDescriptor getDescriptor(final URLClassLoader loader) throws IOException, SAXException {
        /*
         * There is no developer-provided descriptor possible so just
         * use a default one.
         */
        if (acDesc == null) {
            ReadableArchive tempArchive = null;
            final ACCClassLoader tempLoader = AccessController.doPrivileged(
                    new PrivilegedAction<ACCClassLoader>() {

                        @Override
                        public ACCClassLoader run() {
                            return new ACCClassLoader(loader.getURLs(), loader.getParent());
                        }
                    });
            tempArchive = createArchive(tempLoader, mainClass);
            final AppClientArchivist acArchivist = getArchivist(tempArchive, tempLoader);
            archivist.setClassLoader(tempLoader);
            archivist.setDescriptor(acDesc);
            archivist.setAnnotationProcessingRequested(true);
            acDesc = acArchivist.open(tempArchive);
            Application.createVirtualApplication(null, acDesc.getModuleDescriptor());
            acDesc.getApplication().setAppName(appNameFromMainClass(mainClass));
            this.classLoader = loader;
        }
        return acDesc;
    }

    private String appNameFromMainClass(final Class c) {
        return c.getName();
    }

//    private ReadableArchive createArchive(final ClassLoader loader,
//            final Class mainClass) throws IOException, URISyntaxException {
//        Manifest mf = new Manifest();
//        mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass.getName());
//        final File tempFile = File.createTempFile("acc", ".jar");
//        tempFile.deleteOnExit();
//        JarOutputStream jos = new JarOutputStream(
//                new BufferedOutputStream(new FileOutputStream(tempFile)), mf);
//        final String mainClassResourceName = mainClass.getName().replace('.', '/') + ".class";
//        final ZipEntry mainClassEntry = new ZipEntry(mainClassResourceName);
//        jos.putNextEntry(mainClassEntry);
//        InputStream is = loader.getResourceAsStream(mainClassResourceName);
//        int bytesRead;
//        byte[] buffer = new byte[1024];
//        while ( (bytesRead = is.read(buffer)) != -1) {
//            jos.write(buffer, 0, bytesRead);
//        }
//        is.close();
//        jos.closeEntry();
//        jos.close();
//
//        final InputJarArchive result = new InputJarArchive();
//        result.open(new URI("jar", tempFile.toURI().toASCIIString(), null));
//        return result;
//    }

    private ReadableArchive createArchive(final ClassLoader loader,
            final Class mainClass) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Manifest mf = new Manifest();
        Attributes mainAttrs = mf.getMainAttributes();
        /*
         * Note - must set the version or the attributes won't write
         * themselves to the output stream!
         */
        mainAttrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mainAttrs.put(Attributes.Name.MAIN_CLASS, mainClass.getName());
        JarOutputStream jos = new JarOutputStream(baos, mf);

        final String mainClassResourceName = mainClass.getName().replace('.', '/') + ".class";
        final ZipEntry mainClassEntry = new ZipEntry(mainClassResourceName);
        jos.putNextEntry(mainClassEntry);
        InputStream is = loader.getResourceAsStream(mainClassResourceName);
        int bytesRead;
        byte[] buffer = new byte[1024];
        while ( (bytesRead = is.read(buffer)) != -1) {
            jos.write(buffer, 0, bytesRead);
        }
        is.close();
        jos.closeEntry();
        jos.close();

        MemoryMappedArchive mma = new MemoryMappedArchive(baos.toByteArray());
        /*
         * Some archive-related processing looks for the file type from the URI, so set it
         * to something.
         */
        mma.setURI(URI.create("file:///tempClient.jar"));

        return mma;
    }


    private AppClientArchivist getArchivist(final ReadableArchive clientRA,
            final ClassLoader classLoader) throws IOException {
        if (archivist == null) {
            ArchivistFactory af = Util.getArchivistFactory();
            /*
             * Get the archivist by type rather than by archive to avoid
             * having to set the URI to some fake URI that the archivist
             * factory would understand.
             */
            archivist = completeInit((AppClientArchivist)
                    af.getArchivist(DOLUtils.carType()));
        }
        return archivist;
    }

    private AppClientArchivist completeInit(final AppClientArchivist arch) {
            arch.setAnnotationProcessingRequested(true);
            return arch;
    }

    public void validateDescriptor() {
        archivist.validate(classLoader);
    }

    public URI getURI() {
        return null;
    }

    public String getAnchorDir() {
        return null;
    }
}
