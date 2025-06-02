package com.yaxim.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * 🔥 최소한의 AWS 설정 (팀장님 요청에 따라 단순화)
 * - S3Properties 제거
 * - 복잡한 설정 제거
 * - 필수 Bean만 등록
 */
@Configuration
@Slf4j
public class AwsConfig {

    @Bean
    public S3Client s3Client(@Value("${aws.s3.access-key}") String accessKey,
                            @Value("${aws.s3.secret-key}") String secretKey,
                            @Value("${aws.s3.region}") String region) {
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            
            S3Client client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
                    
            log.info("✅ S3Client Bean 생성 완료 (region: {})", region);
            return client;
            
        } catch (Exception e) {
            log.error("❌ S3Client Bean 생성 실패: {}", e.getMessage());
            throw e;
        }
    }

    @Bean
    public S3Presigner s3Presigner(@Value("${aws.s3.access-key}") String accessKey,
                                  @Value("${aws.s3.secret-key}") String secretKey,
                                  @Value("${aws.s3.region}") String region) {
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            
            S3Presigner presigner = S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
                    
            log.info("✅ S3Presigner Bean 생성 완료 (region: {})", region);
            return presigner;
            
        } catch (Exception e) {
            log.error("❌ S3Presigner Bean 생성 실패: {}", e.getMessage());
            throw e;
        }
    }
}
