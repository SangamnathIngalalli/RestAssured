package com.example.two.services;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class PlaceholderService {

    private RequestSpecification getRequestSpec() {
        // Common request configurations can be set here
        // For example, headers, authentication, etc.
        return given()
                .header("Content-Type", "application/json");
                // .auth().oauth2("YOUR_ACCESS_TOKEN"); // Example for auth
    }

    public Response getResource(String resourcePath) {
        return getRequestSpec()
            .when()
                .get(resourcePath);
    }

    public Response createResource(String resourcePath, Object bodyPayload) {
        return getRequestSpec()
            .body(bodyPayload)
            .when()
                .post(resourcePath);
    }

    // Add more methods for PUT, DELETE, etc. as needed
}