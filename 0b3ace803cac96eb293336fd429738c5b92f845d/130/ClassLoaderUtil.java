package com.ctrip.framework.apollo.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ClassLoaderUtil {
  private static final Logger logger = LoggerFactory.getLogger(ClassLoaderUtil.class);

  private static ClassLoader loader = Thread.currentThread().getContextClassLoader();
  private static String classPath = "";

  static {
    if (loader == null) {
      logger.info("Using system class loader");
      loader = ClassLoader.getSystemClassLoader();
    }

    try {
      URL url = loader.getResource("");
      // get class path
      classPath = url.getPath();
      classPath = URLDecoder.decode(classPath, "utf-8");

      // 如果是jar包内的，则返回当前路径
      if (classPath.contains(".jar!")) {
        logger.warn("using config file inline jar!");
        classPath = System.getProperty("user.dir");
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  public static ClassLoader getLoader() {
    return loader;
  }


  public static String getClassPath() {
    return classPath;
  }
}
