package com.yaxim.global.s3;

import com.yaxim.project.exception.*;
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

import java.time.Duration;
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
     * 파일 업로드 (기본 암호화 사용 - AWS에서 설정된 대로)
     */
    public S3UploadResult uploadProjectFile(MultipartFile file, Long projectId){
        validateFile(file);
        
        String originalFileName = file.getOriginalFilename();
        String storedFileName = generateStoredFileName(originalFileName);
        String s3ObjectKey = projectFilesPrefix + "project-" + projectId + "/" + storedFileName;

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


        try {
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

        } catch (Exception e) {
            throw new FileUploadFailedException();
        }

        String fileUrl = generateFileUrl(s3ObjectKey);

            return S3UploadResult.builder()
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .s3ObjectKey(s3ObjectKey)
                    .fileUrl(fileUrl)
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .bucketName(bucketName)
                    .build();
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

            return presignedRequest.url().toString();

        } catch (S3Exception e) {
            throw new FileDownloadUrlGenerateFailedException();
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

        } catch (S3Exception e) {
            throw new FileDeleteFailedException();
        }
    }

    /**
     * 프로젝트 전체 파일 삭제 (프로젝트 삭제 시 사용)
     */
    public void deleteProjectFiles(Long projectId) {
        String prefix = projectFilesPrefix + "project-" + projectId + "/";

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

        // 각 객체 삭제
        for (S3Object s3Object : listResponse.contents()) {
            deleteFile(s3Object.key());
        }
    }

    // === Private Helper Methods ===

    private void validateFile(MultipartFile file) {
        // 빈 파일 여부 검증
        if (file == null || file.isEmpty()) {
            throw new FileShouldNotBeNullException();
        }

        // 파일 크기 검증
        if (file.getSize() > maxFileSize) {
            throw new FileSizeExceededException();
        }

        // 파일 확장자 기반 검증
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        // 이름 기반 검증
        if (fileName != null) {
            String extension = fileName.toLowerCase();
            
            boolean isValidByExtension = extension.endsWith(".docx") || 
                                       extension.endsWith(".xlsx") || 
                                       extension.endsWith(".txt");
            
            if (isValidByExtension) {
                return;
            } else {
                throw new FileFormatNotSupportedException();
            }
        }
        
        // Content-Type 기반 검증
        if (allowedFileTypes != null && 
            !allowedFileTypes.isEmpty() && 
            !allowedFileTypes.contains(contentType)) {
            throw new FileFormatNotSupportedException();
        }
    }

    private String generateStoredFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }

    private Map<String, String> createFileMetadata(MultipartFile file, Long projectId) {
        Map<String, String> metadata = new HashMap<>();
        
        metadata.put("project-id", String.valueOf(projectId));
        metadata.put("upload-timestamp", String.valueOf(System.currentTimeMillis()));
        metadata.put("content-type", file.getContentType());
        metadata.put("file-size", String.valueOf(file.getSize()));
        
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
