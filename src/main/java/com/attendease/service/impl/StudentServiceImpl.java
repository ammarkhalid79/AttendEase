package com.attendease.service.impl;

import com.attendease.dto.StudentForm;
import com.attendease.entity.*;
import com.attendease.exception.*;
import com.attendease.repository.*;
import com.attendease.service.StudentService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {
    private final StudentRepository students;
    private final AccountRepository accounts;
    private final PasswordEncoder encoder;

    public StudentServiceImpl(StudentRepository students, AccountRepository accounts, PasswordEncoder encoder) {
        this.students = students;
        this.accounts = accounts;
        this.encoder = encoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> search(String query) {
        String term = query.strip().toLowerCase(Locale.ROOT);
        return students.findAllByOrderByFullNameAsc().stream()
                .filter(s -> (s.getFullName() + " " + s.getRegistrationNumber() + " " + s.getEmail()
                        + " " + s.getDepartment()).toLowerCase(Locale.ROOT).contains(term)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Student get(long id) {
        return students.findById(id).orElseThrow(() -> new MissingException("Student not found."));
    }

    @Override
    public Student save(Long id, StudentForm form) {
        String registration = form.registrationNumber().strip();
        String email = form.email().strip().toLowerCase(Locale.ROOT);
        if (registration.contains("@")) throw new DomainException("Registration numbers cannot contain @.");
        if (students.findByRegistrationNumberIgnoreCase(registration).filter(s -> !s.getId().equals(id)).isPresent()) {
            throw new DomainException("This registration number is already in use.");
        }
        Student student = id == null ? null : get(id);
        Account account = student == null ? new Account(registration, email, "", Role.STUDENT, true)
                : accounts.findById(student.getUserId()).orElseThrow();
        if (accounts.findByUsernameIgnoreCase(registration).filter(a -> !a.getId().equals(account.getId())).isPresent()
                || accounts.findByEmailIgnoreCase(email).filter(a -> !a.getId().equals(account.getId())).isPresent()) {
            throw new DomainException("The registration number or email belongs to another account.");
        }
        if (student == null || (form.password() != null && !form.password().isBlank())) {
            account.setPasswordHash(encoder.encode(checkPassword(form.password())));
        }
        account.setEmail(email);
        account.setUsername(registration);
        accounts.save(account);
        if (student == null) {
            student = new Student(registration, form.fullName().strip(), email, form.department().strip(),
                    form.semester(), account.getId());
        } else {
            student.setRegistrationNumber(registration);
            student.setFullName(form.fullName().strip());
            student.setEmail(email);
            student.setDepartment(form.department().strip());
            student.setSemester(form.semester());
        }
        return students.save(student);
    }

    public static String checkPassword(String password) {
        if (password == null || password.isBlank() || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new DomainException("Use a password of at least 8 characters and at most 72 UTF-8 bytes.");
        }
        return password;
    }

    @Override
    public void deactivate(long id) {
        accounts.findById(get(id).getUserId()).orElseThrow().setActive(false);
    }
}
