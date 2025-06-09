package com.yaxim.dashboard.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class TeamsTeamActivityCountsResponse {

    @JsonProperty("@odata.context")
    private String odataContext;

    private List<TeamReport> value;

    @Getter
    @Builder
    public static class TeamReport {
        // 리포트 기본 정보
        private String reportRefreshDate;    // "2021-09-01"
        private Integer reportPeriod;        // 7

        // 일별 사용자 활동 카운트 배열
        private List<UserCount> userCounts;
    }

    @Getter
    @Builder
    public static class UserCount {
        // 날짜
        private String reportDate;           // "2021-09-01"

        // 핵심 메트릭들
        private Integer activeUsers;         // 26
        private Integer activeChannels;      // 17
        private Integer guests;              // 4
        private Integer reactions;           // 36
        private Integer meetingsOrganized;   // 0
        private Integer postMessages;        // 83
        private Integer channelMessages;     // 101
        private Integer activeSharedChannels; // 1 (여기서는 Integer!)
        private Integer activeExternalUsers;  // 2 (여기서는 Integer!)
        private Integer replyMessages;       // 10
        private Integer urgentMessages;      // 8
        private Integer mentions;            // 1
    }
}