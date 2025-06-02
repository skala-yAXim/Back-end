package com.yaxim.global.auth.aop;

import com.yaxim.global.auth.jwt.JwtAuthentication;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.exception.UserHasNoAuthorityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TeamRoleAspect {
    private final TeamMemberRepository teamMemberRepository;

    @Around("@annotation(checkRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, CheckRole checkRole) throws Throwable {
        // 현재 인증 정보 가져오기
        JwtAuthentication authentication = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getEmail();

        log.info(String.valueOf(checkRole.value()));

//        // 메서드 인자에서 teamId 찾기
//        String teamId = extractTeamIdFromArgs(joinPoint.getArgs());
//        if (teamId == null) {
//            throw new IllegalArgumentException("teamId가 메서드 파라미터에 필요합니다.");
//        }

//        // 권한 확인
//        TeamMember member = teamMemberRepository.findByTeamIdAndEmail(teamId, email)
//                .orElseThrow(TeamNotFoundException::new);

        TeamMember member = teamMemberRepository.findByEmail(email)
                .orElseThrow(TeamMemberNotMappedException::new);

        log.info(String.valueOf(member.getRole()));

        if (!member.getRole().equals(checkRole.value())) {
            throw new UserHasNoAuthorityException();
        }


        return joinPoint.proceed();
    }

//    private String extractTeamIdFromArgs(Object[] args) {
//        for (Object arg : args) {
//            if (arg instanceof String && ((String) arg).startsWith("team_")) {
//                return (String) arg;
//            }
//        }
//        return null;
//    }
}
