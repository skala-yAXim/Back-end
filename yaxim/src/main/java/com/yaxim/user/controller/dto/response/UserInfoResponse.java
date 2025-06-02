package com.yaxim.user.controller.dto.response;

import com.yaxim.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {
    private long userId;
    private String name;
    private String email;
    private String gitEmail;
}
