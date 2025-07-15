package com.webapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {
    @Value("${aws.region}")
    private String awsRegion;
    @Value("${AWS_ACCESS_KEY_ID}")
    private String accessKey;
    
    @Value("${AWS_SECRET_ACCESS_KEY}")
    private String secretKey;
    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        // The SDK will automatically use the IAM role from the ECS Task Definition
        return S3Client.builder()
                       .region(Region.of(awsRegion))
                       .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                       .build();
    }
}

