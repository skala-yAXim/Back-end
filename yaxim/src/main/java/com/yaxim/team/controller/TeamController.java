package com.yaxim.team.controller;

import com.yaxim.global.auth.aop.CheckRole;
import com.yaxim.global.auth.jwt.JwtAuthentication;
import com.yaxim.team.controller.dto.response.TeamMemberResponse;
import com.yaxim.team.controller.dto.response.TeamResponse;
import com.yaxim.team.service.TeamService;
import com.yaxim.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<TeamResponse> getMyTeam(JwtAuthentication auth) {
        return ResponseEntity.ok(teamService.getUserTeam(auth.getUserId()));
    }

    @CheckRole(UserRole.LEADER)
    @GetMapping("/members")
    public ResponseEntity<List<TeamMemberResponse>> getMyTeamMembers(JwtAuthentication auth) {
        return ResponseEntity.ok(teamService.getUserTeamMembers(auth.getUserId()));
    }

    @PostMapping("/load")
    public ResponseEntity<TeamResponse> initTeam(JwtAuthentication auth) {
        return ResponseEntity.ok(teamService.loadTeam(auth.getUserId()));
    }
}
