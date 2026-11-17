package com.attendease.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long sessionId;
    private Long studentId;
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARCHAR)
    private Status status;

    protected AttendanceRecord() {}

    public AttendanceRecord(Long sessionId, Long studentId, Status status) {
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
