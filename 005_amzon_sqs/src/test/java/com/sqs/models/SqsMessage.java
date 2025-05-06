package com.sqs.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an Amazon SQS message with its core attributes.
 * This class is designed to deserialize JSON responses from the AWS SQS service.
 * The class uses Jackson annotations to map JSON properties to Java fields.
 * 
 * The @JsonIgnoreProperties annotation ensures that unknown properties in the JSON
 * response won't cause deserialization errors, providing flexibility when the
 * AWS API returns additional fields.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SqsMessage {
    /**
     * The unique identifier for the message in the SQS queue.
     * This ID is assigned by Amazon SQS when the message is sent to the queue.
     */
    @JsonProperty("MessageId")
    private String messageId;
    
    /**
     * The actual content/payload of the SQS message.
     * This contains the data that was sent to the queue by the producer.
     */
    @JsonProperty("Body")
    private String body;
    
    /**
     * A token used to interact with the message, particularly for deletion.
     * The receipt handle is required when you want to delete a message from the queue
     * or change the message visibility timeout.
     */
    @JsonProperty("ReceiptHandle")
    private String receiptHandle;
    
    // Getters and setters
    /**
     * Gets the unique identifier of the message.
     * 
     * @return the message ID assigned by Amazon SQS
     */
    public String getMessageId() {
        return messageId;
    }
    
    /**
     * Sets the message identifier.
     * 
     * @param messageId the unique identifier to set
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    /**
     * Gets the message body/content.
     * 
     * @return the message body
     */
    public String getBody() {
        return body;
    }
    
    /**
     * Sets the message body/content.
     * 
     * @param body the message content to set
     */
    public void setBody(String body) {
        this.body = body;
    }
    
    /**
     * Gets the receipt handle of the message.
     * This handle is required for deleting the message from the queue.
     * 
     * @return the receipt handle
     */
    public String getReceiptHandle() {
        return receiptHandle;
    }
    
    /**
     * Sets the receipt handle of the message.
     * 
     * @param receiptHandle the receipt handle to set
     */
    public void setReceiptHandle(String receiptHandle) {
        this.receiptHandle = receiptHandle;
    }
    
    /**
     * Returns a string representation of the SqsMessage object.
     * Useful for logging and debugging purposes.
     * 
     * @return a string containing the message ID, body, and receipt handle
     */
    @Override
    public String toString() {
        return "SqsMessage{" +
                "messageId='" + messageId + '\'' +
                ", body='" + body + '\'' +
                ", receiptHandle='" + receiptHandle + '\'' +
                '}';
    }
}