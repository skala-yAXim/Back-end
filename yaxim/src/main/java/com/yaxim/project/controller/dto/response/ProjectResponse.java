package com.yaxim.project.controller.dto.response;

import com.yaxim.project.entity.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
}
