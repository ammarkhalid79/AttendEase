package com.attendease.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance_sessions")
public class AttendanceSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long courseId;
    private LocalDate sessionDate;
    private int sessionNumber;

    protected AttendanceSession() {}

    public AttendanceSession(Long courseId, LocalDate sessionDate, int sessionNumber) {
        this.courseId = courseId;
        this.sessionDate = sessionDate;
        this.sessionNumber = sessionNumber;
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }
    public int getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }
}
