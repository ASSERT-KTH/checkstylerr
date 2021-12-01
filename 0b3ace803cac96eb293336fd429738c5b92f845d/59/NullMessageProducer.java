package com.ctrip.framework.apollo.cat;

import java.util.Map;

import com.dianping.cat.message.internal.DefaultMessageProducer;

public class NullMessageProducer extends DefaultMessageProducer{

  @Override
  public void logError(Throwable cause) {
    
  }

  @Override
  public void logError(String message, Throwable cause) {
    
  }

  @Override
  public void logEvent(String type, String name) {
    
  }

  @Override
  public void logTrace(String type, String name) {
    
  }

  @Override
  public void logEvent(String type, String name, String status, String nameValuePairs) {
    
  }

  @Override
  public void logTags(String scenario, Map<String, String> indexedTags,
      Map<String, String> storedTags) {
    
  }

  @Override
  public void logTrace(String type, String name, String status, String nameValuePairs) {
    
  }

  @Override
  public void logHeartbeat(String type, String name, String status, String nameValuePairs) {
    
  }

  @Override
  public void logMetric(String name, String status, String nameValuePairs) {
    
  }

}
