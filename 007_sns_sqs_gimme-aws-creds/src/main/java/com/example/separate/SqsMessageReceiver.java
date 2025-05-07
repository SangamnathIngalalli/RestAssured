package com.example.separate;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for receiving and processing messages from an AWS SQS queue.
 */
public class SqsMessageReceiver {
    private final AmazonSQS sqsClient;
    private final String sqsQueueUrl;
    
    /**
     * Creates a new SqsMessageReceiver with the specified credentials and queue URL.
     * 
     * @param credentials AWS credentials to use for authentication
     * @param region AWS region where the SQS queue is located
     * @param sqsQueueUrl URL of the SQS queue to receive messages from
     */
    public SqsMessageReceiver(AWSCredentials credentials, Regions region, String sqsQueueUrl) {
        this.sqsQueueUrl = sqsQueueUrl;
        
        // Initialize SQS client with the provided credentials
        this.sqsClient = AmazonSQSClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }
    
    /**
     * Waits for a message with the specified ID to appear in the SQS queue.
     * 
     * @param messageId The ID of the message to look for
     * @param maxAttempts Maximum number of attempts to find the message
     * @param delayBetweenAttempts Delay between attempts in seconds
     * @return true if the message was found, false otherwise
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public boolean waitForMessage(String messageId, int maxAttempts, int delayBetweenAttempts) 
            throws InterruptedException {
        boolean messageFound = false;
        
        // Try multiple times to find the message, with delays between attempts
        for (int attempt = 0; attempt < maxAttempts && !messageFound; attempt++) {
            // Create a request to receive messages from the SQS queue
            ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
                    .withQueueUrl(sqsQueueUrl)
                    .withMaxNumberOfMessages(10)  // Receive up to 10 messages at once
                    .withWaitTimeSeconds(5);      // Wait up to 5 seconds for messages
            
            // Receive messages from the SQS queue
            List<Message> messages = sqsClient.receiveMessage(receiveRequest).getMessages();
            
            // Process each received message
            for (Message message : messages) {
                // Parse the message body as JSON
                String messageBody = message.getBody();
                
                // The message from SNS is wrapped in another JSON object
                // We need to extract the actual message from the "Message" field
                JSONObject snsWrapper = new JSONObject(messageBody);
                String actualMessage = snsWrapper.getString("Message");
                
                // Parse the actual message as JSON
                JSONObject messageJson = new JSONObject(actualMessage);
                
                // Check if this is the message we're looking for
                if (messageId.equals(messageJson.getString("id"))) {
                    System.out.println("Found message with ID: " + messageId);
                    
                    // Delete the message from the queue since we've processed it
                    DeleteMessageRequest deleteRequest = new DeleteMessageRequest()
                            .withQueueUrl(sqsQueueUrl)
                            .withReceiptHandle(message.getReceiptHandle());
                    sqsClient.deleteMessage(deleteRequest);
                    
                    messageFound = true;
                    break;
                }
            }
            
            // If we haven't found the message yet, wait before trying again
            if (!messageFound && attempt < maxAttempts - 1) {
                System.out.println("Message not found, waiting before next attempt...");
                TimeUnit.SECONDS.sleep(delayBetweenAttempts);
            }
        }
        
        return messageFound;
    }
}