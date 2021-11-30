package com.example;

import com.amazonaws.services.sqs.model.Message;

import java.util.*;
import java.util.concurrent.*;

public class InMemoryQueue {

    /**
    * TODO: Outstanding tasks :)
    *   Other queue attributes, ie. `MaximumMessageSize`, `MessageRetentionPeriod`, etc.
    *    - Their actual implementation
    *    - Need to set default values
    *    - Implement error checking for AWS allowed range of values of other queue attributes,
    *           also should throw the same exception classes
    *  Change `visibilityTimeout`
    */

    private int visibilityTimeout = 30;
    private int maximumMessageSize = 262144;
    private int messageRetentionPeriod = 345600;

    private ScheduledExecutorService executorService;
    private BlockingQueue<String> messages = new LinkedBlockingQueue<>();
    private ConcurrentMap<String, ScheduledFuture> inFlightMessages = new ConcurrentHashMap<>();

    public InMemoryQueue(ScheduledExecutorService executorService) {
        this.visibilityTimeout = 30;
        this.executorService = executorService;
    }

    public InMemoryQueue withVisibilityTimeout(int visibilityTimeout) {
        this.visibilityTimeout = visibilityTimeout;
        return this;
    }

    public void add(String messageBody) {
        messages.add(messageBody);
    }

    public Message poll() {
        String randomUuid = UUID.randomUUID().toString();
        String messageBody = messages.poll();

        // Schedule re-insertion of the message from the queue in the future
        Runnable runnable = () -> {
            messages.add(messageBody);
            inFlightMessages.remove(randomUuid);
        };
        ScheduledFuture future = executorService.schedule(runnable, (long) this.visibilityTimeout, TimeUnit.SECONDS);

        // And save that into our in-flight map
        inFlightMessages.put(randomUuid, future);

        return new Message()
            .withMessageId(randomUuid)
            .withBody(messageBody)
            .withReceiptHandle(randomUuid);
    }

    public void delete(String receiptHandle) {
        ScheduledFuture future = inFlightMessages.remove(receiptHandle);
        if (future == null) {
            throw new RuntimeException("Message does not exists");
        }

        // Cancel the re-insertion of the message in future
        future.cancel(true);
    }

    public int size() {
        return messages.size();
    }

    public int inFlightSize() {
        return inFlightMessages.size();
    }
}
