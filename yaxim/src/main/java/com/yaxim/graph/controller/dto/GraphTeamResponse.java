package com.yaxim.graph.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GraphTeamResponse {
    public List<Team> value;

    public static class Team {
        public String id;
        public String displayName;
        public String description;
    }
}
