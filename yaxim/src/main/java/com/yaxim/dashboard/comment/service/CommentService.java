package com.yaxim.dashboard.comment.service;

import com.yaxim.dashboard.comment.controller.dto.response.CommentResponse;
import com.yaxim.dashboard.comment.entity.TeamComment;
import com.yaxim.dashboard.comment.entity.UserComment;
import com.yaxim.dashboard.comment.exception.TeamCommentNotFoundException;
import com.yaxim.dashboard.comment.exception.UserCommentNotFoundException;
import com.yaxim.dashboard.comment.repository.TeamCommentRepository;
import com.yaxim.dashboard.comment.repository.UserCommentRepository;
import com.yaxim.team.entity.Team;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {
    private final UserCommentRepository userCommentRepository;
    private final TeamCommentRepository teamCommentRepository;
    private final TeamMemberRepository teamMemberRepository;

    public CommentResponse getUserComment(Long userId) {
        UserComment comment = userCommentRepository.findByUserId(userId)
                .orElseThrow(UserCommentNotFoundException::new);

        return CommentResponse.from(comment);
    }

    public CommentResponse getTeamComment(Long userId) {
        Team team = teamMemberRepository.findByUserId(userId)
                .orElseThrow(TeamMemberNotMappedException::new)
                .getTeam();

        TeamComment response = teamCommentRepository.findByTeam(team)
                .orElseThrow(TeamCommentNotFoundException::new);

        return CommentResponse.from(response);
    }

    public void addComment(Users user, String comment) {
        UserComment response = userCommentRepository.findByUser(user)
                .orElse(new UserComment(
                        user,
                        ""
                ));

        response.setComment(comment);

        userCommentRepository.save(response);
    }

    public void addComment(Team team, String comment) {
        TeamComment response = teamCommentRepository.findByTeam(team)
                .orElse(new TeamComment(
                        team,
                        ""
                        )
                );

        response.setComment(comment);

        teamCommentRepository.save(response);
    }
}
