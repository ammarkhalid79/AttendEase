package com.attendease.repository;

import com.attendease.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacherIdOrderByCourseCodeAsc(Long teacherId);
    Optional<Course> findByCourseCodeIgnoreCase(String courseCode);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select c from Course c where c.id = :id and c.teacherId = :teacherId") Optional<Course> lockOwned(@Param("id") Long id, @Param("teacherId") Long teacherId);
}
