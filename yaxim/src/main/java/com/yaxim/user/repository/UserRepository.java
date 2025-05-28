package com.yaxim.user.repository;

import com.yaxim.user.entity.user.UserRole;
import com.yaxim.user.entity.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Users> findByUserRole(UserRole userRole);
}
