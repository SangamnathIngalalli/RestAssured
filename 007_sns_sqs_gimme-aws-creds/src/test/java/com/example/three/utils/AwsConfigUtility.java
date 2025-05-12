package com.example.three.utils;

import com.amazonaws.regions.Regions;

/**
 * Utility class for AWS configuration values
 */

public class AwsConfigUtility {

    // AWS resource identifiers
    private static final String SNS_TOPIC_ARN = "YOUR_SNS_TOPIC_ARN";
    private static final String SQS_QUEUE_URL = "YOUR_SQS_QUEUE_URL";
    private static final Regions REGION = Regions.US_EAST_1;
    
    // JSON file path
    private static final String MESSAGE_JSON_PATH = "src/test/resources/three/message_payload.json";
    
    /**
     * Gets the SNS topic ARN
     * 
     * @return The SNS topic ARN
     */
    public static String getSnsTopicArn() {
        return SNS_TOPIC_ARN;
    }
    
    /**
     * Gets the SQS queue URL
     * 
     * @return The SQS queue URL
     */
    public static String getSqsQueueUrl() {
        return SQS_QUEUE_URL;
    }
    
    /**
     * Gets the AWS region
     * 
     * @return The AWS region
     */
    public static Regions getRegion() {
        return REGION;
    }
    
    /**
     * Gets the path to the message JSON file
     * 
     * @return The path to the message JSON file
     */
    public static String getMessageJsonPath() {
        return MESSAGE_JSON_PATH;
    }
}