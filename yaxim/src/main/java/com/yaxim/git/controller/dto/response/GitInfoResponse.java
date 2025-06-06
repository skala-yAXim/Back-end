package com.yaxim.git.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GitInfoResponse {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String gitId;
    private String gitEmail;
    private String gitUrl;
    private String avatarUrl;
}
