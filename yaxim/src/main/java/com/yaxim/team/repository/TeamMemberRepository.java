package com.yaxim.team.repository;

import com.yaxim.team.entity.Team;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByTeamId(String teamId);
    Optional<TeamMember> findByUserId(Long userId);

    @Query("SELECT u.user FROM TeamMember u WHERE u.team = :team")
    List<Users> getUsersByTeamIn(Team team);
}
