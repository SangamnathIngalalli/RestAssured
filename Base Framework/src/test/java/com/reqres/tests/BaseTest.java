package com.reqres.tests;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class BaseTest {
    private static final ThreadLocal<RequestSpecification> requestSpecThreadLocal = new ThreadLocal<>();

    protected RequestSpecification getRequestSpec() {
        return requestSpecThreadLocal.get();
    }

    @BeforeMethod  // Runs before EVERY test method
    public void setup() {
        RestAssured.baseURI = "https://reqres.in/api";
        
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setContentType(ContentType.JSON);
             
        
        requestSpecThreadLocal.set(builder.build());
    }

    @AfterMethod  // Runs after EVERY test method (cleans up ThreadLocal)
    public void tearDown() {
        requestSpecThreadLocal.remove();
    }
} 