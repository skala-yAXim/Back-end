package com.yaxim.report.entity;

import com.yaxim.team.entity.Team;
import com.yaxim.user.entity.Users;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * 개인 주간 보고서 엔티티
 * BaseReportEntity를 상속받아 user와 team 정보를 추가로 가집니다.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PersonalWeeklyReport extends BaseReport {

    /**
     * 보고서의 주체가 되는 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    /**
     * 보고서가 속한 팀
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Builder
    public PersonalWeeklyReport(LocalDate startDate, LocalDate endDate, String report, Users user, Team team) {
        super(startDate, endDate, report);
        this.user = user;
        this.team = team;
    }

}