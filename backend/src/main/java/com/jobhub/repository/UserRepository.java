package com.jobhub.repository;

import com.jobhub.entity.User;
import com.jobhub.enums.EmployerStatus;
import com.jobhub.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(UserRole role);
    long countByIsActiveTrue();
    Page<User> findByRole(UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.employer e WHERE u.role = :role AND e.status = :status")
    Page<User> findByRoleAndEmployerStatus(
            @Param("role") UserRole role,
            @Param("status")EmployerStatus status,
            Pageable pageable);
}