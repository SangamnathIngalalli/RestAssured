package com.reqres.tests;

import com.reqres.models.User;
import com.reqres.utils.JsonUtils;
import io.restassured.response.Response;
import com.reqres.services.ApiService;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class ReqresApiTests extends BaseTest {

	private final ApiService apiService = ApiService.getInstance();

	@Test
	public void testGetUserPass() throws Exception {
		Response response = apiService.getUserById(2, getRequestSpec());

		getSoftAssert().assertEquals(response.getStatusCode(), 200, "Status code should be 200");
		
		User user = apiService.getUserObjectById(2, getRequestSpec());
		getSoftAssert().assertFalse(user.hasError(), "Should not have error response");
		getSoftAssert().assertEquals(user.getUserData().getId(), 2, "User ID should be 2");
		getSoftAssert().assertEquals(user.getUserData().getEmail(), "janet.weaver@reqres.in", "Email should match");
		getSoftAssert().assertEquals(user.getUserData().getFirst_name(), "Janet", "First name should match");
		getSoftAssert().assertEquals(user.getUserData().getLast_name(), "Weaver", "Last name should match");

		assertAll();
	}

	@Test()
	public void testGetUserFail() throws Exception {
		Response response = apiService.getUserById(-1, getRequestSpec());
		getSoftAssert().assertEquals(response.getStatusCode(), 200, "Status code should be 200");
		apiService.getUserObjectById(-1, getRequestSpec());
	}
}