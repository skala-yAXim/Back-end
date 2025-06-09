package com.yaxim.dashboard.controller.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class TeamsUserActivityUserDetailResponse {

    private List<UserActivity> value;

    @Getter
    @Builder
    public static class UserActivity {
        // 날짜/시간 필드 (String으로 API 응답 그대로)
        private String reportRefreshDate;        // "2017-09-01"
        private String lastActivityDate;         // "2017-09-01"
        private String deletedDate;              // null 가능

        // 식별/메타 정보 필드
        private String tenantDisplayName;        // "Microsoft"
        private String sharedChannelTenantDisplayNames; // "SampleTenant"
        private String userId;                   // "userId-value"
        private String userPrincipalName;        // "userPrincipalName-value"
        private List<String> assignedProducts;   // ["Microsoft 365 ENTERPRISE E5"]
        private String reportPeriod;             // "7"

        // Boolean 상태 필드
        private Boolean isDeleted;               // false
        private Boolean hasOtherAction;          // true
        private Boolean isLicensed;              // true

        // 메시지/활동 카운트 필드
        private Integer teamChatMessageCount;    // 0
        private Integer privateChatMessageCount; // 49
        private Integer callCount;               // 2
        private Integer meetingCount;            // 0
        private Integer postMessages;            // 10
        private Integer replyMessages;           // 1
        private Integer urgentMessages;          // 1

        // 회의 관련 카운트 필드 (API 응답과 정확히 일치)
        private Integer meetingsOrganizedCount;  // 0
        private Integer meetingsAttendedCount;   // 0
        private Integer adHocMeetingsOrganizedCount; // 0
        private Integer adHocMeetingsAttendedCount;  // 0
        private Integer scheduledOneTimeMeetingsOrganizedCount; // 0 (대소문자 수정!)
        private Integer scheduledOneTimeMeetingsAttendedCount;  // 0 (대소문자 수정!)
        private Integer scheduledRecurringMeetingsOrganizedCount; // 0
        private Integer scheduledRecurringMeetingsAttendedCount;  // 0

        // Duration 필드 (00:00:00 형태의 String)
        private String audioDuration;            // "00:00:00"
        private String videoDuration;            // "00:00:00"
        private String screenShareDuration;      // "00:00:00"
    }
}