package com.attendease.config;

import com.attendease.dto.*;
import com.attendease.entity.*;
import com.attendease.repository.AccountRepository;
import com.attendease.service.*;
import com.attendease.service.impl.StudentServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Component
public class BootstrapData implements ApplicationRunner {
    private final AccountRepository accounts;
    private final StudentService students;
    private final CourseService courses;
    private final AttendanceService attendance;
    private final PasswordEncoder passwords;
    private final Environment environment;
    private final String teacherPassword;
    private final String demoPassword;

    public BootstrapData(AccountRepository accounts, StudentService students, CourseService courses,
                         AttendanceService attendance, PasswordEncoder passwords, Environment environment,
                         @Value("${attendease.teacher-password}") String teacherPassword,
                         @Value("${attendease.demo-password}") String demoPassword) {
        this.accounts = accounts;
        this.students = students;
        this.courses = courses;
        this.attendance = attendance;
        this.passwords = passwords;
        this.environment = environment;
        this.teacherPassword = teacherPassword;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!teacherPassword.isBlank() && accounts.findByUsernameIgnoreCase("teacher").isEmpty()) {
            accounts.save(new Account("teacher", "teacher@attendease.example",
                    passwords.encode(StudentServiceImpl.checkPassword(teacherPassword)), Role.TEACHER, true));
        }
        if (environment.acceptsProfiles(Profiles.of("demo")) && accounts.findByUsernameIgnoreCase("demo-teacher").isEmpty()) {
            seedDemo();
        }
    }

    private void seedDemo() {
        String password = StudentServiceImpl.checkPassword(demoPassword);
        long teacher = accounts.save(new Account("demo-teacher", "demo-teacher@attendease.example",
                passwords.encode(password), Role.TEACHER, true)).getId();
        List<Student> roster = new ArrayList<>();
        List<String> names = List.of("Amina Hassan", "Hassan Raza", "Maryam Khan", "Omar Iqbal", "Zainab Ali");
        for (int index = 0; index < names.size(); index++) {
            String registration = "DEMO-00" + (index + 1);
            roster.add(students.save(null, new StudentForm(registration, names.get(index),
                    registration.toLowerCase(Locale.ROOT) + "@attendease.example", "Software Engineering", 4, password)));
        }
        List<CourseForm> examples = List.of(
                new CourseForm("DEMO-SCD", "Software Construction and Development", "Construction practices and patterns."),
                new CourseForm("DEMO-OOP", "Object-Oriented Programming", "Classes, interfaces and software design."),
                new CourseForm("DEMO-DB", "Database Management Systems", "Relational modeling and SQL."));
        for (var example : examples) {
            long course = courses.save(teacher, null, example).getId();
            roster.forEach(student -> courses.enroll(teacher, course, student.getId()));
            for (int daysAgo = 0; daysAgo < 6; daysAgo += 2) {
                Map<Long, Status> marks = new HashMap<>();
                for (int index = 0; index < roster.size(); index++) {
                    Status status = switch (index) {
                        case 0 -> daysAgo == 0 ? Status.PRESENT : Status.ABSENT;
                        case 1 -> daysAgo == 2 ? Status.LATE : Status.PRESENT;
                        default -> Status.PRESENT;
                    };
                    marks.put(roster.get(index).getId(), status);
                }
                attendance.save(teacher, course, LocalDate.now().minusDays(daysAgo), 1, marks);
            }
        }
    }
}
