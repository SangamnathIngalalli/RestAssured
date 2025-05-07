package com.example;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class AwsSnsAndSqsTest {

    private static final String SNS_TOPIC_ARN = "YOUR_SNS_TOPIC_ARN";
    private static final String SQS_QUEUE_URL = "YOUR_SQS_QUEUE_URL";
    private static final Regions REGION = Regions.US_EAST_1; // Change to your region

    @Test
    public void testSendMessageToSnsAndVerifyInSqs() throws InterruptedException {
        // Create a unique message ID to track the message
        String messageId = UUID.randomUUID().toString();
        
        // Create JSON payload
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("message", "Hello from test");
        jsonPayload.put("id", messageId);
        String message = jsonPayload.toString();

        // Initialize SNS client
        AmazonSNS snsClient = AmazonSNSClientBuilder.standard()
                .withRegion(REGION)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        // Publish message to SNS topic
        PublishRequest publishRequest = new PublishRequest(SNS_TOPIC_ARN, message);
        PublishResult publishResult = snsClient.publish(publishRequest);
        
        System.out.println("Message published to SNS. MessageId: " + publishResult.getMessageId());

        // Initialize SQS client
        AmazonSQS sqsClient = AmazonSQSClientBuilder.standard()
                .withRegion(REGION)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        // Wait a bit for the message to propagate from SNS to SQS
        TimeUnit.SECONDS.sleep(5);

        // Check SQS queue for the message
        boolean messageFound = false;
        int maxAttempts = 5;
        
        for (int attempt = 0; attempt < maxAttempts && !messageFound; attempt++) {
            ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest(SQS_QUEUE_URL)
                    .withMaxNumberOfMessages(10)
                    .withWaitTimeSeconds(5);
            
            List<Message> messages = sqsClient.receiveMessage(receiveRequest).getMessages();
            
            for (Message sqsMessage : messages) {
                String body = sqsMessage.getBody();
                System.out.println("Received message: " + body);
                
                // The message from SNS to SQS is wrapped in additional JSON
                if (body.contains(messageId)) {
                    messageFound = true;
                    
                    // Delete the message from the queue after verification
                    sqsClient.deleteMessage(SQS_QUEUE_URL, sqsMessage.getReceiptHandle());
                    break;
                }
            }
            
            if (!messageFound) {
                System.out.println("Message not found, waiting before next attempt...");
                TimeUnit.SECONDS.sleep(2);
            }
        }

        // Assert that the message was found in the SQS queue
        assertThat(messageFound).isTrue();
    }
}