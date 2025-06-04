package com.yaxim.project.repository;

import com.yaxim.project.entity.Project;
import com.yaxim.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // 팀별 프로젝트 목록 조회 (페이징)
    Page<Project> findByTeam(Team team, Pageable pageable);

//    // 프로젝트명으로 검색 (페이징) - 필드명 수정: projectName → name
//    Page<Project> findByTeamIdAndNameContainingIgnoreCase(String teamId, String name, Pageable pageable);
//
//    // 특정 기간 내 프로젝트 조회
//    @Query("SELECT p FROM Project p WHERE p.team.id = :teamId " +
//           "AND (p.startDate BETWEEN :startDate AND :endDate " +
//           "OR p.endDate BETWEEN :startDate AND :endDate " +
//           "OR (p.startDate <= :startDate AND p.endDate >= :endDate)) " +
//           "ORDER BY p.id DESC")
//    Page<Project> findProjectsInDateRange(
//            @Param("teamId") String teamId,
//            @Param("startDate") LocalDateTime startDate,
//            @Param("endDate") LocalDateTime endDate,
//            Pageable pageable);
//
//    // 특정 팀의 최근 프로젝트 조회 (최대 5개)
//    List<Project> findTop5ByTeamIdOrderByIdDesc(String teamId);
//
//    // 팀 내 프로젝트 개수 조회
//    long countByTeamId(String teamId);
//
//    // ✅ 상태별 조회 메서드는 제거 (상태가 동적으로 계산되므로 DB 쿼리로 불가능)
//    // 대신 서비스 레이어에서 메모리 필터링으로 처리
//
//    // 추가적인 조회 메서드들
//
//    // 특정 날짜 이후 시작하는 프로젝트 조회
//    List<Project> findByTeamIdAndStartDateAfterOrderByStartDateAsc(String teamId, LocalDateTime date);
//
//    // 특정 날짜 이전 종료하는 프로젝트 조회
//    List<Project> findByTeamIdAndEndDateBeforeOrderByEndDateDesc(String teamId, LocalDateTime date);
//
//    // 진행 중인 프로젝트 조회 (시작일은 지났고 종료일은 안 지난 것들)
//    @Query("SELECT p FROM Project p WHERE p.team.id = :teamId " +
//           "AND p.startDate <= :now AND (p.endDate IS NULL OR p.endDate >= :now) " +
//           "ORDER BY p.id DESC")
//    List<Project> findInProgressProjects(@Param("teamId") String teamId, @Param("now") LocalDateTime now);
//
//    // 완료된 프로젝트 조회 (종료일이 지난 것들)
//    @Query("SELECT p FROM Project p WHERE p.team.id = :teamId " +
//           "AND p.endDate IS NOT NULL AND p.endDate < :now " +
//           "ORDER BY p.endDate DESC")
//    List<Project> findCompletedProjects(@Param("teamId") String teamId, @Param("now") LocalDateTime now);
//
//    // 시작 전 프로젝트 조회 (시작일이 아직 안 온 것들)
//    @Query("SELECT p FROM Project p WHERE p.team.id = :teamId " +
//           "AND p.startDate IS NOT NULL AND p.startDate > :now " +
//           "ORDER BY p.startDate ASC")
//    List<Project> findBeforeStartProjects(@Param("teamId") String teamId, @Param("now") LocalDateTime now);
}
