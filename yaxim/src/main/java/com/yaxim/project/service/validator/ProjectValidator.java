package com.yaxim.project.service.validator;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;
import com.yaxim.project.controller.dto.request.BaseProjectRequest;
import com.yaxim.project.controller.dto.request.MultipartProjectCreateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 프로젝트 관련 검증 로직을 담당하는 Validator 클래스
 * 팀장님 스타일의 CustomException 패턴 활용
 */
@Component
@Slf4j
public class ProjectValidator {

    // ========== 프로젝트 기본 정보 검증 ==========
    
    /**
     * 프로젝트 기본 정보 검증 (모든 프로젝트 요청에 공통 적용)
     * @param request 프로젝트 요청 DTO
     */
    public void validateProjectBasicInfo(BaseProjectRequest request) {
        validateProjectName(request.getName());
        validateProjectDescription(request.getDescription());
        validateDateRange(request);
    }
    
    /**
     * 프로젝트명 검증
     * @param name 프로젝트명
     */
    private void validateProjectName(String name) {
        if (name != null && name.length() > 100) {
            throw new CustomException(ErrorCode.PROJECT_NAME_TOO_LONG, name.length());
        }
    }
    
    /**
     * 프로젝트 설명 검증
     * @param description 프로젝트 설명
     */
    private void validateProjectDescription(String description) {
        if (description != null && description.length() > 1000) {
            throw new CustomException(ErrorCode.PROJECT_DESCRIPTION_TOO_LONG, description.length());
        }
    }
    
    /**
     * 날짜 범위 검증
     * @param request 프로젝트 요청 DTO
     */
    private void validateDateRange(BaseProjectRequest request) {
        if (!request.isDateRangeValid()) {
            throw new CustomException(ErrorCode.PROJECT_DATE_RANGE_INVALID);
        }
    }

    // ========== 파일 업로드 검증 ==========
    
    /**
     * 파일 업로드 검증 (Multipart 요청에만 적용)
     * @param request Multipart 프로젝트 요청 DTO
     */
    public void validateFileUpload(MultipartProjectCreateRequest request) {
        if (!request.hasFiles()) {
            return; // 파일이 없으면 검증 생략
        }
        
        List<MultipartFile> validFiles = request.getValidFiles();
        
        validateFileCount(validFiles);
        validateFileSizes(validFiles);
        validateFileFormats(request);
    }
    
    /**
     * 파일 개수 검증 (최대 5개)
     * @param files 업로드 파일 리스트
     */
    private void validateFileCount(List<MultipartFile> files) {
        if (files.size() > 5) {
            throw new CustomException(ErrorCode.FILE_COUNT_EXCEEDED, files.size());
        }
    }
    
    /**
     * 파일 크기 검증 (각 파일 최대 50MB)
     * @param files 업로드 파일 리스트
     */
    private void validateFileSizes(List<MultipartFile> files) {
        final long MAX_FILE_SIZE = 50 * 1024 * 1024L; // 50MB
        
        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED, 
                    file.getOriginalFilename(), formatFileSize(file.getSize()));
            }
        }
    }
    
    /**
     * 파일 형식 검증 (DOCX, XLSX, TXT만 허용)
     * @param request Multipart 프로젝트 요청 DTO
     */
    private void validateFileFormats(MultipartProjectCreateRequest request) {
        List<String> invalidFiles = request.getInvalidFileFormats();
        if (!invalidFiles.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_FORMAT_NOT_SUPPORTED, 
                String.join(", ", invalidFiles));
        }
    }
    
    // ========== 유틸리티 메서드 ==========
    
    /**
     * 파일 크기를 읽기 쉬운 형태로 포맷
     * @param size 바이트 단위 파일 크기
     * @return 포맷된 파일 크기 문자열 (예: "10.5MB")
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + "B";
        if (size < 1024 * 1024) return String.format("%.1fKB", size / 1024.0);
        return String.format("%.1fMB", size / (1024.0 * 1024.0));
    }
    
    /**
     * 허용된 파일 확장자 목록
     * @return 허용된 파일 확장자 배열
     */
    public static String[] getAllowedFileExtensions() {
        return new String[]{".docx", ".xlsx", ".txt"};
    }
    
    /**
     * 파일 확장자 검증 (단일 파일용)
     * @param filename 파일명
     * @return 허용된 확장자이면 true
     */
    public static boolean isAllowedFileExtension(String filename) {
        if (filename == null) return false;
        String extension = filename.toLowerCase();
        for (String allowed : getAllowedFileExtensions()) {
            if (extension.endsWith(allowed)) {
                return true;
            }
        }
        return false;
    }
}
