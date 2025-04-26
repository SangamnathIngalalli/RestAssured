package com.reqres.tests;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.reqres.filters.RequestLoggingFilter;
import com.reqres.filters.ResponseLoggingFilter;
import com.reqres.models.User;
import com.reqres.utils.JsonUtils;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ReqresApiTests extends BaseTest {

    @Test
    public void testGetUser() throws Exception {
        SoftAssert softAssert = new SoftAssert();
        
        Response response = RestAssured.given()
                .spec(getRequestSpec())
                .filters(
                  //   RequestLoggingFilter.logHeadersOnly()
                   ResponseLoggingFilter.logAll()
                )
                .when()
                .get("/users/2")
                .then()
                .extract()
                .response();

        softAssert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");
        
        // Validate response against schema
   //     SchemaValidator.validateResponse(response.asString(), "/schemas/user-schema.json");

     User user = JsonUtils.getObjectMapper().readValue(response.asString(), User.class);
   softAssert.assertEquals(user.getUserData().getId(), 2, "User ID should be 2");
   softAssert.assertEquals(user.getUserData().getEmail(), "janet.weaver@reqres.in", "Email should match");
   softAssert.assertEquals(user.getUserData().getFirst_name(), "Janet", "First name should match");
   softAssert.assertEquals(user.getUserData().getLast_name(), "Weaver", "Last name should match");
   
   
   
   
        softAssert.assertAll();
    }

  
} 