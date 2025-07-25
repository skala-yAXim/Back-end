package com.yaxim.dashboard.statics.repository;

import com.yaxim.dashboard.statics.controller.dto.response.TermResponse;
import com.yaxim.dashboard.statics.entity.select.AverageActivity;
import com.yaxim.dashboard.statics.entity.DailyUserActivity;
import com.yaxim.dashboard.statics.entity.Weekday;
import com.yaxim.dashboard.statics.entity.select.SumActivity;
import com.yaxim.dashboard.statics.entity.select.TeamActivity;
import com.yaxim.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserStaticsRepository extends JpaRepository<DailyUserActivity, Long> {
    List<DailyUserActivity> findAllByUserId(Long userId);
    Boolean existsAllByUserId(Long userId);
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DailyUserActivity d")
    Boolean exists();

    @Query("""
        SELECT new com.yaxim.dashboard.statics.entity.select.AverageActivity (
            a.reportDate,
            AVG(a.teamsPost),
            AVG(a.teamsReply),
            AVG(a.docsDocx),
            AVG(a.docsXlsx),
            AVG(a.docsPptx),
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
    Optional<AverageActivity> getUserAvgActivityByWeekDay(Weekday day);

    @Query("""
        SELECT new com.yaxim.dashboard.statics.entity.select.SumActivity (
            SUM(a.teamsPost),
            SUM(a.teamsReply),
            SUM(a.docsDocx),
            SUM(a.docsXlsx),
            SUM(a.docsPptx),
            SUM(a.docsEtc),
            SUM(a.emailReceive),
            SUM(a.emailSend),
            SUM(a.gitPullRequest),
            SUM(a.gitCommit),
            SUM(a.gitIssue)
        )
        FROM DailyUserActivity a
        WHERE a.user.id = :userId
    """)
    SumActivity getUserWeekActivity(Long userId);

    @Query("""
        SELECT new com.yaxim.dashboard.statics.entity.select.TeamActivity (
            a.reportDate,
            SUM(a.teamsPost),
            SUM(a.teamsReply),
            SUM(a.docsDocx),
            SUM(a.docsXlsx),
            SUM(a.docsPptx),
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
    Optional<TeamActivity> getTeamActivityByWeekdayAndUser(Weekday day, List<Users> users);

    @Query("""
        SELECT new com.yaxim.dashboard.statics.controller.dto.response.TermResponse (
         MIN(a.reportDate),
         MAX(a.reportDate)
        )
        FROM DailyUserActivity a
    """)
    Optional<TermResponse> getTerm();
}
