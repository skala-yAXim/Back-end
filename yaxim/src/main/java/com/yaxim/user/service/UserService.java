package com.yaxim.user.service;

import com.yaxim.user.controller.dto.response.UserInfoResponse;
import com.yaxim.user.entity.user.Users;
import com.yaxim.user.entity.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserInfoResponse getUserInfo(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return new UserInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUserRole(),
                user.getGitEmail()
        );
    }
}
