package com.sqs.tests;

import org.testng.annotations.*;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.List;
import java.util.UUID;

/**
 * SqsTests is a test class that verifies the functionality of sending and receiving
 * messages to and from an Amazon SQS queue. It extends BaseSqsTest to inherit setup
 * and teardown functionality.
 */
public class SqsTests extends BaseSqsTest {

    /**
     * Test method to send a JSON message to SQS and verify its receipt.
     * This method performs the following steps:
     * 1. Prepares a unique JSON message.
     * 2. Sends the message to the SQS queue.
     * 3. Attempts to receive the message from the queue.
     * 4. Validates the received message and deletes it from the queue.
     * 5. Asserts all verification points.
     */
    @Test(description = "Test sending a JSON message to SQS and receiving it")
    public void testSendAndReceiveSqsMessage() {
        // 1. Prepare a unique JSON message
        String uniqueId = UUID.randomUUID().toString(); // Generate a unique ID for the message
        String jsonMessage = String.format("{\"messageId\": \"%s\", \"payload\": \"Sample test data for SQS\"}", uniqueId);
        System.out.println("Preparing to send message with unique ID: " + uniqueId);

        // 2. Send the message to the SQS queue
        SendMessageResponse sendResponse = sqsService.sendMessage(jsonMessage); // Send the message
        softAssert.assertNotNull(sendResponse, "Send response should not be null"); // Verify send response is not null
        softAssert.assertNotNull(sendResponse.messageId(), "Message ID from send response should not be null"); // Verify message ID is not null
        System.out.println("Sent message ID: " + (sendResponse != null ? sendResponse.messageId() : "N/A"));

        // 3. Attempt to receive the message
        try {
            System.out.println("Waiting for message to become available in queue...");
            Thread.sleep(2000); // 2-second delay to allow message to be available
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            softAssert.fail("Test interrupted while waiting for SQS message.", e); // Fail the test if interrupted
        }

        List<Message> receivedMessages = sqsService.receiveMessages(); // Receive messages from the queue
        softAssert.assertNotNull(receivedMessages, "Received messages list should not be null"); // Verify received messages list is not null
        softAssert.assertFalse(receivedMessages.isEmpty(), "Should have received at least one message from the queue"); // Verify at least one message is received

        // 4. Validate the received message and delete it
        boolean messageFound = false; // Flag to track if the test message is found
        if (receivedMessages != null && !receivedMessages.isEmpty()) {
            System.out.println("Processing " + receivedMessages.size() + " received messages.");
            for (Message message : receivedMessages) {
                System.out.println("Processing received message ID: " + message.messageId());
                System.out.println("Received message Body: " + message.body());
                // Check if the received message body contains the unique ID we sent
                if (message.body().contains(uniqueId)) {
                    messageFound = true;
                    softAssert.assertEquals(message.body(), jsonMessage, "Received message body should match the sent JSON message");
                    System.out.println("Found the test message. Deleting it...");
                    // Delete the message from the queue to prevent reprocessing
                    sqsService.deleteMessage(message);
                    System.out.println("Test message deleted.");
                    break; // Exit loop once the target message is found and processed
                } else {
                    // Handle unexpected messages if necessary (e.g., log or delete)
                    System.out.println("Received an unexpected message (ID: " + message.messageId() + "). Deleting it.");
                    sqsService.deleteMessage(message);
                }
            }
        } else {
            System.out.println("No messages received from the queue.");
        }

        softAssert.assertTrue(messageFound, "The specific test message sent (ID containing: " + uniqueId + ") was not found among received messages.");

        // 5. Assert all verification points
        System.out.println("Performing final assertions...");
        softAssert.assertAll(); // This will throw an exception if any assertion failed
        System.out.println("Test method completed successfully.");
    }
}