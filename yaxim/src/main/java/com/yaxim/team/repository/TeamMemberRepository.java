package com.yaxim.team.repository;

import com.yaxim.team.entity.Team;
import com.yaxim.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Optional<TeamMember> findByEmail(String email);
    List<TeamMember> findByTeam(Team team);
    List<TeamMember> findByTeamId(String teamId);

    @Query("SELECT u.email FROM TeamMember u WHERE u.team = :team")
    List<String> getEmailsByTeamIn(Team team);
}
