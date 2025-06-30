package com.yaxim.git.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GitInfoResponse {
    private boolean connected;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String gitId;
    private String gitEmail;
    private String gitUrl;
    private String avatarUrl;
}
