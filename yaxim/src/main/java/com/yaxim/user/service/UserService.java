package com.yaxim.user.service;

import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.controller.dto.response.UserInfoResponse;
import com.yaxim.user.entity.Users;
import com.yaxim.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    public UserInfoResponse getUserInfo(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        TeamMember member = teamMemberRepository.findByEmail(user.getEmail())
                .orElseThrow(UserNotFoundException::new);

        return new UserInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                member.getRole(),
                user.getGitEmail()
        );
    }
}
