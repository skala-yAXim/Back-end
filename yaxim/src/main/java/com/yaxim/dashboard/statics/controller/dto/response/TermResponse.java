package com.yaxim.dashboard.statics.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TermResponse {
    private LocalDate startDate;
    private LocalDate endDate;
}
