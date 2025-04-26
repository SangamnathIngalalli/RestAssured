package com.reqres.filters;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.filter.log.LogDetail;
import io.restassured.response.Response;

public class CustomLoggingFilter implements Filter {

    private final LogDetail requestLogDetail;
    private final LogDetail responseLogDetail;
    private final boolean logOnFailureOnly;
    private final String methodToLog;
    private final Integer statusCodeToLog;

    // Constructor to configure logging
    public CustomLoggingFilter(LogDetail requestLogDetail, LogDetail responseLogDetail,
                               boolean logOnFailureOnly, String methodToLog, Integer statusCodeToLog) {
        this.requestLogDetail = requestLogDetail;
        this.responseLogDetail = responseLogDetail;
        this.logOnFailureOnly = logOnFailureOnly;
        this.methodToLog = methodToLog;
        this.statusCodeToLog = statusCodeToLog;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {

        // Log Request if specified
        if (requestLogDetail != null) {
            new RequestLoggingFilter(requestLogDetail).filter(requestSpec, responseSpec, ctx);
        }

        // Proceed with the request and get the response
        Response response = ctx.next(requestSpec, responseSpec);

        // Log Response if specified
        if (responseLogDetail != null) {
            new ResponseLoggingFilter(responseLogDetail)
                .filter(requestSpec, responseSpec, ctx); // Use the original FilterContext
        }

        // Log only on failure
        if (logOnFailureOnly && (response.statusCode() < 200 || response.statusCode() >= 300)) {
            new RequestLoggingFilter(LogDetail.ALL).filter(requestSpec, responseSpec, ctx);
            new ResponseLoggingFilter(LogDetail.ALL).filter(requestSpec, responseSpec, ctx);
        }

        // Log specific HTTP Method or Status Code
        if ((methodToLog != null && methodToLog.equalsIgnoreCase(requestSpec.getMethod())) ||
            (statusCodeToLog != null && response.statusCode() == statusCodeToLog)) {
            new RequestLoggingFilter(LogDetail.ALL).filter(requestSpec, responseSpec, ctx);
            new ResponseLoggingFilter(LogDetail.ALL).filter(requestSpec, responseSpec, ctx);
        }

        return response;
    }

  

    public static CustomLoggingFilter logHeadersOnly() {
        return new CustomLoggingFilter(LogDetail.HEADERS, LogDetail.HEADERS, false, null, null);
    }

   
}
