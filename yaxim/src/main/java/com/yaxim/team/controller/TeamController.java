package com.yaxim.team.controller;

import com.yaxim.global.auth.jwt.JwtAuthentication;
import com.yaxim.team.controller.dto.response.TeamResponse;
import com.yaxim.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @PostMapping("/init/one/{teamId}")
    public ResponseEntity<TeamResponse> initTeam(@PathVariable String teamId,
//                                     @PathVariable String accessToken,
                                     JwtAuthentication auth) {
        return ResponseEntity.ok(teamService.loadOneTeam(auth.getUserId(), teamId));
    }

    @PostMapping("/init/all")
    public ResponseEntity<List<TeamResponse>> initTeam(JwtAuthentication auth) {
        return ResponseEntity.ok(teamService.loadAllTeams(auth.getUserId()));
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getMyTeam(JwtAuthentication auth) {
        return ResponseEntity.ok(teamService.getUserTeams(auth.getUserId()));
    }
}
