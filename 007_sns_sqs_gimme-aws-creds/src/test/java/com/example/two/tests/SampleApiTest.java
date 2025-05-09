package com.example.two.tests;

import com.example.two.base.BaseTest;
import com.example.two.services.PlaceholderService;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SampleApiTest extends BaseTest { // Extend BaseTest to inherit setup

    private PlaceholderService placeholderService = new PlaceholderService();

    @Test(description = "Verify GET request to a sample endpoint")
    public void testGetSampleResource() {
        System.out.println("Executing testGetSampleResource on thread: " + Thread.currentThread().getId());
        Response response = placeholderService.getResource("/users/2"); // Replace with an actual endpoint

        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("Status Code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");
        // Add more assertions based on the expected response
        // e.g., response.then().body("data.id", equalTo(2));
    }

    @Test(description = "Verify POST request to a sample endpoint")
    public void testPostSampleResource() {
        System.out.println("Executing testPostSampleResource on thread: " + Thread.currentThread().getId());
        // Example payload, replace with your actual payload structure
        String requestBody = "{\"name\": \"morpheus\", \"job\": \"leader\"}"; 

        Response response = placeholderService.createResource("/users", requestBody); // Replace with an actual endpoint

        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("Status Code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 201, "Status code should be 201 for creation");
        // Add more assertions
        // e.g., response.then().body("name", equalTo("morpheus"));
    }
}