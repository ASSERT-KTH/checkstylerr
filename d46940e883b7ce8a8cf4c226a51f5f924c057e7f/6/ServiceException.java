package com.ctrip.apollo.core.exception;

public class ServiceException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ServiceException(String str) {
    super(str);
  }

}
