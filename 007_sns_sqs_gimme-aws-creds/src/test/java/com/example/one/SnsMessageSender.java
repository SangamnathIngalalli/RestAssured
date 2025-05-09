package com.example.one;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.json.JSONObject;
import java.util.UUID;




public class SnsMessageSender {
	
	
		private final AmazonSNS snsClient;
	    private final String snsTopicArn;
	    
	    /**
	     * Creates a new SnsMessageSender with the specified credentials and topic ARN.
	     * 
	     * @param credentials AWS credentials to use for authentication
	     * @param region AWS region where the SNS topic is located
	     * @param snsTopicArn ARN of the SNS topic to publish to
	     */
	    public SnsMessageSender(AWSCredentials credentials, Regions region, String snsTopicArn) {
	        this.snsTopicArn = snsTopicArn;
	        
	        // Initialize SNS client with the provided credentials
	        this.snsClient = AmazonSNSClientBuilder.standard()
	                .withRegion(region)
	                .withCredentials(new AWSStaticCredentialsProvider(credentials))
	                .build();
	    }
	    
	    
	    /**
	     * Sends a message to the SNS topic.
	     * 
	     * @param message The message content to send
	     * @return The message ID assigned by SNS
	     */
	    public String sendMessage(String message) {
	        PublishRequest publishRequest = new PublishRequest(snsTopicArn, message);
	        PublishResult publishResult = snsClient.publish(publishRequest);
	        return publishResult.getMessageId();
	    }
	    
	    
	    /**
	     * Creates a JSON message with the specified content and a unique ID.
	     * 
	     * @param messageContent The content of the message
	     * @return A JSON string containing the message content and a unique ID
	     */
	    public static String createJsonMessage(String messageContent) {
	        // Create a unique message ID to track the message
	        String messageId = UUID.randomUUID().toString();
	        
	        // Create JSON payload for the message
	        JSONObject jsonPayload = new JSONObject();
	        jsonPayload.put("message", messageContent);
	        jsonPayload.put("id", messageId);
	        
	        return jsonPayload.toString();
	    }
	}
