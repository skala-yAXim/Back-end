package com.yaxim.report.repository;

import com.yaxim.report.entity.TeamWeeklyReport;
import com.yaxim.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamWeeklyReportRepository extends JpaRepository<TeamWeeklyReport, Long> {

    @Query(value = "SELECT r FROM TeamWeeklyReport r JOIN FETCH r.team WHERE r.team = :team",
            countQuery = "SELECT count(r) FROM TeamWeeklyReport r WHERE r.team = :team")
    Page<TeamWeeklyReport> findByTeam(@Param("team") Team team, Pageable pageable);
}
