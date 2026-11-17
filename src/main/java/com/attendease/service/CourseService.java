package com.attendease.service;

import com.attendease.dto.CourseForm;
import com.attendease.entity.*;
import com.attendease.exception.*;
import com.attendease.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class CourseService {
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;
    private final StudentRepository students;
    private final AccountRepository accounts;

    public CourseService(CourseRepository courses, EnrollmentRepository enrollments,
                         StudentRepository students, AccountRepository accounts) {
        this.courses = courses;
        this.enrollments = enrollments;
        this.students = students;
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public List<Course> list(long teacherId) {
        return courses.findByTeacherIdOrderByCourseCodeAsc(teacherId);
    }

    @Transactional(readOnly = true)
    public Course owned(long teacherId, long id) {
        return courses.findById(id).filter(c -> c.getTeacherId().equals(teacherId))
                .orElseThrow(() -> new MissingException("Course not found."));
    }

    public Course save(long teacherId, Long id, CourseForm form) {
        String code = form.courseCode().strip().toUpperCase(Locale.ROOT);
        String description = Objects.toString(form.description(), "").strip();
        if (courses.findByCourseCodeIgnoreCase(code).filter(c -> !c.getId().equals(id)).isPresent()) {
            throw new DomainException("This course code is already in use.");
        }
        if (id == null) return courses.save(new Course(code, form.courseName().strip(), description, teacherId));
        Course course = owned(teacherId, id);
        course.setCourseCode(code);
        course.setCourseName(form.courseName().strip());
        course.setDescription(description);
        return course;
    }

    public void enroll(long teacherId, long courseId, long studentId) {
        lock(teacherId, courseId);
        Student student = students.findById(studentId).orElseThrow(() -> new MissingException("Student not found."));
        if (!accounts.findById(student.getUserId()).orElseThrow().getActive()) {
            throw new DomainException("This student account is inactive.");
        }
        if (enrollments.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new DomainException("This student is already enrolled.");
        }
        enrollments.save(new Enrollment(studentId, courseId));
    }

    public Course lock(long teacherId, long courseId) {
        return courses.lockOwned(courseId, teacherId).orElseThrow(() -> new MissingException("Course not found."));
    }

    @Transactional(readOnly = true)
    public List<Student> roster(long courseId) {
        return students.findAllById(enrollments.findByCourseId(courseId).stream().map(Enrollment::getStudentId).toList())
                .stream().sorted(Comparator.comparing(Student::getFullName)).toList();
    }

    @Transactional(readOnly = true)
    public List<Student> active(List<Student> roster) {
        var activeAccounts = new HashSet<Long>();
        accounts.findAllById(roster.stream().map(Student::getUserId).toList()).stream()
                .filter(Account::getActive).map(Account::getId).forEach(activeAccounts::add);
        return roster.stream().filter(s -> activeAccounts.contains(s.getUserId())).toList();
    }
}
