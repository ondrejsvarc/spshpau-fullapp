package com.spshpau.projectservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.credentials.access-key-id:#{null}}")
    private String accessKeyIdFromYaml;

    @Value("${aws.credentials.secret-access-key:#{null}}")
    private String secretAccessKeyFromYaml;

    @Bean
    public S3Client s3Client() {
        Region region = Region.of(awsRegion);
        if (accessKeyIdFromYaml != null && secretAccessKeyFromYaml != null) {
            System.out.println("Attempting to use static credentials from application.yml for S3Client"); // Log this
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyIdFromYaml, secretAccessKeyFromYaml);
            return S3Client.builder()
                    .region(region)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
        } else {
            // Fallback to default provider chain
            System.out.println("Using DefaultCredentialsProvider for S3Client");
            return S3Client.builder()
                    .region(region)
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }
    }

    @Bean
    public S3Presigner s3Presigner() {
        Region region = Region.of(awsRegion);
        if (accessKeyIdFromYaml != null && secretAccessKeyFromYaml != null) {
            System.out.println("Attempting to use static credentials from application.yml for S3Presigner"); // Log this
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyIdFromYaml, secretAccessKeyFromYaml);
            return S3Presigner.builder()
                    .region(region)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
        } else {
            System.out.println("Using DefaultCredentialsProvider for S3Presigner"); // Log this
            return S3Presigner.builder()
                    .region(region)
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }
    }
}
