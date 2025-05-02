package com.sqs.utils;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

public class SqsUtils {

    private SqsClient sqsClient;
    private String queueUrl;

    // Constructor - Initialize SQS client and queue URL
    // AWS Credentials should be configured securely (e.g., environment variables, IAM role)
    public SqsUtils(String queueUrl, Region region) {
        this.queueUrl = queueUrl;
        // Ensure your AWS credentials are configured (e.g., environment variables, default profile)
        this.sqsClient = SqsClient.builder()
                                  .region(region)
                                  .build();
    }

    // Method to send a JSON message
    public SendMessageResponse sendMessage(String messageBody) {
        SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(messageBody)
            .build();
        System.out.println("Sending message to SQS: " + messageBody);
        return sqsClient.sendMessage(sendMsgRequest);
    }

    // Method to receive messages (simulating pod1)
    public List<Message> receiveMessages() {
        System.out.println("Receiving messages from SQS...");
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(1) // Adjust as needed
            .waitTimeSeconds(5)     // Use long polling
            .build();
        List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
        System.out.println("Received " + messages.size() + " messages.");
        return messages;
    }

    // Method to delete a message after processing
    public void deleteMessage(Message message) {
        System.out.println("Deleting message: " + message.messageId());
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
            .queueUrl(queueUrl)
            .receiptHandle(message.receiptHandle())
            .build();
        sqsClient.deleteMessage(deleteMessageRequest);
    }

    // Close the SQS client when done
    public void close() {
        if (sqsClient != null) {
            sqsClient.close();
        }
    }
}