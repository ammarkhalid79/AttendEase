package com.attendease.repository;

import com.attendease.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.*;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    Optional<AttendanceSession> findByCourseIdAndSessionDateAndSessionNumber(Long courseId,LocalDate date,int number);
    List<AttendanceSession> findByCourseIdInOrderBySessionDateDescSessionNumberDesc(Collection<Long> courseIds);
}
