package com.reqres.tests;

import io.restassured.response.Response;

import com.reqres.models.User;
import com.reqres.services.ApiService;
import org.testng.annotations.Test;
import io.qameta.allure.*;

public class ReqresApiTests extends BaseTest {

	private final ApiService apiService = ApiService.getInstance();

	@Description("Test to verify retrieving information of a single user PASS")
	@Severity(SeverityLevel.CRITICAL)
	@Feature("User Management")
	@Story("Get single user details")
	@Test
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

	@Description("Test to verify retrieving information of a single user FAIL")
	@Severity(SeverityLevel.CRITICAL)
	@Feature("User Management")
	@Story("Get single user details")
	@Test
	public void testGetUserFail() throws Exception {
		Response response = apiService.getUserById(-1, getRequestSpec());
		getSoftAssert().assertEquals(response.getStatusCode(), 200, "Status code should be 200");
		apiService.getUserObjectById(-1, getRequestSpec());
	}


}