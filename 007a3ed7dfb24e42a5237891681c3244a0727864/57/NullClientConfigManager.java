package com.ctrip.framework.apollo.cat;

import java.io.File;
import java.util.List;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;

public class NullClientConfigManager implements ClientConfigManager{

  @Override
  public Domain getDomain() {
    return null;
  }

  @Override
  public int getMaxMessageLength() {
    return 0;
  }

  @Override
  public String getServerConfigUrl() {
    return null;
  }

  @Override
  public List<Server> getServers() {
    return null;
  }

  @Override
  public int getTaggedTransactionCacheSize() {
    return 0;
  }

  @Override
  public void initialize(File configFile) throws Exception {
    
  }

  @Override
  public boolean isCatEnabled() {
    return false;
  }

  @Override
  public boolean isDumpLocked() {
    return false;
  }

  @Override
  public int getQueueSize() {
    return 0;
  }

}
