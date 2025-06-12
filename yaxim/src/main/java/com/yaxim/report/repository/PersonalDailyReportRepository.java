package com.yaxim.report.repository;

import com.yaxim.report.entity.PersonalDailyReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalDailyReportRepository extends JpaRepository<PersonalDailyReport, Long> {

    @Query(value = "SELECT r FROM PersonalDailyReport r JOIN FETCH r.user JOIN FETCH r.team WHERE r.user.id = :userId",
            countQuery = "SELECT count(r) FROM PersonalDailyReport r WHERE r.user.id = :userId")
    Page<PersonalDailyReport> findByUserId(@Param("userId") Long userId, Pageable pageable);
}

