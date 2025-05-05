package com.reqres.services;

import com.reqres.models.User;
import com.reqres.utils.JsonUtils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.*;

import java.io.IOException;


public class ApiService {
    // Thread-safe singleton pattern
    private static final ThreadLocal<ApiService> instance = ThreadLocal.withInitial(ApiService::new);
    private static final String BASE_URI = "https://reqres.in/api";

    private ApiService() {
        // Private constructor to prevent instantiation
    }

    public static ApiService getInstance() {
        return instance.get();
    }

    public Response getUserById(int userId, RequestSpecification spec) {
        System.out.println("Hitting URL: " + BASE_URI + "/users/" + userId);
        return RestAssured
                .given()
                    .spec(spec)
                    .contentType(ContentType.JSON)
                    .baseUri(BASE_URI)
                .when()
                    .get("/users/{id}", userId)
                .then()
                    .extract()
                .   response();
    }

    public Response getUsersByPage(int page, RequestSpecification spec) {
        System.out.println("Hitting URL: " + BASE_URI + "/users?page=" + page);
        return given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .baseUri(BASE_URI)
                .when()
                .get("/users?page={page}", page)
                .then()
                .extract()
                .response();
    }

    public User getUserObjectById(int userId, RequestSpecification spec) throws IOException {
        Response response = getUserById(userId, spec);
        User user = JsonUtils.getObjectMapper().readValue(response.asString(), User.class);
        
        return user;
    }
}