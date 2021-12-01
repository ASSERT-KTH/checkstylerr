package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.util.ExceptionUtil;
import com.dianping.cat.Cat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class PropertiesConfigFile extends AbstractConfigFile {
  private static final Logger logger = LoggerFactory.getLogger(PropertiesConfigFile.class);
  protected AtomicReference<String> m_contentCache;

  public PropertiesConfigFile(String namespace,
                              ConfigRepository configRepository) {
    super(namespace, configRepository);
    m_contentCache = new AtomicReference<>();
  }

  @Override
  public String getContent() {
    if (m_contentCache.get() == null) {
      m_contentCache.set(doGetContent());
    }
    return m_contentCache.get();
  }

  String doGetContent() {
    if (m_configProperties.get() == null) {
      return null;
    }
    StringWriter writer = new StringWriter();
    try {
      m_configProperties.get().store(writer, null);
      StringBuffer stringBuffer = writer.getBuffer();
      filterPropertiesComment(stringBuffer);
      return stringBuffer.toString();
    } catch (IOException ex) {
      ApolloConfigException exception =
          new ApolloConfigException(String
              .format("Parse properties file content failed for namespace: %s, cause: %s",
                  m_namespace, ExceptionUtil.getDetailMessage(ex)));
      Cat.logError(exception);
      throw exception;
    }
  }

  /**
   * filter out the first comment line
   * @param stringBuffer the string buffer
   * @return true if filtered successfully, false otherwise
   */
  boolean filterPropertiesComment(StringBuffer stringBuffer) {
    //check whether has comment in the first line
    if (stringBuffer.charAt(0) != '#') {
      return false;
    }
    int commentLineIndex = stringBuffer.indexOf("\n");
    if (commentLineIndex == -1) {
      return false;
    }
    stringBuffer.delete(0, commentLineIndex + 1);
    return true;
  }


  @Override
  public boolean hasContent() {
    return m_configProperties.get() != null && !m_configProperties.get().isEmpty();
  }

  @Override
  public ConfigFileFormat getConfigFileFormat() {
    return ConfigFileFormat.Properties;
  }

  @Override
  public synchronized void onRepositoryChange(String namespace, Properties newProperties) {
    super.onRepositoryChange(namespace, newProperties);
    m_contentCache.set(null);
  }
}
