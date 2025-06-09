package com.yaxim.dashboard.controller.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PersonalDashboardResponse {

    // 🙋‍♂️ 개인 활동 기본 수치
    private final PersonalMetrics personalMetrics;

    // 📈 일주일 활동 데이터 (차트용)
    private final List<PersonalWeeklyData> weeklyChart;

    @Getter
    @Builder
    public static class PersonalMetrics {
        private final Integer teamChatCount;        // 팀 채팅
        private final Integer privateChatCount;     // 개인 채팅
        private final Integer postCount;            // 게시물
        private final Integer meetingCount;         // 미팅
        private final Integer callCount;            // 통화
        private final Integer urgentMessages;       // 긴급 메시지
        private final Integer totalActivity;        // 총 활동
    }

    @Getter
    @Builder
    public static class PersonalWeeklyData {
        private final String date;                  // "06-01"
        private final Integer teamChats;            // 팀채팅
        private final Integer privateChats;         // 개인채팅
        private final Integer posts;                // 게시물
        private final Integer meetings;             // 미팅
        private final Integer calls;                // 통화
        private final Integer dailyTotal;           // 일일 총합
    }
}