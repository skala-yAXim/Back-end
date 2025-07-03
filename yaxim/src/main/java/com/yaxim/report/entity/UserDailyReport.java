package com.yaxim.report.entity;

import com.yaxim.global.util.BaseEntity;
import com.yaxim.global.util.JsonConverter;
import com.yaxim.team.entity.Team;
import com.yaxim.user.entity.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserDailyReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Lob
    @Convert(converter = JsonConverter.class)
    @Column(columnDefinition = "TEXT", nullable = false)
    private Map<String, Object> report;

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
    public UserDailyReport(LocalDate date, Map<String, Object> report, Users user, Team team) {
        this.user = user;
        this.team = team;
        this.date = date;
        this.report = report;
    }

}