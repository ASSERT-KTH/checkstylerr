/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deploy.shared;

import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.ReadableArchiveFactory;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;

import jakarta.inject.Inject;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Singleton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LoggerInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;

/**
 * This implementation of the ArchiveFactory interface
 * is capable of creating the right abstraction of the Archive
 * interface depending on the protocol used in the URL.
 *
 * @author Jerome Dochez
 */
@Service
@Singleton
public class ArchiveFactory {

    @Inject
    ServiceLocator habitat;

    public static final Logger deplLogger = org.glassfish.deployment.common.DeploymentContextImpl.deplLogger;

    @LogMessageInfo(message = "Cannot find an archive implementation for {0}", cause="The type of archive being created is not supported.", action="Determine the type of archive requested to see whether another type can be used.", level="SEVERE")
    private static final String IMPLEMENTATION_NOT_FOUND = "NCLS-DEPLOYMENT-00021";

    public WritableArchive createArchive(File path) throws java.io.IOException {
        try {
            /*
             *Use the expanded constructor so illegal characters (such as embedded blanks) in the path
             *will be encoded.
             */
            return createArchive(prepareArchiveURI(path));
        } catch(java.net.URISyntaxException e) {
            return null;
        }
    }

    public WritableArchive createArchive(String protocol, File path) throws java.io.IOException {
        try {
            /*
             *Use the expanded constructor so illegal characters (such as embedded blanks) in the path
             *will be encoded.
             */
            return createArchive(protocol, prepareArchiveURI(path));
        } catch(java.net.URISyntaxException e) {
            return null;
        }
    }

    public ReadableArchive openArchive(File path) throws java.io.IOException {
        try {
            return openArchive(prepareArchiveURI(path));
        } catch(java.net.URISyntaxException e) {
            return null;
        }
    }

    /**
     * Creates a new archivist using the URL as the path. The URL
     * protocol will define the type of desired archive (jar, file, etc)
     * @param path to the archive
     * @return the apropriate archive
     */
    public WritableArchive createArchive(URI path) throws IOException {

        String protocol = path.getScheme();
        return createArchive(protocol, path);
    }

    public WritableArchive createArchive(String protocol, URI path) throws IOException {
        try {
            WritableArchive archive = habitat.getService(WritableArchive.class, protocol);
            if (archive==null) {
                deplLogger.log(Level.SEVERE,
                               IMPLEMENTATION_NOT_FOUND,
                               protocol);
                throw new MalformedURLException("Protocol not supported : " + protocol);
            }

            archive.create(path);
            return archive;
        } catch (MultiException e) {
            LogRecord lr = new LogRecord(Level.SEVERE, IMPLEMENTATION_NOT_FOUND);
            lr.setParameters(new Object[] { protocol });
            lr.setThrown(e);
            deplLogger.log(lr);
            throw new MalformedURLException("Protocol not supported : " + protocol);
        }
    }

    /**
     * It first consults {@link ReadableArchiveFactory} to get an archive, if it does not get then delegates to
     * {@link #openArchive(java.net.URI)}.
     * @param path Application archive, never null
     * @param properties property bag, can contain for example deploy time properties. Never null
     * @return Gives {@link ReadableArchive}.
     * @throws IOException
     */
    public ReadableArchive openArchive(File path, DeployCommandParameters properties) throws IOException {
        URI uri;
        try {
            uri = prepareArchiveURI(path);
        } catch (URISyntaxException e) {
            return null;
        }
        for (ReadableArchiveFactory fac : habitat.<ReadableArchiveFactory>getAllServices(ReadableArchiveFactory.class)) {
            //get the first ReadableArchive and move
            ReadableArchive archive=null;
            try{
                archive = fac.open(uri, properties);
            }catch(Exception e){
                //ignore?
            }
            if(archive == null)
                continue;
            return archive;
        }
        return openArchive(path);
    }

    /**
     * Opens an existing archivist using the URL as the path.
     * The URL protocol will defines the type of desired archive
     * (jar, file, memory, etc...)
     * @param path url to the existing archive
     * @return the appropriate archive
     */
    public ReadableArchive openArchive(URI path) throws IOException {

        String provider = path.getScheme();
        if (provider.equals("file")) {
            // this could be a jar file or a directory
            File f = new File(path);
            if (!f.exists()) throw new FileNotFoundException(f.getPath());
            if (f.isFile()) {
                provider = "jar";
            }
        }
        try {
            ReadableArchive archive = habitat.getService(ReadableArchive.class, provider);
            if (archive==null) {
                deplLogger.log(Level.SEVERE,
                               IMPLEMENTATION_NOT_FOUND,
                               provider);
                throw new MalformedURLException("Protocol not supported : " + provider);
            }
            archive.open(path);
            return archive;
        } catch (MultiException e) {
            LogRecord lr = new LogRecord(Level.SEVERE, IMPLEMENTATION_NOT_FOUND);
            lr.setParameters(new Object[] { provider });
            lr.setThrown(e);
            deplLogger.log(lr);
            throw new MalformedURLException("Protocol not supported : " + provider);
        }
    }

    /**
     *Create a URI for the jar specified by the path string.
     *<p>
     *The steps used here correctly encode "illegal" characters - such as embedded blanks - in
     *the path string that otherwise would render the URI unusable.  The URI constructor that
     *accepts just the path string does not perform this encoding.
     *@param path string for the archive
     *@return URI with any necessary encoding of special characters
     */
    static java.net.URI prepareArchiveURI(File path) throws java.net.URISyntaxException, java.io.UnsupportedEncodingException, java.io.IOException {

        URI archiveURI = path.toURI();
        URI answer = new URI(archiveURI.getScheme(), null /* authority */, archiveURI.getPath(), null /* query */, null /* fragment */);
        return answer;
    }
}
