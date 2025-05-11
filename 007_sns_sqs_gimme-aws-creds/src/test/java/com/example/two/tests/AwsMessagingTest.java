package com.example.two.tests;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Regions;
import com.example.two.base.BaseTest;
import com.example.two.services.AwsMessagingService;
import com.example.two.utils.AwsCredentialsUtil;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class AwsMessagingTest extends BaseTest {
    
    // AWS resource identifiers - replace with your actual values
    private static final String SNS_TOPIC_ARN = "YOUR_SNS_TOPIC_ARN";
    private static final String SQS_QUEUE_URL = "YOUR_SQS_QUEUE_URL";
    private static final Regions REGION = Regions.US_EAST_1;
    
    private AwsMessagingService awsMessagingService;
    
    @BeforeClass
    public void setupAwsService() throws IOException {
        // Load and print AWS credentials
        AWSCredentials credentials = AwsCredentialsUtil.getAwsCredentials();
        System.out.println("AWS Credentials loaded successfully");
        AwsCredentialsUtil.printMaskedCredentials(credentials);
        
        // Initialize the AWS messaging service
        awsMessagingService = new AwsMessagingService(SNS_TOPIC_ARN, SQS_QUEUE_URL, REGION);
        awsMessagingService.initialize();
    }
    
    @Test(description = "Test sending a message to SNS")
    public void testSendMessage() {
        System.out.println("Executing testSendMessage on thread: " + Thread.currentThread().getId());
        
        // Create a unique test message with timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        String messageContent = "Test message from TestNG test at " + timestamp;
        
        // Send the message
        String messageId = awsMessagingService.sendMessage(messageContent);
        
        // Verify we got a message ID
        Assert.assertNotNull(messageId, "Message ID should not be null");
        Assert.assertFalse(messageId.isEmpty(), "Message ID should not be empty");
    }
    
    @Test(description = "Test verifying a message in SQS")
    public void testVerifyMessage() throws InterruptedException {
        System.out.println("Executing testVerifyMessage on thread: " + Thread.currentThread().getId());
        
        // Create and send a message
        String timestamp = String.valueOf(System.currentTimeMillis());
        String messageContent = "Verification test message at " + timestamp;
        String messageId = awsMessagingService.sendMessage(messageContent);
        
        // Verify the message with custom parameters
        boolean messageVerified = awsMessagingService.verifyMessage(messageId, 3, 10, 1);
        
        // Add detailed verification steps
        System.out.println("Message verification result: " + (messageVerified ? "SUCCESS" : "FAILED"));
        if (!messageVerified) {
            System.out.println("WARNING: Message verification failed. This could be due to:");
            System.out.println("1. Incorrect SNS Topic ARN or SQS Queue URL");
            System.out.println("2. Insufficient delay for message propagation");
            System.out.println("3. SQS queue not subscribed to the SNS topic");
            System.out.println("4. AWS credentials issues");
        }
        
        // Assert the verification result
        Assert.assertTrue(messageVerified, "Message should be successfully verified in SQS");
    }
    
    @Test(description = "Test sending a UserProfile object to SNS")
    public void testSendUserProfileMessage() throws InterruptedException {
        System.out.println("Executing testSendUserProfileMessage on thread: " + Thread.currentThread().getId());
        
        // Create a UserProfile object
        UserProfile.Address address = UserProfile.Address.builder()
                .street("123 Main St")
                .city("Springfield")
                .zipCode("12345")
                .build();
        
        UserProfile userProfile = UserProfile.builder()
                .id(101)
                .name("John Doe")
                .email("john.doe@example.com")
                .address(address)
                .isActive(true)
                .build();
        
        // Send the UserProfile object
        String messageId = awsMessagingService.sendUserProfileMessage(userProfile);
        
        // Verify we got a message ID
        Assert.assertNotNull(messageId, "Message ID should not be null");
        Assert.assertFalse(messageId.isEmpty(), "Message ID should not be empty");
        
        // Verify the message was received in SQS
        boolean messageVerified = awsMessagingService.verifyMessage(messageId, 3, 10, 1);
        Assert.assertTrue(messageVerified, "UserProfile message should be successfully verified in SQS");
    }
}