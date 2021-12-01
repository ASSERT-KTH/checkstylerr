package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class XmlConfigFile extends AbstractConfigFile {
  public XmlConfigFile(String namespace,
                       ConfigRepository configRepository) {
    super(namespace, configRepository);
  }

  @Override
  public String getContent() {
    if (m_configProperties.get() == null) {
      return null;
    }
    return m_configProperties.get().getProperty(ConfigConsts.CONFIG_FILE_CONTENT_KEY);
  }

  @Override
  public boolean hasContent() {
    if (m_configProperties.get() == null) {
      return false;
    }
    return m_configProperties.get().containsKey(ConfigConsts.CONFIG_FILE_CONTENT_KEY);

  }

  @Override
  public ConfigFileFormat getConfigFileFormat() {
    return ConfigFileFormat.XML;
  }
}
