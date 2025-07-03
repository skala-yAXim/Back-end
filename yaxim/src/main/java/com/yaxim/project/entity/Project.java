package com.yaxim.project.entity;

import com.yaxim.global.util.BaseEntity;
import com.yaxim.team.entity.Team;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "projects")
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Setter
    @Column(nullable = false, length = 200)
    private String name;

    @Setter
    private Integer progress;

    @NotNull
    @JoinColumn(name = "team_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;

    @Setter
    @Column(length = 1000)
    private String description;

    // ✅ ProjectFile과의 1:N 양방향 관계 (mappedBy 사용)
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProjectFile> projectFiles = new ArrayList<>();

    public Project(String name, Team team, LocalDate startDate, LocalDate endDate, String description) {
        this.name = name;
        this.team = team;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    public void updateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일보다 이후여야 합니다.");
        }
        this.startDate = startDate;
        this.endDate = endDate;
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

    // 동적 상태 계산 메서드 (팀장님 요구사항)
    public ProjectStatus calculateStatus() {
        LocalDate now = LocalDate.now();
        
        if (startDate != null && now.isBefore(startDate)) {
            return ProjectStatus.PLANNING;
        }
        
        if (endDate != null && now.isAfter(endDate)) {
            return ProjectStatus.COMPLETED;
        }
        
        return ProjectStatus.IN_PROGRESS;
    }
}
