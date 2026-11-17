package com.attendease;

import com.attendease.dto.*;
import com.attendease.entity.*;
import com.attendease.exception.*;
import com.attendease.repository.*;
import com.attendease.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:attendease;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureMockMvc
@Transactional
class AttendanceWorkflowTest {
    @Autowired AccountRepository accounts;
    @Autowired StudentService students;
    @Autowired CourseService courses;
    @Autowired EnrollmentRepository enrollments;
    @Autowired AttendanceService attendance;
    @Autowired AttendanceSessionRepository sessions;
    @Autowired AttendanceRecordRepository records;
    @Autowired MockMvc mvc;

    @Test
    void createsCourseAndEnrollsStudentOnce() {
        long teacher = teacher("teacher-a");
        var course = courses.save(teacher, null, new CourseForm("SE-101", "Software Construction", ""));
        assertEquals("SE-101", course.getCourseCode());

        var student = student("SE-11");
        courses.enroll(teacher, course.getId(), student.getId());
        assertTrue(enrollments.existsByCourseIdAndStudentId(course.getId(), student.getId()));
        assertThrows(DomainException.class, () -> courses.enroll(teacher, course.getId(), student.getId()));
    }

    @Test
    void savingSameSessionUpdatesOriginalRosterWithoutDuplicates() {
        long teacher = teacher("teacher-b");
        long course = courses.save(teacher, null, new CourseForm("SCD", "Software Construction", "")).getId();
        var student = student("SE-12");
        courses.enroll(teacher, course, student.getId());
        LocalDate today = LocalDate.now();
        attendance.save(teacher, course, today, 1, Map.of(student.getId(), Status.PRESENT));

        var addedLater = student("SE-13");
        courses.enroll(teacher, course, addedLater.getId());
        assertEquals(1, attendance.sheet(teacher, course, today, 1).size());
        attendance.save(teacher, course, today, 1, Map.of(student.getId(), Status.LATE));

        assertEquals(1, sessions.count());
        assertEquals(1, records.count());
        assertEquals(Status.LATE, attendance.sheet(teacher, course, today, 1).getFirst().status());
        assertTrue(attendance.studentHistory(addedLater.getId()).isEmpty());
        assertEquals("100.0%", attendance.studentCourses(student.getId()).getFirst().summary().display());
    }

    @Test
    void reportKeepsSummaryDenominatorWhenStatusFiltersRowsAndEnforcesOwnership() {
        long teacher = teacher("teacher-c");
        long another = teacher("teacher-d");
        long course = courses.save(teacher, null, new CourseForm("DB", "Databases", "")).getId();
        var student = student("SE-14");
        courses.enroll(teacher, course, student.getId());
        LocalDate today = LocalDate.now();
        attendance.save(teacher, course, today, 1, Map.of(student.getId(), Status.ABSENT));
        attendance.save(teacher, course, today, 2, Map.of(student.getId(), Status.PRESENT));

        var report = attendance.report(teacher, course, student.getId(), today, today, Status.ABSENT);
        assertEquals(1, report.entries().size());
        assertEquals(2, report.conductedSessions());
        assertEquals("50.0%", report.summary().display());
        assertThrows(MissingException.class, () -> attendance.report(another, course, null, null, null, null));
    }

    @Test
    void studentsCannotPostAttendanceEvenWithACsrfToken() throws Exception {
        mvc.perform(post("/teacher/attendance").with(user("1").roles("STUDENT")).with(csrf())
                        .param("courseId", "1").param("date", LocalDate.now().toString()).param("number", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sharedLayoutRendersForLoginTeacherAndStudent() throws Exception {
        long teacher = teacher("teacher-e");
        var student = student("SE-15");
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/css/parent.css")));
        for (String path : new String[]{"", "/students", "/students/new", "/courses", "/courses/new", "/attendance", "/reports"}) {
            mvc.perform(get("/teacher" + path).with(user(Long.toString(teacher)).roles("TEACHER")))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("/css/parent.css")));
        }
        mvc.perform(get("/student").with(user(student.getUserId().toString()).roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("SE-15")));
    }

    private long teacher(String username) {
        return accounts.save(new Account(username, username + "@example.org", "unused-hash", Role.TEACHER, true)).getId();
    }

    private Student student(String registration) {
        return students.save(null, new StudentForm(registration, "Student " + registration,
                registration + "@example.org", "Computing", 3, "integration-fixture-password"));
    }
}
