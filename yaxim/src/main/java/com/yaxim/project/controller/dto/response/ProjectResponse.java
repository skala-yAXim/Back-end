package com.yaxim.project.controller.dto.response;

import com.yaxim.project.entity.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ProjectStatus status;
}
