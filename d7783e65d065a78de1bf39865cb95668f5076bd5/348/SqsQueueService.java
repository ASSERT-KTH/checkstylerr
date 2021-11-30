package com.example;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;

public class SqsQueueService implements QueueService {

    //
    // Task 4: Optionally implement parts of me.
    //
    // This file is a placeholder for an AWS-backed implementation of QueueService.  It is included
    // primarily so you can quickly assess your choices for method signatures in QueueService in
    // terms of how well they map to the implementation intended for a production environment.

    private AmazonSQSClient sqsClient;

    public SqsQueueService(AmazonSQSClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    @Override
    public void sendMessage(String queueUrl, String messageBody) {
        SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, messageBody);
        this.sqsClient.sendMessage(sendMessageRequest);
    }

    @Override
    public Message receiveMessage(String queueUrl) {
        // As per specs, we want to receive only a single message
        ReceiveMessageRequest request = new ReceiveMessageRequest().withQueueUrl(queueUrl).withMaxNumberOfMessages(1);
        ReceiveMessageResult receiveMessageResult = this.sqsClient.receiveMessage(request);
        return receiveMessageResult.getMessages().get(0);
    }

    @Override
    public void deleteMessage(String queueUrl, String receiptHandle) {
        sqsClient.deleteMessage(queueUrl, receiptHandle);
    }
}
