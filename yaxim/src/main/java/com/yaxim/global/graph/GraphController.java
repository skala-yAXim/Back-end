package com.yaxim.global.graph;

import com.yaxim.global.auth.jwt.JwtAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/graph/teams")
@Slf4j
@RequiredArgsConstructor
public class GraphController {
    private final GraphApiService graphService;

    @GetMapping("/my")
    public ResponseEntity<List<GraphTeamResponse.Team>> getTeam(JwtAuthentication auth) {
        return ResponseEntity.ok(graphService.getMyTeams(auth.getUserId()));
    }

    @GetMapping("/only-first-team")
    public ResponseEntity<GraphTeamResponse.Team> getOnlyFirstTeam(JwtAuthentication auth) {
        return ResponseEntity.ok(graphService.getMyFirstTeam(auth.getUserId()));
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<GraphTeamMemberResponse.Members>> getTeamMembers(@PathVariable String teamId,
                                                                                JwtAuthentication auth) {
        return ResponseEntity.ok(graphService.getMyTeamMembers(teamId, auth.getUserId()));
    }
}
