package com.yaxim.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1, max = 200, message = "프로젝트 명은 1자 이상 200자 이하여야 합니다.")
    @Column(nullable = false, length = 200)
    private String name;

    @NotNull
    @Column(nullable = false)
    private Long teamId;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Size(max = 1000, message = "프로젝트 설명은 1000자 이하여야 합니다.")
    @Column(length = 1000)
    private String description;

    // ✅ ProjectFile과의 1:N 양방향 관계 (mappedBy 사용)
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProjectFile> projectFiles = new ArrayList<>();

    // 비즈니스 메서드들
    public void updateBasicInfo(String name, String description) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        this.description = description;
    }

    public void updateDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일보다 이후여야 합니다.");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateTeam(Long teamId) {
        this.teamId = teamId;
    }

    // ✅ ProjectFile 관리 메서드들 (양방향 관계 고려)
    public void addProjectFile(ProjectFile projectFile) {
        this.projectFiles.add(projectFile);
        projectFile.setProject(this); // 양방향 관계 설정
    }

    public void removeProjectFile(ProjectFile projectFile) {
        this.projectFiles.remove(projectFile);
        projectFile.setProject(null); // 양방향 관계 해제
    }

    public void clearProjectFiles() {
        this.projectFiles.forEach(file -> file.setProject(null));
        this.projectFiles.clear();
    }

    // 동적 상태 계산 메서드 (팀장님 요구사항)
    public String calculateStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        if (startDate == null && endDate == null) {
            return "미정";
        }
        
        if (startDate != null && now.isBefore(startDate)) {
            return "시작전";
        }
        
        if (endDate != null && now.isAfter(endDate)) {
            return "완료";
        }
        
        return "진행중";
    }

    // UI에서 요구하는 기간 표시용 메서드
    public String getDateRange() {
        if (startDate == null && endDate == null) {
            return null;
        }
        if (startDate == null) {
            return "~ " + endDate.toLocalDate();
        }
        if (endDate == null) {
            return startDate.toLocalDate() + " ~";
        }
        return startDate.toLocalDate() + " ~ " + endDate.toLocalDate();
    }

    // 편의 메서드들 (기존 코드와의 호환성)
    public String getProjectName() {
        return this.name;
    }

    public String getSummary() {
        return this.description;
    }
}
