package com.yaxim.git.service;

import com.yaxim.git.controller.dto.response.GitInfoResponse;
import com.yaxim.git.entity.GitInfo;
import com.yaxim.git.exception.GitInfoNotFoundException;
import com.yaxim.user.entity.Users;
import com.yaxim.git.repository.GitInfoRepository;
import com.yaxim.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitInfoServiceTest {

    @InjectMocks
    private GitInfoService gitInfoService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private GitInfoRepository gitInfoRepository;

    private Users user;
    private GitInfo gitInfo;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        user = new Users(
                USER_ID,
                "name",
                "email@test.com"
        );

        gitInfo = new GitInfo(
                1L,
                user,
                "test",
                "email@test.com",
                "git.url",
                ""
        );
    }

    @Test
    @DisplayName("Git 정보 조회")
    void testGetGitInfo() {
        // given
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(gitInfoRepository.findByUser(user))
                .thenReturn(Optional.of(gitInfo));

        // when
        GitInfoResponse response = gitInfoService.getGitInfo(user.getId());

        // then
        verify(userRepository).findById(user.getId());
        verify(gitInfoRepository).findByUser(user);
        assertEquals(gitInfo.getGitId(), response.getGitId());
        assertEquals(gitInfo.getGitEmail(), response.getGitEmail());
    }

    @Test
    @DisplayName("Git 정보 업데이트")
    void testUpdateGitInfo() {
        // given
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(gitInfoRepository.findByUser(user))
                .thenReturn(Optional.of(gitInfo));

        String updatedGitUrl = "new.git.url";
        String updatedGitEmail = "newemail@test.com";
        String updatedGitId = "new_git_id";

        gitInfo.setGitUrl(updatedGitUrl);
        gitInfo.setGitEmail(updatedGitEmail);
        gitInfo.setGitId(updatedGitId);

        // when
        gitInfoService.updateGitInfo(user, gitInfo);

        // then
        verify(gitInfoRepository).save(gitInfo);
        assertEquals(updatedGitUrl, "new.git.url");
        assertEquals(updatedGitEmail, "newemail@test.com");
        assertEquals(updatedGitId, "new_git_id");
    }

    @Test
    @DisplayName("Git 연동 정보 삭제")
    void testDeleteGitInfo() {
        // given
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(gitInfoRepository.findByUser(user))
                .thenReturn(Optional.of(gitInfo));

        // when
        gitInfoService.deleteGitInfo(user.getId());

        // then
        verify(gitInfoRepository).delete(gitInfo);
    }

    @Test
    @DisplayName("최초 연동 시 Git 데이터 없을 때 예외처리")
    void testUpdateGitInfoWhenUserFoundAndGitInfoNotFound() {
        // given
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(gitInfoRepository.findByUser(user))
                .thenReturn(Optional.empty());

        // when
        gitInfoService.updateGitInfo(user, gitInfo);

        // then
        verify(gitInfoRepository, times(1))
                .save(any(GitInfo.class));
    }


    @Test
    @DisplayName("Git 연동 정보 업데이트 시 Git 정보가 없을 때 예외처리")
    void testUpdateGitInfoWhenGitInfoNotFound() {
        // given
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(gitInfoRepository.findByUser(user))
                .thenReturn(Optional.empty());

        // when
        gitInfoService.updateGitInfo(user, gitInfo);

        // then
        verify(gitInfoRepository).save(any(GitInfo.class));
    }

    @Test
    @DisplayName("Git 연동 정보 삭제 시 Git 정보가 없을 때 예외처리")
    void testDeleteGitInfoWhenGitInfoNotFound() {
        // given
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(gitInfoRepository.findByUser(user))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(GitInfoNotFoundException.class, () ->
                gitInfoService.deleteGitInfo(user.getId())
        );
        verify(gitInfoRepository, times(0)).delete(any(GitInfo.class));
    }
}