package com.yaxim.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    // ✅ S3Client와 S3Presigner는 별도 Bean 설정 필요 (뒤에서 해결)
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    
    // 🔥 @Value 어노테이션으로 설정값 직접 주입 (S3Properties 제거)
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    @Value("${aws.s3.region}")
    private String region;
    
    @Value("${aws.s3.project-files-prefix}")
    private String projectFilesPrefix;
    
    @Value("${aws.s3.url-expiration-minutes}")
    private int urlExpirationMinutes;
    
    @Value("${aws.s3.max-file-size}")
    private long maxFileSize;
    
    @Value("${aws.s3.cloudfront-url:}")
    private String cloudfrontUrl;
    
    // 🔥 콤마로 구분된 문자열을 List로 변환
    @Value("#{'${aws.s3.allowed-file-types}'.split(',')}")
    private List<String> allowedFileTypes;

    /**
     * 프로젝트 파일 업로드 (기본 암호화 사용 - AWS에서 설정된 대로)
     */
    public S3UploadResult uploadProjectFile(MultipartFile file, Long projectId) {
        log.info("=== S3Service.uploadProjectFile 시작 ===");
        log.info("파일명: {}", file.getOriginalFilename());
        log.info("파일 크기: {} bytes", file.getSize());
        log.info("Content-Type: {}", file.getContentType());
        log.info("프로젝트 ID: {}", projectId);
        
        try {
            validateFile(file);
            log.info("파일 검증 통과");
        } catch (Exception e) {
            log.error("파일 검증 실패: {}", e.getMessage(), e);
            throw e;
        }
        
        String originalFileName = file.getOriginalFilename();
        String storedFileName = generateStoredFileName(originalFileName);
        String s3ObjectKey = projectFilesPrefix + "project-" + projectId + "/" + storedFileName;

        log.info("S3 Object Key: {}", s3ObjectKey);

        try {
            // 메타데이터 설정
            Map<String, String> metadata = createFileMetadata(file, projectId);
            
            // ✅ S3 업로드 - 버킷 기본 암호화 설정 사용
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3ObjectKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .metadata(metadata)
                    .build();

            log.info("S3 업로드 시작 (버킷 기본 암호화 사용)...");
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("S3 업로드 완료 (버킷 기본 암호화 적용됨)");

            String fileUrl = generateFileUrl(s3ObjectKey);
            
            log.info("파일 업로드 성공 - S3 Key: {}, 파일명: {}", s3ObjectKey, originalFileName);
            log.info("=== S3Service.uploadProjectFile 완료 ===");

            return S3UploadResult.builder()
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .s3ObjectKey(s3ObjectKey)
                    .fileUrl(fileUrl)
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .bucketName(bucketName)
                    .build();

        } catch (IOException e) {
            log.error("파일 입출력 오류 - 파일명: {}, 에러: {}", originalFileName, e.getMessage(), e);
            throw new RuntimeException("파일 업로드에 실패했습니다: " + e.getMessage(), e);
        } catch (S3Exception e) {
            log.error("S3 업로드 실패 - S3 Key: {}, 에러: {}", s3ObjectKey, e.getMessage(), e);
            // ✅ 구체적인 해결방법 안내
            if (e.getMessage().contains("KMS") || e.getMessage().contains("InvalidArgument")) {
                log.error("❌ KMS 암호화 관련 에러!");
                log.error("❌ 해결방법: AWS S3 콘솔 → 버킷 선택 → Properties → Default encryption → SSE-S3로 변경");
            }
            throw new RuntimeException("S3 업로드에 실패했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("예상치 못한 오류 - 파일명: {}, 에러: {}", originalFileName, e.getMessage(), e);
            throw new RuntimeException("파일 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * Pre-signed URL 생성 (파일 다운로드용)
     */
    public String generatePresignedUrl(String s3ObjectKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3ObjectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(urlExpirationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            log.info("Pre-signed URL 생성 완료 - S3 Key: {}", s3ObjectKey);
            return presignedUrl;

        } catch (S3Exception e) {
            log.error("Pre-signed URL 생성 실패 - S3 Key: {}, 에러: {}", s3ObjectKey, e.getMessage());
            throw new RuntimeException("파일 다운로드 URL 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String s3ObjectKey) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3ObjectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("파일 삭제 완료 - S3 Key: {}", s3ObjectKey);

        } catch (S3Exception e) {
            log.error("파일 삭제 실패 - S3 Key: {}, 에러: {}", s3ObjectKey, e.getMessage());
            throw new RuntimeException("파일 삭제에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 존재 여부 확인
     */
    public boolean fileExists(String s3ObjectKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3ObjectKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("파일 존재 여부 확인 실패 - S3 Key: {}, 에러: {}", s3ObjectKey, e.getMessage());
            throw new RuntimeException("파일 존재 여부 확인에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 프로젝트 전체 파일 삭제 (프로젝트 삭제 시 사용)
     */
    public void deleteProjectFiles(Long projectId) {
        String prefix = projectFilesPrefix + "project-" + projectId + "/";
        
        try {
            // 해당 접두사를 가진 모든 객체 목록 조회
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            
            // 각 객체 삭제
            for (S3Object s3Object : listResponse.contents()) {
                deleteFile(s3Object.key());
            }

            log.info("프로젝트 전체 파일 삭제 완료 - 프로젝트 ID: {}, 삭제된 파일 수: {}", 
                    projectId, listResponse.contents().size());

        } catch (S3Exception e) {
            log.error("프로젝트 파일 일괄 삭제 실패 - 프로젝트 ID: {}, 에러: {}", projectId, e.getMessage());
            throw new RuntimeException("프로젝트 파일 삭제에 실패했습니다: " + e.getMessage(), e);
        }
    }

    // === Private Helper Methods ===

    private void validateFile(MultipartFile file) {
        log.info("=== 파일 검증 시작 ===");
        
        if (file == null || file.isEmpty()) {
            log.error("파일이 null이거나 비어있음");
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        log.info("파일 존재성 검증 통과");

        if (file.getSize() > maxFileSize) {
            log.error("파일 크기 초과 - 현재: {}MB, 최대: {}MB", 
                    file.getSize() / 1024 / 1024, 
                    maxFileSize / 1024 / 1024);
            throw new IllegalArgumentException("파일 크기가 제한을 초과했습니다. (최대: " + 
                    (maxFileSize / 1024 / 1024) + "MB)");
        }
        log.info("파일 크기 검증 통과 - {}MB", file.getSize() / 1024 / 1024);

        String contentType = file.getContentType();
        log.info("파일 Content-Type: {}", contentType);
        log.info("허용된 파일 타입들: {}", allowedFileTypes);
        
        // ✅ 파일 확장자 기반 검증
        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            String extension = fileName.toLowerCase();
            log.info("파일 확장자 기반 검증 - 파일명: {}", fileName);
            
            boolean isValidByExtension = extension.endsWith(".docx") || 
                                       extension.endsWith(".xlsx") || 
                                       extension.endsWith(".txt");
            
            if (isValidByExtension) {
                log.info("파일 확장자 검증 통과: {}", extension);
                return;
            } else {
                log.error("허용되지 않는 파일 확장자: {}", extension);
                throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. DOCX, XLSX, TXT 파일만 업로드 가능합니다.");
            }
        }
        
        // ✅ Content-Type 기반 검증 (백업)
        if (allowedFileTypes != null && 
            !allowedFileTypes.isEmpty() && 
            !allowedFileTypes.contains(contentType)) {
            log.error("허용되지 않는 Content-Type: {}", contentType);
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + contentType);
        }
        
        log.info("Content-Type 검증 통과");
        log.info("=== 파일 검증 완료 ===");
    }

    private String generateStoredFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    private Map<String, String> createFileMetadata(MultipartFile file, Long projectId) {
        Map<String, String> metadata = new HashMap<>();
        
        metadata.put("project-id", String.valueOf(projectId));
        metadata.put("upload-timestamp", String.valueOf(System.currentTimeMillis()));
        metadata.put("content-type", file.getContentType());
        metadata.put("file-size", String.valueOf(file.getSize()));
        
        log.debug("S3 메타데이터 생성 완료 - 프로젝트 ID: {}", projectId);
        
        return metadata;
    }

    private String generateFileUrl(String s3ObjectKey) {
        if (cloudfrontUrl != null && !cloudfrontUrl.isEmpty()) {
            return cloudfrontUrl + "/" + s3ObjectKey;
        } else {
            return String.format("https://%s.s3.%s.amazonaws.com/%s", 
                    bucketName, region, s3ObjectKey);
        }
    }

    /**
     * S3 업로드 결과 DTO
     */
    @lombok.Builder
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class S3UploadResult {
        private String originalFileName;
        private String storedFileName;
        private String s3ObjectKey;
        private String fileUrl;
        private Long fileSize;
        private String fileType;
        private String bucketName;
    }
}
