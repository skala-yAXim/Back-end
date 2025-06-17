package com.yaxim.global.for_ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class UserDailyListRequest {
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
}
