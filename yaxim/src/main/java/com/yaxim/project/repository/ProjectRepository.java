package com.yaxim.project.repository;

import com.yaxim.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // 팀별 프로젝트 목록 조회 (페이징)
    Page<Project> findByTeamIdOrderByIdDesc(Long teamId, Pageable pageable);

    // 프로젝트명으로 검색 (페이징) - 필드명 수정: projectName → name
    Page<Project> findByTeamIdAndNameContainingIgnoreCase(Long teamId, String name, Pageable pageable);

    // 특정 기간 내 프로젝트 조회
    @Query("SELECT p FROM Project p WHERE p.teamId = :teamId " +
           "AND (p.startDate BETWEEN :startDate AND :endDate " +
           "OR p.endDate BETWEEN :startDate AND :endDate " +
           "OR (p.startDate <= :startDate AND p.endDate >= :endDate)) " +
           "ORDER BY p.id DESC")
    Page<Project> findProjectsInDateRange(
            @Param("teamId") Long teamId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // 특정 팀의 최근 프로젝트 조회 (최대 5개)
    List<Project> findTop5ByTeamIdOrderByIdDesc(Long teamId);

    // 팀 내 프로젝트 개수 조회
    long countByTeamId(Long teamId);

    // ✅ 상태별 조회 메서드는 제거 (상태가 동적으로 계산되므로 DB 쿼리로 불가능)
    // 대신 서비스 레이어에서 메모리 필터링으로 처리
    
    // 추가적인 조회 메서드들
    
    // 특정 날짜 이후 시작하는 프로젝트 조회
    List<Project> findByTeamIdAndStartDateAfterOrderByStartDateAsc(Long teamId, LocalDateTime date);
    
    // 특정 날짜 이전 종료하는 프로젝트 조회  
    List<Project> findByTeamIdAndEndDateBeforeOrderByEndDateDesc(Long teamId, LocalDateTime date);
    
    // 진행 중인 프로젝트 조회 (시작일은 지났고 종료일은 안 지난 것들)
    @Query("SELECT p FROM Project p WHERE p.teamId = :teamId " +
           "AND p.startDate <= :now AND (p.endDate IS NULL OR p.endDate >= :now) " +
           "ORDER BY p.id DESC")
    List<Project> findInProgressProjects(@Param("teamId") Long teamId, @Param("now") LocalDateTime now);
    
    // 완료된 프로젝트 조회 (종료일이 지난 것들)
    @Query("SELECT p FROM Project p WHERE p.teamId = :teamId " +
           "AND p.endDate IS NOT NULL AND p.endDate < :now " +
           "ORDER BY p.endDate DESC")
    List<Project> findCompletedProjects(@Param("teamId") Long teamId, @Param("now") LocalDateTime now);
    
    // 시작 전 프로젝트 조회 (시작일이 아직 안 온 것들)
    @Query("SELECT p FROM Project p WHERE p.teamId = :teamId " +
           "AND p.startDate IS NOT NULL AND p.startDate > :now " +
           "ORDER BY p.startDate ASC")
    List<Project> findBeforeStartProjects(@Param("teamId") Long teamId, @Param("now") LocalDateTime now);
}
