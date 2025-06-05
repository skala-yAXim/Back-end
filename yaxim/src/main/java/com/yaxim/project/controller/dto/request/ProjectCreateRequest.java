package com.yaxim.project.controller.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * 프로젝트 요청 DTO의 공통 필드를 담는 베이스 클래스
 * JSON과 Multipart 요청에서 공통으로 사용되는 필드들을 정의
 */
@Getter
@Setter
@NoArgsConstructor
public class ProjectCreateRequest {

    @NotBlank(message = "프로젝트명은 필수입니다.")
    private String name;

    @NotNull(message = "시작 시각은 필수 입력값입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "종료 시각은 필수 입력값입니다.")
    @Future(message = "종료 시각은 현재 날짜 이후입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Size(max = 1000, message = "프로젝트 설명은 1000자 이하여야 합니다.")
    private String description;

    @NotEmpty(message = "파일은 필수 입력값입니다")
    private List<MultipartFile> files;
}
