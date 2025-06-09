package com.yaxim.graph;

import com.yaxim.global.auth.jwt.TokenService;
import com.yaxim.global.auth.oauth2.exception.OidcTokenExpiredException;
import com.yaxim.graph.controller.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphApiService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://graph.microsoft.com/v1.0")
            .defaultHeader("Content-Type", "application/json")
            .build();

    private final TokenService tokenService;

    private final WebClient webClientTeams = WebClient.builder()
            .baseUrl("https://graph.microsoft.com/beta")
            .defaultHeader("Content-Type", "application/json")
            .build();


    public List<GraphTeamResponse.Team> getMyTeams(Long userId) {
        String accessToken = getAccessToken(userId);
        String url = "/me/joinedTeams";

        GraphTeamResponse response = webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GraphTeamResponse.class)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                })
                .block();

        return response != null ? response.value : List.of();
    }

    public GraphTeamResponse.Team getMyFirstTeam(Long userId) {
        String accessToken = getAccessToken(userId);
        String url = "/me/joinedTeams";

        GraphTeamResponse response = webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GraphTeamResponse.class)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                })
                .block();

        return response != null ? response.value.get(0) : null;
    }

    public List<GraphTeamMemberResponse.Members> getMyTeamMembers(String teamId, Long userId) {
        String accessToken = getAccessToken(userId);
        String url = "/teams/" + teamId + "/members";

        GraphTeamMemberResponse response = webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GraphTeamMemberResponse.class)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                })
                .block();

        return response != null && response.value != null ? response.value : List.of();
    }

    // Teams Analytics 관련 메서드들
    public TeamsUserActivityUserDetailResponse getTeamsUserActivityUserDetail(Long userId, String period) {
        String accessToken = getAccessToken(userId);
        String url = "/reports/getTeamsUserActivityUserDetail(period='" + period + "')?$format=application/json";

        // Beta 도메인에서는 JSON으로 직접 응답
        TeamsUserActivityUserDetailResponse response = webClientTeams.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(TeamsUserActivityUserDetailResponse.class)
                .block();

        return response;

    }
    public TeamsUserActivityCountsResponse getTeamsUserActivityCounts(Long userId, String period) {
        String accessToken = getAccessToken(userId);
        String url = "/reports/getTeamsUserActivityCounts(period='" + period + "')?$format=application/json";

        // Beta 도메인에서는 JSON으로 직접 응답
        TeamsUserActivityCountsResponse response = webClientTeams.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(TeamsUserActivityCountsResponse.class)
                .block();

        return response;
    }
    public TeamsTeamActivityDetailResponse getTeamsTeamActivityDetail(Long userId, String period) {
        String accessToken = getAccessToken(userId);
        String url = "/reports/getTeamsTeamActivityDetail(period='" + period + "')?$format=application/json";

        //Beta 도메인에서는 JSON으로 직접 응답
        TeamsTeamActivityDetailResponse response = webClientTeams.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(TeamsTeamActivityDetailResponse.class)
                .block();
        return response;
    }

    public TeamsTeamActivityCountsResponse getTeamsTeamActivityCounts(Long userId, String period) {
        String accessToken = getAccessToken(userId);
        String url = "/reports/getTeamsTeamActivityCounts(period='" + period + "')?$format=application/json";

        // Beta 도메인에서는 JSON으로 직접 응답
        TeamsTeamActivityCountsResponse response = webClientTeams.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(TeamsTeamActivityCountsResponse.class)
                .block();
        return response;
    }

    private String getAccessToken(Long userId) {
        String token = tokenService.getOidcToken(userId.toString());
        if (token == null) throw new OidcTokenExpiredException();
        return token;
    }
}

