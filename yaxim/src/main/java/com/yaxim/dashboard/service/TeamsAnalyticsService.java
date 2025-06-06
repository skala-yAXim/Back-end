package com.yaxim.dashboard.service;

import com.yaxim.dashboard.controller.dto.response.TeamsUserActivityCountsResponse;
import com.yaxim.dashboard.controller.dto.response.TeamsTeamActivityDetailResponse;
import com.yaxim.dashboard.controller.dto.response.TeamsUserActivityUserDetailResponse;
import com.yaxim.dashboard.exception.GraphApiCallFailedException;
import com.yaxim.dashboard.exception.TeamsAnalyticsNotFoundException;
import com.yaxim.global.graph.GraphApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamsAnalyticsService {

    private final GraphApiService graphApiService;  // ✅ GraphApiService 주입

    public List<TeamsUserActivityCountsResponse> getTeamsUserActivityCounts(Long userId) {
        try {
            log.info("Teams 요약 데이터 조회: userId: {}", userId);

            String period = "D7";
            String reportData = graphApiService.getTeamsUserActivityCounts(userId, period);
            log.info(reportData);

            if (reportData == null || reportData.isEmpty()) {
                throw new TeamsAnalyticsNotFoundException();
            }
            return null;

        } catch (Exception e) {
            log.error("Teams 요약 데이터 조회 실패", e);
            throw new GraphApiCallFailedException();
        }
    }

    public List<TeamsUserActivityUserDetailResponse> getTeamsUserActivityUserDetail(Long userId) {
        try {
            log.info("Teams 활성 사용자 데이터 조회: userId: {}", userId);

            String period = "D7";
            String reportData = graphApiService.getTeamsUserActivityUserDetail(userId, period);
            log.info(reportData);

            if (reportData == null || reportData.isEmpty()) {
                throw new TeamsAnalyticsNotFoundException();
            }
            return null;

        } catch (Exception e) {
            log.error("Teams 활성 사용자 데이터 조회 실패", e);
            throw new GraphApiCallFailedException();
        }
    }
    public List<TeamsTeamActivityDetailResponse> getTeamsTeamActivityDetail(Long userId) {
        try {
            log.info("Teams Team Activity Detail 조회: userID: {}", userId);

            String period = "D7";
            String reportData = graphApiService.getTeamsTeamActivityDetail(userId, period);
            log.info(reportData);

            if (reportData == null || reportData.isEmpty()) {
                throw new TeamsAnalyticsNotFoundException();
            }
            return null;

        } catch (Exception e) {
            log.error("Teams 데이터 조회 실패", e);
            throw new GraphApiCallFailedException();
        }
    }

}