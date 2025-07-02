package com.yaxim.project.controller.dto.response;

import com.yaxim.project.entity.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String name;
    private Integer progress;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
}
