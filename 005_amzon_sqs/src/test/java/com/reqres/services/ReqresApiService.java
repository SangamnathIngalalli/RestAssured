package com.reqres.services;

import com.reqres.models.User;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReqresApiService {
    private static ReqresApiService instance;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ReqresApiService() {
        // Private constructor for singleton pattern
    }

    public static synchronized ReqresApiService getInstance() {
        if (instance == null) {
            instance = new ReqresApiService();
        }
        return instance;
    }

    public Response getUserById(int userId, RequestSpecification requestSpec) {
        return given()
                .spec(requestSpec)
                .pathParam("userId", userId)
                .when()
                .get("/users/{userId}")
                .then()
                .extract()
                .response();
    }

    public User getUserObjectById(int userId, RequestSpecification requestSpec) throws Exception {
        Response response = getUserById(userId, requestSpec);
        String responseBody = response.getBody().asString();
        
        try {
            return objectMapper.readValue(responseBody, User.class);
        } catch (Exception e) {
            throw new Exception("Failed to parse user response: " + e.getMessage(), e);
        }
    }
}