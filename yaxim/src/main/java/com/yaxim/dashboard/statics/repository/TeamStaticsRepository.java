package com.yaxim.dashboard.statics.repository;

import com.yaxim.dashboard.statics.entity.Weekday;
import com.yaxim.dashboard.statics.entity.DailyTeamActivity;
import com.yaxim.dashboard.statics.entity.select.AverageActivity;
import com.yaxim.dashboard.statics.entity.select.SumActivity;
import com.yaxim.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamStaticsRepository extends JpaRepository<DailyTeamActivity, Long> {
    Boolean existsAllByTeamId(String team_id);
    List<DailyTeamActivity> findAllByTeam(Team team);

    @Query("""
    SELECT new com.yaxim.dashboard.statics.entity.select.AverageActivity(
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
    FROM DailyTeamActivity a
    WHERE a.team = :team and a.day = :day
    GROUP BY a.reportDate
    """)
    AverageActivity getTeamAvgByDayAndTeam(Weekday day, Team team);

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
        FROM DailyTeamActivity a
        WHERE a.team = :team
    """)
    SumActivity getTeamWeekActivity(Team team);
}
