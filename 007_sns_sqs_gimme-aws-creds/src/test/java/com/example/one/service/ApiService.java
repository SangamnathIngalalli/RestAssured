package com.example.one.service;

public interface ApiService {
    /**
     * Checks if a message with the given ID exists in the API.
     * 
     * @param messageId The ID of the message to check
     * @return true if the message exists, false otherwise
     */
    boolean checkMessageExists(String messageId);
}