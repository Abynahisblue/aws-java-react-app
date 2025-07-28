package com.webapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
public class S3Config {
    @Value("${aws.region:eu-central-1}")
    private String awsRegion;
    
    @Value("${AWS_ACCESS_KEY_ID:}")
    private String accessKey;
    
    @Value("${AWS_SECRET_ACCESS_KEY:}")
    private String secretKey;
    
    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder().region(Region.of(awsRegion));
        
        // Only use static credentials if both are provided
        if (!accessKey.isEmpty() && !secretKey.isEmpty()) {
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
            builder.credentialsProvider(StaticCredentialsProvider.create(awsCredentials));
        }
        // Otherwise, use default credential provider chain (ECS task role)
        
        return builder.build();
    }
}

