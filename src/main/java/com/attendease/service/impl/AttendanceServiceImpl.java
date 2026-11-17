package com.attendease.service.impl;

import com.attendease.entity.*;
import com.attendease.exception.DomainException;
import com.attendease.repository.*;
import com.attendease.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {
    private final CourseService courses;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollments;
    private final StudentRepository students;
    private final AttendanceSessionRepository sessions;
    private final AttendanceRecordRepository records;

    public AttendanceServiceImpl(CourseService courses, CourseRepository courseRepository,
                                 EnrollmentRepository enrollments, StudentRepository students,
                                 AttendanceSessionRepository sessions, AttendanceRecordRepository records) {
        this.courses = courses;
        this.courseRepository = courseRepository;
        this.enrollments = enrollments;
        this.students = students;
        this.sessions = sessions;
        this.records = records;
    }

    private void validateSession(LocalDate date, int number) {
        if (date == null || date.isAfter(LocalDate.now()) || date.getYear() < 1000 || number < 1)
            throw new DomainException("Select a valid date through today and a positive session number.");
    }

    @Override
    public List<Mark> sheet(long teacherId, long courseId, LocalDate date, int number) {
        validateSession(date, number);
        courses.owned(teacherId, courseId);
        var existing = sessions.findByCourseIdAndSessionDateAndSessionNumber(courseId, date, number);
        return marksFor(courseId, existing.map(s -> records.findBySessionId(s.getId())).orElseGet(List::of),
                existing.isPresent());
    }

    private List<Mark> marksFor(long courseId, List<AttendanceRecord> existing, boolean saved) {
        var statuses = existing.stream().collect(Collectors.toMap(AttendanceRecord::getStudentId, AttendanceRecord::getStatus));
        List<Student> roster = courses.roster(courseId);
        return saved
                ? roster.stream().filter(s -> statuses.containsKey(s.getId()))
                        .map(s -> new Mark(s, statuses.get(s.getId()))).toList()
                : courses.active(roster).stream().map(s -> new Mark(s, null)).toList();
    }

    @Override
    @Transactional
    public void save(long teacherId, long courseId, LocalDate date, int number, Map<Long, Status> marks) {
        validateSession(date, number);
        courses.lock(teacherId, courseId);
        var existing = sessions.findByCourseIdAndSessionDateAndSessionNumber(courseId, date, number);
        List<AttendanceRecord> previous = existing.map(s -> records.findBySessionId(s.getId())).orElseGet(List::of);
        Set<Long> roster = marksFor(courseId, previous, existing.isPresent()).stream()
                .map(mark -> mark.student().getId()).collect(Collectors.toSet());
        if (roster.isEmpty() || !roster.equals(marks.keySet()) || marks.values().stream().anyMatch(Objects::isNull)) {
            throw new DomainException("Reload the attendance sheet and select a status for each listed student.");
        }
        if (existing.isPresent()) {
            previous.forEach(record -> record.setStatus(marks.get(record.getStudentId())));
        } else {
            var session = sessions.save(new AttendanceSession(courseId, date, number));
            records.saveAll(marks.entrySet().stream()
                    .map(mark -> new AttendanceRecord(session.getId(), mark.getKey(), mark.getValue())).toList());
        }
    }

    @Override
    public Report report(long teacherId, Long courseId, Long studentId, LocalDate from, LocalDate to, Status status) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new DomainException("The start date must be on or before the end date.");
        }
        List<Course> selected = courseId == null ? courses.list(teacherId) : List.of(courses.owned(teacherId, courseId));
        List<Entry> eligible = entries(selected).stream()
                .filter(e -> studentId == null || e.student().getId().equals(studentId))
                .filter(e -> from == null || !e.session().getSessionDate().isBefore(from))
                .filter(e -> to == null || !e.session().getSessionDate().isAfter(to)).toList();
        List<Entry> filtered = eligible.stream().filter(e -> status == null || e.status() == status).toList();
        long classSessions = eligible.stream().map(e -> e.session().getId()).distinct().count();
        return new Report(filtered, summarize(eligible), classSessions);
    }

    @Override
    public Dashboard dashboard(long teacherId) {
        var owned = courses.list(teacherId);
        var entries = entries(owned);
        LocalDate today = LocalDate.now();
        var daily = entries.stream().collect(Collectors.groupingBy(e -> e.session().getSessionDate()));
        var perStudent = entries.stream().collect(Collectors.groupingBy(e -> e.student().getId()));
        var active = courses.active(students.findAllByOrderByFullNameAsc());
        var low = active.stream().filter(s -> summarize(perStudent.getOrDefault(s.getId(), List.of())).low()).toList();
        var trend = today.minusDays(6).datesUntil(today.plusDays(1))
                .map(day -> new Trend(day, summarize(daily.getOrDefault(day, List.of())))).toList();
        return new Dashboard(active.size(), owned.size(), trend.getLast().summary(), low, trend,
                entries.stream().limit(8).toList());
    }

    private List<Course> enrolledCourses(long studentId) {
        return courseRepository.findAllById(enrollments.findByStudentId(studentId).stream()
                        .map(Enrollment::getCourseId).toList()).stream()
                .sorted(Comparator.comparing(Course::getCourseCode)).toList();
    }

    @Override
    public List<CourseAttendance> studentCourses(long studentId) {
        var enrolled = enrolledCourses(studentId);
        var history = entries(enrolled).stream().filter(e -> e.student().getId().equals(studentId)).toList();
        return enrolled.stream().map(c -> new CourseAttendance(c,
                summarize(history.stream().filter(e -> e.course().getId().equals(c.getId())).toList()))).toList();
    }

    @Override
    public List<Entry> studentHistory(long studentId) {
        return entries(enrolledCourses(studentId)).stream().filter(e -> e.student().getId().equals(studentId)).toList();
    }

    private List<Entry> entries(List<Course> selected) {
        if (selected.isEmpty()) return List.of();
        var courseById = selected.stream().collect(Collectors.toMap(Course::getId, Function.identity()));
        var conducted = sessions.findByCourseIdInOrderBySessionDateDescSessionNumberDesc(courseById.keySet());
        if (conducted.isEmpty()) return List.of();
        var bySession = records.findBySessionIdIn(conducted.stream().map(AttendanceSession::getId).toList())
                .stream().collect(Collectors.groupingBy(AttendanceRecord::getSessionId));
        var studentById = students.findAllById(bySession.values().stream().flatMap(Collection::stream)
                .map(AttendanceRecord::getStudentId).distinct().toList()).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
        var rosterByCourse = selected.stream().collect(Collectors.toMap(Course::getId,
                c -> enrollments.findByCourseId(c.getId()).stream().map(Enrollment::getStudentId).collect(Collectors.toSet())));
        return conducted.stream().flatMap(session -> bySession.getOrDefault(session.getId(), List.of()).stream()
                .filter(record -> rosterByCourse.get(session.getCourseId()).contains(record.getStudentId()))
                .map(record -> new Entry(courseById.get(session.getCourseId()), session,
                        studentById.get(record.getStudentId()), record.getStatus()))).toList();
    }

    public static Summary summarize(List<Entry> rows) {
        long present = rows.stream().filter(e -> e.status() == Status.PRESENT).count();
        long late = rows.stream().filter(e -> e.status() == Status.LATE).count();
        return new Summary(rows.size(), present, rows.size() - present - late, late);
    }
}
