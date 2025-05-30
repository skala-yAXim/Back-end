package com.yaxim.user.controller.dto.response;

import com.yaxim.user.entity.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {
    private long userId;
    private String name;
    private String email;
    private UserRole userRole;
    private String gitEmail;
}
