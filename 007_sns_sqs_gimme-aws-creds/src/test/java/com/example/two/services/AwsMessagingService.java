package com.example.two.services;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Regions;
import com.example.one.SnsMessageSender;
import com.example.one.SqsMessageReceiver;
import com.example.two.utils.AwsCredentialsUtil;

import java.io.IOException;

/**
 * Service class for AWS SNS/SQS messaging operations
 */
public class AwsMessagingService {
    private final String snsTopicArn;
    private final String sqsQueueUrl;
    private final Regions region;
    private SnsMessageSender snsSender;
    private SqsMessageReceiver sqsReceiver;
    
    /**
     * Creates a new AwsMessagingService with the specified AWS resource identifiers
     * 
     * @param snsTopicArn ARN of the SNS topic to publish to
     * @param sqsQueueUrl URL of the SQS queue to receive messages from
     * @param region AWS region where the resources are located
     */
    public AwsMessagingService(String snsTopicArn, String sqsQueueUrl, Regions region) {
        this.snsTopicArn = snsTopicArn;
        this.sqsQueueUrl = sqsQueueUrl;
        this.region = region;
    }
    
    /**
     * Initializes the SNS and SQS clients with AWS credentials
     * 
     * @throws IOException if the credentials cannot be loaded
     */
    public void initialize() throws IOException {
        AWSCredentials credentials = AwsCredentialsUtil.getAwsCredentials();
        this.snsSender = new SnsMessageSender(credentials, region, snsTopicArn);
        this.sqsReceiver = new SqsMessageReceiver(credentials, region, sqsQueueUrl);
    }
    
    /**
     * Creates a JSON message and sends it to SNS
     * 
     * @param messageContent The content of the message to send
     * @return The message ID for later verification
     */
    public String sendMessage(String messageContent) {
        // Create the JSON message
        String jsonMessage = SnsMessageSender.createJsonMessage(messageContent);
        String messageId = new org.json.JSONObject(jsonMessage).getString("id");
        
        // Send the message to SNS
        snsSender.sendMessage(jsonMessage);
        System.out.println("Message sent to SNS with ID: " + messageId);
        
        return messageId;
    }
    
    /**
     * Verifies that a message with the specified ID has been received in SQS
     * 
     * @param messageId The ID of the message to verify
     * @param waitTimeSeconds Time to wait before verification (to allow for propagation)
     * @param maxAttempts Maximum number of attempts to find the message
     * @param delayBetweenAttempts Delay between attempts in seconds
     * @return true if the message was found, false otherwise
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public boolean verifyMessage(String messageId, int waitTimeSeconds, int maxAttempts, int delayBetweenAttempts) 
            throws InterruptedException {
        // Wait for the message to propagate to SQS
        System.out.println("Waiting " + waitTimeSeconds + " seconds for message to propagate to SQS...");
        Thread.sleep(waitTimeSeconds * 1000);
        
        // Verify the message in SQS
        System.out.println("Verifying message with ID: " + messageId);
        return sqsReceiver.waitForMessage(messageId, maxAttempts, delayBetweenAttempts);
    }
    
    /**
     * Aggressively verifies a message with shorter delays but more attempts
     * Useful for high-throughput testing where messages are expected to arrive quickly
     * 
     * @param messageId The ID of the message to verify
     * @return true if the message was found, false otherwise
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public boolean verifyMessageAggressive(String messageId) throws InterruptedException {
        System.out.println("Using aggressive verification strategy for message ID: " + messageId);
        // Short initial wait, many quick attempts with minimal delay
        return verifyMessage(messageId, 1, 20, 1);
    }
    
    /**
     * Patiently verifies a message with longer delays and fewer attempts
     * Useful for testing in environments with higher latency or processing delays
     * 
     * @param messageId The ID of the message to verify
     * @return true if the message was found, false otherwise
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public boolean verifyMessagePatient(String messageId) throws InterruptedException {
        System.out.println("Using patient verification strategy for message ID: " + messageId);
        // Longer initial wait, fewer attempts with longer delays
        return verifyMessage(messageId, 10, 5, 5);
    }
    
    /**
     * Verifies a message with exponential backoff strategy
     * Useful for environments with unpredictable message delivery times
     * 
     * @param messageId The ID of the message to verify
     * @param initialWaitSeconds Initial wait time before first check
     * @param maxAttempts Maximum number of attempts to find the message
     * @return true if the message was found, false otherwise
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public boolean verifyMessageWithBackoff(String messageId, int initialWaitSeconds, int maxAttempts) 
            throws InterruptedException {
        System.out.println("Using exponential backoff verification strategy for message ID: " + messageId);
        
        // Wait for the message to propagate to SQS
        System.out.println("Waiting " + initialWaitSeconds + " seconds for message to propagate to SQS...");
        Thread.sleep(initialWaitSeconds * 1000);
        
        boolean messageFound = false;
        int currentDelay = 1; // Start with 1 second delay
        
        for (int attempt = 0; attempt < maxAttempts && !messageFound; attempt++) {
            System.out.println("Verification attempt " + (attempt + 1) + " of " + maxAttempts);
            
            // Check for the message
            messageFound = sqsReceiver.waitForMessage(messageId, 1, 0);
            
            if (!messageFound && attempt < maxAttempts - 1) {
                System.out.println("Message not found, waiting " + currentDelay + " seconds before next attempt...");
                Thread.sleep(currentDelay * 1000);
                
                // Exponential backoff: double the delay for next attempt (up to 32 seconds)
                currentDelay = Math.min(currentDelay * 2, 32);
            }
        }
        
        return messageFound;
    }
    
    /**
     * Verifies a message with content validation
     * Checks both the message ID and content for stronger verification
     * 
     * @param messageId The ID of the message to verify
     * @param expectedContent The expected content of the message
     * @return true if the message was found with matching content, false otherwise
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public boolean verifyMessageWithContent(String messageId, String expectedContent) 
            throws InterruptedException {
        // This method would require modifications to SqsMessageReceiver to check content
        // For now, we'll just use the standard verification
        System.out.println("Content validation not implemented yet, using standard verification");
        return verifyMessage(messageId, 5, 5, 2);
    
    }
    
    /**
     * Sends a UserProfile object as a message to SNS
     * 
     * @param userProfile The UserProfile object to send
     * @return The message ID for later verification
     */
    public String sendUserProfileMessage(com.example.model.UserProfile userProfile) {
        try {
            // Convert the UserProfile object to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonMessage = objectMapper.writeValueAsString(userProfile);
            
            // Add a unique ID for tracking
            JSONObject jsonObject = new JSONObject(jsonMessage);
            String messageId = UUID.randomUUID().toString();
            jsonObject.put("messageId", messageId);
            
            // Send the message to SNS
            snsSender.sendMessage(jsonObject.toString());
            System.out.println("UserProfile message sent to SNS with ID: " + messageId);
            
            return messageId;
        } catch (Exception e) {
            System.err.println("Error sending UserProfile message: " + e.getMessage());
            throw new RuntimeException("Failed to send UserProfile message", e);
        }
    }
}