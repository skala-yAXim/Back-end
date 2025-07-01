package com.yaxim.dashboard.statics.service;

import com.yaxim.dashboard.statics.controller.dto.response.AverageStaticsResponse;
import com.yaxim.dashboard.statics.controller.dto.response.GeneralStaticsResponse;
import com.yaxim.dashboard.statics.entity.DailyUserActivity;
import com.yaxim.dashboard.statics.entity.Weekday;
import com.yaxim.dashboard.statics.entity.select.AverageActivity;
import com.yaxim.dashboard.statics.repository.UserStaticsRepository;
import com.yaxim.user.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStaticsServiceTest {

    @InjectMocks
    private UserStaticsService userStaticsService;

    @Mock
    private UserStaticsRepository userStaticsRepository;

    private static final Long USER_ID = 1L;

    private DailyUserActivity activity;

    @BeforeEach
    void setUp() {
        Users user = new Users(
                USER_ID,
                "name",
                "email@test.com"
        );

        activity = new DailyUserActivity(
                user,
                Weekday.MONDAY
        );
    }

    @Test
    @DisplayName("개인 업무 통계 조회")
    void shouldReturnUserStaticsWhenGetUserStaticIsCalled() {
        // given
        when(userStaticsRepository.existsAllByUserId(USER_ID))
                .thenReturn(true);
        when(userStaticsRepository.findAllByUserId(USER_ID))
                .thenReturn(Collections.singletonList(activity));

        // when
        List<GeneralStaticsResponse> responses = userStaticsService.getUserStatic(USER_ID);

        // then
        assertEquals(GeneralStaticsResponse.from(activity), responses.get(0));
        verify(userStaticsRepository).findAllByUserId(USER_ID);
    }

    @Test
    @DisplayName("존재하지 않는 업무 통계 예외처리")
    void shouldHandleExceptionWhenUserStaticsNotFound() {
        // given
        when(userStaticsRepository.existsAllByUserId(USER_ID))
                .thenReturn(false);

        // when
        List<GeneralStaticsResponse> responses = userStaticsService.getUserStatic(USER_ID);

        // then
        verify(userStaticsRepository).existsAllByUserId(USER_ID);
        assertEquals(0, responses.size());
    }

    @Test
    @DisplayName("유저 전체 데이터 없을 때 예외처리")
    void testGetUsersAverageStatic_EmptyResponse_WhenDataDoesNotExist() {
        // given
        when(userStaticsRepository.exists())
                .thenReturn(false);

        // when
        List<AverageStaticsResponse> result = userStaticsService.getUsersAverageStatic();

        // then
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    @DisplayName("일부 요일 데이터 없을 때 예외처리")
    void testGetUsersAverageStatic_ExceptionHandlingForWeekdays() {
        // given
        when(userStaticsRepository.exists())
                .thenReturn(true);
        // Weekday가 7개니까 7번 호출된다고 가정
        when(userStaticsRepository.getUserAvgActivityByWeekDay(any(Weekday.class)))
                .thenReturn(Optional.of(new AverageActivity()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new AverageActivity()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new AverageActivity()))
                .thenReturn(Optional.empty());

        // when
        List<AverageStaticsResponse> result = userStaticsService.getUsersAverageStatic();

        // then
        assertFalse(result.isEmpty());
        verify(userStaticsRepository, times(Weekday.values().length))
                .getUserAvgActivityByWeekDay(any(Weekday.class));
    }
}