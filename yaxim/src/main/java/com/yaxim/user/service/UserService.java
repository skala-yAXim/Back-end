package com.yaxim.user.service;

import com.yaxim.git.entity.GitInfo;
import com.yaxim.git.exception.GitInfoNotFoundException;
import com.yaxim.git.repository.GitInfoRepository;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.controller.dto.request.UserInfoRequest;
import com.yaxim.user.controller.dto.response.UserInfoResponse;
import com.yaxim.user.entity.Users;
import com.yaxim.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final GitInfoRepository gitInfoRepository;

    public UserInfoResponse getUserInfo(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return getUserResponse(user);
    }

    @Transactional
    public UserInfoResponse updateUserInfo(UserInfoRequest request, Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.setName(request.getName());

        return getUserResponse(user);
    }

    private UserInfoResponse getUserResponse(Users user) {
        TeamMember member = teamMemberRepository.findByEmail(user.getEmail())
                .orElseThrow(UserNotFoundException::new);

        GitInfo gitInfo = gitInfoRepository.findByUser(user)
                .orElse(new GitInfo());

        return new UserInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                member.getRole(),
                gitInfo.getGitEmail()
        );
    }
}
