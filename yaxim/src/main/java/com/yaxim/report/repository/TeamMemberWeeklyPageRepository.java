package com.yaxim.report.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yaxim.report.controller.dto.request.TeamMemberWeeklyPageRequest;
import com.yaxim.report.entity.UserWeeklyReport;
import com.yaxim.team.entity.Team;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.yaxim.report.entity.QUserWeeklyReport.userWeeklyReport;

@Repository
@Slf4j
@RequiredArgsConstructor
public class TeamMemberWeeklyPageRepository {
    private final JPAQueryFactory queryFactory;

    public Page<UserWeeklyReport> findTeamMemberWeekly(
            TeamMemberWeeklyPageRequest request,
            Team team,
            Pageable pageable
    ) {
            List<UserWeeklyReport> content =
                    queryFactory
                            .select(userWeeklyReport)
                            .from(userWeeklyReport)
                            .where(
                                    userIdIn(request.getUserId()),
                                    dateBetween(request.getStartDate(), request.getEndDate()),
                                    teamEquals(team)
                            )
                            .offset(pageable.getOffset())
                            .limit(pageable.getPageSize())
                            .orderBy(getSort(pageable, userWeeklyReport))
                            .fetch();

            JPAQuery<Long> countQuery = queryFactory
                    .select(userWeeklyReport.count())
                    .from(userWeeklyReport)
                    .where(
                            userIdIn(request.getUserId()),
                            dateBetween(request.getStartDate(), request.getEndDate())
                    );

            return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression userIdIn(List<Long> userIds) {
        return (userIds != null && !userIds.isEmpty()) ?
                userWeeklyReport.user.id.in(userIds) : null;
    }

    private BooleanExpression dateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            // 시작 날짜와 종료 날짜가 둘 다 존재할 때
            return userWeeklyReport.endDate.between(startDate, endDate);
        } else if (startDate != null) {
            // 종료 날짜가 없을 때 -> 시작 날짜 이후 모든 데이터
            return userWeeklyReport.endDate.goe(startDate);
        } else if (endDate != null) {
            // 시작 날짜가 없을 때 -> 종료 날짜 이전 모든 데이터
            return userWeeklyReport.endDate.loe(endDate);
        } else {
            // 시작 날짜와 종료 날짜가 모두 없으면 모든 데이터 반환
            return null;
        }
    }

    private BooleanExpression teamEquals(Team team) {
        return team != null ? userWeeklyReport.team.eq(team) : null;
    }

    public static <T> OrderSpecifier<?>[] getSort(Pageable pageable, EntityPathBase<T> qClass) {
        return pageable.getSort().stream().map(order ->
                        new OrderSpecifier(
                                Order.valueOf(order.getDirection().name()),
                                Expressions.path(Object.class, qClass, order.getProperty())
                        )).toList()
                .toArray(new OrderSpecifier[0]);
    }
}
