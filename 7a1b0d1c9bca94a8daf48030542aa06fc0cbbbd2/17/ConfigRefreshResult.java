package com.ctrip.apollo.model;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigRefreshResult {
  private String m_namespace;
  private Properties m_properties;
  private List<PropertyChange> m_changes;

  public ConfigRefreshResult(String namespace, Properties properties) {
    this.m_namespace = namespace;
    this.m_properties = properties;
    m_changes = Lists.newArrayList();
  }

  public Properties getProperties() {
    return m_properties;
  }

  public String getNamespace() {
    return m_namespace;
  }

  public List<PropertyChange> getChanges() {
    return m_changes;
  }

  public void setChanges(List<PropertyChange> changes) {
    this.m_changes = changes;
  }

  public boolean hasChanges() {
    return !m_changes.isEmpty();
  }
}
