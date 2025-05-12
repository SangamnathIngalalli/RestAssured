package com.example.three.services;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials; // Added import
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Service class for AWS SNS/SQS messaging operations
 */
public class MessagingService {
    private final String snsTopicArn;
    private final String sqsQueueUrl;
    private final Regions region;
    private AmazonSNS snsClient;
    private AmazonSQS sqsClient;
    private final String awsAccessKey; // Added field
    private final String awsSecretKey; // Added field
    private final String awsSessionToken; // Added field
    
    /**
     * Creates a new MessagingService with the specified AWS resource identifiers and session credentials
     * 
     * @param snsTopicArn ARN of the SNS topic to publish to
     * @param sqsQueueUrl URL of the SQS queue to receive messages from
     * @param region AWS region where the resources are located
     * @param awsAccessKey AWS Access Key ID
     * @param awsSecretKey AWS Secret Access Key
     * @param awsSessionToken AWS Session Token
     */
    public MessagingService(String snsTopicArn, String sqsQueueUrl, Regions region, 
                            String awsAccessKey, String awsSecretKey, String awsSessionToken) {
        this.snsTopicArn = snsTopicArn;
        this.sqsQueueUrl = sqsQueueUrl;
        this.region = region;
        this.awsAccessKey = awsAccessKey; // Initialize field
        this.awsSecretKey = awsSecretKey; // Initialize field
        this.awsSessionToken = awsSessionToken; // Initialize field
    }
    
    /**
     * Initializes the SNS and SQS clients with AWS credentials
     * 
     * @throws IOException if the credentials cannot be loaded (though now direct)
     */
    public void initialize() throws IOException {
        // Use BasicSessionCredentials with the provided keys and token
        AWSCredentials credentials = new BasicSessionCredentials(awsAccessKey, awsSecretKey, awsSessionToken);
        
        // Initialize SNS client
        this.snsClient = AmazonSNSClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
        
        // Initialize SQS client
        this.sqsClient = AmazonSQSClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
        
        System.out.println("AWS SNS and SQS clients initialized successfully");
    }
    
    /**
     * Sends a message to SNS from a JSON file
     * 
     * @param jsonFilePath Path to the JSON file containing the message
     * @return The message ID for later verification
     * @throws IOException if the file cannot be read
     */
    public String sendMessageFromJsonFile(String jsonFilePath) throws IOException {
        // Read the JSON file
        String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
        
        // Add a unique ID for tracking
        JSONObject jsonObject = new JSONObject(jsonContent);
        String messageId = UUID.randomUUID().toString();
        jsonObject.put("messageId", messageId);
        
        // Send the message to SNS
        String jsonMessage = jsonObject.toString();
        PublishRequest publishRequest = new PublishRequest(snsTopicArn, jsonMessage);
        PublishResult publishResult = snsClient.publish(publishRequest);
        
        System.out.println("Message sent to SNS with ID: " + messageId);
        System.out.println("SNS Message ID: " + publishResult.getMessageId());
        
        return messageId;
    }
    
    /**
     * Checks for a message with the specified ID in the SQS queue
     * 
     * @param messageId The ID of the message to look for
     * @param waitTimeSeconds Time to wait before checking (to allow for propagation)
     * @param maxAttempts Maximum number of attempts to find the message
     * @param delayBetweenAttempts Delay between attempts in seconds
     * @return true if the message was found, false otherwise
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public boolean checkMessageInSqs(String messageId, int waitTimeSeconds, int maxAttempts, int delayBetweenAttempts) 
            throws InterruptedException {
        // Wait for the message to propagate to SQS
        System.out.println("Waiting " + waitTimeSeconds + " seconds for message to propagate to SQS...");
        Thread.sleep(waitTimeSeconds * 1000);
        
        boolean messageFound = false;
        
        // Try multiple times to find the message, with delays between attempts
        for (int attempt = 0; attempt < maxAttempts && !messageFound; attempt++) {
            System.out.println("Checking SQS queue, attempt " + (attempt + 1) + " of " + maxAttempts);
            
            // Create a request to receive messages from the SQS queue
            ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
                    .withQueueUrl(sqsQueueUrl)
                    .withMaxNumberOfMessages(10)  // Receive up to 10 messages at once
                    .withWaitTimeSeconds(5);      // Wait up to 5 seconds for messages
            
            // Receive messages from the SQS queue
            List<Message> messages = sqsClient.receiveMessage(receiveRequest).getMessages();
            System.out.println("Received " + messages.size() + " messages");
            
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
                if (messageId.equals(messageJson.getString("messageId"))) {
                    System.out.println("Found message with ID: " + messageId);
                    System.out.println("Message content: " + messageJson.toString());
                    
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
                System.out.println("Message not found, waiting " + delayBetweenAttempts + " seconds before next attempt...");
                Thread.sleep(delayBetweenAttempts * 1000);
            }
        }
        
        if (messageFound) {
            System.out.println("Message verification successful");
        } else {
            System.out.println("Message verification failed after " + maxAttempts + " attempts");
        }
        
        return messageFound;
    }
}