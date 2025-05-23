package com.example.three.tests;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.example.three.base.BaseTest;
import com.example.three.services.MessagingService;
import com.example.three.utils.AwsConfigUtility;
import com.example.three.services.AwsCredentialService; // Import AwsCredentialService
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

public class SampleApiTest extends BaseTest {
    
    private MessagingService messagingService;
    
    @BeforeMethod // Changed from @BeforeClass to @BeforeMethod
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
            accessKey = credentials.getAWSAccessKeyId();
            secretKey = credentials.getAWSSecretKey();
            System.err.println("Warning: AWS credentials did not include a session token.");
        }

        // Initialize the AWS messaging service using the config utility and loaded credentials
        messagingService = new MessagingService(
            AwsConfigUtility.getSnsTopicArn(),
            AwsConfigUtility.getSqsQueueUrl(),
            AwsConfigUtility.getRegion(),
            accessKey, 
            secretKey, 
            sessionToken
        );
        messagingService.initialize();
        
        System.out.println("AWS messaging service initialized for thread: " + Thread.currentThread().getId());
    }
    
    @Test(description = "Test sending a message from JSON file to SNS and verifying in SQS")
    public void testSendMessageFromJsonFile() throws IOException, InterruptedException {
        System.out.println("Executing testSendMessageFromJsonFile on thread: " + Thread.currentThread().getId());
        
        // Send message from JSON file using the path from config utility
        String messageId = messagingService.sendMessageFromJsonFile(AwsConfigUtility.getMessageJsonPath());
        
        // Verify we got a message ID
        getSoftAssert().assertNotNull(messageId, "Message ID should not be null");
        
        // Check the message in SQS
        boolean messageVerified = messagingService.checkMessageInSqs(messageId, 5, 3, 2);
        
        // Assert the verification result
        getSoftAssert().assertTrue(messageVerified, "Message should be successfully verified in SQS");
    
        getSoftAssert().assertAll();
    }
}