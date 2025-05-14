package com.example.one;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Regions;
import com.example.one.service.ReqresApiService;

import java.io.IOException;

/**
 * Example class demonstrating how to use the separate components for AWS messaging.
 */
public class AwsMessagingExample {
    // AWS resource identifiers - replace with your actual values
    private static final String SNS_TOPIC_ARN = "YOUR_SNS_TOPIC_ARN";
    private static final String SQS_QUEUE_URL = "YOUR_SQS_QUEUE_URL";
    private static final Regions REGION = Regions.US_EAST_1;
    
    public static void main(String[] args) {
        try {
            // Step 1: Load AWS credentials from gimme-aws-creds
            AWSCredentials credentials = AwsCredentialsManager.loadGimmeAwsCredentials();
            System.out.println("Successfully loaded AWS credentials");
            
            // Step 2: Create SNS message sender and send a message
            SnsMessageSender snsSender = new SnsMessageSender(credentials, REGION, SNS_TOPIC_ARN);
            String messageContent = "Hello from AWS Messaging Example";
            String jsonMessage = SnsMessageSender.createJsonMessage(messageContent);
            
            // Extract the message ID from the JSON for later verification
            String messageId = new org.json.JSONObject(jsonMessage).getString("id");
            
            // Send the message to SNS
            String snsMessageId = snsSender.sendMessage(jsonMessage);
            System.out.println("Message published to SNS. MessageId: " + snsMessageId);
            
            // Step 3: Create SQS message receiver and wait for the message
            SqsMessageReceiver sqsReceiver = new SqsMessageReceiver(credentials, REGION, SQS_QUEUE_URL);
            
            // Wait for the message to propagate from SNS to SQS
            System.out.println("Waiting for message to propagate to SQS...");
            Thread.sleep(5000);  // 5 seconds
            
            // Check for the message in SQS
            boolean messageFound = sqsReceiver.waitForMessage(messageId, 5, 2);
            
            if (messageFound) {
                System.out.println("Successfully verified message in SQS queue");
                
                // Step 4: Call Reqres API to check if the message exists
                System.out.println("Checking message in Reqres API...");
                ReqresApiService apiService = new ReqresApiService();
                boolean apiMessageFound = apiService.checkMessageExists(messageId);
                
                if (apiMessageFound) {
                    System.out.println("Successfully verified message in Reqres API");
                } else {
                    System.out.println("Failed to find message in Reqres API");
                }
            } else {
                System.out.println("Failed to find message in SQS queue");
            }
            
        } catch (IOException e) {
            System.err.println("Error loading AWS credentials: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Operation interrupted: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}