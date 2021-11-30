package com.example;

import com.amazonaws.services.sqs.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Very much inspired by https://github.com/square/tape
 */
public class FileQueue {


    /** Backing storage implementation. */
    private final QueueFile queueFile;

    /** Reusable byte output buffer. */
    private final DirectByteArrayOutputStream bytes = new DirectByteArrayOutputStream();

    public FileQueue(File file) throws IOException {
        this.queueFile = new QueueFile(file);
    }

    public void add(String messageBody) throws IOException {
        bytes.reset();
        bytes.write(messageBody.getBytes(StandardCharsets.UTF_8));
        queueFile.add(bytes.getArray(), 0, bytes.size());
    }

    public Message poll() throws IOException {

        String randomUuid = UUID.randomUUID().toString();
        String messageBody = this.peek();

        return new Message()
                .withMessageId(randomUuid)
                .withBody(messageBody)
                .withReceiptHandle(randomUuid);
    }

    private String peek() throws IOException {
        byte[] bytes = queueFile.peek();
        this.remove();
        return  (bytes == null) ? null : new String(bytes, StandardCharsets.UTF_8);
    }

    public int size() {
        return queueFile.size();
    }

    public final void remove() throws IOException {
        queueFile.remove();
    }

    public final void close() {
        try {
            queueFile.close();
        } catch (IOException e) {
            // TODO: Need to handle this
            e.printStackTrace();
        }
    }

    /** Enables direct access to the internal array. Avoids unnecessary copying. */
    private static class DirectByteArrayOutputStream extends ByteArrayOutputStream {

        public DirectByteArrayOutputStream() {
            super();
        }

        /**
         * Gets a reference to the internal byte array.  The {@link #size()} method indicates how many
         * bytes contain actual data added since the last {@link #reset()} call.
         */
        public byte[] getArray() {
            return buf;
        }
    }
}
