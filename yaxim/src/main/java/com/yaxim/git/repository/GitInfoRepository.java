package com.yaxim.git.repository;

import com.yaxim.git.entity.GitInfo;
import com.yaxim.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GitInfoRepository extends JpaRepository<GitInfo, Long> {
    Optional<GitInfo> findByUser(Users user);
    Optional<GitInfo> findByGitId(String gitId);
}
