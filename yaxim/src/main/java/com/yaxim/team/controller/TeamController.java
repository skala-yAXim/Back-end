package com.yaxim.team.controller;

import com.yaxim.global.auth.aop.CheckRole;
import com.yaxim.global.auth.jwt.JwtAuthentication;
import com.yaxim.team.controller.dto.response.TeamMemberResponse;
import com.yaxim.team.controller.dto.response.TeamResponse;
import com.yaxim.team.service.TeamService;
import com.yaxim.user.entity.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "팀 조회 및 동기화 기능", description = "팀에 관한 정보를 조회하거나, Microsoft Teams에 있는 팀의 정보와 동기화합니다.")
@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @Operation(summary = "팀 정보 조회", description = "팀 아이디(id), 팀 이름(name), 팀 설명(description)을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 유효함"),
            @ApiResponse(responseCode = "401", description = "토큰 유효하지 않음 또는 없음"),
            @ApiResponse(responseCode = "404", description = "팀 정보를 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<TeamResponse> getMyTeam(JwtAuthentication auth) {
        return ResponseEntity.ok(teamService.getUserTeam(auth.getUserId()));
    }

    @CheckRole(UserRole.LEADER)
    @Operation(summary = "[팀장 기능] 구성원 정보 조회", description = "팀원 아이디(id), 팀원 이름(name), 팀원 이메일(email) 목록을 조회합니다. 아직 가입되지 않은 사용자의 이름은 '아직 가입하지 않은 사용자입니다.'로 표시됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 유효함"),
            @ApiResponse(responseCode = "401", description = "토큰 유효하지 않음 또는 없음, 권한이 없음"),
            @ApiResponse(responseCode = "404", description = "팀 정보를 찾을 수 없음")
    })
    @GetMapping("/members")
    public ResponseEntity<List<TeamMemberResponse>> getMyTeamMembers(JwtAuthentication auth) {
        return ResponseEntity.ok(teamService.getUserTeamMembers(auth.getUserId()));
    }

    @Operation(summary = "팀 정보 동기화", description = "Microsoft Teams에 등록되어 있는 팀 정보와 동기화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 유효함"),
            @ApiResponse(responseCode = "401", description = "토큰 유효하지 않음 또는 없음"),
            @ApiResponse(responseCode = "404", description = "팀 정보를 찾을 수 없음")
    })
    @PostMapping("/load")
    public ResponseEntity<TeamResponse> initTeam(JwtAuthentication auth) {
        return ResponseEntity.ok(teamService.loadTeam(auth.getUserId()));
    }
}
