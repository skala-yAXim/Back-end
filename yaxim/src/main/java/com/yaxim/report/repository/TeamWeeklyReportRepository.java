package com.yaxim.report.repository;

import com.yaxim.report.entity.TeamWeeklyReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface TeamWeeklyReportRepository extends JpaRepository<TeamWeeklyReport, Long> {

    @Query(value = "SELECT r FROM TeamWeeklyReport r JOIN FETCH r.team WHERE r.team.id = :teamId AND r.startDate BETWEEN :start AND :end",
            countQuery = "SELECT count(r) FROM TeamWeeklyReport r WHERE r.team.id = :teamId AND r.startDate BETWEEN :start AND :end")
    Page<TeamWeeklyReport> findByTeamIdAndDateRange(@Param("teamId") String teamId, @Param("start") LocalDate start, @Param("end") LocalDate end, Pageable pageable);
}
