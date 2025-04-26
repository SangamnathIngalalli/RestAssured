package com.reqres.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;

import java.util.Arrays;
import java.util.List;

public class RequestResponseLogger {
    
    public static List<Filter> logAll() {
        return Arrays.asList(
            new RequestLoggingFilter(LogDetail.ALL),
            new ResponseLoggingFilter(LogDetail.ALL)
        );
    }

    public static List<Filter> logHeadersOnly() {
        return Arrays.asList(
            new RequestLoggingFilter(LogDetail.HEADERS),
            new ResponseLoggingFilter(LogDetail.HEADERS)
        );
    }

    public static List<Filter> logBodyOnly() {
        return Arrays.asList(
            new RequestLoggingFilter(LogDetail.BODY),
            new ResponseLoggingFilter(LogDetail.BODY)
        );
    }

    public static List<Filter> logRequestHeadersResponseBody() {
        return Arrays.asList(
            new RequestLoggingFilter(LogDetail.HEADERS),
            new ResponseLoggingFilter(LogDetail.BODY)
        );
    }
} 