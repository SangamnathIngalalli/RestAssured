package com.example.three.base;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;

/**
 * Base test class for all API tests
 */
public class BaseTest {
    
    @BeforeClass
    public void setup() {
        // Basic RestAssured configuration
        RestAssured.baseURI = "https://api.example.com"; // Replace with your actual API base URL
        RestAssured.basePath = "/api";
        
        // Log request and response details for debugging
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        System.out.println("BaseTest setup complete");
    }
}