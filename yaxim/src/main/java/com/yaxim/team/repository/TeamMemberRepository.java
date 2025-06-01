package com.yaxim.team.repository;

import com.yaxim.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
//    List<TeamMember> findAllByUserId(Long userId);
    List<TeamMember> findAllByEmail(String email);
}
