package com.yaxim.user.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoRequest {
    private String name;
    private String gitEmail;
}
