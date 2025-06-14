package com.yaxim.report.entity;

import com.yaxim.global.util.BaseEntity;
import com.yaxim.global.util.JsonConverter;
import com.yaxim.team.entity.Team;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Map;

/**
 * 팀 주간 보고서 엔티티
 * BaseEntity를 상속받아 team 정보만 가집니다.
 * 보고서 생성자(creator) 정보는 이 엔티티에 직접 저장되지 않고,
 * 서비스 로직에서 생성 시점의 인증 정보(JwtAuthentication)를 활용합니다.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TeamWeeklyReport extends BaseEntity {

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
     * 보고서의 주체가 되는 팀
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Builder
    public TeamWeeklyReport(LocalDate startDate, LocalDate endDate, Map<String, Object> report, Team team) {
        this.team = team;
        this.startDate = startDate;
        this.endDate = endDate;
        this.report = report;
    }
}
