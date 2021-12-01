package com.ctrip.apollo.spi;

import com.ctrip.apollo.Config;
import com.ctrip.apollo.internals.DefaultConfig;

import org.unidal.lookup.annotation.Named;

import java.io.File;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Named(type = ConfigFactory.class, value = "default")
public class DefaultConfigFactory implements ConfigFactory {
  private File m_baseDir;

  @Override
  public Config create(String namespace) {
    return new DefaultConfig(m_baseDir, namespace);
  }
}
