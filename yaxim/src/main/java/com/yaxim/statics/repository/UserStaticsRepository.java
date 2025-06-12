package com.yaxim.statics.repository;

import com.yaxim.statics.entity.select.AverageActivity;
import com.yaxim.statics.entity.DailyUserActivity;
import com.yaxim.statics.entity.Weekday;
import com.yaxim.statics.entity.select.TeamActivity;
import com.yaxim.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserStaticsRepository extends JpaRepository<DailyUserActivity, Long> {
    List<DailyUserActivity> findAllByUserId(Long userId);

    @Query("""
        SELECT new com.yaxim.statics.entity.select.AverageActivity (
            a.reportDate,
            AVG(a.teamsPost),
            AVG(a.docsDocx),
            AVG(a.docsXlsx),
            AVG(a.docsTxt),
            AVG(a.docsEtc),
            AVG(a.emailReceive),
            AVG(a.emailSend),
            AVG(a.gitPullRequest),
            AVG(a.gitCommit),
            AVG(a.gitIssue)
        )
        FROM DailyUserActivity a
        WHERE a.day = :day
        GROUP BY a.reportDate
    """)
    AverageActivity getUserAvgActivityByWeekDay(Weekday day);

    @Query("""
        SELECT new com.yaxim.statics.entity.select.TeamActivity (
            a.reportDate,
            SUM(a.teamsPost),
            SUM(a.docsDocx),
            SUM(a.docsXlsx),
            SUM(a.docsTxt),
            SUM(a.docsEtc),
            SUM(a.emailReceive),
            SUM(a.emailSend),
            SUM(a.gitPullRequest),
            SUM(a.gitCommit),
            SUM(a.gitIssue)
        )
        FROM DailyUserActivity a
        WHERE a.user in :users AND a.day = :day
        GROUP BY a.reportDate
    """)
    TeamActivity getTeamActivityByWeekdayAndUser(Weekday day, List<Users> users);

}
