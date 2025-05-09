package com.example.two.base;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeSuite;

public class BaseTest {

    @BeforeSuite(alwaysRun = true)
    public void setup() {
        // Set the base URI for your API
        RestAssured.baseURI = "https://api.example.com"; // Replace with your API's base URI
        // You can also set other default configurations here, e.g.,
        // RestAssured.port = 8080;
        // RestAssured.basePath = "/v1";
        System.out.println("BaseTest setup: RestAssured.baseURI set to " + RestAssured.baseURI);
    }
}