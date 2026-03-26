package com.jobhub.repository;

import com.jobhub.entity.Employer;
import com.jobhub.enums.EmployerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployerRepository extends JpaRepository<Employer, Long> {
    Optional<Employer> findByUser_Id(Long userId);
    boolean existsByUser_Id(Long userId);
    List<Employer> findByStatus(EmployerStatus status);
}