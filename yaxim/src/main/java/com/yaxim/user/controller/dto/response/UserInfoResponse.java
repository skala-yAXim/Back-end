package com.yaxim.user.controller.dto.response;

import com.yaxim.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {
    private Long userId;
    private String name;
    private String email;
    private UserRole userRole;
    private String gitEmail;
}
