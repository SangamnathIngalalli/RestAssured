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
 * <p>
 * This class handles the following responsibilities:
 * <ul>
 *   <li>Setting up the SQS service with the configured queue URL and region</li>
 *   <li>Configuring the AWS profile for authentication</li>
 *   <li>Creating a fresh SoftAssert instance for each test method</li>
 *   <li>Cleaning up resources after tests complete</li>
 * </ul>
 * <p>
 * To use this class, extend it and implement test methods that use the
 * provided sqsService and softAssert fields.
 */
public abstract class BaseSqsTest {
    protected SqsService sqsService; // SQS service instance for sending/receiving messages
    protected SoftAssert softAssert; // SoftAssert instance for flexible assertions

    // Placeholder for the SQS Queue URL. Replace with your actual SQS Queue URL.
    protected String testQueueUrl = "YOUR_SQS_QUEUE_URL";
    // AWS Region where your SQS Queue is located. Replace with your actual region.
    protected Region awsRegion = Region.US_EAST_1; // Example: us-east-1
    // AWS Profile to use (created by gimme-aws-creds)
    protected String awsProfile = System.getProperty("aws.profile", "default");

    /**
     * Setup method that runs once before any test methods in the class.
     * It initializes the SQS service with the specified queue URL and region.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Validates that the queue URL and region are properly configured</li>
     *   <li>Sets the AWS profile system property for authentication</li>
     *   <li>Initializes the SQS service</li>
     * </ol>
     * <p>
     * Before running tests, you must set the testQueueUrl to a valid SQS queue URL
     * either by overriding the field in a subclass or by setting it through a
     * system property or configuration file.
     * 
     * @throws IllegalArgumentException if testQueueUrl or awsRegion is not properly configured
     */
    @BeforeClass
    public void setup() {
        // Check if the queue URL and region are properly configured
        if (testQueueUrl == null || testQueueUrl.equals("YOUR_SQS_QUEUE_URL") || awsRegion == null) {
            throw new IllegalArgumentException("Please configure testQueueUrl and awsRegion before running tests.");
        }
        System.out.println("Setting up SQS Service for Queue: " + testQueueUrl + " in Region: " + awsRegion + " using profile: " + awsProfile);
        
        // Set the AWS profile to use
        System.setProperty("aws.profile", awsProfile);
        
        sqsService = SqsService.getInstance(); // Get the singleton instance of SqsService
        sqsService.initialize(testQueueUrl, awsRegion); // Initialize the SQS service
    }

    /**
     * Before each test method, a new SoftAssert instance is created.
     * This ensures that assertions in each test method are independent.
     * <p>
     * Using SoftAssert allows tests to continue execution after assertion failures,
     * collecting all failures rather than stopping at the first one. This provides
     * more comprehensive test results.
     */
    @BeforeMethod
    public void beforeMethod() {
        softAssert = new SoftAssert(); // Initialize a new SoftAssert for each test method
        System.out.println("Starting new test method...");
    }

    /**
     * Teardown method that runs once after all test methods in the class.
     * It closes the SQS service to release any resources.
     * <p>
     * This method ensures proper cleanup of AWS resources and connections
     * to prevent resource leaks. The alwaysRun=true attribute ensures this
     * method runs even if test methods or setup fail.
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