package com.yaxim.project.repository;

import com.yaxim.project.entity.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {

    // 프로젝트별 파일 목록 조회
    List<ProjectFile> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    // 프로젝트별 파일 개수 조회
    long countByProjectId(Long projectId);

    // 프로젝트별 파일 크기 합계 조회
    @Query("SELECT SUM(pf.fileSize) FROM ProjectFile pf WHERE pf.projectId = :projectId")
    Optional<Long> sumFileSizeByProjectId(@Param("projectId") Long projectId);

    // S3 Object Key로 파일 조회
    Optional<ProjectFile> findByS3ObjectKey(String s3ObjectKey);

    // 프로젝트와 원본 파일명으로 파일 조회 (중복 방지용)
    Optional<ProjectFile> findByProjectIdAndOriginalFileName(Long projectId, String originalFileName);

    // 특정 파일 타입별 파일 조회
    List<ProjectFile> findByProjectIdAndFileTypeContainingOrderByCreatedAtDesc(Long projectId, String fileType);

    // 이미지 파일만 조회
    @Query("SELECT pf FROM ProjectFile pf WHERE pf.projectId = :projectId " +
           "AND (pf.fileType LIKE 'image/%') ORDER BY pf.createdAt DESC")
    List<ProjectFile> findImageFilesByProjectId(@Param("projectId") Long projectId);

    // PDF 파일만 조회
    @Query("SELECT pf FROM ProjectFile pf WHERE pf.projectId = :projectId " +
           "AND pf.fileType = 'application/pdf' ORDER BY pf.createdAt DESC")
    List<ProjectFile> findPdfFilesByProjectId(@Param("projectId") Long projectId);

    // Office 파일만 조회
    @Query("SELECT pf FROM ProjectFile pf WHERE pf.projectId = :projectId " +
           "AND (pf.fileType LIKE 'application/vnd.ms-%' OR " +
           "pf.fileType LIKE 'application/vnd.openxmlformats-officedocument%') " +
           "ORDER BY pf.createdAt DESC")
    List<ProjectFile> findOfficeFilesByProjectId(@Param("projectId") Long projectId);

    // 파일 크기가 특정 값 이상인 파일 조회
    List<ProjectFile> findByProjectIdAndFileSizeGreaterThanOrderByFileSizeDesc(Long projectId, Long minFileSize);

    // 프로젝트별 파일 삭제 (프로젝트 삭제 시 사용)
    void deleteByProjectId(Long projectId);

    // 여러 프로젝트의 파일 일괄 조회
    List<ProjectFile> findByProjectIdInOrderByCreatedAtDesc(List<Long> projectIds);
}
