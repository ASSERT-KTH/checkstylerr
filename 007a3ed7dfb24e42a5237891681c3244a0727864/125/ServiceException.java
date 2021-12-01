package com.ctrip.framework.apollo.core.exception;

public class ServiceException extends AbstractBaseException {

  /**
   * 
   */
  private static final long serialVersionUID = -6529123764065547791L;

  public ServiceException(String str) {
    super(str);
  }

  public ServiceException(String str, Exception e) {
    super(str, e);
  }
}
