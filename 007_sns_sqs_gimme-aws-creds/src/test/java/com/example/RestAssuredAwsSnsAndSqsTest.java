package com.example;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import io.restassured.RestAssured;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class RestAssuredAwsSnsAndSqsTest {

    private static final String SNS_TOPIC_ARN = "YOUR_SNS_TOPIC_ARN";
    private static final String SQS_QUEUE_URL = "YOUR_SQS_QUEUE_URL";
    private static final String SQS_ENDPOINT = "https://sqs.us-east-1.amazonaws.com"; // Change to your region
    private static final Regions REGION = Regions.US_EAST_1; // Change to your region
    
    private static AWSCredentials awsCredentials;
    
    @BeforeAll
    public static void setup() throws IOException {
        // Load AWS credentials from gimme-aws-creds output file
        awsCredentials = loadGimmeAwsCredentials();
    }
    
    private static AWSCredentials loadGimmeAwsCredentials() throws IOException {
        // Default location for gimme-aws-creds output
        String credentialsFilePath = System.getProperty("user.home") + "/.aws/credentials";
        File credentialsFile = new File(credentialsFilePath);
        
        if (!credentialsFile.exists()) {
            throw new IOException("AWS credentials file not found. Please run gimme-aws-creds first.");
        }
        
        String accessKeyId = null;
        String secretAccessKey = null;
        
        // Read credentials file to extract access key and secret key
        try (BufferedReader reader = new BufferedReader(new FileReader(credentialsFile))) {
            String line;
            boolean inDefaultProfile = false;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.equals("[default]")) {
                    inDefaultProfile = true;
                    continue;
                } else if (line.startsWith("[") && line.endsWith("]")) {
                    inDefaultProfile = false;
                    continue;
                }
                
                if (inDefaultProfile) {
                    if (line.startsWith("aws_access_key_id")) {
                        accessKeyId = line.split("=")[1].trim();
                    } else if (line.startsWith("aws_secret_access_key")) {
                        secretAccessKey = line.split("=")[1].trim();
                    }
                }
                
                if (accessKeyId != null && secretAccessKey != null) {
                    break;
                }
            }
        }
        
        if (accessKeyId == null || secretAccessKey == null) {
            throw new IOException("Could not find AWS credentials in the credentials file.");
        }
        
        return new BasicAWSCredentials(accessKeyId, secretAccessKey);
    }

    @Test
    public void testSendMessageToSnsAndVerifyInSqsWithRestAssured() throws InterruptedException {
        // Create a unique message ID to track the message
        String messageId = UUID.randomUUID().toString();
        
        // Create JSON payload
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("message", "Hello from RestAssured test");
        jsonPayload.put("id", messageId);
        String message = jsonPayload.toString();

        // Initialize SNS client and publish message using gimme-aws-creds credentials
        AmazonSNS snsClient = AmazonSNSClientBuilder.standard()
                .withRegion(REGION)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        PublishRequest publishRequest = new PublishRequest(SNS_TOPIC_ARN, message);
        PublishResult publishResult = snsClient.publish(publishRequest);
        
        System.out.println("Message published to SNS. MessageId: " + publishResult.getMessageId());

        // Wait for the message to propagate from SNS to SQS
        TimeUnit.SECONDS.sleep(5);

        // Initialize SQS client for message deletion after verification using gimme-aws-creds credentials
        AmazonSQS sqsClient = AmazonSQSClientBuilder.standard()
                .withRegion(REGION)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        // Use RestAssured to check SQS queue for the message
        boolean messageFound = false;
        int maxAttempts = 5;
        
        for (int attempt = 0; attempt < maxAttempts && !messageFound; attempt++) {
            // Configure RestAssured
            RestAssured.baseURI = SQS_ENDPOINT;
            
            // Use RestAssured to receive messages from SQS
            // We need to add AWS authentication headers for RestAssured
            String response = given()
                    .param("Action", "ReceiveMessage")
                    .param("QueueUrl", SQS_QUEUE_URL)
                    .param("MaxNumberOfMessages", "10")
                    .param("WaitTimeSeconds", "5")
                    .header("X-Amz-Content-Sha256", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                    .header("X-Amz-Date", java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                            .withZone(java.time.ZoneOffset.UTC).format(java.time.Instant.now()))
                    .header("Authorization", "AWS4-HMAC-SHA256 Credential=" + awsCredentials.getAWSAccessKeyId() + "/" +
                            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE) +
                            "/" + REGION.getName() + "/sqs/aws4_request, SignedHeaders=host;x-amz-content-sha256;x-amz-date, Signature=")
                    .when()
                    .get()
                    .then()
                    .statusCode(200)
                    .extract()
                    .response()
                    .asString();
            
            // Parse the XML response
            if (response.contains(messageId)) {
                messageFound = true;
                System.out.println("Message found in SQS queue!");
                
                // Extract receipt handle to delete the message
                String receiptHandle = response.substring(
                        response.indexOf("<ReceiptHandle>") + 15,
                        response.indexOf("</ReceiptHandle>")
                );
                
                // Delete the message from the queue
                sqsClient.deleteMessage(SQS_QUEUE_URL, receiptHandle);
            } else {
                System.out.println("Message not found, waiting before next attempt...");
                TimeUnit.SECONDS.sleep(2);
            }
        }

        // Assert that the message was found in the SQS queue
        assertThat(messageFound).isTrue();
    }
}