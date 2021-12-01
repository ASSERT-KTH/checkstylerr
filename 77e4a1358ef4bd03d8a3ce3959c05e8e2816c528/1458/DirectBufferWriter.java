/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.zeebe.util.buffer;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public final class DirectBufferWriter implements BufferWriter {
  protected DirectBuffer buffer;
  protected int offset;
  protected int length;

  @Override
  public int getLength() {
    return length;
  }

  @Override
  public void write(final MutableDirectBuffer writeBuffer, final int writeOffset) {
    writeBuffer.putBytes(writeOffset, buffer, offset, length);
  }

  public DirectBufferWriter wrap(final DirectBuffer buffer, final int offset, final int length) {
    this.buffer = buffer;
    this.offset = offset;
    this.length = length;

    return this;
  }

  public DirectBufferWriter wrap(final DirectBuffer buffer) {
    return wrap(buffer, 0, buffer.capacity());
  }

  public void reset() {
    buffer = null;
    offset = -1;
    length = 0;
  }
}
