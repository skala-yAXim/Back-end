package com.yaxim.project.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "프로젝트 수정 요청")
public class ProjectUpdateRequest {
    @NotNull(message = "프로젝트 ID는 필수 입력값입니다.")
    private Long id;

    @NotBlank(message = "프로젝트명은 필수 입력값입니다.")
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

    private List<MultipartFile> files;

    private List<Long> deleteFileIds;
}
