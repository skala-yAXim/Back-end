package com.yaxim.global.graph;

import com.yaxim.global.auth.jwt.TokenService;
import com.yaxim.global.auth.oauth2.exception.OidcTokenExpiredException;
import io.netty.handler.codec.http2.Http2Headers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    // api 호출하는 거 여기다
    // Teams Analytics 관련 메서드들
    public String getTeamsUserActivityUserDetail(Long userId, String period) {
        String accessToken = getAccessToken(userId);
        String url = "/reports/getTeamsUserActivityUserDetail(period='D7')?$format=application/json";
        log.info(url);

        // Beta 도메인에서는 JSON으로 직접 응답
        String jsonResponse = webClientTeams.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchangeToMono(response -> {
                    if (response.statusCode().value() == 302) {
                        return Mono.justOrEmpty(response.headers().header("Location").stream().findFirst());
                    } else {
                        return response.bodyToMono(String.class)
                                .doOnNext(body -> log.warn(":경고: Unexpected response: {}", body))
                                .then(Mono.empty());
                    }
                })
                .block();

        log.info("✅ JSON 응답:\n{}", jsonResponse);
        return jsonResponse;

    }
    public String getTeamsTeamActivityDetail(Long userId, String period) {
        String accessToken = getAccessToken(userId);
        String url = "/reports/getTeamsTeamActivityDetail(period='" + period + "')";
        log.info(url);

        // Beta 도메인에서는 JSON으로 직접 응답
        String jsonResponse = webClientTeams.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("✅ JSON 응답:\n{}", jsonResponse);
        return jsonResponse;
    }


    public String getTeamsUserActivityCounts(Long userId, String period) {
        String accessToken = getAccessToken(userId);
        String url = "/reports/microsoft.graph.getTeamsUserActivityCounts(period='D7')";
        log.info(url);

        // Beta 도메인에서는 JSON으로 직접 응답
        String jsonResponse = webClientTeams.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchangeToMono(response -> {
                    if (response.statusCode().value() == 302) {
                        return Mono.justOrEmpty(response.headers().header("Location").stream().findFirst());
                    } else {
                        return response.bodyToMono(String.class)
                                .doOnNext(body -> log.warn(":경고: Unexpected response: {}", body))
                                .then(Mono.empty());
                    }
                })
                .block();

        log.info("✅ JSON 응답:\n{}", jsonResponse);
        return jsonResponse;
    }

    private String getAccessToken(Long userId) {
        String token = tokenService.getOidcToken(userId.toString());
        if (token == null) throw new OidcTokenExpiredException();
        return token;
    }
}

