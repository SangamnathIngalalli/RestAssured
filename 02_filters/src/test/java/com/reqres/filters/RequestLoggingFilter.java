package com.reqres.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.filter.log.LogDetail;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class RequestLoggingFilter implements Filter {
    private final LogDetail logDetail;
    private final boolean logOnFailureOnly;
    private final String methodToLog;

    public RequestLoggingFilter(LogDetail logDetail, boolean logOnFailureOnly, String methodToLog) {
        this.logDetail = logDetail;
        this.logOnFailureOnly = logOnFailureOnly;
        this.methodToLog = methodToLog;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                          FilterableResponseSpecification responseSpec,
                          FilterContext ctx) {
        // First check if we should log based on method
        boolean shouldLog = methodToLog == null || 
                           methodToLog.equalsIgnoreCase(requestSpec.getMethod());
        
        // Log request details before sending if configured to do so and method matches
        if (shouldLog && logDetail != null && !logOnFailureOnly) {
            logRequest(requestSpec, logDetail);
        }
        
        // Proceed with the request - avoid manipulating the filter chain during iteration
        Response response = ctx.next(requestSpec, responseSpec);
        
        // After getting response, check if we should log on failure
        if (shouldLog && logOnFailureOnly && (response.statusCode() < 200 || response.statusCode() >= 300)) {
            logRequest(requestSpec, LogDetail.ALL);
        }
        
        return response;
    }

    private void logRequest(FilterableRequestSpecification requestSpec, LogDetail detail) {
        try {
            switch (detail) {
                case ALL:
                    System.out.println("REQUEST: " + requestSpec.getMethod() + " " + requestSpec.getURI());
                    System.out.println("Headers: " + requestSpec.getHeaders());
                    if (requestSpec.getBody() != null) {
                        System.out.println("Body: " + requestSpec.getBody().toString());
                    }
                    break;
                case HEADERS:
                    System.out.println("REQUEST HEADERS: " + requestSpec.getHeaders());
                    break;
                case BODY:
                    if (requestSpec.getBody() != null) {
                        System.out.println("REQUEST BODY: " + requestSpec.getBody().toString());
                    }
                    break;
                case COOKIES:
                    System.out.println("REQUEST COOKIES: " + requestSpec.getCookies());
                    break;
                case PARAMS:
                    System.out.println("REQUEST PARAMS: " + requestSpec.getQueryParams());
                    break;
                default:
                    // No logging
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error while logging request: " + e.getMessage());
        }
    }

    // Factory methods
    public static RequestLoggingFilter logAll() {
        return new RequestLoggingFilter(LogDetail.ALL, false, null);
    }

    public static RequestLoggingFilter logHeadersOnly() {
        return new RequestLoggingFilter(LogDetail.HEADERS, false, null);
    }

    public static RequestLoggingFilter logBodyOnly() {
        return new RequestLoggingFilter(LogDetail.BODY, false, null);
    }

    public static RequestLoggingFilter logOnFailure() {
        return new RequestLoggingFilter(null, true, null);
    }

    public static RequestLoggingFilter logForMethod(String method) {
        return new RequestLoggingFilter(null, false, method);
    }
}
