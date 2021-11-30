/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util.io;

import java.io.*;
import java.io.File;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.ObjectAnalyzer;

/**
 * The hierarchy of directories above a running DAS or server instance can get
 * messy to deal with -- thus this class.  This class is a bullet-proof holder of
 * that information.
 *
 * Instances and DAS are arranged differently:
 *
 * examples:
 * DAS
 * domainDir = getServerDir() == C:/glassfish6/glassfish/domains/domain1
 * domainsDir = getServerParentDir() == C:/glassfish6/glassfish/domains
 * grandparent-dir is meaningless
 *
 * Instance
 * instanceDir = getServerDir() == C:/glassfish6/glassfish/nodes/mymachine/instance1
 * agentDir = getServerParentDir() == C:/glassfish6/glassfish/nodes/mymachine
 * agentsDir = getServerGrandParentDir() == C:/glassfish6/glassfish/nodes
 *
 * Currently in all cases the name of the serverDir is the name of the server --
 * by our definition.
 *
 * THIS CLASS IS GUARANTEED THREAD SAFE
 * THIS CLASS IS GUARANTEED IMMUTABLE
 *
 * Contract:  Caller is supposed to NOT call methods on an instance of this class.
 * It's "advanced java" to be able to do that anyway.
 * I don't allow half-baked data out.  It's all or none.  The "valid" flag
 * is checked and if invalid -- all methods return null.  They don't throw an Exception
 * because the caller is not supposed to call the methods - it would just annoy
 * the caller.
 *
 * @author Byron Nevins
 * @since 3.1
 * Created: April 19, 2010
 */
public class ServerDirs {
    // do-nothing constructor
    /**
     *
     */
    public ServerDirs() {
        serverName = null;
        serverDir = null;
        agentDir = null;
        parentDir = null;
        grandParentDir = null;
        configDir = null;
        domainXml = null;
        pidFile = null;
        valid = false;
        localPassword = null;
        localPasswordFile = null;
        dasPropertiesFile = null;
    }

    public ServerDirs(File leaf) throws IOException {
        if (leaf == null)
            throw new IllegalArgumentException(strings.get("ServerDirs.nullArg", "ServerDirs.ServerDirs()"));

        if (!leaf.isDirectory())
            throw new IOException(strings.get("ServerDirs.badDir", leaf));

        serverDir = SmartFile.sanitize(leaf);
        serverName = serverDir.getName();

        // note that serverDir has been "smart-filed" so we don't have to worry
        // about getParentFile() which has issues with relative paths...
        parentDir = serverDir.getParentFile();

        if (parentDir == null || !parentDir.isDirectory())
            throw new IOException(strings.get("ServerDirs.badParentDir", serverDir));

        // grandparent dir is optional.  It can be null for DAS for instance...
        grandParentDir = parentDir.getParentFile();
        configDir = new File(serverDir, "config");
        domainXml = new File(configDir, "domain.xml");
        pidFile = new File(configDir, "pid");
        localPasswordFile = new File(configDir, "local-password");

        String localPasswordBuffer = null;  // need an atomic assign tor localPassword
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(localPasswordFile));
            localPasswordBuffer = r.readLine();
        }
        catch (Exception e) {
            // needs no handling
        }
        finally {
            localPassword = localPasswordBuffer;
            if (r != null) {
                try {
                    r.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }

        // bnevins May 17 -- perhaps we should have a NodeAgentDirs ???
        agentDir = new File(parentDir, "agent");
        dasPropertiesFile = new File(parentDir, "agent/config/das.properties");
        valid = true;
    }

    public final String getServerName() {
        if (!valid)
            return null;

        return serverName;
    }

    /**
     * Return a message suitable for printing, not just for errors.
     * @return
     */
    public final String deletePidFile() {
        if (!valid)
            return "Internal Error: ServerDirs is in an invalid state";

        if (!pidFile.isFile())
            return null;

        String message = "pid file " + pidFile + " exists, removing it.";

        if (!pidFile.delete()) {
            // Hmmm... can't delete it, don't use it
            // TODO -- try to go inside it and delete the contents
            return message + "  Couldn't remove pid file";
        }
        return message;
    }

    public ServerDirs refresh() throws IOException {
        return new ServerDirs(serverDir);
    }

    // getters & setters section below
    public final File getServerDir() {
        if (!valid)
            return null;
        return serverDir;
    }

    public final File getAgentDir(){
         if (!valid)
            return null;
        return agentDir;
    }

    public final File getServerParentDir() {
        if (!valid)
            return null;
        return parentDir;
    }

    public final File getServerGrandParentDir() {
        if (!valid)
            return null;
        return grandParentDir;
    }

    public final File getDomainXml() {
        if (!valid)
            return null;

        return domainXml;
    }

    public final File getConfigDir() {
        if (!valid)
            return null;

        return configDir;
    }

    public final File getPidFile() {
        if (!valid)
            return null;

        return pidFile;
    }

    public final File getDasPropertiesFile() {
        return dasPropertiesFile;
    }

    public String getLocalPassword() {
        return localPassword;
    }

    public final File getLocalPasswordFile() {
        if (!valid)
            return null;

        return localPasswordFile;
    }

    public final boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        return ObjectAnalyzer.toString(this);
    }
    ///////////////////////////////////////////////////////////////////////////
    ///////////           All Private Below           /////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    private final String serverName;
    private final File serverDir;
    private final File parentDir;
    private final File agentDir;
    private final File grandParentDir;
    private final File configDir;
    private final File domainXml;
    private final File pidFile;
    private final boolean valid;
    private final String localPassword;
    private final File localPasswordFile;
    private final File dasPropertiesFile; // this only makes sense for instances...
    // Can be shared among classes in the package
    static final LocalStringsImpl strings = new LocalStringsImpl(ServerDirs.class);
    // root-dir/config/domain.xml
}
