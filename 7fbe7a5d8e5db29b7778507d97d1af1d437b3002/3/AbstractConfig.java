package com.ctrip.framework.apollo.internals;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.util.parser.Parsers;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public abstract class AbstractConfig implements Config {
  private static final Logger logger = LoggerFactory.getLogger(AbstractConfig.class);
  private static final String LONG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
  private static final String MEDIUM_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
  private static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";
  private static final String[] DATE_FORMATS = {LONG_DATE_FORMAT, MEDIUM_DATE_FORMAT,
      SHORT_DATE_FORMAT};

  private static ExecutorService m_executorService;
  private List<ConfigChangeListener> m_listeners = Lists.newCopyOnWriteArrayList();

  static {
    m_executorService = Executors.newCachedThreadPool(ApolloThreadFactory
        .create("Config", true));

  }

  @Override
  public void addChangeListener(ConfigChangeListener listener) {
    if (!m_listeners.contains(listener)) {
      m_listeners.add(listener);
    }
  }

  @Override
  public Integer getIntProperty(String key, Integer defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return Integer.parseInt(value);
      }
    } catch (Throwable ex) {
      Cat.logError(new ApolloConfigException(
          String.format("getIntProperty for %s failed, return default value %d", key,
              defaultValue), ex));
    }
    return defaultValue;
  }

  @Override
  public Long getLongProperty(String key, Long defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return Long.parseLong(value);
      }
    } catch (Throwable ex) {
      Cat.logError(new ApolloConfigException(
          String.format("getLongProperty for %s failed, return default value %d", key,
              defaultValue), ex));
    }
    return defaultValue;
  }

  @Override
  public Short getShortProperty(String key, Short defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return Short.parseShort(value);
      }
    } catch (Throwable ex) {
      Cat.logError(new ApolloConfigException(
          String.format("getShortProperty for %s failed, return default value %d", key,
              defaultValue), ex));
    }
    return defaultValue;
  }

  @Override
  public Float getFloatProperty(String key, Float defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return Float.parseFloat(value);
      }
    } catch (Throwable ex) {
      Cat.logError(new ApolloConfigException(
          String.format("getFloatProperty for %s failed, return default value %f", key,
              defaultValue), ex));
    }
    return defaultValue;
  }

  @Override
  public Double getDoubleProperty(String key, Double defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return Double.parseDouble(value);
      }
    } catch (Throwable ex) {
      Cat.logError(new ApolloConfigException(
          String.format("getDoubleProperty for %s failed, return default value %f", key,
              defaultValue), ex));
    }
    return defaultValue;
  }

  @Override
  public Byte getByteProperty(String key, Byte defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return Byte.parseByte(value);
      }
    } catch (Throwable ex) {
      Cat.logError(new ApolloConfigException(
          String.format("getByteProperty for %s failed, return default value %d", key,
              defaultValue), ex));
    }
    return defaultValue;
  }

  @Override
  public Boolean getBooleanProperty(String key, Boolean defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return Boolean.parseBoolean(value);
      }
    } catch (Throwable ex) {
      Cat.logError(new ApolloConfigException(
          String.format("getBooleanProperty for %s failed, return default value %b", key,
              defaultValue), ex));
    }
    return defaultValue;
  }

  @Override
  public String[] getArrayProperty(String key, String delimiter, String[] defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return value.split(delimiter);
      }
    } catch (Throwable ex) {
      Cat.logError(new ApolloConfigException(
          String.format("getArrayProperty for %s failed, return default value", key), ex));
    }
    return defaultValue;
  }

  @Override
  public <T extends Enum<T>> T getEnumProperty(String key, Class<T> enumType, T defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return Enum.valueOf(enumType, value);
      }
    } catch (Throwable ex) {
      Cat.logError(new ApolloConfigException(
          String.format("getEnumProperty for %s failed, return default value %s", key,
              defaultValue), ex));
    }

    return defaultValue;
  }

  @Override
  public Date getDateProperty(String key, Date defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return Parsers.forDate().parse(value);
      }
    } catch (Throwable ex) {
      Cat.logError(new ApolloConfigException(
          String.format("getDateProperty for %s failed, return default value %s", key,
              defaultValue), ex));
    }

    return defaultValue;
  }

  @Override
  public Date getDateProperty(String key, String format, Date defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return Parsers.forDate().parse(value, format);
      }
    } catch (Throwable ex) {
      Cat.logError(new ApolloConfigException(
          String.format("getDateProperty for %s failed, return default value %s", key,
              defaultValue), ex));
    }

    return defaultValue;
  }

  @Override
  public Date getDateProperty(String key, String format, Locale locale, Date defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return Parsers.forDate().parse(value, format, locale);
      }
    } catch (Throwable ex) {
      Cat.logError(new ApolloConfigException(
          String.format("getDateProperty for %s failed, return default value %s", key,
              defaultValue), ex));
    }

    return defaultValue;
  }

  @Override
  public long getDurationProperty(String key, long defaultValue) {
    try {
      String value = getProperty(key, null);

      if (value != null) {
        return Parsers.forDuration().parseToMillis(value);
      }
    } catch (Throwable ex) {
      Cat.logError(new ApolloConfigException(
          String.format("getDurationProperty for %s failed, return default value %d", key,
              defaultValue), ex));
    }

    return defaultValue;
  }

  protected void fireConfigChange(final ConfigChangeEvent changeEvent) {
    for (final ConfigChangeListener listener : m_listeners) {
      m_executorService.submit(new Runnable() {
        @Override
        public void run() {
          String listenerName = listener.getClass().getName();
          Transaction transaction = Cat.newTransaction("Apollo.ConfigChangeListener", listenerName);
          try {
            listener.onChange(changeEvent);
            transaction.setStatus(Message.SUCCESS);
          } catch (Throwable ex) {
            transaction.setStatus(ex);
            Cat.logError(ex);
            logger.error("Failed to invoke config change listener {}", listenerName, ex);
          } finally {
            transaction.complete();
          }
        }
      });
    }
  }

  List<ConfigChange> calcPropertyChanges(String namespace, Properties previous,
                                         Properties current) {
    if (previous == null) {
      previous = new Properties();
    }

    if (current == null) {
      current = new Properties();
    }

    Set<String> previousKeys = previous.stringPropertyNames();
    Set<String> currentKeys = current.stringPropertyNames();

    Set<String> commonKeys = Sets.intersection(previousKeys, currentKeys);
    Set<String> newKeys = Sets.difference(currentKeys, commonKeys);
    Set<String> removedKeys = Sets.difference(previousKeys, commonKeys);

    List<ConfigChange> changes = Lists.newArrayList();

    for (String newKey : newKeys) {
      changes.add(new ConfigChange(namespace, newKey, null, current.getProperty(newKey),
          PropertyChangeType.ADDED));
    }

    for (String removedKey : removedKeys) {
      changes.add(new ConfigChange(namespace, removedKey, previous.getProperty(removedKey), null,
          PropertyChangeType.DELETED));
    }

    for (String commonKey : commonKeys) {
      String previousValue = previous.getProperty(commonKey);
      String currentValue = current.getProperty(commonKey);
      if (Objects.equal(previousValue, currentValue)) {
        continue;
      }
      changes.add(new ConfigChange(namespace, commonKey, previousValue, currentValue,
          PropertyChangeType.MODIFIED));
    }

    return changes;
  }
}
