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
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TeamRoleAspect {
    private final TeamMemberRepository teamMemberRepository;

    @Around("@within(com.yaxim.global.auth.aop.CheckRole) || @annotation(com.yaxim.global.auth.aop.CheckRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        // 현재 인증 정보 가져오기
        JwtAuthentication authentication = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
        Long userId = authentication.getUserId();

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        // 1. 메서드에서 어노테이션 우선 조회
        CheckRole checkRole = method.getAnnotation(CheckRole.class);

        // 2. 없으면 클래스에서 조회
        if (checkRole == null) {
            checkRole = joinPoint.getTarget().getClass().getAnnotation(CheckRole.class);
        }

        TeamMember member = teamMemberRepository.findByUserId(userId)
                .orElseThrow(TeamMemberNotMappedException::new);

        if (!member.getRole().equals(checkRole.value())) {
            throw new UserHasNoAuthorityException();
        }


        return joinPoint.proceed();
    }
}
