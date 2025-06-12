package com.yaxim.statics.repository;

import com.yaxim.statics.entity.Weekday;
import com.yaxim.statics.entity.DailyTeamActivity;
import com.yaxim.statics.entity.select.AverageActivity;
import com.yaxim.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamStaticsRepository extends JpaRepository<DailyTeamActivity, Long> {
    Boolean existsAllByTeamId(String team_id);
    List<DailyTeamActivity> findAllByTeam(Team team);

    @Query("""
    SELECT new com.yaxim.statics.entity.select.AverageActivity(
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
    FROM DailyTeamActivity a
    WHERE a.team = :team and a.day = :day
    GROUP BY a.reportDate
    """)
    AverageActivity getTeamAvgByDayAndTeam(Weekday day, Team team);
}
