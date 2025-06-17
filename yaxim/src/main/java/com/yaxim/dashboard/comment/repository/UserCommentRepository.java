package com.yaxim.dashboard.comment.repository;

import com.yaxim.dashboard.comment.entity.UserComment;
import com.yaxim.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCommentRepository extends JpaRepository<UserComment, Long> {
    Optional<UserComment> findByUser(Users user);
    Optional<UserComment> findByUserId(Long userId);
}
