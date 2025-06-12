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

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserDailyReport extends BaseReport {

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
    public UserDailyReport(LocalDate startDate, LocalDate endDate, String report, Users user, Team team) {
        // super()를 통해 부모 클래스의 필드를 초기화합니다.
        super(startDate, endDate, report);
        this.user = user;
        this.team = team;
    }

}