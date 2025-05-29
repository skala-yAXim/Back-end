package com.yaxim.global.auth.graph;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GraphApiService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://graph.microsoft.com/v1.0")
            .defaultHeader("Content-Type", "application/json")
            .build();

    public List<String> getUserTeams(String accessToken) {
        String url = "/me/memberOf";

        GraphGroupResponse response = webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GraphGroupResponse.class)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                })
                .block();

        if (response == null || response.value == null) return List.of();

        // Teams만 필터링 (Microsoft 365 그룹은 groupTypes에 "Unified" 포함됨)
        return response.value.stream()
                .filter(g -> g.groupTypes != null && g.groupTypes.contains("Unified"))
                .map(g -> g.displayName)
                .toList();
    }

    // 내부 DTO 정의
    static class GraphGroupResponse {
        public List<Group> value;
    }

    static class Group {
        public String id;
        public String displayName;
        public List<String> groupTypes;
    }
}

