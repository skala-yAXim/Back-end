package com.yaxim.project.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yaxim.project.entity.Project;
import com.yaxim.team.entity.Team;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.yaxim.project.entity.QProject.project;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ProjectCustomRepository {
    private final JPAQueryFactory queryFactory;

    public Page<Project> findByTeamAndProjectName(
            Team team,
            String projectName,
            Pageable pageable
    ) {
        List<Project> projects = queryFactory
                .selectFrom(project)
                .where(
                        projectNameLike(projectName),
                        teamEquals(team)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(project.count())
                .from(project)
                .where(
                        projectNameLike(projectName),
                        teamEquals(team)
                );

        return PageableExecutionUtils.getPage(
                projects,
                pageable,
                countQuery::fetchOne
        );
    }

    public List<Project> findAllInProgress() {
        return queryFactory
                .selectFrom(project)
                .where(
                        currentTimeBetween()
                )
                .fetch();
    }

    private BooleanExpression projectNameLike(String projectName) {
        return projectName != null && !projectName.isEmpty()
                ? project.name.like("%" + projectName + "%")
                : null;
    }

    private BooleanExpression teamEquals(Team team) {
        return team != null ? project.team.eq(team) : null;
    }

    private BooleanExpression currentTimeBetween() {
        LocalDate now = LocalDate.now();
        return project.startDate.loe(now).and(project.endDate.goe(now));
    }

}
