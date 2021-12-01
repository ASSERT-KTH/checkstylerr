package com.ctrip.apollo.client.model;

import com.google.common.collect.Lists;

import org.springframework.core.env.CompositePropertySource;

import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class PropertySourceReloadResult {
  private CompositePropertySource propertySource;
  private List<PropertyChange> changes;

  public PropertySourceReloadResult(CompositePropertySource propertySource) {
    this.propertySource = propertySource;
    changes = Lists.newArrayList();
  }

  public PropertySourceReloadResult(CompositePropertySource propertySource,
                                    List<PropertyChange> changes) {
    this.propertySource = propertySource;
    this.changes = changes;
  }

  public CompositePropertySource getPropertySource() {
    return propertySource;
  }

  public void setPropertySource(CompositePropertySource propertySource) {
    this.propertySource = propertySource;
  }

  public List<PropertyChange> getChanges() {
    return changes;
  }

  public void setChanges(List<PropertyChange> changes) {
    this.changes = changes;
  }

  public boolean hasChanges() {
    return !changes.isEmpty();
  }
}
