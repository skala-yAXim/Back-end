package com.yaxim.git.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GitInfoResponse {
    private String gitId;
    private String gitEmail;
    private String gitUrl;
}
