package com.yaxim.report.entity;

import com.yaxim.global.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String report;

    // 자식 클래스에서 호출할 수 있도록 protected 생성자 추가
    protected BaseReport(LocalDate startDate, LocalDate endDate, String report) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.report = report;
    }
}
