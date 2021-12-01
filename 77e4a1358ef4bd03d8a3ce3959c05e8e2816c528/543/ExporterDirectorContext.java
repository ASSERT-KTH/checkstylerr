/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.zeebe.broker.exporter.stream;

import io.zeebe.broker.exporter.repo.ExporterDescriptor;
import io.zeebe.db.ZeebeDb;
import io.zeebe.logstreams.log.LogStream;
import java.util.Collection;

public final class ExporterDirectorContext {

  private int id;
  private String name;
  private LogStream logStream;
  private Collection<ExporterDescriptor> descriptors;
  private ZeebeDb zeebeDb;

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public LogStream getLogStream() {
    return logStream;
  }

  public Collection<ExporterDescriptor> getDescriptors() {
    return descriptors;
  }

  public ZeebeDb getZeebeDb() {
    return zeebeDb;
  }

  public ExporterDirectorContext id(final int id) {
    this.id = id;
    return this;
  }

  public ExporterDirectorContext name(final String name) {
    this.name = name;
    return this;
  }

  public ExporterDirectorContext logStream(final LogStream logStream) {
    this.logStream = logStream;
    return this;
  }

  public ExporterDirectorContext descriptors(final Collection<ExporterDescriptor> descriptors) {
    this.descriptors = descriptors;
    return this;
  }

  public ExporterDirectorContext zeebeDb(final ZeebeDb zeebeDb) {
    this.zeebeDb = zeebeDb;
    return this;
  }
}
