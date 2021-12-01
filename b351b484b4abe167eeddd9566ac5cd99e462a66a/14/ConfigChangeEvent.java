package com.ctrip.apollo.model;

import java.util.Map;
import java.util.Set;

/**
 * A change event when a namespace's config is changed.
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigChangeEvent {
  private final String m_namespace;
  private final Map<String, ConfigChange> changes;

  /**
   * Constructor.
   * @param namespace the namespace of this change
   * @param changes the actual changes
   */
  public ConfigChangeEvent(String namespace,
                           Map<String, ConfigChange> changes) {
    this.m_namespace = namespace;
    this.changes = changes;
  }

  /**
   * Get the keys changed.
   * @return the list of the keys
   */
  public Set<String> changedKeys() {
    return changes.keySet();
  }

  /**
   * Get a specific change instance for the key specified.
   * @param key the changed key
   * @return the change instance
   */
  public ConfigChange getChange(String key) {
    return changes.get(key);
  }

  /**
   * Get the changes. Please note that the returned Map is immutable.
   * @return changes
   */
  public Map<String, ConfigChange> getChanges() {
    return changes;
  }

  /**
   * Get the namespace of this change event.
   * @return the namespace
   */
  public String getNamespace() {
    return m_namespace;
  }
}
