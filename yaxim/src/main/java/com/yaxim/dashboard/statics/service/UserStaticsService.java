package com.yaxim.dashboard.statics.service;

import com.yaxim.dashboard.statics.controller.dto.response.SumStaticResponse;
import com.yaxim.dashboard.statics.entity.select.AverageActivity;
import com.yaxim.dashboard.statics.controller.dto.response.AverageStaticsResponse;
import com.yaxim.dashboard.statics.controller.dto.response.GeneralStaticsResponse;
import com.yaxim.dashboard.statics.entity.DailyUserActivity;
import com.yaxim.dashboard.statics.entity.Weekday;
import com.yaxim.dashboard.statics.repository.UserStaticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserStaticsService {
    private final UserStaticsRepository userStaticsRepository;

    public List<GeneralStaticsResponse> getUserStatic(Long userId) {
        List<DailyUserActivity> activities = userStaticsRepository.findAllByUserId(userId);

        return activities.stream()
                .map(GeneralStaticsResponse::from)
                .toList();
    }

    public List<AverageStaticsResponse> getUsersAverageStatic() {
        List<AverageActivity> activities = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            activities.add(userStaticsRepository.getUserAvgActivityByWeekDay(Weekday.of(i)));
        }

        return activities.stream()
                .map(AverageStaticsResponse::from)
                .toList();
    }

    public SumStaticResponse getUserWeekStatics(Long userId) {
        return SumStaticResponse.from(
                userStaticsRepository.getUserWeekActivity(userId)
        );
    }
}
