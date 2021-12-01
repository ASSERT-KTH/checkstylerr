package com.ctrip.apollo.client.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ClassLoaderUtil {
  private static final Logger logger = LoggerFactory.getLogger(ClassLoaderUtil.class);

  private static ClassLoader loader = Thread.currentThread().getContextClassLoader();

  static {
    if (loader == null) {
      logger.info("Using system class loader");
      loader = ClassLoader.getSystemClassLoader();
    }
  }

  public static ClassLoader getLoader() {
    return loader;
  }
}
