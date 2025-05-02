package com.sqs.tests;

import com.sqs.services.SqsService;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.asserts.SoftAssert;
import software.amazon.awssdk.regions.Region;

/**
 * BaseSqsTest is an abstract class that provides common setup and teardown
 * functionality for SQS-related tests. It initializes the SQS service and
 * provides a SoftAssert instance for assertions.
 */
public abstract class BaseSqsTest {
    protected SqsService sqsService; // SQS service instance for sending/receiving messages
    protected SoftAssert softAssert; // SoftAssert instance for flexible assertions

    // Placeholder for the SQS Queue URL. Replace with your actual SQS Queue URL.
    protected String testQueueUrl = "YOUR_SQS_QUEUE_URL";
    // AWS Region where your SQS Queue is located. Replace with your actual region.
    protected Region awsRegion = Region.US_EAST_1; // Example: us-east-1

    /**
     * Setup method that runs once before any test methods in the class.
     * It initializes the SQS service with the specified queue URL and region.
     */
    @BeforeClass
    public void setup() {
        // Check if the queue URL and region are properly configured
        if (testQueueUrl == null || testQueueUrl.equals("YOUR_SQS_QUEUE_URL") || awsRegion == null) {
            throw new IllegalArgumentException("Please configure testQueueUrl and awsRegion before running tests.");
        }
        System.out.println("Setting up SQS Service for Queue: " + testQueueUrl + " in Region: " + awsRegion);
        sqsService = SqsService.getInstance(); // Get the singleton instance of SqsService
        sqsService.initialize(testQueueUrl, awsRegion); // Initialize the SQS service
    }

    /**
     * Before each test method, a new SoftAssert instance is created.
     * This ensures that assertions in each test method are independent.
     */
    @BeforeMethod
    public void beforeMethod() {
        softAssert = new SoftAssert(); // Initialize a new SoftAssert for each test method
        System.out.println("Starting new test method...");
    }

    /**
     * Teardown method that runs once after all test methods in the class.
     * It closes the SQS service to release any resources.
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        System.out.println("Closing SQS client...");
        if (sqsService != null) {
            sqsService.close(); // Close the SQS service to release resources
        }
        System.out.println("SQS client closed.");
    }
}