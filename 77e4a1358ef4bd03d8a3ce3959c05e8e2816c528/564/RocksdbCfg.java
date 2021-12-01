/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.zeebe.broker.system.configuration;

import io.zeebe.db.impl.rocksdb.RocksDbConfiguration;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;
import org.springframework.util.unit.DataSize;

public final class RocksdbCfg implements ConfigurationEntry {

  private Properties columnFamilyOptions;
  private boolean enableStatistics = RocksDbConfiguration.DEFAULT_STATISTICS_ENABLED;
  private DataSize memoryLimit = DataSize.ofBytes(RocksDbConfiguration.DEFAULT_MEMORY_LIMIT);
  private int maxOpenFiles = RocksDbConfiguration.DEFAULT_UNLIMITED_MAX_OPEN_FILES;
  private int maxWriteBufferNumber = RocksDbConfiguration.DEFAULT_MAX_WRITE_BUFFER_NUMBER;
  private int minWriteBufferNumberToMerge =
      RocksDbConfiguration.DEFAULT_MIN_WRITE_BUFFER_NUMBER_TO_MERGE;
  private int ioRateBytesPerSecond = RocksDbConfiguration.DEFAULT_IO_RATE_BYTES_PER_SECOND;
  private boolean disableWal = RocksDbConfiguration.DEFAULT_WAL_DISABLED;

  @Override
  public void init(final BrokerCfg globalConfig, final String brokerBase) {
    if (columnFamilyOptions == null) {
      // lazy init to not have to deal with null when using these options
      columnFamilyOptions = new Properties();
    } else {
      // Since (some of) the columnFamilyOptions may have been provided as environment variables,
      // we must do some transformations on the entries of this properties object.
      columnFamilyOptions = initColumnFamilyOptions(columnFamilyOptions);
    }
  }

  private static Properties initColumnFamilyOptions(final Properties original) {
    final var result = new Properties();
    original.entrySet().stream()
        .map(RocksDBColumnFamilyOption::new)
        .forEach(entry -> result.put(entry.key, entry.value));
    return result;
  }

  public Properties getColumnFamilyOptions() {
    return columnFamilyOptions;
  }

  public void setColumnFamilyOptions(final Properties columnFamilyOptions) {
    this.columnFamilyOptions = columnFamilyOptions;
  }

  public boolean isEnableStatistics() {
    return enableStatistics;
  }

  public void setEnableStatistics(final boolean enableStatistics) {
    this.enableStatistics = enableStatistics;
  }

  public DataSize getMemoryLimit() {
    return memoryLimit;
  }

  public void setMemoryLimit(final DataSize memoryLimit) {
    this.memoryLimit = memoryLimit;
  }

  public int getMaxOpenFiles() {
    return maxOpenFiles;
  }

  public void setMaxOpenFiles(final int maxOpenFiles) {
    this.maxOpenFiles = maxOpenFiles;
  }

  public int getMaxWriteBufferNumber() {
    return maxWriteBufferNumber;
  }

  public void setMaxWriteBufferNumber(final int maxWriteBufferNumber) {
    this.maxWriteBufferNumber = maxWriteBufferNumber;
  }

  public int getMinWriteBufferNumberToMerge() {
    return minWriteBufferNumberToMerge;
  }

  public void setMinWriteBufferNumberToMerge(final int minWriteBufferNumberToMerge) {
    this.minWriteBufferNumberToMerge = minWriteBufferNumberToMerge;
  }

  public int getIoRateBytesPerSecond() {
    return ioRateBytesPerSecond;
  }

  public void setIoRateBytesPerSecond(final int ioRateBytesPerSecond) {
    this.ioRateBytesPerSecond = ioRateBytesPerSecond;
  }

  public boolean isDisableWal() {
    return disableWal;
  }

  public void setDisableWal(final boolean disableWal) {
    this.disableWal = disableWal;
  }

  public RocksDbConfiguration createRocksDbConfiguration() {
    return new RocksDbConfiguration()
        .setColumnFamilyOptions(columnFamilyOptions)
        .setMaxOpenFiles(maxOpenFiles)
        .setMaxWriteBufferNumber(maxWriteBufferNumber)
        .setMemoryLimit(memoryLimit.toBytes())
        .setMinWriteBufferNumberToMerge(minWriteBufferNumberToMerge)
        .setStatisticsEnabled(enableStatistics)
        .setIoRateBytesPerSecond(ioRateBytesPerSecond)
        .setWalDisabled(disableWal);
  }

  @Override
  public String toString() {
    return "RocksdbCfg{"
        + "columnFamilyOptions="
        + columnFamilyOptions
        + ", enableStatistics="
        + enableStatistics
        + ", memoryLimit="
        + memoryLimit
        + ", maxOpenFiles="
        + maxOpenFiles
        + ", maxWriteBufferNumber="
        + maxWriteBufferNumber
        + ", minWriteBufferNumberToMerge="
        + minWriteBufferNumberToMerge
        + ", ioRateBytesPerSecond="
        + ioRateBytesPerSecond
        + ", disableWal="
        + disableWal
        + '}';
  }

  private static final class RocksDBColumnFamilyOption {

    private static final Pattern DOT_CHAR_PATTERN = Pattern.compile("\\.");
    private static final String UNDERSCORE_CHAR = "_";
    private final String key;
    private final Object value;

    private RocksDBColumnFamilyOption(final Entry<Object, Object> entry) {
      Objects.requireNonNull(entry.getKey());
      // The key of the entry may contain dot chars when provided as an Environment Variable.
      // These should always be replaced with underscores to match the available property names.
      // For example:
      // `ZEEBE_BROKER_EXPERIMENTAL_ROCKSDB_COLUMNFAMILYOPTIONS_WRITE_BUFFER_SIZE=8388608`
      // would result in a key with name `write.buffer.size`, but should be `write_buffer_size`.
      key = replaceAllDotCharsWithUnderscore(entry.getKey().toString());
      value = entry.getValue(); // the value can stay the same
    }

    private static String replaceAllDotCharsWithUnderscore(final String key) {
      return DOT_CHAR_PATTERN.matcher(key).replaceAll(UNDERSCORE_CHAR);
    }
  }
}
