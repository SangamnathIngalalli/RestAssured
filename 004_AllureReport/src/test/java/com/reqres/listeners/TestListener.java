package com.reqres.listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import java.util.concurrent.ConcurrentHashMap;
import java.net.SocketTimeoutException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLHandshakeException;
import java.util.concurrent.TimeoutException;

/**
 * This class implements the ITestListener interface from TestNG to listen to 
 * various events during the test execution lifecycle. It logs the details 
 * of test execution, including the test suite and individual test results.
 * The listener helps in tracking the execution flow, logging success, 
 * failure, skipping reasons, and execution time.
 * 
 * @author Sangamnath Ingalalli
 */
public class TestListener implements ITestListener {
    private final ConcurrentHashMap<String, Integer> retryCountMap = new ConcurrentHashMap<>();
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * Called before the test suite starts executing.
     * This method can be used for initialization before tests begin, such as
     * preparing test data or logging suite-related information.
     * 
     * @param context - the current test context which contains information 
     * about the suite being executed.
     */
    @Override
    public void onStart(ITestContext context) {
        // Log that the test suite has started execution
        System.out.println("Test Suite: " + context.getName() + " is starting");
    }

    /**
     * Called after the test suite finishes executing.
     * This method logs the summary of the test suite's execution, including 
     * the number of tests that passed, failed, and were skipped.
     * 
     * @param context - the current test context which contains information 
     * about the suite that has completed.
     */
    @Override
    public void onFinish(ITestContext context) {
        // Log that the test suite has finished execution
        System.out.println("Test Suite: " + context.getName() + " has finished");
        // Log the total number of passed, failed, and skipped tests in the suite
        System.out.println("Total tests run: " + context.getPassedTests().size() + 
                           " passed, " + context.getFailedTests().size() + 
                           " failed, " + context.getSkippedTests().size() + " skipped");
    }

    /**
     * Called before each individual test method begins execution.
     * It logs the start of each test method.
     * 
     * @param result - the result of the test that is about to be executed, 
     * which contains information about the test method.
     */
    @Override
    public void onTestStart(ITestResult result) {
        // Log that the specific test method has started
        System.out.println("Test Method: " + getTestName(result) + " is starting");
    }

    /**
     * Called when an individual test method passes successfully.
     * It logs the success message for the passed test and records the 
     * execution duration.
     * 
     * @param result - the result of the test that passed.
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        // Log that the test method has passed successfully
        if (result.wasRetried()) {
            System.out.println("Test Method: " + getTestName(result) + " PASSED after retry");
        } else {
            System.out.println("Test Method: " + getTestName(result) + " PASSED");
        }
        // Log the duration for the test method
        logTestDuration(result);
        // Reset retry count on success
        retryCountMap.remove(getTestKey(result));
    }

    /**
     * Called when an individual test method fails.
     * It logs the failure message for the test, including the reason for failure 
     * (if available) and records the test's execution duration.
     * 
     * @param result - the result of the test that failed, containing details 
     * about the exception and failure reason.
     */
    @Override
    public void onTestFailure(ITestResult result) {
        Throwable throwable = result.getThrowable();
        String failureType = getFailureType(throwable);
        
        if (shouldRetry(result)) {
            int retryCount = retryCountMap.getOrDefault(getTestKey(result), 0);
            retryCount++;
            retryCountMap.put(getTestKey(result), retryCount);
            
            System.out.println("Retrying test " + getTestName(result) + 
                             " due to network exception: " + failureType + 
                             " (Attempt " + retryCount + " of " + MAX_RETRY_COUNT + ")");
            result.setStatus(ITestResult.SKIP);
        } else {
            if (result.wasRetried()) {
                System.out.println("Test Method: " + getTestName(result) + " FAILED after all retry attempts");
            } else {
                System.out.println("Test Method: " + getTestName(result) + " FAILED");
            }
            System.out.println("Failure Type: " + failureType);
            System.out.println("Failure Reason: " + throwable.getMessage());
            logTestDuration(result);
            // Reset retry count on final failure
            retryCountMap.remove(getTestKey(result));
            System.out.println("Test Method: " + getTestName(result) + " FAILED");
        }
        
        System.out.println("Failure Type: " + failureType);
       System.out.println("Failure Reason: " + throwable.getMessage());
        logTestDuration(result);
    }

    /**
     * Called when an individual test method is skipped.
     * It logs the skip message and the reason (if any) for skipping.
     * 
     * @param result - the result of the test that was skipped, including 
     * any exception or reason for the skip.
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("Test Method: " + getTestName(result) + " was SKIPPED");
        if (result.getThrowable() != null) {
            System.out.println("Skip Reason: " + result.getThrowable().getMessage());
        }
    }

    /**
     * Called when a test method fails, but the failure is within the allowed 
     * success percentage, as defined in the test configuration.
     * This is useful for scenarios where some tests are allowed to fail, but 
     * still counted as passing within a certain success threshold.
     * 
     * @param result - the result of the test that failed but within the 
     * success percentage.
     */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // Log that the test failed but within the acceptable success percentage
        System.out.println("Test Method: " + getTestName(result) + 
                           " failed but within success percentage");
        // Log the duration for the test method
        logTestDuration(result);
    }

    /**
     * Helper method to generate a descriptive name for the test, including 
     * both the method name and the class name.
     * 
     * @param result - the test result containing the method and class names.
     * @return - the formatted string with method and class names.
     */
    private String getTestName(ITestResult result) {
        // Format the test name to include method and class names
        return result.getMethod().getMethodName() + 
               " (Test Class: " + result.getTestClass().getName() + ")";
    }

    /**
     * Helper method to log the duration of the test method execution.
     * The duration is calculated by subtracting the start time from the end time.
     * 
     * @param result - the test result containing the start and end timestamps.
     */
    private void logTestDuration(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        System.out.println("Test duration: " + duration + "ms");
    }

    private String getTestKey(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    }

    private boolean shouldRetry(ITestResult result) {
        Throwable throwable = result.getThrowable();
        if (throwable == null) return false;
        
        return isNetworkException(throwable) && 
               retryCountMap.getOrDefault(getTestKey(result), 0) < MAX_RETRY_COUNT;
    }

    private String getFailureType(Throwable throwable) {
        if (throwable instanceof SocketTimeoutException) return "Connection/Read Timeout";
        if (throwable instanceof ConnectException) return "Connection Refused";
        if (throwable instanceof UnknownHostException) return "DNS Resolution Failure";
        if (throwable instanceof SSLHandshakeException) return "SSL Handshake Failure";
        if (throwable instanceof TimeoutException) return "General Timeout";
        
        String message = throwable.getMessage();
        if (message != null) {
            if (message.contains("HTTP 500")) return "Internal Server Error";
            if (message.contains("HTTP 502")) return "Bad Gateway";
            if (message.contains("HTTP 503")) return "Service Unavailable";
            if (message.contains("HTTP 504")) return "Gateway Timeout";
        }
        return "Unknown Error";
    }

    private boolean isNetworkException(Throwable throwable) {
        String message = throwable.getMessage();
        if (message != null) {
            if (message.contains("HTTP 500") ||
                message.contains("HTTP 502") ||
                message.contains("HTTP 503") ||
                message.contains("HTTP 504")) {
                return true;
            }
        }

        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            return isNetworkException(cause);
        }

        return false;
    }
}
