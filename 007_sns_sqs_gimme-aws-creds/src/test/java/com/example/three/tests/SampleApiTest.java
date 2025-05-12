package com.example.three.tests;

import com.example.three.base.BaseTest;
import com.example.three.services.MessagingService;
import com.example.three.utils.AwsConfigUtility;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class SampleApiTest extends BaseTest {
    
    private MessagingService messagingService;
    
    @BeforeClass
    public void setupAwsService() throws IOException {
        // Initialize the AWS messaging service using the config utility
        messagingService = new MessagingService(
            AwsConfigUtility.getSnsTopicArn(),
            AwsConfigUtility.getSqsQueueUrl(),
            AwsConfigUtility.getRegion());
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