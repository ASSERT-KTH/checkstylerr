/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.zeebe.dispatcher;

import static io.zeebe.dispatcher.impl.log.DataFrameDescriptor.HEADER_LENGTH;
import static io.zeebe.dispatcher.impl.log.DataFrameDescriptor.TYPE_PADDING;
import static io.zeebe.dispatcher.impl.log.DataFrameDescriptor.lengthOffset;
import static io.zeebe.dispatcher.impl.log.DataFrameDescriptor.typeOffset;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

/**
 * Represents a claimed fragment in the buffer.
 *
 * <p>Reusable but not threadsafe.
 */
public class ClaimedFragment {
  protected final UnsafeBuffer buffer;

  private Runnable onCompleteHandler;

  public ClaimedFragment() {
    buffer = new UnsafeBuffer(0, 0);
  }

  public void wrap(
      final UnsafeBuffer underlyingbuffer,
      final int fragmentOffset,
      final int fragmentLength,
      final Runnable onCompleteHandler) {
    this.onCompleteHandler = onCompleteHandler;
    buffer.wrap(underlyingbuffer, fragmentOffset, fragmentLength);
  }

  public int getOffset() {
    return HEADER_LENGTH;
  }

  public int getLength() {
    return buffer.capacity() - HEADER_LENGTH;
  }

  public int getFragmentLength() {
    return buffer.capacity();
  }

  /** Returns the claimed fragment to write in. */
  public MutableDirectBuffer getBuffer() {
    return buffer;
  }

  /** Commit the fragment so that it can be read by subscriptions. */
  public void commit() {
    // commit the message by writing the positive framed length
    buffer.putIntOrdered(lengthOffset(0), buffer.capacity());
    onCompleteHandler.run();
    reset();
  }

  /** Commit the fragment and mark it as failed. It will be ignored by subscriptions. */
  public void abort() {
    // abort the message by setting type to padding and writing the positive framed length
    buffer.putInt(typeOffset(0), TYPE_PADDING);
    buffer.putIntOrdered(lengthOffset(0), buffer.capacity());
    onCompleteHandler.run();
    reset();
  }

  private void reset() {
    buffer.wrap(0, 0);
    onCompleteHandler = null;
  }

  public boolean isOpen() {
    return getFragmentLength() > 0;
  }
}
