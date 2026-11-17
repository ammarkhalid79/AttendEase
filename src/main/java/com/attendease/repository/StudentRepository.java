package com.attendease.repository;

import com.attendease.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.*;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRegistrationNumberIgnoreCase(String registrationNumber);
    Optional<Student> findByUserId(Long userId);
    List<Student> findAllByOrderByFullNameAsc();
}
