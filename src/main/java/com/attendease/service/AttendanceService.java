package com.attendease.service;

import com.attendease.entity.*;
import java.time.LocalDate;
import java.util.*;

public interface AttendanceService {
    record Mark(Student student, Status status) {}
    record Entry(Course course, AttendanceSession session, Student student, Status status) {}
    record Summary(long conducted, long present, long absent, long late) {
        public Double percentage() { return conducted == 0 ? null : 100.0 * (present + late) / conducted; }
        public boolean low() { return conducted > 0 && (present + late) * 4 < conducted * 3; }
        public String display() {
            return conducted == 0 ? "N/A" : String.format(Locale.ROOT, "%.1f%%", percentage());
        }
    }
    record CourseAttendance(Course course, Summary summary) {}
    record Report(List<Entry> entries, Summary summary, long conductedSessions) {}
    record Trend(LocalDate date, Summary summary) {}
    record Dashboard(int students, int courses, Summary today, List<Student> low,
                     List<Trend> trend, List<Entry> recent) {}

    List<Mark> sheet(long teacherId, long courseId, LocalDate date, int number);
    void save(long teacherId, long courseId, LocalDate date, int number, Map<Long, Status> marks);
    Report report(long teacherId, Long courseId, Long studentId, LocalDate from, LocalDate to, Status status);
    Dashboard dashboard(long teacherId);
    List<CourseAttendance> studentCourses(long studentId);
    List<Entry> studentHistory(long studentId);
}
