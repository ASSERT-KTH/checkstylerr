/*
 * Copyright (c) 2017, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: FixedLengthFrameDecoder.java
 * Date: 2017-11-25
 * Author: sandao
 */

package org.smartboot.socket.extension.decoder;

import java.nio.ByteBuffer;

/**
 * 指定长度的解码器
 *
 * @author 三刀
 * @version V1.0 , 2017/10/20
 */
public class FixedLengthFrameDecoder implements SmartDecoder{
    private ByteBuffer buffer;
    private boolean finishRead;

    public FixedLengthFrameDecoder(int frameLength) {
        if (frameLength <= 0) {
            throw new IllegalArgumentException("frameLength must be a positive integer: " + frameLength);
        } else {
            buffer = ByteBuffer.allocate(frameLength);
        }
    }

    public boolean decode(ByteBuffer byteBuffer) {
        if (finishRead) {
            throw new RuntimeException("delimiter has finish read");
        }
        if (buffer.remaining() >= byteBuffer.remaining()) {
            buffer.put(byteBuffer);
        } else {
            int limit = byteBuffer.limit();
            byteBuffer.limit(byteBuffer.position() + buffer.remaining());
            buffer.put(byteBuffer);
            byteBuffer.limit(limit);
        }

        if (buffer.hasRemaining()) {
            return false;
        }
        buffer.flip();
        finishRead = true;
        return true;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}
