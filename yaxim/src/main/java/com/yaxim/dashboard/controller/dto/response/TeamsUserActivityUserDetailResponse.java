package com.yaxim.dashboard.controller.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class TeamsUserActivityUserDetailResponse {
    // 날짜/시간 필드
    private LocalDateTime reportRefreshDate;
    private LocalDateTime lastActivityDate;
    private LocalDateTime deletedDate;

    // 식별/메타 정보 필드
    private String tenantDisplayName;
    private String sharedChannelTenantDisplayNames;
    private String userId;
    private String userPrincipalName;
    private String assignedProducts;
    private String reportPeriod;

    // Boolean 상태 필드
    private Boolean isDeleted;
    private Boolean hasOtherAction;
    private Boolean isLicensed;

    // 메시지/활동 카운트 필드
    private Integer teamChatMessageCount;
    private Integer privateChatMessageCount;
    private Integer callCount;
    private Integer meetingCount;
    private Integer postMessages;
    private Integer replyMessages;
    private Integer urgentMessages;

    // 회의 관련 카운트 필드
    private Integer meetingsOrganizedCount;
    private Integer meetingsAttendedCount;
    private Integer adHocMeetingsOrganizedCount;
    private Integer adHocMeetingsAttendedCount;
    private Integer scheduledOnetimeMeetingsOrganizedCount;
    private Integer scheduledOnetimeMeetingsAttendedCount;
    private Integer scheduledRecurringMeetingsOrganizedCount;
    private Integer scheduledRecurringMeetingsAttendedCount;

    // Duration 문자열 필드
    private String audioDuration;
    private String videoDuration;
    private String screenShareDuration;

    // Duration 초 단위 필드
    private Integer audioDurationInSeconds;
    private Integer videoDurationInSeconds;
    private Integer screenShareDurationInSeconds;
}