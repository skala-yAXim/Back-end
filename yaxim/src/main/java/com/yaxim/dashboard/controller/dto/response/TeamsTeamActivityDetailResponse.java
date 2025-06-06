package com.yaxim.dashboard.controller.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class TeamsTeamActivityDetailResponse {
    // 날짜/시간 필드
    private LocalDateTime reportRefreshDate;
    private LocalDateTime lastActivityDate;

    // 식별/메타 정보 필드
    private String teamName;
    private String teamId;
    private String teamType;
    private String reportPeriod;

    // 활동 지표 필드
    private Integer activeUsers;
    private Integer activeChannels;
    private Integer guests;
    private Integer reactions;
    private Integer meetingsOrganized;
    private Integer postMessages;
    private Integer replyMessages;
    private Integer channelMessages;
    private Integer urgentMessages;
    private Integer mentions;
    private Integer activeSharedChannels;
    private Integer activeExternalUsers;
}
