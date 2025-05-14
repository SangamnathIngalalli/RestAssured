package com.example.one.service;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Service class for interacting with the Reqres API.
 */
public class ReqresApiService {
    
    private final String baseUri;
    
    /**
     * Creates a new ReqresApiService with the default base URI.
     */
    public ReqresApiService() {
        this("https://reqres.in/api");
    }
    
    /**
     * Creates a new ReqresApiService with the specified base URI.
     * 
     * @param baseUri The base URI for the Reqres API
     */
    public ReqresApiService(String baseUri) {
        this.baseUri = baseUri;
    }
    
    /**
     * Checks if a message with the given ID exists in the Reqres API.
     * 
     * @param messageId The ID of the message to check
     * @return true if the message exists, false otherwise
     */
    public boolean checkMessageExists(String messageId) {
        try {
            // Create a request specification with the base URI
            RequestSpecification requestSpec = RestAssured.given()
                .baseUri(baseUri)
                .contentType(ContentType.JSON)
                .queryParam("id", messageId);
            
            // Make the API call
            Response response = requestSpec.get("/users");
            
            // Check if the response status code is 200 (OK)
            if (response.getStatusCode() == 200) {
                // Parse the response body as JSON
                String responseBody = response.getBody().asString();
                org.json.JSONObject jsonResponse = new org.json.JSONObject(responseBody);
                
                // Check if the data array is not empty
                if (jsonResponse.has("data") && !jsonResponse.getJSONArray("data").isEmpty()) {
                    // For demonstration purposes, we're considering the message found
                    // if any data is returned. In a real scenario, you would check
                    // if the specific message ID exists in the response.
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error checking message in Reqres API: " + e.getMessage());
            return false;
        }
    }
}