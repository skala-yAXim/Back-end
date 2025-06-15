package com.yaxim.global.for_ai.dto.response;

import com.yaxim.project.controller.dto.response.ProjectDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TeamWithProjectResponse {
    private String id;
    private String name;
    private String description;
    private List<ProjectDetailResponse> projects;
}
