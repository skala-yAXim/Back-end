package com.yaxim.graph.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GraphTeamMemberResponse {
    public List<Members> value;

    @Getter
    public static class Members {
        public String id;
        public String displayName;
        public String userId;
        public String email;
        public List<String> roles;
    }
}
