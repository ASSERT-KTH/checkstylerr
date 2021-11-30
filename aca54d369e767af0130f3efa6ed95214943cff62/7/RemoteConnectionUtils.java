package com.databasepreservation.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.model.exception.ModuleException;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RemoteConnectionUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConnectionUtils.class);
  public static final Integer localPort = 4321;
  private static final String localHost = "localhost";

  public static String replaceHostAndPort(String connectionURL) {
    final String[] remoteHostAndPort = getRemoteHostAndPort(connectionURL);
    if (remoteHostAndPort.length == 2) {
      connectionURL = connectionURL.replace(remoteHostAndPort[0], localHost);
      connectionURL = connectionURL.replace(String.valueOf(remoteHostAndPort[1]), String.valueOf(localPort));
    }

    return connectionURL;
  }

  /**
   *
   * Make SSH connection to remote machine
   *
   */
  public static Session createRemoteSession(String sshHost, String sshUser, String sshPassword, String sshPortNumber,
    String connectionURL) throws ModuleException {

    int sshPort;
    if (sshPortNumber != null) {
      sshPort = Integer.parseInt(sshPortNumber);
    } else {
      sshPort = 22;
    }

    final String[] remoteHostAndPort = getRemoteHostAndPort(connectionURL);
    if (remoteHostAndPort.length == 2) {
      String remoteHost = remoteHostAndPort[0];
      int remotePort = Integer.parseInt(remoteHostAndPort[1]);

      try {
        JSch jsch = new JSch();
        Session session = jsch.getSession(sshUser, sshHost, sshPort);

        session.setPassword(sshPassword);
        session.setConfig("StrictHostKeyChecking", "no");
        LOGGER.debug("Establishing SSH Connection");
        session.connect();

        int assigned_port = session.setPortForwardingL(RemoteConnectionUtils.localPort, remoteHost, remotePort);
        LOGGER.debug("localhost:{} -> {}:{}", assigned_port, remoteHost, remotePort);

        return session;
      } catch (Exception e) {
        throw new ModuleException().withMessage("Could not establish SSH connection").withCause(e);
      }
    }

    return null;
  }

  private static String[] getRemoteHostAndPort(String connectionURL) {

    final String regex = "\\/\\/(.*?)[;/]";
    final Pattern pattern = Pattern.compile(regex);
    final Matcher matcher = pattern.matcher(connectionURL);

    String hostAndPortResult = null;

    while (matcher.find()) {
      hostAndPortResult = matcher.group(1);
    }

    if (hostAndPortResult != null) {
      return hostAndPortResult.split(":");
    }

    return new String[]{};
  }
}
