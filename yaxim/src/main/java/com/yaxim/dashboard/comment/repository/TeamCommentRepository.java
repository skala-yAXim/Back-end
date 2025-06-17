package com.yaxim.dashboard.comment.repository;

import com.yaxim.dashboard.comment.entity.TeamComment;
import com.yaxim.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamCommentRepository extends JpaRepository<TeamComment, Long> {
    Optional<TeamComment> findByTeam(Team team);
}
