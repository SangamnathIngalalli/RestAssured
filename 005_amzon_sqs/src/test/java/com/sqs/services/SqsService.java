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
 * <p>
 * This service implements the thread-local singleton pattern to ensure thread safety
 * in multi-threaded test environments. Each thread gets its own instance of the service,
 * preventing concurrency issues when multiple tests run in parallel.
 * <p>
 * Usage example:
 * <pre>
 * SqsService service = SqsService.getInstance();
 * service.initialize("https://sqs.us-east-1.amazonaws.com/123456789012/my-queue", Region.US_EAST_1);
 * service.sendMessage("{\"key\":\"value\"}");
 * </pre>
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
     * Each thread will receive its own instance of the service.
     *
     * @return the thread-local singleton instance of SqsService
     */
    public static SqsService getInstance() {
        return instance.get();
    }

    /**
     * Initializes the SqsService with the specified queue URL and AWS region.
     * This method must be called before using other methods.
     * <p>
     * The initialization creates a new SqsUtils instance which establishes
     * a connection to the specified SQS queue using the AWS SDK.
     *
     * @param queueUrl the URL of the SQS queue
     * @param awsRegion the AWS region where the SQS queue is located
     * @throws IllegalArgumentException if queueUrl or awsRegion is null
     */
    public void initialize(String queueUrl, Region awsRegion) {
        this.queueUrl = queueUrl;
        this.awsRegion = awsRegion;
        this.sqsUtils = new SqsUtils(queueUrl, awsRegion);
    }

    /**
     * Sends a message to the SQS queue.
     * <p>
     * This method delegates to the underlying SqsUtils to send the message.
     * The message should be a valid JSON string for better compatibility with
     * other AWS services that might process the message.
     *
     * @param messageBody the body of the message to be sent
     * @return the response from the send message operation
     * @throws IllegalStateException if the service has not been initialized
     * @throws software.amazon.awssdk.services.sqs.model.SqsException if an error occurs while sending the message
     */
    public SendMessageResponse sendMessage(String messageBody) {
        System.out.println("Sending message to queue: " + queueUrl);
        return sqsUtils.sendMessage(messageBody);
    }

    /**
     * Receives messages from the SQS queue.
     * <p>
     * This method delegates to the underlying SqsUtils to receive messages.
     * By default, it will attempt to receive up to 1 message with a 5-second
     * long polling wait time.
     *
     * @return a list of messages received from the queue (may be empty if no messages are available)
     * @throws IllegalStateException if the service has not been initialized
     * @throws software.amazon.awssdk.services.sqs.model.SqsException if an error occurs while receiving messages
     */
    public List<Message> receiveMessages() {
        System.out.println("Receiving messages from queue: " + queueUrl);
        return sqsUtils.receiveMessages();
    }

    /**
     * Deletes a message from the SQS queue.
     * <p>
     * This method delegates to the underlying SqsUtils to delete the message.
     * After a message is processed, it should be deleted to prevent it from
     * being received again when the visibility timeout expires.
     *
     * @param message the message to be deleted
     * @throws IllegalStateException if the service has not been initialized
     * @throws software.amazon.awssdk.services.sqs.model.SqsException if an error occurs while deleting the message
     * @throws NullPointerException if message is null
     */
    public void deleteMessage(Message message) {
        sqsUtils.deleteMessage(message);
    }

    /**
     * Closes the SQS client and releases any resources.
     * This method should be called when the service is no longer needed.
     * <p>
     * It's important to call this method to properly clean up resources,
     * especially in test environments to prevent resource leaks.
     */
    public void close() {
        if (sqsUtils != null) {
            sqsUtils.close();
        }
    }
}