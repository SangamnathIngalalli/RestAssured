package com.example.three.tests;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.example.three.base.BaseTest;
import com.example.three.services.MessagingService;
import com.example.three.utils.AwsConfigUtility;
import com.example.three.services.AwsCredentialService; // Import AwsCredentialService

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class SampleApiTest extends BaseTest {
    
    private MessagingService messagingService;
    
    @BeforeClass
    public void setupAwsService() throws IOException {
        // Load credentials using AwsCredentialService
        AWSCredentials credentials = AwsCredentialService.getGimmeAwsCredentials();
        
        String accessKey;
        String secretKey;
        String sessionToken = null; // Initialize to null

        if (credentials instanceof BasicSessionCredentials) {
            BasicSessionCredentials sessionCredentials = (BasicSessionCredentials) credentials;
            accessKey = sessionCredentials.getAWSAccessKeyId();
            secretKey = sessionCredentials.getAWSSecretKey();
            sessionToken = sessionCredentials.getSessionToken();
        } else {
            // Handle case where it might be BasicAWSCredentials (no session token)
            // This might indicate an issue if gimme-aws-creds should always provide a session token
            accessKey = credentials.getAWSAccessKeyId();
            secretKey = credentials.getAWSSecretKey();
            // sessionToken remains null or you could throw an error if session is mandatory
            System.err.println("Warning: AWS credentials did not include a session token.");
        }

        // Initialize the AWS messaging service using the config utility and loaded credentials
        messagingService = new MessagingService(
            AwsConfigUtility.getSnsTopicArn(),
            AwsConfigUtility.getSqsQueueUrl(),
            AwsConfigUtility.getRegion(),
            accessKey, 
            secretKey, 
            sessionToken // This can be null if not a session credential
        );
        messagingService.initialize();
        
        System.out.println("AWS messaging service initialized");
    }
    
    @Test(description = "Test sending a message from JSON file to SNS and verifying in SQS")
    public void testSendMessageFromJsonFile() throws IOException, InterruptedException {
        System.out.println("Executing testSendMessageFromJsonFile on thread: " + Thread.currentThread().getId());
        
        // Send message from JSON file using the path from config utility
        String messageId = messagingService.sendMessageFromJsonFile(AwsConfigUtility.getMessageJsonPath());
        
        // Verify we got a message ID
        Assert.assertNotNull(messageId, "Message ID should not be null");
        
        // Check the message in SQS
        boolean messageVerified = messagingService.checkMessageInSqs(messageId, 5, 3, 2);
        
        // Assert the verification result
        Assert.assertTrue(messageVerified, "Message should be successfully verified in SQS");
    }
}