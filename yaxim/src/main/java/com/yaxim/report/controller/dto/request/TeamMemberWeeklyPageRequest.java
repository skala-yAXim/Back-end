package com.yaxim.report.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class TeamMemberWeeklyPageRequest {
    private List<Long> userId;
    private LocalDate startDate;
    private LocalDate endDate;
}
