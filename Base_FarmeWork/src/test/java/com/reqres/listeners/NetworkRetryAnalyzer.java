package com.reqres.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLHandshakeException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkRetryAnalyzer implements IRetryAnalyzer {
    private final ConcurrentHashMap<String, Integer> retryCountMap = new ConcurrentHashMap<>();
    private static final int MAX_RETRY_COUNT = 3;

    @Override
    public boolean retry(ITestResult result) {
        String testKey = getTestKey(result);
        int currentRetryCount = retryCountMap.getOrDefault(testKey, 0);
        
        if (currentRetryCount < MAX_RETRY_COUNT) {
            Throwable throwable = result.getThrowable();
            if (throwable != null && isNetworkException(throwable)) {
                retryCountMap.put(testKey, currentRetryCount + 1);
                System.out.println("Retrying test " + testKey + 
                                 " due to network exception: " + 
                                 throwable.getClass().getSimpleName() + 
                                 " (Attempt " + (currentRetryCount + 1) + 
                                 " of " + MAX_RETRY_COUNT + ")");
                return true;
            }
        }
        return false;
    }

    private String getTestKey(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    }

    private boolean isNetworkException(Throwable throwable) {
        if (throwable instanceof SocketTimeoutException ||
            throwable instanceof ConnectException ||
            throwable instanceof UnknownHostException ||
            throwable instanceof SSLHandshakeException ||
            throwable instanceof TimeoutException) {
            return true;
        }

        // Check for HTTP status codes in the message
        String message = throwable.getMessage();
        if (message != null) {
            if (message.contains("HTTP 500") ||
                message.contains("HTTP 502") ||
                message.contains("HTTP 503") ||
                message.contains("HTTP 504")) {
                return true;
            }
        }

        // Check for nested exceptions
        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            return isNetworkException(cause);
        }

        return false;
    }
} 