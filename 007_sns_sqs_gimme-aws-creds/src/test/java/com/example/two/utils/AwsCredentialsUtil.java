package com.example.two.utils;

import com.amazonaws.auth.AWSCredentials;
import com.example.one.AwsCredentialsManager;

import java.io.IOException;

/**
 * Utility class for AWS credentials operations in tests
 */
public class AwsCredentialsUtil {
    
    /**
     * Loads AWS credentials using the AwsCredentialsManager from package one
     * 
     * @return AWSCredentials object containing the access key ID and secret access key
     * @throws IOException if the credentials file cannot be read or does not contain valid credentials
     */
    public static AWSCredentials getAwsCredentials() throws IOException {
        return AwsCredentialsManager.loadGimmeAwsCredentials();
    }
    
    /**
     * Prints masked AWS credentials for verification purposes
     * 
     * @param credentials The AWS credentials to mask and print
     */
    public static void printMaskedCredentials(AWSCredentials credentials) {
        if (credentials != null) {
            String accessKeyId = credentials.getAWSAccessKeyId();
            String secretKey = credentials.getAWSSecretKey();
            
            // Mask the credentials for security
            String maskedAccessKey = maskString(accessKeyId);
            String maskedSecretKey = maskString(secretKey);
            
            System.out.println("AWS Access Key ID: " + maskedAccessKey);
            System.out.println("AWS Secret Access Key: " + maskedSecretKey);
        } else {
            System.out.println("No credentials available");
        }
    }
    
    /**
     * Masks a string by showing only the first and last 4 characters
     * 
     * @param input The string to mask
     * @return The masked string
     */
    private static String maskString(String input) {
        if (input == null || input.length() <= 8) {
            return "****";
        }
        
        return input.substring(0, 4) + "****" + input.substring(input.length() - 4);
    }
}