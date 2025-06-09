package com.yaxim.dashboard.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class TeamsTeamActivityDetailResponse {

    @JsonProperty("@odata.context")
    private String odataContext;

    private List<TeamActivity> value;

    @Getter
    @Builder
    public static class TeamActivity {
        // 날짜/시간 필드 (String으로 API 응답 그대로)
        private String reportRefreshDate;  // "2021-09-01"
        private String lastActivityDate;   // "2021-09-01"

        // 팀 식별 정보
        private String teamName;          // "sampleTeam"
        private String teamId;            // "a063d832-ae9a-467d-8cb4-17c073260890"
        private String teamType;          // "Private"

        // 활동 세부 정보 배열
        private List<ActivityDetail> details;
    }

    @Getter
    @Builder
    public static class ActivityDetail {
        // 리포트 기간
        private Integer reportPeriod;     // 7

        // 활동 지표들
        private Integer activeUsers;      // 26
        private Integer activeChannels;   // 17
        private Integer guests;           // 4
        private Integer reactions;        // 36
        private Integer meetingsOrganized; // 0
        private Integer postMessages;     // 0
        private Integer replyMessages;    // 0
        private Integer channelMessages;  // 0
        private Integer urgentMessages;   // 0
        private Integer mentions;         // 0

        // API에서 String으로 오는 필드들
        private String activeSharedChannels;   // "6"
        private String activeExternalUsers;    // "8"
    }
}