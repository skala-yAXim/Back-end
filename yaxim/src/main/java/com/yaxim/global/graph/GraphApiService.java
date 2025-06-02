package com.yaxim.global.graph;

import com.yaxim.global.auth.jwt.TokenService;
import com.yaxim.global.auth.oauth2.exception.OidcTokenExpiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private String getAccessToken(Long userId) {
        String token = tokenService.getOidcToken(userId.toString());
        if (token == null) throw new OidcTokenExpiredException();
        return token;
    }
}

