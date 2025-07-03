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

/**
 * 개인 주간 보고서 엔티티
 * BaseEntity를 상속받아 user와 team 정보를 추가로 가집니다.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserWeeklyReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

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
    public UserWeeklyReport(LocalDate startDate, LocalDate endDate, Map<String, Object> report, Users user, Team team) {
        this.user = user;
        this.team = team;
        this.startDate = startDate;
        this.endDate = endDate;
        this.report = report;
    }

}