package com.yaxim.dashboard.comment.service;

import com.yaxim.dashboard.comment.controller.dto.response.CommentResponse;
import com.yaxim.dashboard.comment.entity.TeamComment;
import com.yaxim.dashboard.comment.entity.UserComment;
import com.yaxim.dashboard.comment.exception.TeamCommentNotFoundException;
import com.yaxim.dashboard.comment.exception.UserCommentNotFoundException;
import com.yaxim.dashboard.comment.repository.TeamCommentRepository;
import com.yaxim.dashboard.comment.repository.UserCommentRepository;
import com.yaxim.team.entity.Team;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.entity.UserRole;
import com.yaxim.user.entity.Users;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private UserCommentRepository userCommentRepository;
    @Mock
    private TeamCommentRepository teamCommentRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;

    private Team team;
    private Users user;
    private TeamMember teamMember;

    private static final Long USER_ID = 1L;
    private static final String TEAM_ID = "test_team_id";

    @BeforeEach
    void setUp() {

        team = new Team(
                TEAM_ID,
                "test",
                "description"
        );

        user = new Users(
                USER_ID,
                "name",
                "email@test.com"
        );

        teamMember = new TeamMember(
                team,
                user,
                UserRole.LEADER
        );
    }

    @Test
    @DisplayName("유저 데일리 한줄평 조회")
    public void getUserComment_ExistingUserComment_ReturnsCommentResponse() {
        // Given
        UserComment expectedUserComment = new UserComment(user, "Test comment");

        Mockito.when(userCommentRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(expectedUserComment));

        // When
        CommentResponse actualResponse = commentService.getUserComment(USER_ID);

        // Then
        Mockito.verify(userCommentRepository).findByUserId(USER_ID);
        Assertions.assertNotNull(actualResponse);
    }

    @Test
    @DisplayName("존재하지 않는 한줄평 조회 예외처리(유저)")
    public void getUserComment_NonExistingUserComment_ThrowsUserCommentNotFoundException() {
        // Given
        Mockito.when(userCommentRepository.findByUserId(USER_ID))
                .thenReturn(Optional.empty());

        // When & Then
        Assertions.assertThrows(
                UserCommentNotFoundException.class, () ->
                        commentService.getUserComment(USER_ID)
        );
        Mockito.verify(userCommentRepository).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("팀 위클리 한줄평 조회")
    public void getTeamComment_ExistingTeamComment_ReturnsCommentResponse() {
        // Given
        TeamComment expectedTeamComment = new TeamComment(team, "Team comment");

        Mockito.when(teamMemberRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(teamMember));
        Mockito.when(teamCommentRepository.findByTeam(team))
                .thenReturn(Optional.of(expectedTeamComment));

        // When
        CommentResponse actualResponse = commentService.getTeamComment(USER_ID);

        // Then
        Mockito.verify(teamMemberRepository).findByUserId(USER_ID);
        Mockito.verify(teamCommentRepository).findByTeam(team);
        Assertions.assertNotNull(actualResponse);
    }

    @Test
    @DisplayName("존재하지 않는 한줄평 조회 예외처리(팀)")
    public void getTeamComment_NonExistingTeamComment_ThrowsTeamCommentNotFoundException() {
        // Given
        Mockito.when(teamMemberRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(teamMember));
        Mockito.when(teamCommentRepository.findByTeam(team))
                .thenReturn(Optional.empty());

        // When & Then
        Assertions.assertThrows(
                TeamCommentNotFoundException.class, () ->
                        commentService.getTeamComment(USER_ID)
        );
        Mockito.verify(teamMemberRepository).findByUserId(USER_ID);
        Mockito.verify(teamCommentRepository).findByTeam(team);
    }
}
