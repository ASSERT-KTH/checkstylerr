package com.ctrip.apollo.internals;

import com.google.common.collect.Lists;

import com.ctrip.apollo.util.ExceptionUtil;
import com.dianping.cat.Cat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public abstract class AbstractConfigRepository implements ConfigRepository {
  private static final Logger logger = LoggerFactory.getLogger(AbstractConfigRepository.class);
  private List<RepositoryChangeListener> m_listeners = Lists.newCopyOnWriteArrayList();

  protected void trySync() {
    try {
      sync();
    } catch (Throwable ex) {
      Cat.logError(ex);
      logger
          .warn("Sync config failed with repository {}, reason: {}", this.getClass(), ExceptionUtil
              .getDetailMessage(ex));
    }
  }

  protected abstract void sync();

  @Override
  public void addChangeListener(RepositoryChangeListener listener) {
    if (!m_listeners.contains(listener)) {
      m_listeners.add(listener);
    }
  }

  @Override
  public void removeChangeListener(RepositoryChangeListener listener) {
    m_listeners.remove(listener);
  }

  protected void fireRepositoryChange(String namespace, Properties newProperties) {
    for (RepositoryChangeListener listener : m_listeners) {
      try {
        listener.onRepositoryChange(namespace, newProperties);
      } catch (Throwable ex) {
        Cat.logError(ex);
        logger.error("Failed to invoke repository change listener {}", listener.getClass(), ex);
      }
    }
  }
}
