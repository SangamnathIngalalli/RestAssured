package com.sqs.services;

import com.sqs.utils.SqsUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.List;

/**
 * SqsService is a thread-safe singleton class that provides methods to interact with
 * Amazon SQS. It uses SqsUtils to perform operations like sending, receiving,
 * and deleting messages from an SQS queue.
 */
public class SqsService {
    // Thread-safe singleton pattern
    private static final ThreadLocal<SqsService> instance = ThreadLocal.withInitial(SqsService::new);
    private SqsUtils sqsUtils; // Utility class for SQS operations
    private String queueUrl;
    private Region awsRegion;

    /**
     * Private constructor to prevent instantiation from outside the class.
     * This is part of the singleton pattern.
     */
    private SqsService() {
        // Private constructor to prevent instantiation
    }

    /**
     * Returns the thread-local singleton instance of SqsService.
     *
     * @return the thread-local singleton instance of SqsService
     */
    public static SqsService getInstance() {
        return instance.get();
    }

    /**
     * Initializes the SqsService with the specified queue URL and AWS region.
     * This method must be called before using other methods.
     *
     * @param queueUrl the URL of the SQS queue
     * @param awsRegion the AWS region where the SQS queue is located
     */
    public void initialize(String queueUrl, Region awsRegion) {
        this.queueUrl = queueUrl;
        this.awsRegion = awsRegion;
        this.sqsUtils = new SqsUtils(queueUrl, awsRegion);
    }

    /**
     * Sends a message to the SQS queue.
     *
     * @param messageBody the body of the message to be sent
     * @return the response from the send message operation
     */
    public SendMessageResponse sendMessage(String messageBody) {
        System.out.println("Sending message to queue: " + queueUrl);
        return sqsUtils.sendMessage(messageBody);
    }

    /**
     * Receives messages from the SQS queue.
     *
     * @return a list of messages received from the queue
     */
    public List<Message> receiveMessages() {
        System.out.println("Receiving messages from queue: " + queueUrl);
        return sqsUtils.receiveMessages();
    }

    /**
     * Deletes a message from the SQS queue.
     *
     * @param message the message to be deleted
     */
    public void deleteMessage(Message message) {
        sqsUtils.deleteMessage(message);
    }

    /**
     * Closes the SQS client and releases any resources.
     * This method should be called when the service is no longer needed.
     */
    public void close() {
        if (sqsUtils != null) {
            sqsUtils.close();
        }
    }
}