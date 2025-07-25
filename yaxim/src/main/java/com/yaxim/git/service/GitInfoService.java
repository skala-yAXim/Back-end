package com.yaxim.git.service;

import com.yaxim.git.controller.dto.request.GitWebhookRequest;
import com.yaxim.git.controller.dto.response.GitInfoResponse;
import com.yaxim.git.entity.GitInfo;
import com.yaxim.git.exception.GitInfoNotFoundException;
import com.yaxim.git.repository.GitInfoRepository;
import com.yaxim.team.entity.Team;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.entity.Users;
import com.yaxim.user.exception.UserHasNoAuthorityException;
import com.yaxim.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class GitInfoService {
    private final GitInfoRepository gitInfoRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public void updateGitInfo(Users user, GitInfo gitInfo) {
        Users managedUser = userRepository.findById(user.getId())
                .orElseThrow(UserNotFoundException::new);

        GitInfo info = gitInfoRepository.findByUser(managedUser)
                        .orElse(new GitInfo(managedUser));
        info.setGitId(gitInfo.getGitId());
        info.setGitEmail(gitInfo.getGitEmail());
        info.setGitUrl(gitInfo.getGitUrl());
        info.setAvatarUrl(gitInfo.getAvatarUrl());
        info.setUpdatedAt(LocalDateTime.now());

        gitInfoRepository.save(info);
    }

    @Transactional
    public GitInfoResponse getGitInfo(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        GitInfo info = gitInfoRepository.findByUser(user)
                .orElse(new GitInfo(user));
//                .orElseThrow(GitInfoNotFoundException::new);

        if (info.getGitId() == null) {
            return getGitResponse(false, info);
        } else {
            return getGitResponse(true, info);
        }
    }

    private GitInfoResponse getGitResponse(boolean isConnected, GitInfo info) {
        return new GitInfoResponse(
                isConnected,
                info.getCreatedAt(),
                info.getUpdatedAt(),
                info.getGitId(),
                info.getGitEmail(),
                info.getGitUrl(),
                info.getAvatarUrl()
        );
    }

    @Transactional
    public void updateTeamGitInfo(GitWebhookRequest request) {
        String gitId = request.getSender().getLogin();

        GitInfo info = gitInfoRepository.findByGitId(gitId)
                .orElseThrow(GitInfoNotFoundException::new);

        Users user = info.getUser();

        TeamMember member = teamMemberRepository.findByUserId(user.getId())
                .orElseThrow(UserNotFoundException::new);

        if (!member.getRole().isLeader()) throw new UserHasNoAuthorityException();

        Team team = member.getTeam();

        team.setInstallationId(request.getInstallation().getId());
    }

    @Transactional
    public void deleteGitInfo(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        GitInfo info = gitInfoRepository.findByUser(user)
                .orElseThrow(GitInfoNotFoundException::new);

        gitInfoRepository.delete(info);
    }
}
