package com.sqs.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SqsMessage {
    @JsonProperty("MessageId")
    private String messageId;
    
    @JsonProperty("Body")
    private String body;
    
    @JsonProperty("ReceiptHandle")
    private String receiptHandle;
    
    // Getters and setters
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public String getReceiptHandle() {
        return receiptHandle;
    }
    
    public void setReceiptHandle(String receiptHandle) {
        this.receiptHandle = receiptHandle;
    }
    
    @Override
    public String toString() {
        return "SqsMessage{" +
                "messageId='" + messageId + '\'' +
                ", body='" + body + '\'' +
                ", receiptHandle='" + receiptHandle + '\'' +
                '}';
    }
}