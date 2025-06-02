package com.yaxim.team.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamResponse {
    private String id;
    private String name;
    private String description;
}
