package com.attendease.repository;

import com.attendease.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.*;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByCourseId(Long courseId);
    List<Enrollment> findByStudentId(Long studentId);
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
}
