package com.yaxim.report.entity;

import com.yaxim.global.util.BaseEntity;
import com.yaxim.team.entity.Team;
import com.yaxim.user.entity.Users;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
@Entity
public class Reports extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;

    private String report;

    //@JdbcTypeCode(SqlTypes.JSON)
    //@Column(columnDefinition = "jsonb")
    //private Daily report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
}