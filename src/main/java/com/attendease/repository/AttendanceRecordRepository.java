package com.attendease.repository;

import com.attendease.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.*;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findBySessionId(Long sessionId);
    List<AttendanceRecord> findBySessionIdIn(Collection<Long> sessionIds);
}
