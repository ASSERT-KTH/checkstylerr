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

package com.sun.enterprise.admin.cli.cluster;

import java.util.logging.Level;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.FileUtils;
import java.io.*;
import java.util.*;

import jakarta.inject.Inject;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.glassfish.cluster.ssh.util.SSHUtil;


import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

/**
 * @author Byron Nevins
 */
@Service(name = "install-node-ssh")
@PerLookup
public class InstallNodeSshCommand extends InstallNodeBaseCommand {
    @Param(name = "sshuser", optional = true, defaultValue = "${user.name}")
    private String user;
    @Param(optional = true, defaultValue = "22", name = "sshport")
    int port;
    @Param(optional = true)
    String sshkeyfile;
    @Inject
    private SSHLauncher sshLauncher;
    //storing password to prevent prompting twice
    private Map<String, char[]> sshPasswords = new HashMap<String, char[]>();

    @Override
    String getRawRemoteUser() {
        return user;
    }

    @Override
    int getRawRemotePort() {
        return port;
    }

    @Override
    String getSshKeyFile() {
        return sshkeyfile;
    }

    @Override
    protected void validate() throws CommandException {
        super.validate();
        if (sshkeyfile == null) {
            //if user hasn't specified a key file check if key exists in
            //default location
            String existingKey = SSHUtil.getExistingKeyFile();
            if (existingKey == null) {
                promptPass = true;
            }
            else {
                sshkeyfile = existingKey;
            }
        }
        else {
            validateKey(sshkeyfile);
        }

        //we need the key passphrase if key is encrypted
        if (sshkeyfile != null && SSHUtil.isEncryptedKey(sshkeyfile)) {
            sshkeypassphrase = getSSHPassphrase(true);
        }
    }

    @Override
    void copyToHosts(File zipFile, ArrayList<String> binDirFiles) throws CommandException {
        // exception handling is too complicated to mess with in the real method.
        // the idea is to catch everything here and re-throw as one kind
        // the caller is just going to do it anyway so we may as well do it here.
        // And it makes the signature simpler for other subclasses...
        try {
            copyToHostsInternal(zipFile, binDirFiles);
        }
        catch (CommandException ex) {
            throw ex;
        }
        catch (JSchException ex) {
            throw new CommandException(ex);
        }
        catch (InterruptedException ex) {
            throw new CommandException(ex);
        }
        catch (IOException ex) {
            throw new CommandException(ex);
        }
    }

    private void copyToHostsInternal(File zipFile, ArrayList<String> binDirFiles) throws JSchException, IOException, InterruptedException, CommandException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        boolean prompt = promptPass;
        for (String host : hosts) {
            sshLauncher.init(getRemoteUser(), host, getRemotePort(), sshpassword, getSshKeyFile(), sshkeypassphrase, logger);

            if (getSshKeyFile() != null && !sshLauncher.checkConnection()) {
                //key auth failed, so use password auth
                prompt = true;
            }

            if (prompt) {
                String sshpass = null;
                if (sshPasswords.containsKey(host))
                    sshpass = String.valueOf(sshPasswords.get(host));
                else
                    sshpass = getSSHPassword(host);

                //re-initialize
                sshLauncher.init(getRemoteUser(), host, getRemotePort(), sshpass, getSshKeyFile(), sshkeypassphrase, logger);
                prompt = false;
            }

            String sshInstallDir = getInstallDir().replace('\\', '/');

            SFTPClient sftpClient = sshLauncher.getSFTPClient();
            ChannelSftp sftpChannel = sftpClient.getSftpChannel();
            try {
                if (!sftpClient.exists(sshInstallDir)) {
                    sftpClient.mkdirs(sshInstallDir, 0755);
                }
            }
            catch (SftpException ioe) {
                logger.info(Strings.get("mkdir.failed", sshInstallDir, host));
                throw new IOException(ioe);
            }

            //delete the sshInstallDir contents if non-empty
            try {
                //get list of file in DAS sshInstallDir
                List<String> files = getListOfInstallFiles(sshInstallDir);
                deleteRemoteFiles(sftpClient, files, sshInstallDir, getForce());
            }
            catch (SftpException ex) {
                logger.finer("Failed to remove sshInstallDir contents");
                throw new IOException(ex);
            }
            catch (IOException ex) {
                logger.finer("Failed to remove sshInstallDir contents");
                throw new IOException(ex);
            }

            String zip = zipFile.getCanonicalPath();
            try {
                logger.info("Copying " + zip + " (" + zipFile.length() + " bytes)"
                        + " to " + host + ":" + sshInstallDir);
                // TODO: Looks like we need to quote the paths to scp in case they contain spaces.
                sftpChannel.cd(sftpChannel.getHome());
                sftpChannel.cd(sshInstallDir);
                sftpChannel.put(zipFile.getAbsolutePath(), zipFile.getName());
                if (logger.isLoggable(Level.FINER))
                    logger.finer("Copied " + zip + " to " + host + ":" +
                                                                sshInstallDir);
            }
            catch (SftpException ex) {
                logger.info(Strings.get("cannot.copy.zip.file", zip, host));
                throw new IOException(ex);
            }

            try {
                logger.info("Installing " + getArchiveName() + " into " + host + ":" + sshInstallDir);
                String unzipCommand = "cd '" + sshInstallDir + "'; jar -xvf " + getArchiveName();
                int status = sshLauncher.runCommand(unzipCommand, outStream);
                if (status != 0) {
                    logger.info(Strings.get("jar.failed", host, outStream.toString()));
                    throw new CommandException("Remote command output: " + outStream.toString());
                }
                if (logger.isLoggable(Level.FINER))
                    logger.finer("Installed " + getArchiveName() + " into " +
                                    host + ":" + sshInstallDir);
            }
            catch (IOException ioe) {
                logger.info(Strings.get("jar.failed", host, outStream.toString()));
                throw new IOException(ioe);
            }

            try {
                logger.info("Removing " + host + ":" + sshInstallDir + "/" + getArchiveName());
                sftpChannel.cd(sftpChannel.getHome());
                sftpChannel.rm(sshInstallDir + "/" + getArchiveName());
                if (logger.isLoggable(Level.FINER))
                    logger.finer("Removed " + host + ":" + sshInstallDir + "/" +
                                                            getArchiveName());
            }
            catch (SftpException ioe) {
                logger.info(Strings.get("remove.glassfish.failed", host, sshInstallDir));
                throw new IOException(ioe);
            }
            sftpClient.close();

            sftpClient = sshLauncher.getSFTPClient();

            // unjarring doesn't retain file permissions, hence executables need
            // to be fixed with proper permissions
            logger.info("Fixing file permissions of all bin files under " + host + ":" + sshInstallDir);
            try {
                if (binDirFiles.isEmpty()) {
                    //binDirFiles can be empty if the archive isn't a fresh one
                    searchAndFixBinDirectoryFiles(sshInstallDir, sftpClient);
                }
                else {
                    for (String binDirFile : binDirFiles) {
                        sftpClient.chmod(sshInstallDir + "/" + binDirFile, 0755);
                    }
                }
                if (logger.isLoggable(Level.FINER))
                    logger.finer("Fixed file permissions of all bin files " +
                                    "under " + host + ":" + sshInstallDir);
            }
            catch (SftpException ioe) {
                logger.info(Strings.get("fix.permissions.failed", host, sshInstallDir));
                throw new IOException(ioe);
            }

            if (Constants.v4) {
                logger.info("Fixing file permissions for nadmin file under " + host + ":"
                            + sshInstallDir + "/" + SystemPropertyConstants.getComponentName() + "/lib");
                try {
                    sftpClient.chmod((sshInstallDir + "/" + SystemPropertyConstants.getComponentName() + "/lib/nadmin"), 0755);
                    if (logger.isLoggable(Level.FINER))
                        logger.finer("Fixed file permission for nadmin under " +
                                    host + ":" + sshInstallDir + "/" +
                                    SystemPropertyConstants.getComponentName() +
                                    "/lib/nadmin");
                }
                catch (SftpException ioe) {
                    logger.info(Strings.get("fix.permissions.failed", host, sshInstallDir));
                    throw new IOException(ioe);
                }
            }
            sftpClient.close();
        }
    }

    /**
     * Recursively list install dir and identify "bin" directory. Change permissions
     * of files under "bin" directory.
     * @param installDir GlassFish install root
     * @param sftpClient ftp client handle
     * @throws SftpException
     */
    private void searchAndFixBinDirectoryFiles(String installDir, SFTPClient sftpClient) throws SftpException {
        for (LsEntry directoryEntry : (List<LsEntry>) sftpClient.getSftpChannel().ls(installDir)) {
            if (directoryEntry.getFilename().equals(".") || directoryEntry.getFilename().equals(".."))
                continue;
            else if (directoryEntry.getAttrs().isDir()) {
                String subDir = installDir + "/" + directoryEntry.getFilename();
                if (directoryEntry.getFilename().equals("bin")) {
                    fixAllFiles(subDir, sftpClient);
                } else {
                    searchAndFixBinDirectoryFiles(subDir, sftpClient);
                }
            }
        }
    }

    /**
     * Set permissions of all files under specified directory. Note that this
     * doesn't check the file type before changing the permissions.
     * @param binDir directory where file permissions need to be fixed
     * @param sftpClient ftp client handle
     * @throws SftpException
     */
    private void fixAllFiles(String binDir, SFTPClient sftpClient) throws SftpException {
        for (LsEntry directoryEntry : (List<LsEntry>) sftpClient.getSftpChannel().ls(binDir)) {
            if (directoryEntry.getFilename().equals(".") || directoryEntry.getFilename().equals(".."))
                continue;
            else {
                String fName = binDir + "/" + directoryEntry.getFilename();
                sftpClient.chmod(fName, 0755);
            }
        }
    }

    /**
     * Determines if GlassFish is installed on remote host at specified location.
     * Uses SSH launcher to execute 'asadmin version'
     * @param host remote host
     * @throws JSchException
     * @throws CommandException
     * @throws IOException
     * @throws InterruptedException
     */
    private void checkIfAlreadyInstalled(String host, String sshInstallDir) throws JSchException, CommandException, IOException, InterruptedException {
        //check if an installation already exists on remote host
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            String asadmin = Constants.v4 ? "/lib/nadmin' version --local --terse" : "/bin/asadmin' version --local --terse";
            String cmd = "'" + sshInstallDir + "/" + SystemPropertyConstants.getComponentName() + asadmin;
            int status = sshLauncher.runCommand(cmd, outStream);
            if (status == 0) {
                if (logger.isLoggable(Level.FINER))
                    logger.finer(host + ":'" + cmd + "'" +
                                " returned [" + outStream.toString() + "]");
                throw new CommandException(Strings.get("install.dir.exists", sshInstallDir));
            }
            else {
                if (logger.isLoggable(Level.FINER))
                    logger.finer(host + ":'" + cmd + "'" +
                                " failed [" + outStream.toString() + "]");
            }
        }
        catch (IOException ex) {
            logger.info(Strings.get("glassfish.install.check.failed", host));
            throw new IOException(ex);
        }
    }

    @Override
    final void precopy() throws CommandException {
        if (getForce())
            return;

        boolean prompt = promptPass;
        for (String host : hosts) {
            sshLauncher.init(getRemoteUser(), host, getRemotePort(), sshpassword, getSshKeyFile(), sshkeypassphrase, logger);

            if (getSshKeyFile() != null && !sshLauncher.checkConnection()) {
                //key auth failed, so use password auth
                prompt = true;
            }

            if (prompt) {
                String sshpass = getSSHPassword(host);
                sshPasswords.put(host, sshpass.toCharArray());
                //re-initialize
                sshLauncher.init(getRemoteUser(), host, getRemotePort(), sshpass, getSshKeyFile(), sshkeypassphrase, logger);
                prompt = false;
            }

            String sshInstallDir = getInstallDir().replaceAll("\\\\", "/");

            try {
                SFTPClient sftpClient = sshLauncher.getSFTPClient();
                if (sftpClient.exists(sshInstallDir)){
                    checkIfAlreadyInstalled(host, sshInstallDir);
                }
                sftpClient.close();
            }
            catch (SftpException ex) {
                throw new CommandException(ex);
            }
            catch (IOException ex) {
                throw new CommandException(ex);
            }
            catch (JSchException ex) {
                throw new CommandException(ex);
            }
            catch (InterruptedException ex) {
                throw new CommandException(ex);
            }
        }
    }
}
