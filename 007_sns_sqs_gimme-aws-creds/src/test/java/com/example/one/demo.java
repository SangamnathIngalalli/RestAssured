package com.example.one;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import java.util.HashMap;
import java.util.Map;

public class demo {

    // Replace with your SNS Topic ARN
    private static final String SNS_TOPIC_ARN = "YOUR_SNS_TOPIC_ARN"; 
    // Replace with your desired AWS Region
    private static final Regions AWS_REGION = Regions.US_EAST_1; 

    public static void main(String[] args) {
        sendMessageToSns();
    }

    public static void sendMessageToSns() {
        // Hardcoded JSON payload
        String jsonPayload = "{\"type\":\"order\",\"source\":\"java-demo\",\"timestamp\":\"2023-10-27T12:00:00Z\",\"data\":{\"orderId\":78901,\"item\":\"widget\",\"quantity\":5,\"status\":\"pending\"}}";

        // Message attributes
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("messageType", new MessageAttributeValue()
                .withDataType("String")
                .withStringValue("OrderNotification"));
        messageAttributes.put("priority", new MessageAttributeValue()
                .withDataType("String")
                .withStringValue("High"));
        messageAttributes.put("orderAmount", new MessageAttributeValue()
                .withDataType("Number")
                .withStringValue("150.75")); // Number attributes are sent as strings

        try {
            // Use DefaultAWSCredentialsProviderChain to find credentials
            // (e.g., from environment variables, system properties, profile file, EC2 instance metadata)
            AWSCredentialsProvider credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();

            AmazonSNS snsClient = AmazonSNSClientBuilder.standard()
                    .withRegion(AWS_REGION)
                    .withCredentials(credentialsProvider)
                    .build();

            PublishRequest publishRequest = new PublishRequest()
                    .withTopicArn(SNS_TOPIC_ARN)
                    .withMessage(jsonPayload)
                    .withMessageAttributes(messageAttributes);
            
            // You can also set a Subject for the message (optional)
            // .withSubject("New Order Received - ID: 78901");

            PublishResult result = snsClient.publish(publishRequest);

            System.out.println("Message sent successfully!");
            System.out.println("Message ID: " + result.getMessageId());
            if (result.getSequenceNumber() != null) {
                System.out.println("Sequence Number (for FIFO topics): " + result.getSequenceNumber());
            }

        } catch (Exception e) {
            System.err.println("Error sending message to SNS: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
