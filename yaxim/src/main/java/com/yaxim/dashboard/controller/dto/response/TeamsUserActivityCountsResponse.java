package com.yaxim.dashboard.controller.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class TeamsUserActivityCountsResponse {

    private List<UserActivityCount> value;

    @Getter
    @Builder
    public static class UserActivityCount {
        // 날짜 필드 (String으로 API 응답 그대로)
        private String reportRefreshDate;    // "2017-09-01"
        private String reportDate;           // "2017-09-01"

        // 메시지 관련 카운트
        private Integer teamChatMessages;    // 26
        private Integer postMessages;        // 3
        private Integer replyMessages;       // 1
        private Integer privateChatMessages; // 17

        // 커뮤니케이션 카운트
        private Integer calls;               // 4
        private Integer meetings;            // 0

        // Duration 필드 (00:00:00 형태의 String)
        private String audioDuration;        // "00:00:00"
        private String videoDuration;        // "00:00:00"
        private String screenShareDuration;  // "00:00:00"

        // 회의 관련 카운트
        private Integer meetingsOrganized;   // 0
        private Integer meetingsAttended;    // 0

        // 리포트 기간
        private String reportPeriod;         // "7"
    }
}