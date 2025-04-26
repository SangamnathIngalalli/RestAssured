package com.reqres.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.filter.log.LogDetail;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class ResponseLoggingFilter implements Filter {
    private final LogDetail logDetail;
    private final int maxStatusCode;

    public ResponseLoggingFilter(LogDetail logDetail, int maxStatusCode) {
        this.logDetail = logDetail;
        this.maxStatusCode = maxStatusCode;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                          FilterableResponseSpecification responseSpec,
                          FilterContext ctx) {
        // Execute the request first
        Response response = ctx.next(requestSpec, responseSpec);
        
        // Then log response based on configuration
        boolean shouldLog = maxStatusCode <= 0 || response.statusCode() <= maxStatusCode;
        
        if (shouldLog && logDetail != null) {
            logResponse(response, logDetail);
        }
        
        return response;
    }

    private void logResponse(Response response, LogDetail detail) {
        try {
            switch (detail) {
                case ALL:
                    System.out.println("RESPONSE Status: " + response.getStatusCode() + " " + response.getStatusLine());
                    System.out.println("Headers: " + response.getHeaders());
                    System.out.println("Body: " + response.asPrettyString());
                    break;
                case HEADERS:
                    System.out.println("RESPONSE HEADERS: " + response.getHeaders());
                    break;
                case BODY:
                    System.out.println("RESPONSE BODY: " + response.asPrettyString());
                    break;
                case STATUS:
                    System.out.println("RESPONSE STATUS: " + response.getStatusCode() + " " + response.getStatusLine());
                    break;
                default:
                    // No logging
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error while logging response: " + e.getMessage());
        }
    }

    // Factory methods
    public static ResponseLoggingFilter logAll() {
        return new ResponseLoggingFilter(LogDetail.ALL, 0);
    }

    public static ResponseLoggingFilter logHeadersOnly() {
        return new ResponseLoggingFilter(LogDetail.HEADERS, 0);
    }

    public static ResponseLoggingFilter logBodyOnly() {
        return new ResponseLoggingFilter(LogDetail.BODY, 0);
    }

    public static ResponseLoggingFilter logStatusOnly() {
        return new ResponseLoggingFilter(LogDetail.STATUS, 0);
    }

    public static ResponseLoggingFilter logOnlySuccessResponses() {
        return new ResponseLoggingFilter(LogDetail.ALL, 299);
    }
}