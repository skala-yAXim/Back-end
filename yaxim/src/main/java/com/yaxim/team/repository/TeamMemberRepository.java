package com.yaxim.team.repository;

import com.yaxim.team.entity.Team;
import com.yaxim.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Optional<TeamMember> findByEmail(String email);
    List<TeamMember> findByTeam(Team team);
    List<TeamMember> findByTeamId(String teamId);
//    Optional<TeamMember> findByTeamIdAndEmail(String teamId, String email);
}
