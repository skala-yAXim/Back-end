package com.yaxim.team.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamMemberResponse {
    private Long id;
    private String name;
    private String email;
}
