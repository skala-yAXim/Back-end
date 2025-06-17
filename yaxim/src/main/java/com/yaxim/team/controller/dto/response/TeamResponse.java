package com.yaxim.team.controller.dto.response;

import com.yaxim.team.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TeamResponse {
    private String id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String name;
    private String description;
    private String weeklyTemplate;

    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getCreatedAt(),
                team.getUpdatedAt(),
                team.getName(),
                team.getDescription(),
                team.getWeeklyTemplate()
        );
    }
}
