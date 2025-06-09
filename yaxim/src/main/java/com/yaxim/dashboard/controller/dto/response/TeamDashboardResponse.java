package com.yaxim.dashboard.controller.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class TeamDashboardResponse {

    // 👥 팀 기본 정보 및 활동 수치
    private final TeamMetrics teamMetrics;

    // 📈 팀 일주일 활동 데이터 (차트용)
    private final List<TeamWeeklyData> weeklyChart;

    @Getter
    @Builder
    public static class TeamMetrics {
        private final String teamName;              // 팀명
        private final String teamType;              // "Public" | "Private"
        private final Integer activeUsers;          // 활성 사용자 수
        private final Integer activeChannels;       // 활성 채널 수
        private final Integer channelMessages;      // 채널 메시지 수
        private final Integer postMessages;         // 게시물 수
        private final Integer reactions;            // 반응 수
        private final Integer mentions;             // 멘션 수
        private final String lastActivityDate;      // 마지막 활동일
    }

    @Getter
    @Builder
    public static class TeamWeeklyData {
        private final String date;                  // "06-01"
        private final Integer activeUsers;          // ✅ 매칭
        private final Integer channelMessages;      // ✅ API 필드명 사용
        private final Integer postMessages;         // ✅ API 필드명 사용
        private final Integer meetingsOrganized;    // ✅ API 필드명 사용
        private final Integer reactions;            // ✅ API 필드명 사용
        private final Integer mentions;             // ✅ API 필드명 사용
        private final Integer urgentMessages;       // ✅ API 필드명 사용
        private final Integer dailyTotal;           // 계산 필드          // 일일 총합
    }
}