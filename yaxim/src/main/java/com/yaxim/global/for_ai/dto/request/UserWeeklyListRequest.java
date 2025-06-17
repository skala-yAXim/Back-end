package com.yaxim.global.for_ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class UserWeeklyListRequest {
    private String teamId;
    private LocalDate startDate;
    private LocalDate endDate;
}
