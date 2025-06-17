package com.yaxim.global.for_ai.dto.response;

import com.yaxim.project.controller.dto.response.ProjectDetailResponse;
import com.yaxim.team.controller.dto.response.TeamMemberResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TeamWithMemberAndProjectResponse {
    private String id;
    private String name;
    private String description;
    private String weeklyTemplate;
    private List<TeamMemberResponse> members;
    private List<ProjectDetailResponse> projects;
}
