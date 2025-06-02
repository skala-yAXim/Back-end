package com.yaxim.team.entity;

import com.yaxim.global.graph.GraphTeamMemberResponse;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.entity.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Team {
    @Id
    private String id;
    private String name;
    private String description;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> members;

    public Team(String id, String displayName, String description) {
        this.id = id;
        this.name = displayName;
        this.description = description;
    }

    public void addMember(TeamMember member) {
        members.add(member);
        member.setTeam(this);
    }

    public void removeMember(TeamMember member) {
        members.remove(member);
        member.setTeam(null);
    }

    public void syncMembers(List<GraphTeamMemberResponse.Members> newMembers, TeamMemberRepository teamMemberRepository) {
        List<TeamMember> currentMembers = teamMemberRepository.findByTeam(this);

        Map<String, TeamMember> currentMap = currentMembers.stream()
                .collect(Collectors.toMap(TeamMember::getEmail, tm -> tm));

        Set<String> newEmails = newMembers.stream()
                .map(GraphTeamMemberResponse.Members::getEmail)
                .collect(Collectors.toSet());

        // 1. 삭제 대상
        for (TeamMember member : currentMembers) {
            if (!newEmails.contains(member.getEmail())) {
                teamMemberRepository.delete(member);
            }
        }

        // 2. 추가 및 수정 대상
        for (GraphTeamMemberResponse.Members m : newMembers) {
            UserRole newRole = m.roles.isEmpty() ? UserRole.MEMBER : UserRole.LEADER;
            TeamMember existing = currentMap.get(m.getEmail());

            if (existing == null) {
                // 추가
                teamMemberRepository.save(new TeamMember(this, m.getEmail(), newRole));
            } else if (existing.getRole() != newRole) {
                // 역할 변경
                existing.updateRole(newRole);
                teamMemberRepository.save(existing);
            }
        }
    }
}
