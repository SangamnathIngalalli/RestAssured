package com.reqres.tests;

import org.testng.annotations.Test;

import com.reqres.models.User;
import com.reqres.services.ApiService;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;

@Epic("Reqres API Testing")
@Feature("User Management")
public class ReqresApiTests extends BaseTest {

	private final ApiService apiService = ApiService.getInstance();

	@Test
	@Story("Get User by ID")
	@Severity(SeverityLevel.CRITICAL)
	@Description("Test to verify that a user can be successfully retrieved by ID")
	public void testGetUserPass() throws Exception {
		Response response = apiService.getUserById(2, getRequestSpec());

		getSoftAssert().assertEquals(response.getStatusCode(), 200, "Status code should be 200");

		User user = apiService.getUserObjectById(2, getRequestSpec());
		getSoftAssert().assertEquals(user.getUserData().getId(), 2, "User ID should be 2");
		getSoftAssert().assertEquals(user.getUserData().getEmail(), "janet.weaver@reqres.in", "Email should match");

		getSoftAssert().assertEquals(user.getUserData().getFirstName(), "Janet", "First name should match");
		getSoftAssert().assertEquals(user.getUserData().getLastName(), "Weaver", "Last name should match");

		assertAll();
	}

	@Test
	@Story("Get User by ID - Negative Test")
	@Severity(SeverityLevel.NORMAL)
	@Description("Test to verify system behavior when retrieving a non-existent user")
	public void testGetUserFail() throws Exception {
		Response response = apiService.getUserById(2, getRequestSpec());
		getSoftAssert().assertEquals(response.getStatusCode(), 2000, "Status code should be 200");
		apiService.getUserObjectById(2, getRequestSpec());

		assertAll();
	}

}