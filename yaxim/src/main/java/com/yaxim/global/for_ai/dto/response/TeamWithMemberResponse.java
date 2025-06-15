package com.yaxim.global.for_ai.dto.response;

import com.yaxim.team.controller.dto.response.TeamMemberResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class TeamWithMemberResponse {
    private String id;
    private String name;
    private String description;
    private List<TeamMemberResponse> members;
}
