/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
package com.databasepreservation.managers;

import java.io.InputStream;
import java.nio.file.Path;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RemoteConnectionManager {

  private String host;
  private String username;
  private String password;
  private String portNumber;
  private boolean configured;
  private static RemoteConnectionManager instance = null;
  private ChannelSftp channelSftp = null;

  private RemoteConnectionManager() {
  }

  public static RemoteConnectionManager getInstance() {
    if (instance == null) {
      instance = new RemoteConnectionManager();
    }
    return instance;
  }

  public void setup(String host, String username, String password, String portNumber) {
    if (!this.configured) {
      this.host = host;
      this.username = username;
      this.password = password;
      this.portNumber = portNumber;
      this.configured = true;
    }
  }

  public boolean isConfigured() {
    return configured;
  }

  public InputStream getInputStream(Path blobPath) throws JSchException, SftpException {
    if (channelSftp == null) {
      JSch jsch = new JSch();
      Session session = jsch.getSession(username, host, Integer.parseInt(portNumber));

      session.setPassword(password);
      session.setConfig("StrictHostKeyChecking", "no");
      session.connect();

      Channel channel = session.openChannel("sftp");
      channel.connect();
      channelSftp = (ChannelSftp) channel;
    }

    return channelSftp.get(blobPath.normalize().toString());
  }

  public static void destroy() {
    instance = null;
  }
}
