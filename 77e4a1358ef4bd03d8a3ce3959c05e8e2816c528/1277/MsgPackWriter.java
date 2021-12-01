/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.zeebe.msgpack.spec;

import static io.zeebe.msgpack.spec.MsgPackCodes.ARRAY16;
import static io.zeebe.msgpack.spec.MsgPackCodes.ARRAY32;
import static io.zeebe.msgpack.spec.MsgPackCodes.BIN16;
import static io.zeebe.msgpack.spec.MsgPackCodes.BIN32;
import static io.zeebe.msgpack.spec.MsgPackCodes.BIN8;
import static io.zeebe.msgpack.spec.MsgPackCodes.BYTE_ORDER;
import static io.zeebe.msgpack.spec.MsgPackCodes.FALSE;
import static io.zeebe.msgpack.spec.MsgPackCodes.FIXARRAY_PREFIX;
import static io.zeebe.msgpack.spec.MsgPackCodes.FIXMAP_PREFIX;
import static io.zeebe.msgpack.spec.MsgPackCodes.FIXSTR_PREFIX;
import static io.zeebe.msgpack.spec.MsgPackCodes.FLOAT32;
import static io.zeebe.msgpack.spec.MsgPackCodes.FLOAT64;
import static io.zeebe.msgpack.spec.MsgPackCodes.INT16;
import static io.zeebe.msgpack.spec.MsgPackCodes.INT32;
import static io.zeebe.msgpack.spec.MsgPackCodes.INT64;
import static io.zeebe.msgpack.spec.MsgPackCodes.INT8;
import static io.zeebe.msgpack.spec.MsgPackCodes.MAP16;
import static io.zeebe.msgpack.spec.MsgPackCodes.MAP32;
import static io.zeebe.msgpack.spec.MsgPackCodes.NIL;
import static io.zeebe.msgpack.spec.MsgPackCodes.STR16;
import static io.zeebe.msgpack.spec.MsgPackCodes.STR32;
import static io.zeebe.msgpack.spec.MsgPackCodes.STR8;
import static io.zeebe.msgpack.spec.MsgPackCodes.TRUE;
import static io.zeebe.msgpack.spec.MsgPackCodes.UINT16;
import static io.zeebe.msgpack.spec.MsgPackCodes.UINT32;
import static io.zeebe.msgpack.spec.MsgPackCodes.UINT64;
import static io.zeebe.msgpack.spec.MsgPackCodes.UINT8;
import static org.agrona.BitUtil.SIZE_OF_BYTE;
import static org.agrona.BitUtil.SIZE_OF_DOUBLE;
import static org.agrona.BitUtil.SIZE_OF_FLOAT;
import static org.agrona.BitUtil.SIZE_OF_INT;
import static org.agrona.BitUtil.SIZE_OF_LONG;
import static org.agrona.BitUtil.SIZE_OF_SHORT;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * This class uses signed value semantics. That means, an integer 0xffff_ffff is treated as -1
 * instead of 2^33 - 1, etc.
 */
public final class MsgPackWriter {
  private MutableDirectBuffer buffer;
  private int offset;

  public MsgPackWriter wrap(final MutableDirectBuffer buffer, final int offset) {
    this.buffer = buffer;
    this.offset = offset;

    return this;
  }

  public MsgPackWriter writeArrayHeader(final int size) {
    ensurePositive(size);

    if (size < (1 << 4)) {
      buffer.putByte(offset, (byte) (FIXARRAY_PREFIX | size));
      ++offset;
    } else if (size < (1 << 16)) {
      buffer.putByte(offset, ARRAY16);
      ++offset;

      buffer.putShort(offset, (short) size, BYTE_ORDER);
      offset += SIZE_OF_SHORT;
    } else {
      buffer.putByte(offset, ARRAY32);
      ++offset;

      buffer.putInt(offset, size, BYTE_ORDER);
      offset += SIZE_OF_INT;
    }

    return this;
  }

  public MsgPackWriter writeMapHeader(final int size) {
    ensurePositive(size);

    if (size < (1 << 4)) {
      buffer.putByte(offset, (byte) (FIXMAP_PREFIX | size));
      ++offset;
    } else if (size < (1 << 16)) {
      buffer.putByte(offset, MAP16);
      ++offset;

      buffer.putShort(offset, (short) size, MsgPackCodes.BYTE_ORDER);
      offset += SIZE_OF_SHORT;
    } else {
      offset = writeMap32Header(offset, size);
    }

    return this;
  }

  private int writeMap32Header(int offset, final int size) {
    buffer.putByte(offset, MAP32);
    ++offset;

    buffer.putInt(offset, size, MsgPackCodes.BYTE_ORDER);
    offset += SIZE_OF_INT;

    return offset;
  }

  /**
   * use this method if the map size is not known upfront. Record the offset before calling this
   * method and then use {@link #writeReservedMapHeader(int, int)} later.
   */
  public void reserveMapHeader() {
    offset = writeMap32Header(offset, 0);
  }

  /** does not change the writer's offset */
  public void writeReservedMapHeader(final int offset, final int size) {
    writeMap32Header(offset, size);
  }

  public MsgPackWriter writeRaw(final DirectBuffer buffer) {
    return writeRaw(buffer, 0, buffer.capacity());
  }

  public MsgPackWriter writeRaw(final DirectBuffer buff, final int offset, final int length) {
    buffer.putBytes(this.offset, buff, offset, length);
    this.offset += length;

    return this;
  }

  public MsgPackWriter writeString(final DirectBuffer bytes) {
    return writeString(bytes, 0, bytes.capacity());
  }

  public MsgPackWriter writeString(final DirectBuffer buff, final int offset, final int length) {
    writeStringHeader(length);
    writeRaw(buff, offset, length);
    return this;
  }

  /**
   * Integer is the term in the msgpack spec for all natural numbers
   *
   * @param v value to write
   * @return this object
   */
  public MsgPackWriter writeInteger(final long v) {
    if (v < -(1L << 5)) {
      if (v < -(1L << 15)) {
        if (v < -(1L << 31)) {
          buffer.putByte(offset, INT64);
          ++offset;
          buffer.putLong(offset, v, BYTE_ORDER);
          offset += SIZE_OF_LONG;
        } else {
          buffer.putByte(offset, INT32);
          ++offset;
          buffer.putInt(offset, (int) v, BYTE_ORDER);
          offset += SIZE_OF_INT;
        }
      } else {
        if (v < -(1 << 7)) {
          buffer.putByte(offset, INT16);
          ++offset;
          buffer.putShort(offset, (short) v, BYTE_ORDER);
          offset += SIZE_OF_SHORT;
        } else {
          buffer.putByte(offset, INT8);
          ++offset;
          buffer.putByte(offset, (byte) v);
          ++offset;
        }
      }
    } else if (v < (1 << 7)) {
      buffer.putByte(offset, (byte) v);
      ++offset;
    } else {
      if (v < (1L << 16)) {
        if (v < (1 << 8)) {
          buffer.putByte(offset, UINT8);
          ++offset;
          buffer.putByte(offset, (byte) v);
          ++offset;
        } else {
          buffer.putByte(offset, UINT16);
          ++offset;
          buffer.putShort(offset, (short) v, BYTE_ORDER);
          offset += SIZE_OF_SHORT;
        }
      } else {
        if (v < (1L << 32)) {
          buffer.putByte(offset, UINT32);
          ++offset;
          buffer.putInt(offset, (int) v, BYTE_ORDER);
          offset += SIZE_OF_INT;
        } else {
          buffer.putByte(offset, UINT64);
          ++offset;
          buffer.putLong(offset, v, BYTE_ORDER);
          offset += SIZE_OF_LONG;
        }
      }
    }
    return this;
  }

  public MsgPackWriter writeStringHeader(final int len) {
    ensurePositive(len);
    if (len < (1 << 5)) {
      buffer.putByte(offset, (byte) (FIXSTR_PREFIX | len));
      ++offset;
    } else if (len < (1 << 8)) {
      buffer.putByte(offset, STR8);
      ++offset;

      buffer.putByte(offset, (byte) len);
      ++offset;
    } else if (len < (1 << 16)) {
      buffer.putByte(offset, STR16);
      ++offset;

      buffer.putShort(offset, (short) len, BYTE_ORDER);
      offset += SIZE_OF_SHORT;
    } else {
      buffer.putByte(offset, STR32);
      ++offset;

      buffer.putInt(offset, len, BYTE_ORDER);
      offset += SIZE_OF_INT;
    }

    return this;
  }

  public MsgPackWriter writeBinary(final DirectBuffer data) {
    return writeBinary(data, 0, data.capacity());
  }

  public MsgPackWriter writeBinary(final DirectBuffer data, final int offset, final int length) {
    writeBinaryHeader(length);
    writeRaw(data, offset, length);
    return this;
  }

  public MsgPackWriter writeBinaryHeader(final int len) {
    ensurePositive(len);
    if (len < (1 << 8)) {
      buffer.putByte(offset, BIN8);
      ++offset;

      buffer.putByte(offset, (byte) len);
      ++offset;
    } else if (len < (1 << 16)) {
      buffer.putByte(offset, BIN16);
      ++offset;

      buffer.putShort(offset, (short) len, BYTE_ORDER);
      offset += SIZE_OF_SHORT;
    } else {
      buffer.putByte(offset, BIN32);
      ++offset;

      buffer.putInt(offset, len, BYTE_ORDER);
      offset += SIZE_OF_INT;
    }

    return this;
  }

  public MsgPackWriter writeBoolean(final boolean val) {
    buffer.putByte(offset, val ? TRUE : FALSE);
    ++offset;

    return this;
  }

  public MsgPackWriter writeNil() {
    buffer.putByte(offset, NIL);
    ++offset;

    return this;
  }

  /**
   * Float is the term in the msgpack spec
   *
   * @param value to write
   * @return this object
   */
  public MsgPackWriter writeFloat(final double value) {

    final float floatValue = (float) value;

    if ((double) floatValue == value) {
      buffer.putByte(offset, FLOAT32);
      ++offset;

      buffer.putFloat(offset, floatValue, BYTE_ORDER);
      offset += SIZE_OF_FLOAT;
    } else {
      buffer.putByte(offset, FLOAT64);
      ++offset;

      buffer.putDouble(offset, value, BYTE_ORDER);
      offset += SIZE_OF_DOUBLE;
    }

    return this;
  }

  public int getOffset() {
    return offset;
  }

  public static int getEncodedMapHeaderLenght(final int size) {
    final int length;

    if (size < (1 << 4)) {
      length = SIZE_OF_BYTE;
    } else if (size < (1 << 16)) {
      length = SIZE_OF_BYTE + SIZE_OF_SHORT;
    } else {
      length = SIZE_OF_BYTE + SIZE_OF_INT;
    }

    return length;
  }

  public static int getEncodedArrayHeaderLenght(final int size) {
    final int length;

    if (size < (1 << 4)) {
      length = SIZE_OF_BYTE;
    } else if (size < (1 << 16)) {
      length = SIZE_OF_BYTE + SIZE_OF_SHORT;
    } else {
      length = SIZE_OF_BYTE + SIZE_OF_INT;
    }

    return length;
  }

  public static int getEncodedStringHeaderLength(final int len) {
    final int encodedLength;

    if (len < (1 << 5)) {
      encodedLength = SIZE_OF_BYTE;
    } else if (len < (1 << 8)) {
      encodedLength = SIZE_OF_BYTE + SIZE_OF_BYTE;
    } else if (len < (1 << 16)) {
      encodedLength = SIZE_OF_BYTE + SIZE_OF_SHORT;
    } else {
      encodedLength = SIZE_OF_BYTE + SIZE_OF_INT;
    }

    return encodedLength;
  }

  public static int getEncodedStringLength(final int len) {
    return getEncodedStringHeaderLength(len) + len;
  }

  public static int getEncodedLongValueLength(final long v) {
    int length = 1;

    if (v < -(1L << 5)) {
      if (v < -(1L << 15)) {
        if (v < -(1L << 31)) {
          length += SIZE_OF_LONG;
        } else {
          length += SIZE_OF_INT;
        }
      } else {
        if (v < -(1 << 7)) {
          length += SIZE_OF_SHORT;
        } else {
          length += SIZE_OF_BYTE;
        }
      }
    } else if (v >= (1 << 7)) {
      if (v < (1L << 16)) {
        if (v < (1 << 8)) {
          length += SIZE_OF_BYTE;
        } else {
          length += SIZE_OF_SHORT;
        }
      } else {
        if (v < (1L << 32)) {
          length += SIZE_OF_INT;
        } else {
          length += SIZE_OF_LONG;
        }
      }
    }

    return length;
  }

  public static int getEncodedBooleanValueLength() {
    return 1;
  }

  public static int getEncodedBinaryValueLength(final int len) {
    final int headerLength;

    if (len < (1 << 8)) {
      headerLength = SIZE_OF_BYTE + SIZE_OF_BYTE;
    } else if (len < (1 << 16)) {
      headerLength = SIZE_OF_BYTE + SIZE_OF_SHORT;
    } else {
      headerLength = SIZE_OF_BYTE + SIZE_OF_INT;
    }

    return headerLength + len;
  }

  private void ensurePositive(final long size) {
    try {
      MsgPackHelper.ensurePositive(size);
    } catch (final MsgpackException e) {
      throw new MsgpackWriterException(e);
    }
  }
}
