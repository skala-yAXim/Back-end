package com.yaxim.dashboard.controller.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamsUserActivityCountsResponse {
    private final int activeUsers;      // 활성 사용자
    private final int activeGuests;     // 활성 게스트
    private final int meetings;         // 모임
    private final int posts;           // 개시물
    private final int replies;         // 회신
    private final int visits;          // 방문
    private final int mentions;        // 멘션
    private final String period;
}
