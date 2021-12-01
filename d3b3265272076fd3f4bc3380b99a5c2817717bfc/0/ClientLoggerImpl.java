/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.visualization.shared.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.visualization.shared.client.ClientLoggerService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Client logging servlet implementation
 *
 * @author Luis Faria
 */
public class ClientLoggerImpl extends RemoteServiceServlet implements ClientLoggerService {
  private String getUserInfo() {
    String ret;
    String address = this.getThreadLocalRequest().getRemoteAddr();
    ret = "[" + address + "] ";
    return ret;
  }

  public void debug(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.debug(getUserInfo() + object);
  }

  public void debug(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.debug(getUserInfo() + object, error);
  }

  public void error(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.error(getUserInfo() + object);
    sendError(classname, object, null);

  }

  public void error(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.error(getUserInfo() + object, error);
    sendError(classname, object, error);
  }

  public void fatal(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.error(getUserInfo() + object);
    sendError(classname, object, null);
  }

  public void fatal(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.error(getUserInfo() + object, error);
    sendError(classname, object, error);
  }

  public void info(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.info(getUserInfo() + object);
  }

  public void info(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.info(getUserInfo() + object, error);
  }

  public void trace(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.trace(getUserInfo() + object);
  }

  public void trace(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.trace(getUserInfo() + object, error);
  }

  public void warn(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.warn(getUserInfo() + object);
  }

  public void warn(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.warn(getUserInfo() + object, error);
  }

  public void pagehit(String pagename) {
    Logger logger = LoggerFactory.getLogger(ClientLoggerImpl.class);
    // try {
    // RODAClient rodaClient = RodaClientFactory.getRodaWuiClient();
    // String username = RodaClientFactory.getRodaClient(
    // this.getThreadLocalRequest().getSession()).getUsername();
    // LogEntryParameter[] parameters = new LogEntryParameter[] {
    // new LogEntryParameter("hostname", getThreadLocalRequest()
    // .getRemoteHost()),
    // new LogEntryParameter("address", getThreadLocalRequest()
    // .getRemoteAddr()),
    // new LogEntryParameter("user", getThreadLocalRequest()
    // .getRemoteUser()),
    // new LogEntryParameter("pagename", pagename) };
    //
    // LogEntry logEntry = new LogEntry();
    // logEntry.setAction(LOG_ACTION_WUI_PAGEHIT);
    // logEntry.setParameters(parameters);
    // logEntry.setUsername(username);
    //
    // rodaClient.getLoggerService().addLogEntry(logEntry);
    // } catch (RemoteException e) {
    // logger.error("Error logging page hit", e);
    // } catch (RODAClientException e) {
    // logger.error("Error logging page hit", e);
    // } catch (LoginException e) {
    // logger.error("Error logging page hit", e);
    // } catch (LoggerException e) {
    // logger.error("Error logging page hit", e);
    // }
  }

  public void destroy() {
    super.destroy();
    LogManager.shutdown();
  }

  /**
   * Send error to logging services
   *
   * @param classname
   *          the name of the class that generated the error
   * @param message
   *          the error message
   * @param error
   *          the error throwable
   */
  public void sendError(String classname, String message, Throwable error) {
    Logger logger = LoggerFactory.getLogger(ClientLoggerImpl.class);

    // try {
    // RODAClient rodaClient = RodaClientFactory.getRodaWuiClient();
    // String username = RodaClientFactory.getRodaClient(
    // this.getThreadLocalRequest().getSession()).getUsername();
    // List<LogEntryParameter> parameters = new Vector<>();
    // parameters.add(new LogEntryParameter("hostname",
    // getThreadLocalRequest().getRemoteHost()));
    // parameters.add(new LogEntryParameter("address",
    // getThreadLocalRequest().getRemoteAddr()));
    // parameters.add(new LogEntryParameter("port",
    // getThreadLocalRequest().getRemotePort() + ""));
    // parameters.add(new LogEntryParameter("classname", classname));
    // parameters.add(new LogEntryParameter("error", message));
    // if (error != null) {
    // parameters.add(new LogEntryParameter("message", error.getMessage()));
    // }
    // //
    // LogEntry logEntry = new LogEntry();
    // logEntry.setAction(LOG_ACTION_WUI_ERROR);
    // logEntry.setParameters(parameters.toArray(new
    // LogEntryParameter[parameters.size()]));
    // logEntry.setUsername(username);
    //
    // rodaClient.getLoggerService().addLogEntry(logEntry);
    // } catch (RemoteException e) {
    // logger.error("Error logging login", e);
    // } catch (LoginException e) {
    // logger.error("Error logging login", e);
    // } catch (LoggerException e) {
    // logger.error("Error logging login", e);
    // } catch (RODAClientException e) {
    // logger.error("Error logging login", e);
    // }
  }

}
