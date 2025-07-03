package com.yaxim.user.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoRequest {
    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String name;
}
