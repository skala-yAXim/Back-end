package com.yaxim.git.service;

import com.yaxim.git.controller.dto.response.GitInfoResponse;
import com.yaxim.git.entity.GitInfo;
import com.yaxim.git.exception.GitInfoNotFoundException;
import com.yaxim.git.repository.GitInfoRepository;
import com.yaxim.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GitInfoService {
    private final GitInfoRepository gitInfoRepository;

    @Transactional
    public void updateGitInfo(Users user, GitInfo gitInfo) {
        GitInfo info = gitInfoRepository.findByUser(user)
                        .orElse(new GitInfo(user));
        info.setGitId(gitInfo.getGitId());
        info.setGitEmail(gitInfo.getGitEmail());

        gitInfoRepository.save(info);
    }
}
