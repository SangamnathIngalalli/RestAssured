package com.reqres.tests;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.asserts.SoftAssert;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import com.reqres.config.Config;

public abstract class BaseTest {

	private static final ThreadLocal<RequestSpecification> requestSpecThreadLocal = new ThreadLocal<>();
	private static final ThreadLocal<SoftAssert> softAssertThreadLocal = new ThreadLocal<>();

	protected SoftAssert getSoftAssert() {
		return softAssertThreadLocal.get();
	}

	protected void assertAll() {
		getSoftAssert().assertAll();
	}

	protected RequestSpecification getRequestSpec() {
		return requestSpecThreadLocal.get();
	}

	@BeforeMethod // Runs before EVERY test method
	public void setup() {
		softAssertThreadLocal.set(new SoftAssert());
		RequestSpecBuilder builder = new RequestSpecBuilder()
				.setBaseUri(Config.getBaseURI())
				.setContentType(ContentType.JSON);
		requestSpecThreadLocal.set(builder.build());
	}

	@AfterMethod // Runs after EVERY test method (cleans up ThreadLocal)
	public void tearDown() {
		requestSpecThreadLocal.remove();
		softAssertThreadLocal.remove();
	}
}